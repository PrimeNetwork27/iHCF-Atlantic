package com.doctordark.util.scoreboard.nametag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import com.doctordark.util.scoreboard.tablist.shared.MinecraftVersion;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;

import me.scifi.hcf.HCF;

public final class FrozenNametagHandler {
	private static final Map<String, Map<String, NametagInfo>> teamMap = new ConcurrentHashMap<>();

	private static final List<NametagInfo> registeredTeams = Collections.synchronizedList(new ArrayList<>());

	private static int teamCreateIndex = 1;

	private static final List<NametagProvider> providers = new ArrayList<>();

	private static boolean nametagRestrictionEnabled = false;

	private static String nametagRestrictBypass = "";

	private static boolean initiated = false;

	private static boolean async = true;

	private static int updateInterval = 2;

	public static void init() {
		if (HCF.getPlugin().getConfig().getBoolean("disableNametags", false)) {
			return;
		}
		Preconditions.checkState((!initiated));
		initiated = true;
		nametagRestrictionEnabled = HCF.getPlugin().getConfig().getBoolean("NametagPacketRestriction.Enabled", false);
		nametagRestrictBypass = HCF.getPlugin().getConfig().getString("NametagPacketRestriction.BypassPrefix")
				.replace("&", "");
		(new NametagThread()).start();
		HCF.getPlugin().getServer().getPluginManager().registerEvents(new NametagListener(), HCF.getPlugin());
		registerProvider(new NametagProvider.DefaultNametagProvider());
	}

	public static void registerProvider(NametagProvider newProvider) {
		providers.add(newProvider);
		providers.sort((a, b) -> Ints.compare(b.getWeight(), a.getWeight()));
	}

	public static void reloadPlayer(Player toRefresh) {
		NametagUpdate update = new NametagUpdate(toRefresh);
		if (async) {
			NametagThread.getPendingUpdates().put(update, true);
		} else {
			applyUpdate(update);
		}
	}

	public static void reloadOthersFor(Player refreshFor) {
		for (Player toRefresh : HCF.getPlugin().getServer().getOnlinePlayers()) {
			if (refreshFor == toRefresh) {
				continue;
			}
			reloadPlayer(toRefresh, refreshFor);
		}
	}

	public static void reloadPlayer(Player toRefresh, Player refreshFor) {
		NametagUpdate update = new NametagUpdate(toRefresh, refreshFor);
		if (async) {
			NametagThread.getPendingUpdates().put(update, Boolean.valueOf(true));
		} else {
			applyUpdate(update);
		}
	}

	protected static void applyUpdate(NametagUpdate nametagUpdate) {
		Player toRefreshPlayer = HCF.getPlugin().getServer().getPlayerExact(nametagUpdate.getToRefresh());
		if (toRefreshPlayer == null) {
			return;
		}
		if (nametagUpdate.getRefreshFor() == null) {
			for (Player refreshFor : HCF.getPlugin().getServer().getOnlinePlayers()) {
				reloadPlayerInternal(toRefreshPlayer, refreshFor);
			}
		} else {
			Player refreshForPlayer = HCF.getPlugin().getServer().getPlayerExact(nametagUpdate.getRefreshFor());
			if (refreshForPlayer != null) {
				reloadPlayerInternal(toRefreshPlayer, refreshForPlayer);
			}
		}
	}

	protected static void reloadPlayerInternal(Player toRefresh, Player refreshFor) {
		if (!refreshFor.hasMetadata("qLibNametag-LoggedIn")) {
			return;
		}
		NametagInfo provided = null;
		int providerIndex = 0;
		while (provided == null) {
			provided = providers.get(providerIndex++).fetchNametag(toRefresh, refreshFor);
		}
		String prefix;
		if (MinecraftVersion.getCurrent().getProtocolVersion() > 5 && nametagRestrictionEnabled
				&& (prefix = provided.getPrefix()) != null && !prefix.equalsIgnoreCase(nametagRestrictBypass)) {
			return;
		}
		Map<String, NametagInfo> teamInfoMap = new HashMap<>();
		if (teamMap.containsKey(refreshFor.getName())) {
			teamInfoMap = teamMap.get(refreshFor.getName());
		}
		(ScoreboardTeamPacket.create(provided.getName(), Collections.singletonList(toRefresh.getName()), 3))
				.sendToPlayer(refreshFor);
		teamInfoMap.put(toRefresh.getName(), provided);
		teamMap.put(refreshFor.getName(), teamInfoMap);
	}

	protected static void initiatePlayer(Player player) {
		for (NametagInfo teamInfo : registeredTeams) {
			teamInfo.getTeamAddPacket().sendToPlayer(player);
		}
	}

	protected static NametagInfo getOrCreate(String prefix, String suffix) {
		for (NametagInfo teamInfo : registeredTeams) {
			if (!teamInfo.getPrefix().equals(prefix) || !teamInfo.getSuffix().equals(suffix)) {
				continue;
			}
			return teamInfo;
		}
		NametagInfo newTeam = new NametagInfo(String.valueOf(teamCreateIndex++), prefix, suffix);
		registeredTeams.add(newTeam);
		ScoreboardTeamPacket addPacket = newTeam.getTeamAddPacket();
		for (Player player : HCF.getPlugin().getServer().getOnlinePlayers()) {
			addPacket.sendToPlayer(player);
		}
		return newTeam;
	}

	protected static Map<String, Map<String, NametagInfo>> getTeamMap() {
		return teamMap;
	}

	public static boolean isNametagRestrictionEnabled() {
		return nametagRestrictionEnabled;
	}

	public static void setNametagRestrictionEnabled(boolean nametagRestrictionEnabled) {
		FrozenNametagHandler.nametagRestrictionEnabled = nametagRestrictionEnabled;
	}

	public static String getNametagRestrictBypass() {
		return nametagRestrictBypass;
	}

	public static void setNametagRestrictBypass(String nametagRestrictBypass) {
		FrozenNametagHandler.nametagRestrictBypass = nametagRestrictBypass;
	}

	public static boolean isInitiated() {
		return initiated;
	}

	public static boolean isAsync() {
		return async;
	}

	public static void setAsync(boolean async) {
		FrozenNametagHandler.async = async;
	}

	public static int getUpdateInterval() {
		return updateInterval;
	}

	public static void setUpdateInterval(int updateInterval) {
		FrozenNametagHandler.updateInterval = updateInterval;
	}
}

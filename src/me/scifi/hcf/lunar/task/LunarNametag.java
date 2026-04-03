package me.scifi.hcf.lunar.task;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.doctordark.util.CC;
import com.doctordark.util.JavaUtils;

import me.scifi.hcf.ConfigurationService;
import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.FactionManager;
import me.scifi.hcf.faction.type.PlayerFaction;
import me.scifi.hcf.staffmode.Vanish;

public class LunarNametag implements Runnable {

	private final HCF plugin = HCF.getPlugin();

	@Override
	public void run() {
		for (Player viewer : Bukkit.getOnlinePlayers()) {
			for (Player target : Bukkit.getOnlinePlayers()) {
				updateNametags(viewer, target);
			}
		}
	}

	public String getFactionRank(PlayerFaction target) {
		List<PlayerFaction> list = HCF.getPlugin().getManagerHandler().getFactionManager().getFactions().stream()
				.filter(f -> f instanceof PlayerFaction).map(f -> (PlayerFaction) f)
				.sorted(Comparator.comparingLong(PlayerFaction::getPoints).reversed()).collect(Collectors.toList());

		for (int i = 0; i < list.size(); i++) {
			PlayerFaction faction = list.get(i);

			if (faction.equals(target)) {
				return String.valueOf(i + 1);
			}
		}

		return "";
	}

	public String getFormattedRank(String string) {
		if (string.equalsIgnoreCase("1")) {
			return "&4①";
		}
		if (string.equalsIgnoreCase("2")) {
			return "&6②";
		}
		if (string.equalsIgnoreCase("3")) {
			return "&a③";
		}
		return "&a" + string;
	}

	private void updateNametags(Player viewer, Player target) {
		FactionManager factionManager = plugin.getManagerHandler().getFactionManager();

		try {
			PlayerFaction viewerFaction = getPlayerFactionSafe(viewer, factionManager);
			PlayerFaction targetFaction = getPlayerFactionSafe(target, factionManager);

			ChatColor color = determineColor(viewer, target, viewerFaction, targetFaction);

			List<String> coloredNames = new ArrayList<>();
			coloredNames.add(color + target.getName());

			if (targetFaction != null) {
				String factionTag = "#" + getFormattedRank(getFactionRank(targetFaction)) + ChatColor.GOLD + " ["
						+ getFormattedFactionName(targetFaction, viewer) + " "
						+ JavaUtils.format(targetFaction.getDeathsUntilRaidable(false))
						+ targetFaction.getRegenStatus().getSymbol() + ChatColor.GOLD + "]";
				coloredNames.add(factionTag);
			}
			if (plugin.getStaffModeManager().getStaffMode().contains(target.getUniqueId())) {
				if (Vanish.isPlayerVanished(target)) {
					coloredNames.add(CC.translate("&e* [&7&oStaff&e-&7&oMode&e]"));
				} else {
					coloredNames.add(CC.translate("&e[&7&oStaff&e-&7&oMode&e]"));
				}
			}

			if (plugin.getRank().getGroupPrefix(viewer) != null) {
				coloredNames.add(CC.translate(plugin.getRank().getGroupPrefix(target)));
			}
			plugin.getLunarAPI().getNametagManager().overrideNametags(target, viewer, coloredNames);

		} catch (Exception e) {

			Bukkit.getLogger().warning("Error updating nametags for " + viewer.getName() + " viewing "
					+ target.getName() + ": " + e.getMessage());

		}
	}

	private PlayerFaction getPlayerFactionSafe(Player player, FactionManager factionManager) {
		try {
			return factionManager.getPlayerFaction(player);
		} catch (Exception e) {

			return null;
		}
	}

	private ChatColor determineColor(Player viewer, Player target, PlayerFaction viewerFaction,
			PlayerFaction targetFaction) {
		if (viewer.equals(target)) {
			return ConfigurationService.TEAMMATE_COLOUR;
		}

		if (viewerFaction == null) {
			return ConfigurationService.ENEMY_COLOUR;
		}

		if (targetFaction != null && viewerFaction.equals(targetFaction)) {
			return ConfigurationService.TEAMMATE_COLOUR;
		}

		try {
			return viewerFaction.getRelation(target).toChatColour();
		} catch (Exception e) {
			return getFallbackColor(viewerFaction, targetFaction);
		}
	}

	private ChatColor getFallbackColor(PlayerFaction viewerFaction, PlayerFaction targetFaction) {
		if (targetFaction == null) {
			return ConfigurationService.ENEMY_COLOUR;
		}

		if (viewerFaction.getAlliedFactions().contains(targetFaction)) {
			return ConfigurationService.ALLY_COLOUR;
		}

		return ChatColor.RED;
	}

	private String getFormattedFactionName(PlayerFaction faction, Player viewer) {
		try {
			return faction.getDisplayName(viewer);
		} catch (Exception e) {
			return faction.getName();
		}
	}
}
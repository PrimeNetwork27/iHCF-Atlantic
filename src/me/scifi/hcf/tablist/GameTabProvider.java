package me.scifi.hcf.tablist;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.doctordark.util.CC;
import com.doctordark.util.JavaUtils;

import io.github.nosequel.tab.shared.entry.TabElement;
import io.github.nosequel.tab.shared.entry.TabElementHandler;
import me.scifi.hcf.ConfigurationService;
import me.scifi.hcf.HCF;
import me.scifi.hcf.eventgame.EventTimer;
import me.scifi.hcf.eventgame.faction.EventFaction;
import me.scifi.hcf.eventgame.faction.KothFaction;
import me.scifi.hcf.faction.FactionManager;
import me.scifi.hcf.faction.FactionMember;
import me.scifi.hcf.faction.type.PlayerFaction;
import me.scifi.hcf.user.FactionUser;

public class GameTabProvider implements TabElementHandler {

	private final Comparator<PlayerFaction> FACTION_COMPARATOR;

	private final Comparator<FactionMember> ROLE_COMPARATOR;

	public GameTabProvider() {
		this.FACTION_COMPARATOR = Comparator.comparingInt(playerFaction -> playerFaction.getOnlinePlayers().size());
		this.ROLE_COMPARATOR = Comparator.comparingInt(playerMember -> playerMember.getRole().ordinal());
	}

	public String getHeader(Player player) {
		return null;
	}

	public String getFooter(Player player) {
		return null;
	}

	private static String getCardinalDirection(Player player) {
		double rotation = ((player.getLocation().getYaw() - 90.0F) % 360.0F);
		if (rotation < 0.0D) {
			rotation += 360.0D;
		}
		if (0.0D <= rotation && rotation < 22.5D) {
			return "W";
		}
		if (22.5D <= rotation && rotation < 67.5D) {
			return "NW";
		}
		if (67.5D <= rotation && rotation < 112.5D) {
			return "N";
		}
		if (112.5D <= rotation && rotation < 157.5D) {
			return "NE";
		}
		if (157.5D <= rotation && rotation < 202.5D) {
			return "E";
		}
		if (202.5D <= rotation && rotation < 247.5D) {
			return "SE";
		}
		if (247.5D <= rotation && rotation < 292.5D) {
			return "S";
		}
		if (292.5D <= rotation && rotation < 337.5D) {
			return "SW";
		}
		if (337.5D <= rotation && rotation < 360.0D) {
			return "W";
		}
		return null;
	}

	@Override
	public TabElement getElement(Player player) {
		final TabElement element = new TabElement();
		FactionManager factionManager = HCF.getPlugin().getManagerHandler().getFactionManager();
		PlayerFaction playerFaction = HCF.getPlugin().getManagerHandler().getFactionManager()
				.getPlayerFaction(player.getUniqueId());
		FactionUser factionUser = HCF.getPlugin().getManagerHandler().getUserManager().getUser(player.getUniqueId());

		Location homeLoc = (playerFaction == null) ? null : playerFaction.getHome();
		String home = (homeLoc == null) ? "None" : homeLoc.getBlockX() + ", " + homeLoc.getBlockZ();

		int onlinePlayers = Bukkit.getServer().getOnlinePlayers().size();
		int maxPlayers = Bukkit.getServer().getMaxPlayers();

		EventTimer eventTimer = HCF.getPlugin().getManagerHandler().getTimerManager().getEventTimer();
		EventFaction eventFaction = eventTimer.getEventFaction();
		KothFaction kothFaction = (KothFaction) eventFaction;
		String koth = (eventFaction == null) ? "None" : (" &3&9&l" + eventFaction.getName());
		String kothcoords = (eventFaction == null) ? ""
				: " &3&3%x% &7, &b%z%".replace("%x%", String.valueOf(kothFaction.getCaptureZone().getCuboid().getX1()))
						.replace("%z%", String.valueOf(kothFaction.getCaptureZone().getCuboid().getZ1()));
		// ==============================
		// Column 0 - Player & Faction
		// ==============================
		element.add(0, 0, CC.translate("&5Home"));
		element.add(0, 1, CC.translate("&f" + home));
		element.add(0, 3, CC.translate("&5Team Info"));

		if (playerFaction != null) {
			String online = String.valueOf(playerFaction.getOnlineMembers().size());
			String DTR = playerFaction.getDtrColour() + JavaUtils.format(playerFaction.getDeathsUntilRaidable(false))
					+ playerFaction.getRegenStatus().getSymbol();
			String balance = String.valueOf(playerFaction.getBalance());
			element.add(0, 4, CC.translate("&eDTR&e: " + DTR));
			element.add(0, 5, CC.translate("&eOnline&e: &f" + online));
			element.add(0, 6, CC.translate("&eBalance&e: &f" + balance));
		} else {
			element.add(0, 4, CC.translate("&eYou do not"));
			element.add(0, 5, CC.translate("&ehave a team"));
			element.add(0, 6, CC.translate("&e/f create <name>"));
		}

		element.add(0, 8, CC.translate("&5Player Info"));
		element.add(0, 9, CC.translate("&eKills&e: &f" + factionUser.getKills()));
		element.add(0, 10, CC.translate("&eDeaths&e: &f" + factionUser.getDeaths()));
		element.add(0, 12, CC.translate("&5Your Location"));
		String locationName = factionManager.getFactionAt(player.getLocation()) != null
				? factionManager.getFactionAt(player.getLocation()).getDisplayName(player)
				: ConfigurationService.WILDERNESS_COLOUR + "Wilderness";
		element.add(0, 13, CC.translate(locationName));
		element.add(0, 14, CC.translate("&e" + player.getLocation().getBlockX() + ", "
				+ player.getLocation().getBlockZ() + " &f[" + getCardinalDirection(player) + "]"));

		// ==============================
		// Column 1 - Faction Members
		// ==============================
		element.add(1, 0, CC.translate(HCF.getPlugin().getConfig().getString("server-name")));
		element.add(1, 2, CC.translate(playerFaction == null ? "" : ("&2" + playerFaction.getName())));

		if (playerFaction != null) {
			List<FactionMember> members = playerFaction.getMembers().values().stream()
					.filter(member -> Bukkit.getPlayer(member.getUniqueId()) != null).sorted(ROLE_COMPARATOR)
					.collect(Collectors.toList());

			for (int j = 3; j < 20; j++) {
				int index = j - 3;
				if (members.size() > index) {
					if (j == 18 && members.size() > 18) {
						element.add(1, j, CC.translate(" &3 &band " + (members.size() - 19) + " more..."));
					} else {
						FactionMember target = members.get(index);
						element.add(1, j, CC.translate(" &3&a" + target.getRole().getAstrix() + target.getName()));
					}
				}
			}
		} else {
			for (int j = 3; j < 20; j++) {
				element.add(1, j, "");
			}
		}

		// ==============================
		// Column 2 - Map Info
		// ==============================
		element.add(2, 0, CC.translate("&5End Portal"));
		element.add(2, 1, CC.translate("&e1000, 1000"));
		element.add(2, 2, CC.translate("&ein each quadrant"));
		element.add(2, 4, CC.translate("&5Map Kit"));
		element.add(2, 5, CC.translate("&eProt 1, Sharp 1"));
		element.add(2, 7, CC.translate("&5Map Border"));
		element.add(2, 8, CC.translate("&e3000 x 3000"));
		element.add(2, 10, CC.translate("&5Online"));
		element.add(2, 11, CC.translate("&e" + onlinePlayers + "/" + maxPlayers));
		if (HCF.getPlugin().getManagerHandler().getTimerManager().getEventTimer().getRemaining() > 0L) {
			element.add(2, 13, CC.translate("&5" + (HCF.getPlugin().getManagerHandler().getTimerManager()
					.getEventTimer().getEventFaction().getEventType().getDisplayName())));
			element.add(2, 14, CC.translate(koth));
			element.add(2, 15, CC.translate(kothcoords));
		}

		// ==============================
		// Column 3 - Team List
		// ==============================
		element.add(3, 0, CC.translate("&5Team List"));

		List<PlayerFaction> playerTeams = factionManager.getFactions().stream().filter(x -> x instanceof PlayerFaction)
				.map(x -> (PlayerFaction) x).filter(x -> !x.getOnlineMembers().isEmpty()).distinct()
				.sorted(FACTION_COMPARATOR).collect(Collectors.toList());

		Collections.reverse(playerTeams);

		for (int i = 0; i < 12 && i < playerTeams.size(); i++) {
			PlayerFaction next = playerTeams.get(i);
			String factionDTR = next.getDtrColour() + JavaUtils.format(next.getDeathsUntilRaidable(false))
					+ next.getRegenStatus().getSymbol();
			element.add(3, i + 1, CC.translate(next.getDisplayName(player)) + " (" + next.getOnlinePlayers().size()
					+ ") " + factionDTR);
		}

		return element;
	}

}

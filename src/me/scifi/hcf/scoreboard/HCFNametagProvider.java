package me.scifi.hcf.scoreboard;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.doctordark.util.scoreboard.nametag.NametagInfo;
import com.doctordark.util.scoreboard.nametag.NametagProvider;

import me.scifi.hcf.ConfigurationService;
import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.FactionManager;
import me.scifi.hcf.faction.type.PlayerFaction;

public class HCFNametagProvider extends NametagProvider {

	private final FactionManager manager = HCF.getPlugin().getManagerHandler().getFactionManager();

	public HCFNametagProvider() {
		super("Atlantic Provider", 5);
	}

	@Override
	public NametagInfo fetchNametag(Player toRefresh, Player refreshFor) {
		PlayerFaction viewerTeam = manager.getPlayerFaction(refreshFor);
		NametagInfo nametagInfo = null;

		if (viewerTeam != null) {
			if (viewerTeam.isMember(toRefresh.getUniqueId())) {
				nametagInfo = createNametag(toRefresh, ConfigurationService.TEAMMATE_COLOUR.toString(), "");
			} else if (viewerTeam.isAlly(toRefresh.getUniqueId())) {
				nametagInfo = createNametag(toRefresh, ConfigurationService.ALLY_COLOUR.toString(), "");
			}
		}

		// If we already found something above they override these, otherwise we can do
		// these checks.
		if (nametagInfo == null) {
			if (viewerTeam != null && viewerTeam.getTarget() != null
					&& viewerTeam.getTarget().getMembers().containsKey(toRefresh.getUniqueId())) {
				nametagInfo = createNametag(toRefresh, ChatColor.LIGHT_PURPLE.toString(), "");
			}
		}

		// You always see yourself as green.
		if (refreshFor.equals(toRefresh)) {
			nametagInfo = createNametag(toRefresh, ConfigurationService.TEAMMATE_COLOUR.toString().toString(), "");
		}

		// If nothing custom was set, fall back on red.
		return (nametagInfo == null ? createNametag(toRefresh, ConfigurationService.ENEMY_COLOUR.toString(), "")
				: nametagInfo);
	}

	private NametagInfo createNametag(Player displayed, String prefix, String suffix) {
		return createNametag(prefix, suffix);

	}
}

package me.scifi.hcf.features.lunar.task;

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
import me.scifi.hcf.features.staffmode.Vanish;

public class LunarNametag implements Runnable {

	private final HCF plugin = HCF.getPlugin();
	private final FactionManager factionManager = plugin.getManagerHandler().getFactionManager();

	@Override
	public void run() {
		for (Player viewer : Bukkit.getOnlinePlayers()) {
			for (Player target : Bukkit.getOnlinePlayers()) {
				updateNametags(viewer, target);
			}
		}
	}

	private void updateNametags(Player viewer, Player target) {
		try {
			PlayerFaction viewerFaction = getPlayerFactionSafe(viewer);
			PlayerFaction targetFaction = getPlayerFactionSafe(target);

			ChatColor color = determineColor(viewer, target, viewerFaction, targetFaction);

			List<String> coloredNames = new ArrayList<>();
			coloredNames.add(color + target.getName());

			if (targetFaction != null) {
				String factionTag = CC.translate(getFormattedRank(getFactionRank(targetFaction))) + ChatColor.GOLD
						+ " [" + getFormattedFactionName(targetFaction, viewer) + " "
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

			if (plugin.getRank().getGroupPrefix(target) != null) {
				String prefix = CC.translate(plugin.getRank().getGroupPrefix(target));
				if (!ChatColor.stripColor(prefix).trim().isEmpty()) {
					coloredNames.add(prefix);
				}
			}

			plugin.getLunarAPI().getNametagManager().overrideNametags(target, viewer, coloredNames);

		} catch (Exception e) {
			Bukkit.getLogger().warning("Error updating nametags for " + viewer.getName() + " viewing "
					+ target.getName() + ": " + e.getMessage());
		}
	}

	private ChatColor determineColor(Player viewer, Player target, PlayerFaction viewerFaction,
			PlayerFaction targetFaction) {
		// Mismo jugador
		if (viewer.equals(target)) {
			return ConfigurationService.TEAMMATE_COLOUR;
		}

		// Sin facción
		if (viewerFaction == null) {
			return ConfigurationService.ENEMY_COLOUR;
		}

		// Mismo equipo
		if (targetFaction != null && viewerFaction.equals(targetFaction)) {
			return ConfigurationService.TEAMMATE_COLOUR;
		}

		// Aliado
		if (viewerFaction.isAlly(target.getUniqueId())) {
			return ConfigurationService.ALLY_COLOUR;
		}

		// Focus
		if (viewerFaction.getTarget() != null
				&& viewerFaction.getTarget().getMembers().containsKey(target.getUniqueId())) {
			return ChatColor.LIGHT_PURPLE;
		}

		// Enemigo
		return ConfigurationService.ENEMY_COLOUR;
	}

	private PlayerFaction getPlayerFactionSafe(Player player) {
		try {
			return factionManager.getPlayerFaction(player);
		} catch (Exception e) {
			return null;
		}
	}

	public String getFactionRank(PlayerFaction target) {
		List<PlayerFaction> list = factionManager.getFactions().stream().filter(f -> f instanceof PlayerFaction)
				.map(f -> (PlayerFaction) f).sorted(Comparator.comparingLong(PlayerFaction::getPoints).reversed())
				.collect(Collectors.toList());

		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).equals(target)) {
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

	private String getFormattedFactionName(PlayerFaction faction, Player viewer) {
		try {
			return faction.getDisplayName(viewer);
		} catch (Exception e) {
			return faction.getName();
		}
	}
}
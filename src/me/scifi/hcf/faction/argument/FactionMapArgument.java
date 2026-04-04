package me.scifi.hcf.faction.argument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.doctordark.compat.com.google.common.collect.GuavaCompat;
import com.doctordark.util.command.CommandArgument;

import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.LandMap;
import me.scifi.hcf.faction.claim.Claim;
import me.scifi.hcf.user.FactionUser;
import me.scifi.hcf.visualise.VisualType;

/**
 * Faction argument used to view a interactive map of {@link Claim}s.
 */
public class FactionMapArgument extends CommandArgument {

	private final HCF plugin;

	public FactionMapArgument(HCF plugin) {
		super("map", "View all claims around your chunk.");
		this.plugin = plugin;
	}

	@Override
	public String getUsage(String label) {
		return '/' + label + ' ' + getName() + " [factionName]";
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-ONLY"));
			return true;
		}

		Player player = (Player) sender;
		UUID uuid = player.getUniqueId();

		final FactionUser factionUser = plugin.getManagerHandler().getUserManager().getUser(uuid);
		final VisualType visualType;
		if (args.length <= 1) {
			visualType = VisualType.CLAIM_MAP;
		} else if ((visualType = GuavaCompat.getIfPresent(VisualType.class, args[1]).orNull()) == null) {
			player.sendMessage(plugin.getMessagesYML().getString("FACTION-MAP-VISUALTYPENOTFOUND")
					.replace("{visualType}", args[1]));
			return true;
		}

		boolean newShowingMap = !factionUser.isShowClaimMap();
		if (newShowingMap) {
			if (!LandMap.updateMap(player, plugin, visualType, true)) {
				return true;
			}
		} else {
			plugin.getManagerHandler().getVisualiseHandler().clearVisualBlocks(player, visualType, null);
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-MAP-DISABLEDCLAIMPILLARS"));
		}

		factionUser.setShowClaimMap(newShowingMap);
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (args.length != 2 || !(sender instanceof Player)) {
			return Collections.emptyList();
		}

		VisualType[] values = VisualType.values();
		List<String> results = new ArrayList<>(values.length);
		for (VisualType visualType : values) {
			results.add(visualType.name());
		}

		return results;
	}
}

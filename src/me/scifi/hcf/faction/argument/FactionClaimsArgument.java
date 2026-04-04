package me.scifi.hcf.faction.argument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.doctordark.util.command.CommandArgument;

import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.claim.Claim;
import me.scifi.hcf.faction.type.ClaimableFaction;
import me.scifi.hcf.faction.type.Faction;
import me.scifi.hcf.faction.type.PlayerFaction;

/**
 * Faction argument used to check {@link Claim}s made by {@link Faction}s.
 */
public class FactionClaimsArgument extends CommandArgument {

	private final HCF plugin;

	public FactionClaimsArgument(HCF plugin) {
		super("claims", "View all claims for a faction.");
		this.plugin = plugin;
	}

	@Override
	public String getUsage(String label) {
		return '/' + label + ' ' + getName() + " [factionName]";
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		PlayerFaction selfFaction = sender instanceof Player
				? plugin.getManagerHandler().getFactionManager().getPlayerFaction((Player) sender)
				: null;
		ClaimableFaction targetFaction;
		if (args.length < 2) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Usage: " + getUsage(label));
				return true;
			}

			if (selfFaction == null) {
				sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-ONLY"));
				return true;
			}

			targetFaction = selfFaction;
		} else {
			Faction faction = plugin.getManagerHandler().getFactionManager().getContainingFaction(args[1]);

			if (faction == null) {
				sender.sendMessage(
						plugin.getMessagesYML().getString("FACTION-PLAYER-NOT-FOUND").replace("{1}", args[1]));
				return true;
			}

			if (!(faction instanceof ClaimableFaction)) {
				sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-CLAIMS-HAVE"));
				return true;
			}

			targetFaction = (ClaimableFaction) faction;
		}

		Collection<Claim> claims = targetFaction.getClaims();

		if (claims.isEmpty()) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-CLAIMS-EMPTY").replace("{faction}",
					targetFaction.getDisplayName(sender)));
			return true;
		}

		if (sender instanceof Player && !sender.isOp()
				&& (targetFaction instanceof PlayerFaction && ((PlayerFaction) targetFaction).getHome() == null)) {
			if (selfFaction != targetFaction) {
				sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-CLAIMS-NOTHQ").replace("{faction}",
						targetFaction.getDisplayName(sender)));
				return true;
			}
		}
		sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-CLAIMS-MESSAGE")
				.replace("{faction}", targetFaction.getDisplayName(sender))
				.replace("{size}", String.valueOf(claims.size())));

		for (Claim claim : claims) {
			sender.sendMessage(ChatColor.GRAY + " " + claim.getFormattedName());
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (args.length != 2 || !(sender instanceof Player)) {
			return Collections.emptyList();
		} else if (args[1].isEmpty()) {
			return null;
		} else {
			Player player = ((Player) sender);
			List<String> results = new ArrayList<>(
					plugin.getManagerHandler().getFactionManager().getFactionNameMap().keySet());
			for (Player target : Bukkit.getServer().getOnlinePlayers()) {
				if (player.canSee(target) && !results.contains(target.getName())) {
					results.add(target.getName());
				}
			}

			return results;
		}
	}
}

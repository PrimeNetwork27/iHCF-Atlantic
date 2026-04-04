package me.scifi.hcf.faction.argument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.doctordark.util.command.CommandArgument;
import com.google.common.collect.ImmutableList;

import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.FactionMember;
import me.scifi.hcf.faction.claim.Claim;
import me.scifi.hcf.faction.struct.Role;
import me.scifi.hcf.faction.type.PlayerFaction;

public class FactionUnclaimArgument extends CommandArgument {

	private final HCF plugin;

	public FactionUnclaimArgument(HCF plugin) {
		super("unclaim", "Unclaims land from your faction.");
		this.plugin = plugin;
	}

	@Override
	public String getUsage(String label) {
		return '/' + label + ' ' + getName() + " [all]";
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-ONLY"));
			return true;
		}

		Player player = (Player) sender;
		PlayerFaction playerFaction = plugin.getManagerHandler().getFactionManager().getPlayerFaction(player);

		if (playerFaction == null) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-NOT-FACTION"));
			return true;
		}

		FactionMember factionMember = playerFaction.getMember(player);

		if (factionMember.getRole() != Role.LEADER) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-UNCLAIM-MUST"));
			return true;
		}

		Collection<Claim> factionClaims = playerFaction.getClaims();

		if (factionClaims.isEmpty()) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-UNCLAIM-NOTCLAIM"));
			return true;
		}

		// Find out what claims the player wants removed.
		Collection<Claim> removingClaims;
		if (args.length > 1 && args[1].equalsIgnoreCase("all")) {
			removingClaims = new ArrayList<>(factionClaims);
		} else {
			Location location = player.getLocation();
			Claim claimAt = plugin.getManagerHandler().getFactionManager().getClaimAt(location);
			if (claimAt == null || !factionClaims.contains(claimAt)) {
				sender.sendMessage(plugin.getMessagesYML().getString("FACTION-UNCLAIM-WRONG"));
				return true;
			}

			removingClaims = Collections.singleton(claimAt);
		}

		if (!playerFaction.removeClaims(removingClaims, player)) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-UNCLAIM-ERROR"));
			return true;
		}

		int removingAmount = removingClaims.size();
		playerFaction.broadcast(plugin.getMessagesYML().getString("FACTION-UNCLAIM-BROADCAST")
				.replace("{sender}", factionMember.getRole().getAstrix() + sender.getName())
				.replace("{claim}", removingAmount + " claim" + (removingAmount > 1 ? "s" : "")));
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		return args.length == 2 ? COMPLETIONS : Collections.<String>emptyList();
	}

	private static final ImmutableList<String> COMPLETIONS = ImmutableList.of("all");
}

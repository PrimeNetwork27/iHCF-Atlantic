package me.scifi.hcf.faction.argument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.doctordark.util.command.CommandArgument;
import com.google.common.collect.ImmutableList;

import me.scifi.hcf.ConfigurationService;
import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.FactionMember;
import me.scifi.hcf.faction.struct.Role;
import me.scifi.hcf.faction.type.PlayerFaction;

public class FactionUninviteArgument extends CommandArgument {

	private final HCF plugin;

	public FactionUninviteArgument(HCF plugin) {
		super("uninvite", "Revoke an invitation to a player.", new String[] { "deinvite", "deinv", "uninv", "revoke" });
		this.plugin = plugin;
	}

	@Override
	public String getUsage(String label) {
		return '/' + label + ' ' + getName() + " <all|playerName>";
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-ONLY"));
			return true;
		}

		if (args.length < 2) {
			sender.sendMessage(ChatColor.RED + "Usage: " + getUsage(label));
			return true;
		}

		Player player = (Player) sender;
		PlayerFaction playerFaction = plugin.getManagerHandler().getFactionManager().getPlayerFaction(player);

		if (playerFaction == null) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-NOT-FACTION"));
			return true;
		}

		FactionMember factionMember = playerFaction.getMember(player);

		if (factionMember.getRole() == Role.MEMBER) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-UNINVITE-MUST"));
			return true;
		}

		Set<String> invitedPlayerNames = playerFaction.getInvitedPlayerNames();

		if (args[1].equalsIgnoreCase("all")) {
			invitedPlayerNames.clear();
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-UNINVITE-ALL"));
			return true;
		}

		if (!invitedPlayerNames.remove(args[1])) {
			sender.sendMessage(
					plugin.getMessagesYML().getString("FACTION-UNINVITE-PENDING").replace("{name}", args[1]));
			return true;
		}
		playerFaction.broadcast(plugin.getMessagesYML().getString("FACTION-UNINVITE-BROADCAST")
				.replace("{sender}", factionMember.getRole().getAstrix() + sender.getName())
				.replace("{target}", ConfigurationService.ENEMY_COLOUR + args[1]));

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (args.length != 2 || !(sender instanceof Player)) {
			return Collections.emptyList();
		}

		Player player = (Player) sender;
		PlayerFaction playerFaction = plugin.getManagerHandler().getFactionManager().getPlayerFaction(player);
		if (playerFaction == null || playerFaction.getMember(player.getUniqueId()).getRole() == Role.MEMBER) {
			return Collections.emptyList();
		}

		List<String> results = new ArrayList<>(COMPLETIONS);
		results.addAll(playerFaction.getInvitedPlayerNames());
		return results;
	}

	private static final ImmutableList<String> COMPLETIONS = ImmutableList.of("all");
}

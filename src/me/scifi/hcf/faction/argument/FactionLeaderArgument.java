package me.scifi.hcf.faction.argument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.doctordark.util.command.CommandArgument;

import me.scifi.hcf.ConfigurationService;
import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.FactionMember;
import me.scifi.hcf.faction.struct.Role;
import me.scifi.hcf.faction.type.PlayerFaction;

public class FactionLeaderArgument extends CommandArgument {

	private final HCF plugin;

	public FactionLeaderArgument(HCF plugin) {
		super("leader", "Sets the new leader for your faction.");
		this.plugin = plugin;
		this.aliases = new String[] { "setleader", "newleader" };
	}

	@Override
	public String getUsage(String label) {
		return '/' + label + ' ' + getName() + " <playerName>";
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

		UUID uuid = player.getUniqueId();
		FactionMember selfMember = playerFaction.getMember(uuid);
		Role selfRole = selfMember.getRole();

		if (selfRole != Role.LEADER) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-SETLEADER-LEADER"));
			return true;
		}

		FactionMember targetMember = playerFaction.getMember(args[1]);

		if (targetMember == null) {
			sender.sendMessage(
					plugin.getMessagesYML().getString("FACTION-SETLEADER-NOTFOUND").replace("{name}", args[1]));
			return true;
		}

		if (targetMember.getUniqueId().equals(uuid)) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-SETLEADER-SELF"));
			return true;
		}

		targetMember.setRole(Role.LEADER);
		selfMember.setRole(Role.CAPTAIN);
		playerFaction.broadcast(plugin.getMessagesYML().getString("FACTION-SETLEADER-BROADCAST")
				.replace("{sender}",
						ConfigurationService.TEAMMATE_COLOUR + selfMember.getRole().getAstrix() + selfMember.getName())
				.replace("{target}", ConfigurationService.TEAMMATE_COLOUR + targetMember.getRole().getAstrix()
						+ targetMember.getName()));

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (args.length != 2 || !(sender instanceof Player)) {
			return Collections.emptyList();
		}

		Player player = (Player) sender;
		PlayerFaction playerFaction = plugin.getManagerHandler().getFactionManager().getPlayerFaction(player);
		if (playerFaction == null || (playerFaction.getMember(player.getUniqueId()).getRole() != Role.LEADER)) {
			return Collections.emptyList();
		}

		List<String> results = new ArrayList<>();
		Map<UUID, FactionMember> members = playerFaction.getMembers();
		for (Map.Entry<UUID, FactionMember> entry : members.entrySet()) {
			if (entry.getValue().getRole() != Role.LEADER) {
				OfflinePlayer target = Bukkit.getOfflinePlayer(entry.getKey());
				String targetName = target.getName();
				if (targetName != null && !results.contains(targetName)) {
					results.add(targetName);
				}
			}
		}

		return results;
	}
}

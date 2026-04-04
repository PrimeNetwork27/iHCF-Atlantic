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

import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.FactionMember;
import me.scifi.hcf.faction.struct.Relation;
import me.scifi.hcf.faction.struct.Role;
import me.scifi.hcf.faction.type.PlayerFaction;

public class FactionPromoteArgument extends CommandArgument {

	private final HCF plugin;

	public FactionPromoteArgument(HCF plugin) {
		super("promote", "Promotes a player to a captain.");
		this.plugin = plugin;
		this.aliases = new String[] { "captain", "officer", "mod", "moderator" };
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
		UUID uuid = player.getUniqueId();

		PlayerFaction playerFaction = plugin.getManagerHandler().getFactionManager().getPlayerFaction(uuid);

		if (playerFaction == null) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-NOT-FACTION"));
			return true;
		}

		if (playerFaction.getMember(uuid).getRole() != Role.LEADER) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PROMOTE-LEADER"));
			return true;
		}

		FactionMember targetMember = playerFaction.getMember(args[1]);

		if (targetMember == null) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PROMOTE-NOTFOUND"));
			return true;
		}

		if (targetMember.getRole() != Role.MEMBER) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PROMOTE-ALREADY")
					.replace("{target}", targetMember.getName()).replace("{role}", targetMember.getRole().getName()));
			return true;
		}

		Role role = Role.CAPTAIN;
		targetMember.setRole(role);
		playerFaction.broadcast(plugin.getMessagesYML().getString("FACTION-PROMOTE-SUCCESFULLY").replace("{target}",
				Relation.MEMBER.toChatColour() + role.getAstrix() + targetMember.getName()));
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (args.length != 2 || !(sender instanceof Player)) {
			return Collections.emptyList();
		}

		Player player = (Player) sender;
		PlayerFaction playerFaction = plugin.getManagerHandler().getFactionManager().getPlayerFaction(player);
		if (playerFaction == null || playerFaction.getMember(player.getUniqueId()).getRole() != Role.LEADER) {
			return Collections.emptyList();
		}

		List<String> results = new ArrayList<>();
		for (Map.Entry<UUID, FactionMember> entry : playerFaction.getMembers().entrySet()) {
			if (entry.getValue().getRole() == Role.MEMBER) {
				OfflinePlayer target = Bukkit.getOfflinePlayer(entry.getKey());
				String targetName = target.getName();
				if (targetName != null) {
					results.add(targetName);
				}
			}
		}

		return results;
	}
}

package me.scifi.hcf.faction.argument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
import me.scifi.hcf.faction.type.Faction;
import me.scifi.hcf.faction.type.PlayerFaction;

/**
 * Faction argument used to demote players to members in {@link Faction}s.
 */
public class FactionDemoteArgument extends CommandArgument {

	private final HCF plugin;

	public FactionDemoteArgument(HCF plugin) {
		super("demote", "Demotes a player to a member.", new String[] { "uncaptain", "delcaptain", "delofficer" });
		this.plugin = plugin;
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

		if (playerFaction.getMember(player.getUniqueId()).getRole() != Role.LEADER) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-DEMOTE-LEADER"));
			return true;
		}

		FactionMember targetMember = playerFaction.getMember(args[1]);

		if (targetMember == null) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-LEADER-TARGET-NOT-IN-FACTION"));
			return true;
		}

		if (targetMember.getRole() != Role.CAPTAIN) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-DEMOTE-ONLY-CAPTAIN"));
			return true;
		}

		targetMember.setRole(Role.MEMBER);
		playerFaction.broadcast(plugin.getMessagesYML().getString("FACTION-PLAYER-DEMOTE-BROADCAST")
				.replace("{memberName}", Relation.MEMBER.toChatColour() + targetMember.getName()));
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
		Collection<UUID> keySet = playerFaction.getMembers().keySet();
		for (UUID entry : keySet) {
			OfflinePlayer target = Bukkit.getOfflinePlayer(entry);
			String targetName = target.getName();
			if (targetName != null && playerFaction.getMember(target.getUniqueId()).getRole() == Role.CAPTAIN) {
				results.add(targetName);
			}
		}

		return results;
	}
}

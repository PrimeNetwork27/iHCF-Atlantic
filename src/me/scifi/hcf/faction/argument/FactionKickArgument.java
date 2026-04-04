package me.scifi.hcf.faction.argument;

import java.util.ArrayList;
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

import me.scifi.hcf.ConfigurationService;
import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.FactionMember;
import me.scifi.hcf.faction.struct.Role;
import me.scifi.hcf.faction.type.PlayerFaction;

public class FactionKickArgument extends CommandArgument {

	private final HCF plugin;

	public FactionKickArgument(HCF plugin) {
		super("kick", "Kick a player from the faction.");
		this.plugin = plugin;
		this.aliases = new String[] { "kickmember", "kickplayer" };
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

		if (playerFaction.isRaidable() && !ConfigurationService.KIT_MAP
				&& !plugin.getManagerHandler().getEotwHandler().isEndOfTheWorld()) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-KICK-RAIDABLE"));
			return true;
		}

		FactionMember targetMember = playerFaction.getMember(args[1]);

		if (targetMember == null) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-KICK-NOTFOUND").replace("{name}", args[1]));
			return true;
		}

		Role selfRole = playerFaction.getMember(player.getUniqueId()).getRole();

		if (selfRole == Role.MEMBER) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-KICK-OFFICER"));
			return true;
		}

		Role targetRole = targetMember.getRole();

		if (targetRole == Role.LEADER) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-KICK-LEADER"));
			return true;
		}

		if (targetRole == Role.CAPTAIN && selfRole == Role.CAPTAIN) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-KICK-MUSTLEADER"));
			return true;
		}

		Player onlineTarget = targetMember.toOnlinePlayer();
		if (playerFaction.removeMember(sender, onlineTarget, targetMember.getUniqueId(), true, true)) {
			if (onlineTarget != null) {
				onlineTarget.sendMessage(plugin.getMessagesYML().getString("FACTION-KICK-BROADCASTPLAYER")
						.replace("{sender}", sender.getName()));
			}
			playerFaction.broadcast(plugin.getMessagesYML().getString("FACTION-KICK-BROADCAST")
					.replace("{target}", ConfigurationService.ENEMY_COLOUR + targetMember.getName())
					.replace("{sender}", playerFaction.getMember(player).getRole().getAstrix() + sender.getName()));

		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (args.length != 2 || !(sender instanceof Player)) {
			return Collections.emptyList();
		}

		Player player = (Player) sender;
		PlayerFaction playerFaction = plugin.getManagerHandler().getFactionManager().getPlayerFaction(player);
		if (playerFaction == null) {
			return Collections.emptyList();
		}

		Role memberRole = playerFaction.getMember(player.getUniqueId()).getRole();
		if (memberRole == Role.MEMBER) {
			return Collections.emptyList();
		}

		List<String> results = new ArrayList<>();
		for (UUID entry : playerFaction.getMembers().keySet()) {
			Role targetRole = playerFaction.getMember(entry).getRole();
			if (targetRole == Role.LEADER || (targetRole == Role.CAPTAIN && memberRole != Role.LEADER)) {
				continue;
			}

			OfflinePlayer target = Bukkit.getOfflinePlayer(entry);
			String targetName = target.getName();
			if (targetName != null && !results.contains(targetName)) {
				results.add(targetName);
			}
		}

		return results;
	}
}

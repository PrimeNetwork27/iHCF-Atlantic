package me.scifi.hcf.faction.argument;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.doctordark.util.command.CommandArgument;
import com.google.common.collect.ImmutableList;

import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.struct.Role;
import me.scifi.hcf.faction.type.PlayerFaction;

public class FactionAnnouncementArgument extends CommandArgument {

	private final HCF plugin;

	public FactionAnnouncementArgument(HCF plugin) {
		super("announcement", "Set your faction announcement.", new String[] { "announce", "motd" });
		this.plugin = plugin;
	}

	@Override
	public String getUsage(String label) {
		return '/' + label + ' ' + getName() + " <newAnnouncement>";
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

		if (playerFaction.getMember(player.getUniqueId()).getRole() == Role.MEMBER) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-ANNOUNCEMENT-MUST-OFFICER"));
			return true;
		}

		String oldAnnouncement = playerFaction.getAnnouncement();
		String newAnnouncement;
		if (args[1].equalsIgnoreCase("clear") || args[1].equalsIgnoreCase("none")
				|| args[1].equalsIgnoreCase("remove")) {
			newAnnouncement = null;
		} else {
			newAnnouncement = StringUtils.join(args, ' ', 1, args.length);
		}

		if (oldAnnouncement == null && newAnnouncement == null) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-ANNOUNCEMENT-UNSET"));
			return true;
		}

		if (oldAnnouncement != null && newAnnouncement != null && oldAnnouncement.equals(newAnnouncement)) {
			plugin.getMessagesYML().getString("FACTION-ANNOUNCEMENT-ALREADY").replace("{announcement}",
					newAnnouncement);
			return true;
		}

		playerFaction.setAnnouncement(newAnnouncement);

		if (newAnnouncement == null) {
			playerFaction.broadcast(plugin.getMessagesYML().getString("FACTION-ANNOUNCEMENT-CLEAR").replace("{sender}",
					sender.getName()));
			return true;
		}
		playerFaction.broadcast(
				plugin.getMessagesYML().getString("FACTION-ANNOUNCEMENT-UPDATE").replace("{sender}", player.getName())
						.replace("{old}", (oldAnnouncement != null ? oldAnnouncement : "none"))
						.replace("{new}", newAnnouncement));

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return Collections.emptyList();
		} else if (args.length == 2) {
			return CLEAR_LIST;
		} else {
			return Collections.emptyList();
		}
	}

	private static final ImmutableList<String> CLEAR_LIST = ImmutableList.of("clear");
}

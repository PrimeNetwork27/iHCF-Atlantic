package me.scifi.hcf.faction.argument;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.doctordark.util.command.CommandArgument;

import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.type.Faction;
import me.scifi.hcf.faction.type.PlayerFaction;

/**
 * Faction argument used to check invites for {@link Faction}s.
 */
public class FactionInvitesArgument extends CommandArgument {

	private final HCF plugin;

	public FactionInvitesArgument(HCF plugin) {
		super("invites", "View faction invitations.");
		this.plugin = plugin;
	}

	@Override
	public String getUsage(String label) {
		return '/' + label + ' ' + getName();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-ONLY"));
			return true;
		}

		List<String> receivedInvites = new ArrayList<>();
		for (Faction faction : plugin.getManagerHandler().getFactionManager().getFactions()) {
			if (faction instanceof PlayerFaction) {
				PlayerFaction targetPlayerFaction = (PlayerFaction) faction;
				if (targetPlayerFaction.getInvitedPlayerNames().contains(sender.getName())) {
					receivedInvites.add(targetPlayerFaction.getDisplayName(sender));
				}
			}
		}

		PlayerFaction playerFaction = plugin.getManagerHandler().getFactionManager().getPlayerFaction((Player) sender);
		String delimiter = ChatColor.WHITE + ", " + ChatColor.GRAY;

		if (playerFaction != null) {
			Set<String> sentInvites = playerFaction.getInvitedPlayerNames();
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-INVITES-SENT-TO")
					.replace("{factionName}", playerFaction.getDisplayName(sender)).replace("{sentSize}",
							String.valueOf(sentInvites.size()).replace("{invited}",
									(sentInvites.isEmpty() ? "Your faction has not invited anyone."
											: StringUtils.join(sentInvites, delimiter)))));
		}
		sender.sendMessage(plugin.getMessagesYML().getString("FACTION-INVITES-SELF").replace("{size}",
				String.valueOf(receivedInvites.size()).replace("{factions}",
						(receivedInvites.isEmpty() ? "No factions have invited you."
								: StringUtils.join(receivedInvites, ChatColor.WHITE + delimiter)))));

		return true;
	}
}

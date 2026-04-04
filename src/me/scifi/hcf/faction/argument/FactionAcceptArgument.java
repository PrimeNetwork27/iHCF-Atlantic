package me.scifi.hcf.faction.argument;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.doctordark.util.command.CommandArgument;

import me.scifi.hcf.ConfigurationService;
import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.FactionMember;
import me.scifi.hcf.faction.struct.ChatChannel;
import me.scifi.hcf.faction.struct.Relation;
import me.scifi.hcf.faction.struct.Role;
import me.scifi.hcf.faction.type.Faction;
import me.scifi.hcf.faction.type.PlayerFaction;

/**
 * Faction argument used to accept invitations from {@link Faction}s.
 */
public class FactionAcceptArgument extends CommandArgument {

	private final HCF plugin;

	public FactionAcceptArgument(HCF plugin) {
		super("accept", "Accept a join request from an existing faction.", new String[] { "join", "a" });
		this.plugin = plugin;
	}

	@Override
	public String getUsage(String label) {
		return '/' + label + ' ' + getName() + " <factionName>";
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

		if (plugin.getManagerHandler().getFactionManager().getPlayerFaction(player) != null) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-ALREADY"));
			return true;
		}

		Faction faction = plugin.getManagerHandler().getFactionManager().getContainingFaction(args[1]);

		if (faction == null) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-NOT-FOUND").replace("{1}", args[1]));
			return true;
		}

		if (!(faction instanceof PlayerFaction)) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-JOIN-SYSTEMFACTION"));
			return true;
		}

		PlayerFaction targetFaction = (PlayerFaction) faction;

		if (targetFaction.getMembers().size() >= ConfigurationService.MAX_MEMBERS_PER_FACTION) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-FACTION-FULL").replace("{factionName}",
					faction.getDisplayName(sender).replace("{limit}",
							String.valueOf(ConfigurationService.MAX_MEMBERS_PER_FACTION))));
			return true;
		}

		if (!targetFaction.isOpen() && !targetFaction.getInvitedPlayerNames().contains(player.getName())) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-NOT-INVITED").replace("{factionName}",
					faction.getDisplayName(sender)));
			return true;
		}

		if (targetFaction.addMember(player, player, player.getUniqueId(),
				new FactionMember(player, ChatChannel.PUBLIC, Role.MEMBER))) {
			targetFaction.broadcast(plugin.getMessagesYML().getString("FACTION-PLAYER-JOINED").replace("{memberName}",
					Relation.MEMBER.toChatColour() + sender.getName()));
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (args.length != 2 || !(sender instanceof Player)) {
			return Collections.emptyList();
		}

		return plugin.getManagerHandler().getFactionManager().getFactions().stream()
				.filter(faction -> faction instanceof PlayerFaction
						&& ((PlayerFaction) faction).getInvitedPlayerNames().contains(sender.getName()))
				.map(faction -> sender.getName()).collect(Collectors.toList());
	}
}

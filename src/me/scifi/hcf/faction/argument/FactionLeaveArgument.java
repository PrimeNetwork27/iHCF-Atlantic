package me.scifi.hcf.faction.argument;

import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.doctordark.util.command.CommandArgument;

import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.struct.Relation;
import me.scifi.hcf.faction.struct.Role;
import me.scifi.hcf.faction.type.PlayerFaction;

public class FactionLeaveArgument extends CommandArgument {

	private final HCF plugin;

	public FactionLeaveArgument(HCF plugin) {
		super("leave", "Leave your current faction.");
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

		Player player = (Player) sender;
		PlayerFaction playerFaction = plugin.getManagerHandler().getFactionManager().getPlayerFaction(player);

		if (playerFaction == null) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-NOT-FACTION"));
			return true;
		}

		UUID uuid = player.getUniqueId();
		if (playerFaction.getMember(uuid).getRole() == Role.LEADER) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-LEAVE-LEADER").replace("{label}", label));

			return true;
		}

		if (playerFaction.removeMember(player, player, player.getUniqueId(), false, false)) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-LEAVE-BROADCAST"));
			playerFaction.broadcast(plugin.getMessagesYML().getString("FACTION-LEAVE-BROADCASTFACTION")
					.replace("{sender}", Relation.ENEMY.toChatColour() + sender.getName()));
		}

		return true;
	}
}

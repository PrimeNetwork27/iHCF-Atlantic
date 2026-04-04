package me.scifi.hcf.faction.argument;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.doctordark.util.command.CommandArgument;

import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.FactionMember;
import me.scifi.hcf.faction.struct.Role;
import me.scifi.hcf.faction.type.PlayerFaction;

public class FactionOpenArgument extends CommandArgument {

	private final HCF plugin;

	public FactionOpenArgument(HCF plugin) {
		super("open", "Opens the faction to the public.");
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

		FactionMember factionMember = playerFaction.getMember(player.getUniqueId());

		if (factionMember.getRole() != Role.LEADER) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-OPEN-LEADER"));
			return true;
		}

		boolean newOpen = !playerFaction.isOpen();
		playerFaction.setOpen(newOpen);
		playerFaction.broadcast(
				plugin.getMessagesYML().getString("FACTION-OPEN-BROADCAST").replace("{player}", sender.getName())
						.replace("{state}", (newOpen ? plugin.getMessagesYML().getString("FACTION-OPEN-TRUE")
								: plugin.getMessagesYML().getString("FACTION-OPEN-FALSE"))));

		return true;
	}
}

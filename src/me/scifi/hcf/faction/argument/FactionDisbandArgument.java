package me.scifi.hcf.faction.argument;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.doctordark.util.command.CommandArgument;

import me.scifi.hcf.ConfigurationService;
import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.struct.Role;
import me.scifi.hcf.faction.type.PlayerFaction;

public class FactionDisbandArgument extends CommandArgument {

	private final HCF plugin;

	public FactionDisbandArgument(HCF plugin) {
		super("disband", "Disband your faction.");
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

		if (playerFaction.isRaidable() && !ConfigurationService.KIT_MAP
				&& !plugin.getManagerHandler().getEotwHandler().isEndOfTheWorld()) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-DISBAND-RAIDABLE"));
			return true;
		}

		if (playerFaction.getMember(player.getUniqueId()).getRole() != Role.LEADER) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-DISBAND-LEADER"));
			return true;
		}

		plugin.getManagerHandler().getFactionManager().removeFaction(playerFaction, sender);
		return true;
	}
}

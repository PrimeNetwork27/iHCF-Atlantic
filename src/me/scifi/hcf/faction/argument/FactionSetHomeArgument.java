package me.scifi.hcf.faction.argument;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.doctordark.util.command.CommandArgument;

import me.scifi.hcf.ConfigurationService;
import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.FactionMember;
import me.scifi.hcf.faction.claim.Claim;
import me.scifi.hcf.faction.struct.Role;
import me.scifi.hcf.faction.type.PlayerFaction;

public class FactionSetHomeArgument extends CommandArgument {

	private final HCF plugin;

	public FactionSetHomeArgument(HCF plugin) {
		super("sethome", "Sets the faction home location.");
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

		FactionMember factionMember = playerFaction.getMember(player);

		if (factionMember.getRole() == Role.MEMBER) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-SETHOME-OFFICER"));
			return true;
		}

		Location location = player.getLocation();

		boolean insideTerritory = false;
		for (Claim claim : playerFaction.getClaims()) {
			if (claim.contains(location)) {
				insideTerritory = true;
				break;
			}
		}

		if (!insideTerritory) {
			player.sendMessage(plugin.getMessagesYML().getString("FACTION-SETHOME-TERRITORY"));
			return true;
		}

		playerFaction.setHome(location);
		playerFaction.broadcast(plugin.getMessagesYML().getString("FACTION-SETHOME-BROADCAST").replace("{sender}",
				ConfigurationService.TEAMMATE_COLOUR + factionMember.getRole().getAstrix() + sender.getName()));

		return true;
	}

}

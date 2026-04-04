package me.scifi.hcf.faction.argument;

import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import com.doctordark.util.command.CommandArgument;

import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.claim.ClaimHandler;
import me.scifi.hcf.faction.type.PlayerFaction;

public class FactionClaimArgument extends CommandArgument {

	private final HCF plugin;

	public FactionClaimArgument(HCF plugin) {
		super("claim", "Claim land in the Wilderness.", new String[] { "claimland" });
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
		UUID uuid = player.getUniqueId();

		PlayerFaction playerFaction = plugin.getManagerHandler().getFactionManager().getPlayerFaction(uuid);

		if (playerFaction == null) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-NOT-FACTION"));
			return true;
		}

		if (playerFaction.isRaidable()) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-CLAIM-RAIDABLE"));
			return true;
		}

		PlayerInventory inventory = player.getInventory();

		if (inventory.contains(ClaimHandler.CLAIM_WAND)) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-CLAIM-WAND-ALREADY"));
			return true;
		}

		if (inventory.contains(ClaimHandler.SUBCLAIM_WAND)) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-CLAIM-SUBCLAIM-WAND"));
			return true;
		}

		if (!inventory.addItem(ClaimHandler.CLAIM_WAND).isEmpty()) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-CLAIM-INVENTORY-FULL"));
			return true;
		}
		sender.sendMessage(
				plugin.getMessagesYML().getString("FACTION-PLAYER-CLAIM-WAND-GIVED").replace("{label}", label));
		return true;
	}
}

package me.scifi.hcf.faction.argument;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.doctordark.util.command.CommandArgument;

import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.claim.Claim;
import me.scifi.hcf.faction.claim.ClaimHandler;
import me.scifi.hcf.faction.struct.Role;
import me.scifi.hcf.faction.type.PlayerFaction;

public class FactionClaimChunkArgument extends CommandArgument {

	private static final int CHUNK_RADIUS = 7;
	private final HCF plugin;

	public FactionClaimChunkArgument(HCF plugin) {
		super("claimchunk", "Claim a chunk of land in the Wilderness.", new String[] { "chunkclaim" });
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

		if (playerFaction.isRaidable()) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-CLAIM-RAIDABLE"));
			return true;
		}

		if (playerFaction.getMember(player.getUniqueId()).getRole() == Role.MEMBER) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-CLAIMCHUNK-MUST-OFFICER"));
			return true;
		}

		Location location = player.getLocation();
		plugin.getManagerHandler().getClaimHandler().tryPurchasing(player,
				new Claim(playerFaction,
						location.clone().add(CHUNK_RADIUS, ClaimHandler.MIN_CLAIM_HEIGHT, CHUNK_RADIUS),
						location.clone().add(-CHUNK_RADIUS, ClaimHandler.MAX_CLAIM_HEIGHT, -CHUNK_RADIUS)));

		return true;
	}
}

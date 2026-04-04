package me.scifi.hcf.faction;

import java.io.IOException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.doctordark.util.command.ArgumentExecutor;
import com.doctordark.util.command.CommandArgument;

import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.argument.FactionAcceptArgument;
import me.scifi.hcf.faction.argument.FactionAllyArgument;
import me.scifi.hcf.faction.argument.FactionAnnouncementArgument;
import me.scifi.hcf.faction.argument.FactionBoxArgument;
import me.scifi.hcf.faction.argument.FactionChatArgument;
import me.scifi.hcf.faction.argument.FactionClaimArgument;
import me.scifi.hcf.faction.argument.FactionClaimChunkArgument;
import me.scifi.hcf.faction.argument.FactionClaimsArgument;
import me.scifi.hcf.faction.argument.FactionCreateArgument;
import me.scifi.hcf.faction.argument.FactionDemoteArgument;
import me.scifi.hcf.faction.argument.FactionDepositArgument;
import me.scifi.hcf.faction.argument.FactionDisbandArgument;
import me.scifi.hcf.faction.argument.FactionFocusArgument;
import me.scifi.hcf.faction.argument.FactionHelpArgument;
import me.scifi.hcf.faction.argument.FactionHomeArgument;
import me.scifi.hcf.faction.argument.FactionInviteArgument;
import me.scifi.hcf.faction.argument.FactionInvitesArgument;
import me.scifi.hcf.faction.argument.FactionKickArgument;
import me.scifi.hcf.faction.argument.FactionLeaderArgument;
import me.scifi.hcf.faction.argument.FactionLeaveArgument;
import me.scifi.hcf.faction.argument.FactionListArgument;
import me.scifi.hcf.faction.argument.FactionMapArgument;
import me.scifi.hcf.faction.argument.FactionMessageArgument;
import me.scifi.hcf.faction.argument.FactionOpenArgument;
import me.scifi.hcf.faction.argument.FactionPromoteArgument;
import me.scifi.hcf.faction.argument.FactionRenameArgument;
import me.scifi.hcf.faction.argument.FactionSetHomeArgument;
import me.scifi.hcf.faction.argument.FactionShowArgument;
import me.scifi.hcf.faction.argument.FactionStuckArgument;
import me.scifi.hcf.faction.argument.FactionSubclaimArgumentExecutor;
import me.scifi.hcf.faction.argument.FactionTopArgument;
import me.scifi.hcf.faction.argument.FactionUnallyArgument;
import me.scifi.hcf.faction.argument.FactionUnclaimArgument;
import me.scifi.hcf.faction.argument.FactionUninviteArgument;
import me.scifi.hcf.faction.argument.FactionUnsubclaimArgument;
import me.scifi.hcf.faction.argument.FactionWithdrawArgument;
import me.scifi.hcf.faction.argument.staff.FactionChatSpyArgument;
import me.scifi.hcf.faction.argument.staff.FactionClaimForArgument;
import me.scifi.hcf.faction.argument.staff.FactionClearClaimsArgument;
import me.scifi.hcf.faction.argument.staff.FactionForceDemoteArgument;
import me.scifi.hcf.faction.argument.staff.FactionForceJoinArgument;
import me.scifi.hcf.faction.argument.staff.FactionForceKickArgument;
import me.scifi.hcf.faction.argument.staff.FactionForceLeaderArgument;
import me.scifi.hcf.faction.argument.staff.FactionForcePromoteArgument;
import me.scifi.hcf.faction.argument.staff.FactionMuteArgument;
import me.scifi.hcf.faction.argument.staff.FactionPunishArgument;
import me.scifi.hcf.faction.argument.staff.FactionRemoveArgument;
import me.scifi.hcf.faction.argument.staff.FactionSetDeathbanMultiplierArgument;
import me.scifi.hcf.faction.argument.staff.FactionSetDtrArgument;
import me.scifi.hcf.faction.argument.staff.FactionSetDtrRegenArgument;
import me.scifi.hcf.faction.argument.staff.FactionSetPointsArgument;

/**
 * Class to handle the command and tab completion for the faction command.
 */
public class FactionExecutor extends ArgumentExecutor {

	private final CommandArgument helpArgument;

	public FactionExecutor(HCF plugin) {
		super("faction");

		addArgument(new FactionAcceptArgument(plugin));
		addArgument(new FactionAllyArgument(plugin));
		addArgument(new FactionAnnouncementArgument(plugin));
		addArgument(new FactionBoxArgument(plugin));
		addArgument(new FactionChatArgument(plugin));
		addArgument(new FactionChatSpyArgument(plugin));
		addArgument(new FactionClaimArgument(plugin));
		addArgument(new FactionClaimChunkArgument(plugin));
		addArgument(new FactionClaimForArgument(plugin));
		addArgument(new FactionClaimsArgument(plugin));
		addArgument(new FactionClearClaimsArgument(plugin));
		addArgument(new FactionCreateArgument(plugin));
		addArgument(new FactionDemoteArgument(plugin));
		addArgument(new FactionDepositArgument(plugin));
		addArgument(new FactionDisbandArgument(plugin));
		addArgument(new FactionSetDtrRegenArgument(plugin));
		addArgument(new FactionForceDemoteArgument(plugin));
		addArgument(new FactionForceJoinArgument(plugin));
		addArgument(new FactionForceKickArgument(plugin));
		addArgument(new FactionForceLeaderArgument(plugin));
		addArgument(new FactionForcePromoteArgument(plugin));
		addArgument(helpArgument = new FactionHelpArgument(this));
		addArgument(new FactionHomeArgument(this, plugin));
		addArgument(new FactionInviteArgument(plugin));
		addArgument(new FactionInvitesArgument(plugin));
		addArgument(new FactionKickArgument(plugin));
		addArgument(new FactionLeaderArgument(plugin));
		addArgument(new FactionLeaveArgument(plugin));
		addArgument(new FactionListArgument(plugin));
		addArgument(new FactionMapArgument(plugin));
		addArgument(new FactionMessageArgument(plugin));
		addArgument(new FactionOpenArgument(plugin));
		addArgument(new FactionRemoveArgument(plugin));
		addArgument(new FactionRenameArgument(plugin));
		addArgument(new FactionPromoteArgument(plugin));
		addArgument(new FactionSetDtrArgument(plugin));
		addArgument(new FactionSetDeathbanMultiplierArgument(plugin));
		addArgument(new FactionSetHomeArgument(plugin));
		addArgument(new FactionShowArgument(plugin));
		addArgument(new FactionStuckArgument(plugin));
		addArgument(new FactionSubclaimArgumentExecutor(plugin));
		addArgument(new FactionUnclaimArgument(plugin));
		addArgument(new FactionUnallyArgument(plugin));
		addArgument(new FactionUninviteArgument(plugin));
		addArgument(new FactionUnsubclaimArgument(plugin));
		addArgument(new FactionWithdrawArgument(plugin));
		addArgument(new FactionSetPointsArgument(plugin));
		addArgument(new FactionTopArgument(plugin));
		addArgument(new FactionMuteArgument(plugin));
		addArgument(new FactionPunishArgument(plugin));
		addArgument(new FactionFocusArgument(plugin));
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (args.length < 1) {
			try {
				helpArgument.onCommand(sender, command, label, args);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}

		CommandArgument argument = getArgument(args[0]);
		if (argument != null) {
			String permission = argument.getPermission();
			if (permission == null || sender.hasPermission(permission)) {
				try {
					argument.onCommand(sender, command, label, args);
				} catch (IOException e) {
					e.printStackTrace();
				}

				return true;
			}
		}

		try {
			helpArgument.onCommand(sender, command, label, args);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
}

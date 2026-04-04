package me.scifi.hcf.faction.argument;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.doctordark.util.command.CommandArgument;

import me.scifi.hcf.DurationFormatter;
import me.scifi.hcf.HCF;
import me.scifi.hcf.timer.type.StuckTimer;

/**
 * Faction argument used to teleport to a nearby {@link org.bukkit.Location}
 * safely if stuck.
 */
public class FactionStuckArgument extends CommandArgument {

	private final HCF plugin;

	public FactionStuckArgument(HCF plugin) {
		super("stuck", "Teleport to a safe position.", new String[] { "trap", "trapped" });
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

		if (player.getWorld().getEnvironment() != World.Environment.NORMAL) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-STUCK-WORLD"));
			return true;
		}

		StuckTimer stuckTimer = plugin.getManagerHandler().getTimerManager().getStuckTimer();

		if (!stuckTimer.setCooldown(player, player.getUniqueId())) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-STUCK-ALREADY").replace("{timer}",
					stuckTimer.getDisplayName()));
			return true;
		}

		sender.sendMessage(plugin.getMessagesYML().getString("FACTION-STUCK-BROADCAST")
				.replace("{timer}", stuckTimer.getDisplayName())
				.replace("{remaining}", DurationFormatter.getRemaining(stuckTimer.getRemaining(player), true, false))
				.replace("{distance}", String.valueOf(StuckTimer.MAX_MOVE_DISTANCE)));

		return true;
	}
}

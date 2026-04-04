package me.scifi.hcf.faction.argument;

import java.io.IOException;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.doctordark.util.command.CommandArgument;

import me.scifi.hcf.ConfigurationService;
import me.scifi.hcf.DurationFormatter;
import me.scifi.hcf.HCF;
import me.scifi.hcf.eventgame.faction.EventFaction;
import me.scifi.hcf.faction.FactionExecutor;
import me.scifi.hcf.faction.type.Faction;
import me.scifi.hcf.faction.type.PlayerFaction;
import me.scifi.hcf.timer.PlayerTimer;

/**
 * Faction argument used to teleport to {@link Faction} home {@link Location}s.
 */
public class FactionHomeArgument extends CommandArgument {

	private final FactionExecutor factionExecutor;
	private final HCF plugin;

	public FactionHomeArgument(FactionExecutor factionExecutor, HCF plugin) {
		super("home", "Teleport to the faction home.");
		this.factionExecutor = factionExecutor;
		this.plugin = plugin;
	}

	@Override
	public String getUsage(String label) {
		return '/' + label + ' ' + getName();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) throws IOException {
		if (!(sender instanceof Player)) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-ONLY"));
			return true;
		}

		Player player = (Player) sender;

		if (args.length >= 2 && args[1].equalsIgnoreCase("set")) {
			factionExecutor.getArgument("sethome").onCommand(sender, command, label, args);
			return true;
		}

		UUID uuid = player.getUniqueId();

		PlayerTimer timer = plugin.getManagerHandler().getTimerManager().getEnderPearlTimer();
		long remaining = timer.getRemaining(player);

		if (remaining > 0L && !ConfigurationService.HOME_ENDERPEARL) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-HOME-PEARL")
					.replace("{timerName}", timer.getDisplayName())
					.replace("{remaining}", DurationFormatter.getRemaining(remaining, true, false)));

			return true;
		}

		if ((remaining = (timer = plugin.getManagerHandler().getTimerManager().getCombatTimer())
				.getRemaining(player)) > 0L) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-HOME-COMBAT")
					.replace("{timerName}", timer.getDisplayName())
					.replace("{remaining}", DurationFormatter.getRemaining(remaining, true, false)));

			return true;
		}

		PlayerFaction playerFaction = plugin.getManagerHandler().getFactionManager().getPlayerFaction(uuid);

		if (playerFaction == null) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-NOT-FACTION"));
			return true;
		}

		Location home = playerFaction.getHome();

		if (home == null) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-HOME-NOT-SET"));
			return true;
		}

		Faction factionAt = plugin.getManagerHandler().getFactionManager().getFactionAt(player.getLocation());

		if (factionAt instanceof EventFaction) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-HOME-EVENTZONE"));
			return true;
		}

		/*
		 * if (factionAt != playerFaction && factionAt instanceof PlayerFaction) {
		 * player.sendMessage(ChatColor.RED + "You may not warp in enemy claims. Use " +
		 * ChatColor.YELLOW + '/' + label + " stuck" + ChatColor.RED + " if trapped.");
		 * return true; }
		 */

		long millis;
		if (factionAt.isSafezone()) {
			millis = 0L;
		} else {
			switch (player.getWorld().getEnvironment()) {
			case THE_END:
				if (!ConfigurationService.HOME_END) {
					sender.sendMessage(plugin.getMessagesYML().getString("FACTION-HOME-END"));
					return true;
				}
				millis = ConfigurationService.cooldown_end;
				break;
			case NETHER:
				millis = ConfigurationService.cooldown_nether;
				break;
			default:
				millis = ConfigurationService.cooldown_overworld;
				break;
			}
		}

		if (factionAt != playerFaction && factionAt instanceof PlayerFaction) {
			millis *= 2L;
		}
		plugin.getManagerHandler().getTimerManager().getTeleportTimer().teleport(player, home, millis,
				plugin.getMessagesYML().getString("FACTION-HOME-SUCCESS").replace("{remaining}",
						DurationFormatter.getRemaining(millis, true, false)),
				PlayerTeleportEvent.TeleportCause.COMMAND);

		return true;
	}
}

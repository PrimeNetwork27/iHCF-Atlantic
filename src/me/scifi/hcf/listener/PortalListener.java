package me.scifi.hcf.listener;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffectType;

import com.doctordark.util.CC;
import com.doctordark.util.LocationUtil;

import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import lombok.Setter;
import me.scifi.hcf.ConfigurationService;
import me.scifi.hcf.DurationFormatter;
import me.scifi.hcf.HCF;
import me.scifi.hcf.timer.PlayerTimer;

public class PortalListener implements Listener {

	private static final long PORTAL_MESSAGE_DELAY_THRESHOLD = 2500L;
	@Setter
	private Location endExit;

	private final TObjectLongMap<UUID> messageDelays = new TObjectLongHashMap<>();
	private final HCF plugin;

	public PortalListener(HCF plugin) {
		this.plugin = plugin;
		this.endExit = LocationUtil.deserializeLocation(HCF.getPlugin().getConfig().getString("ENDEXIT"));
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	public void onEntityPortal(EntityPortalEvent event) {
		if (event.getEntity() instanceof EnderDragon) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	public void onPlayerPortal(PlayerPortalEvent event) {
		if (event.getCause() != PlayerTeleportEvent.TeleportCause.END_PORTAL) {
			return;
		}

		if (event.getTo() != null) {
			World toWorld = event.getTo().getWorld();
			if (toWorld != null && toWorld.getEnvironment() == World.Environment.THE_END) {
				event.useTravelAgent(false);
				event.setTo(toWorld.getSpawnLocation());
				return;
			}
		}

		World fromWorld = event.getFrom().getWorld();
		if (fromWorld != null && fromWorld.getEnvironment() == World.Environment.THE_END) {
			event.useTravelAgent(false);
			event.setTo(endExit);
		}
	}

	@EventHandler
	public void onEndMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		if (p.getWorld().getEnvironment() == World.Environment.THE_END) {
			if (p.getLocation().getBlock().isLiquid()) {
				p.teleport(endExit);
			}
		}
	}

	// Prevent players jumping the End with Strength.
	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	public void onWorldChanged(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();
		World from = event.getFrom();
		World to = player.getWorld();
		if (from.getEnvironment() != World.Environment.THE_END && to.getEnvironment() == World.Environment.THE_END
				&& player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
			player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onPortalEnter(PlayerPortalEvent event) {
		if (event.getCause() != PlayerTeleportEvent.TeleportCause.END_PORTAL) {
			return;
		}

		Location to = event.getTo();
		World toWorld = to.getWorld();
		if (toWorld == null) {
			return; // safe-guard if the End or Nether is disabled
		}

		if (toWorld.getEnvironment() == World.Environment.THE_END) {
			Player player = event.getPlayer();

			// Prevent entering the end if it's closed.
			if (!ConfigurationService.END_IS_OPEN) {
				message(player, ChatColor.RED + "The End is currently closed.");
				event.setCancelled(true);
				return;
			}

			// Prevent entering the end if the player is Spawn Tagged.
			PlayerTimer timer = plugin.getManagerHandler().getTimerManager().getCombatTimer();
			long remaining;
			if ((remaining = timer.getRemaining(player)) > 0L) {
				String msg = plugin.getMessagesYML().getString("END-COMBAT-TIMER")
						.replace("{combatName}", timer.getDisplayName())
						.replace("{duration}", DurationFormatter.getRemaining(remaining, true, false));
				message(player, CC.translate(msg));

				event.setCancelled(true);
				return;
			}

			// Prevent entering the end if the player is PVP Protected.
			timer = plugin.getManagerHandler().getTimerManager().getPvpTimer();
			if ((remaining = timer.getRemaining(player)) > 0L) {
				String msg = plugin.getMessagesYML().getString("END-PROTECTION-TIMER")
						.replace("{protectionName}", timer.getDisplayName())
						.replace("{duration}", DurationFormatter.getRemaining(remaining, true, false));
				message(player, msg);

				event.setCancelled(true);
				return;
			}

			event.useTravelAgent(false);
			event.setTo(toWorld.getSpawnLocation().add(0.5, 0, 0.5));
		}
	}

	private void message(Player player, String message) {
		long last = messageDelays.get(player.getUniqueId());
		long millis = System.currentTimeMillis();
		if (last != messageDelays.getNoEntryValue() && (last + PORTAL_MESSAGE_DELAY_THRESHOLD) - millis > 0L) {
			return;
		}

		messageDelays.put(player.getUniqueId(), millis);
		player.sendMessage(message);
	}
}

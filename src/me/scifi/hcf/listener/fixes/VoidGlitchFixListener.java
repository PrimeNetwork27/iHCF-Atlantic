package me.scifi.hcf.listener.fixes;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.doctordark.util.BukkitUtils;

/**
 * Listener that prevents players being killed by the void in the Overworld.
 */
public class VoidGlitchFixListener implements Listener {

	private final HashSet<UUID> recentlyTeleported = new HashSet<>();

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onPlayerDamage(EntityDamageEvent event) {
		if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
			Entity entity = event.getEntity();
			if (entity instanceof Player) {

				if (entity.getWorld().getEnvironment() == World.Environment.THE_END) {
					return;
				}

				Location destination = BukkitUtils.getHighestLocation(entity.getLocation());
				if (destination != null && entity.teleport(destination, PlayerTeleportEvent.TeleportCause.PLUGIN)) {
					event.setCancelled(true);
					recentlyTeleported.add(entity.getUniqueId());
					entity.sendMessage(ChatColor.YELLOW + "You were saved from the void.");
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onFallDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
			Player player = (Player) event.getEntity();
			if (recentlyTeleported.contains(player.getUniqueId())) {
				event.setCancelled(true);
				recentlyTeleported.remove(player.getUniqueId());
			}
		}
	}
}

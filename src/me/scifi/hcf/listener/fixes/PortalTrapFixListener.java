package me.scifi.hcf.listener.fixes;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

/**
 * Listener that prevents {@link Player}s from being trapped in portals.
 */
public class PortalTrapFixListener implements Listener {

	@EventHandler
	public void onNetherPortalFix(final PlayerPortalEvent e) {
		if (e.getTo().getWorld().getEnvironment() == World.Environment.NETHER) {
			// e.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "Some of the blocks around
			// the portal have been updated to prevent nether portal trapping.");
			final Location l = e.getTo();
			final int range = 3;
			final int minX = l.getBlockX() - 5;
			final int minY = l.getBlockY() - 2;
			final int minZ = l.getBlockZ() - 5;
			for (int x = minX; x < minX + range; ++x) {
				for (int y = minY; y < minY + range; ++y) {
					for (int z = minZ; z < minZ + range; ++z) {
						final Block block = e.getTo().getWorld().getBlockAt(x, y, z);
						final Block platform = e.getTo().getWorld().getBlockAt(x, (int) (l.getY() - 2.0), z);
						if (platform.getType() != Material.OBSIDIAN) {
							platform.setType(Material.OBSIDIAN);
						}
						if (block.getType() == Material.LAVA || block.getType() == Material.STATIONARY_LAVA
								|| block.getType() == Material.SAND || block.getType() == Material.QUARTZ_ORE
								|| block.getType() == Material.NETHERRACK || block.getType() == Material.GRAVEL
								|| block.getType() == Material.SOUL_SAND || block.getType() == Material.NETHER_BRICK
								|| block.getType() == Material.COBBLESTONE) {
							block.setType(Material.AIR);
						}
					}
				}
			}
		}
	}
}
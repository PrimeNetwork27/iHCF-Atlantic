package me.scifi.hcf.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.World;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;

import me.scifi.hcf.HCF;
import me.scifi.hcf.Utils;
import net.md_5.bungee.api.ChatColor;

public class CoreListener implements Listener {

	private HCF plugin;

	protected Map<UUID, ItemStack[]> armor = new HashMap<>();
	protected Map<UUID, ItemStack[]> content = new HashMap<>();

	public CoreListener(HCF plugin) {
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (player.getWorld().getEnvironment() == World.Environment.NETHER
				&& event.getBlock().getState() instanceof CreatureSpawner
				&& !player.hasPermission(ProtectionListener.PROTECTION_BYPASS_PERMISSION)) {

			event.setCancelled(true);
			player.sendMessage(ChatColor.RED + "You cannot break spawners in the nether.");
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if (player.getWorld().getEnvironment() == World.Environment.NETHER
				&& event.getBlock().getState() instanceof CreatureSpawner
				&& !player.hasPermission(ProtectionListener.PROTECTION_BYPASS_PERMISSION)) {

			event.setCancelled(true);
			player.sendMessage(ChatColor.RED + "You cannot place spawners in the nether.");
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (e.getInventory() == null) {
			return;
		}

		if (!e.getInventory().getName().startsWith(Utils.chat("&cInspecting"))) {
			return;
		}

		e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onWeatherChange(WeatherChangeEvent e) {
		boolean isRaining = e.toWeatherState();
		if (isRaining) {
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent event) {
		event.setQuitMessage(null);

		Player player = event.getPlayer();
		plugin.getManagerHandler().getVisualiseHandler().clearVisualBlocks(player, null, null, false);
		plugin.getManagerHandler().getUserManager().getUser(player.getUniqueId()).setShowClaimMap(false);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();
		plugin.getManagerHandler().getVisualiseHandler().clearVisualBlocks(player, null, null, false);
		plugin.getManagerHandler().getUserManager().getUser(player.getUniqueId()).setShowClaimMap(false);
	}

}

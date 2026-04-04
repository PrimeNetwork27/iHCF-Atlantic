package me.scifi.hcf.features.staffmode;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.doctordark.util.CC;
import com.doctordark.util.ItemBuilder;

import me.scifi.hcf.HCF;
import me.scifi.hcf.inventories.Inventories;

public class StaffModeListener implements Listener {

	private HCF plugin;

	public StaffModeListener(HCF plugin) {
		this.plugin = plugin;
	}

	private StaffModeManager getManager() {
		return plugin.getStaffModeManager();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		if (getManager().getStaffMode().contains(p.getUniqueId())) {
			p.sendMessage(CC.translate(plugin.getMessagesYML().getString("STAFFMODE-BLOCK-PLACE")));
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlace(BlockPlaceEvent e) {
		if (Vanish.vanishedPlayers.contains(e.getPlayer().getUniqueId())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();
		if (getManager().getStaffMode().contains(p.getUniqueId())) {
			p.sendMessage(CC.translate(plugin.getMessagesYML().getString("STAFFMODE-BLOCK-BREAK")));
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBreak(BlockBreakEvent e) {
		if (Vanish.vanishedPlayers.contains(e.getPlayer().getUniqueId())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDamage(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof Player)) {
			return;
		}
		Player p = (Player) e.getEntity();
		if (getManager().getStaffMode().contains(p.getUniqueId()) || Vanish.isPlayerVanished(p)) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (!(e.getDamager() instanceof Player)) {
			return;
		}
		Player damager = (Player) e.getDamager();
		if (getManager().getStaffMode().contains(damager.getUniqueId()) || Vanish.isPlayerVanished(damager)) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (player.hasPermission("hcf.command.staffmode")) {
			getManager().putInStaffMode(player);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		if (!getManager().getStaffMode().contains(p.getUniqueId())) {
			return;
		}
		if (e.getItem() == null || !e.getItem().hasItemMeta()) {
			return;
		}

		ItemStack held = e.getItem();

		if (held.isSimilar(getManager().getDye())) {
			Vanish.disableVanish(p);
			ItemStack disabled = new ItemBuilder(Material.INK_SACK, 1, (byte) 8)
					.displayName(CC.translate(plugin.getMessagesYML().getString("STAFFMODE.VANISHITEM.NAME.DISABLED")))
					.lore(CC.translate(plugin.getMessagesYML().getStringList("STAFFMODE.VANISHITEM.LORE"))).build();
			p.getInventory().setItemInHand(disabled);
		} else if (held.getType() == Material.INK_SACK && held.getDurability() == 8) {
			Vanish.setVanished(p);
			p.getInventory().setItemInHand(getManager().getDye().clone());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onInteractTP(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (!getManager().getStaffMode().contains(p.getUniqueId())) {
			return;
		}
		if (e.getItem() == null || !e.getItem().isSimilar(getManager().getHead())) {
			return;
		}

		Random random = new Random();
		if (HCF.getOnlinePlayers().size() > 1) {
			int index = random.nextInt(HCF.getOnlinePlayers().size());
			Player to = (Player) Bukkit.getServer().getOnlinePlayers().toArray()[index];
			if (to != p) {
				p.teleport(to);
				p.sendMessage(CC.translate(plugin.getMessagesYML().getString("TELEPORT-SUCCESSFUL-MESSAGE")));
			} else {
				p.sendMessage(CC.translate("&cYou cannot teleport to yourself."));
			}
		} else {
			p.sendMessage(CC.translate(plugin.getMessagesYML().getString("NOT-ENOUGH-PLAYERS")));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onRightClick(PlayerInteractEntityEvent e) {
		Player p = e.getPlayer();
		if (!getManager().getStaffMode().contains(p.getUniqueId())) {
			return;
		}
		if (!(e.getRightClicked() instanceof Player)) {
			return;
		}

		Player rightClicked = (Player) e.getRightClicked();
		ItemStack held = p.getInventory().getItemInHand();
		if (held == null) {
			return;
		}

		if (held.isSimilar(getManager().getIce()) && p.hasPermission("hcf.command.freeze")) {
			Bukkit.getServer().dispatchCommand(p, "freeze " + rightClicked.getName());
		} else if (held.isSimilar(getManager().getBook())) {
			Inventories.staffInventoryInspector(p, rightClicked);
			p.sendMessage(CC.translate("&eNow Inspecting &f" + rightClicked.getName()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onDrop(PlayerDropItemEvent e) {
		if (getManager().getStaffMode().contains(e.getPlayer().getUniqueId())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPickup(PlayerPickupItemEvent e) {
		if (getManager().getStaffMode().contains(e.getPlayer().getUniqueId())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onDisconnect(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		if (getManager().getStaffMode().contains(p.getUniqueId())) {
			getManager().removeFromStaffMode(p);
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (e.getInventory() != null && e.getInventory().getTitle().startsWith(CC.translate("&cInspecting: "))) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onGameModeChange(PlayerGameModeChangeEvent e) {
		Player p = e.getPlayer();
		if (getManager().getStaffMode().contains(p.getUniqueId())) {
			p.sendMessage(CC.translate(plugin.getMessagesYML().getString("STAFFMODE-GAMEMODE-SWITCH")));
			p.setGameMode(GameMode.CREATIVE);
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onInventoryInteract(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		if (getManager().getStaffMode().contains(p.getUniqueId())) {
			e.setCancelled(true);
		}
	}
}
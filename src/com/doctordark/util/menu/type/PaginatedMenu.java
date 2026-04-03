package com.doctordark.util.menu.type;

import java.lang.reflect.Field;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.doctordark.util.menu.Button;
import com.doctordark.util.menu.IMenu;
import com.doctordark.util.menu.InventoryUtil;
import com.doctordark.util.menu.anottation.MenuProcessor;
import com.doctordark.util.menu.anottation.Slot;

import lombok.Getter;
import me.scifi.hcf.HCF;

@Getter
public abstract class PaginatedMenu implements IMenu {

	protected HCF plugin;

	private int page;

	public final Inventory inventory;

	public PaginatedMenu(int size, int page) {
		plugin = HCF.getPlugin();

		this.page = page;

		inventory = plugin.getServer().createInventory(this, size,
				getTitle().length() > 32 ? getTitle().substring(0, 32) : getTitle());
		MenuProcessor.populate(this); // Slot
		updateContents();
	}

	public abstract int getTotalPages();

	public abstract void updateContents();

	public void displayTo(Player player) {
		MenuProcessor.populate(this);
		player.openInventory(inventory);
	}

	public ItemStack getItemAt(int slot) {
		ItemStack existing = inventory.getItem(slot);
		if (existing != null && existing.getType() != Material.AIR) {
			return existing;
		}
		ItemStack fill = getFillMaterial();
		return fill != null ? fill : null;
	}

	@Override
	public ItemStack getFillMaterial() {
		return null;
	}

	public void setPage(int page) {
		if (page < 0) {
			page = 0;
		}
		if (page >= getTotalPages()) {
			page = getTotalPages() - 1;
		}

		this.page = page;

		for (HumanEntity viewer : inventory.getViewers()) {
			InventoryUtil.changeTitle((Player) viewer, getTitle());
		}

		updateContents();
	}

	@Override
	public void open(Player player) {
		player.openInventory(getInventory());

		setPage(getPage());
	}

	@Override
	public void onInventoryClick(InventoryClickEvent event) {
		event.setCancelled(true);
		if (!(event.getWhoClicked() instanceof Player)) {
			return;
		}
		if (event.getClickedInventory() == null) {
			return;
		}
		if (!event.getClickedInventory().equals(inventory)) {
			return;
		}

		Player player = (Player) event.getWhoClicked();
		int slot = event.getSlot();

		for (Field field : getClass().getDeclaredFields()) {
			if (!field.isAnnotationPresent(Slot.class)) {
				continue;
			}
			if (field.getAnnotation(Slot.class).value() != slot) {
				continue;
			}

			field.setAccessible(true);
			try {
				Object value = field.get(this);
				if (value instanceof Button) {
					((Button) value).getAction().accept(player);
				}
			} catch (Exception ignored) {
			}
			return;
		}
	}

	@Override
	public void onInventoryDrag(InventoryDragEvent event) {
		Inventory topInventory = event.getView().getTopInventory();
		if (topInventory.equals(inventory)) {
			event.setCancelled(true);
		}
	}
}
package com.doctordark.util.menu.type;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.doctordark.util.menu.Button;
import com.doctordark.util.menu.IMenu;
import com.doctordark.util.menu.anottation.Slot;

import lombok.Getter;
import me.scifi.hcf.HCF;

@Getter
public abstract class Menu implements IMenu {
	protected HCF plugin;
	public final Inventory inventory;
	private final Map<Button, Slot> registeredButtons = new HashMap<>();
	private final Map<Integer, Button> registeredButtonPositions = new HashMap<>();

	public Menu(int size, String title) {
		plugin = HCF.getPlugin();
		inventory = plugin.getServer().createInventory(this, size, title);
	}

	public void displayTo(Player player) {
		populate();
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

	public void setItem(ItemStack stack, int slot) {
		inventory.setItem(slot, stack);
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
		Button button = registeredButtonPositions.get(event.getSlot());
		if (button != null) {
			button.getAction().accept(player);
		}
	}

	public void registerButton(Button button, int slot) {
		registeredButtonPositions.put(slot, button);
		inventory.setItem(slot, button.getItem());
	}

	public void populate() {
		for (Field field : getClass().getDeclaredFields()) {
			if (!field.isAnnotationPresent(Slot.class)) {
				continue;
			}
			field.setAccessible(true);
			Slot annotation = field.getAnnotation(Slot.class);
			int slot = annotation.value();
			try {
				Object value = field.get(this);
				if (value instanceof Button) {
					inventory.setItem(slot, ((Button) value).getItem());
					registeredButtonPositions.put(slot, (Button) value);
					registeredButtons.put((Button) value, annotation);
				} else if (value instanceof ItemStack) {
					inventory.setItem(slot, (ItemStack) value);
				}
			} catch (Exception ignored) {
			}
		}

		for (Method method : getClass().getMethods()) {
			if (!method.isAnnotationPresent(Slot.class)) {
				continue;
			}
			if (method.getParameterCount() != 0) {
				continue;
			}
			if (!ItemStack.class.isAssignableFrom(method.getReturnType())) {
				continue;
			}
			int slot = method.getAnnotation(Slot.class).value();
			try {
				ItemStack item = (ItemStack) method.invoke(this);
				if (item != null) {
					inventory.setItem(slot, item);
				}
			} catch (Exception ignored) {
			}
		}

		ItemStack fill = getFillMaterial();
		if (fill != null) {
			for (int i = 0; i < inventory.getSize(); i++) {
				ItemStack current = inventory.getItem(i);
				if (current == null || current.getType() == Material.AIR) {
					inventory.setItem(i, fill);
				}
			}
		}
	}

}
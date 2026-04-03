package com.doctordark.util.menu.anottation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.doctordark.util.menu.Button;
import com.doctordark.util.menu.IMenu;
import com.doctordark.util.menu.type.ChestMenu;

public class MenuProcessor {

	public static void populate(IMenu menu) {
		for (Field field : menu.getClass().getDeclaredFields()) {
			if (!field.isAnnotationPresent(Slot.class))
				continue;
			field.setAccessible(true);
			int slot = field.getAnnotation(Slot.class).value();
			try {
				Object value = field.get(menu);
				if (value instanceof Button) {
					menu.getInventory().setItem(slot, ((Button) value).getItem());
				} else if (value instanceof ItemStack) {
					menu.getInventory().setItem(slot, (ItemStack) value);
				}
			} catch (Exception ignored) {
			}
		}

		for (Method method : menu.getClass().getMethods()) {
			if (!method.isAnnotationPresent(Slot.class))
				continue;
			if (method.getParameterCount() != 0)
				continue;
			if (!ItemStack.class.isAssignableFrom(method.getReturnType()))
				continue;
			int slot = method.getAnnotation(Slot.class).value();
			try {
				ItemStack item = (ItemStack) method.invoke(menu);
				if (item != null)
					menu.getInventory().setItem(slot, item);
			} catch (Exception ignored) {
			}
		}

		if (menu instanceof ChestMenu) {
			ChestMenu chestMenu = (ChestMenu) menu;
			ItemStack fill = chestMenu.getFillMaterial();
			if (fill != null) {
				for (int i = 0; i < menu.getInventory().getSize(); i++) {
					ItemStack current = menu.getInventory().getItem(i);
					if (current == null || current.getType() == Material.AIR) {
						menu.getInventory().setItem(i, fill);
					}
				}
			}
		}
	}
}
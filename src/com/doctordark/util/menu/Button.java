package com.doctordark.util.menu;

import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Button {

	private final ItemStack item;
	private final Consumer<Player> action;

	public static Button of(ItemStack item, Consumer<Player> action) {
		return new Button(item, action);
	}

	public static Button of(ItemStack item) {
		return new Button(item, player -> {
		});
	}
}
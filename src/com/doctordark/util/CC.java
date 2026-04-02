package com.doctordark.util;

import org.bukkit.ChatColor;

public class CC {

	public static String translate(String s) {
		return ChatColor.translateAlternateColorCodes('&', s);
	}

	public static String color(String s) {
		return translate(s);
	}
}
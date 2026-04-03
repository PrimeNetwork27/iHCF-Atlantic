package com.doctordark.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

public class CC {

	public static String translate(String s) {
		return ChatColor.translateAlternateColorCodes('&', s);
	}

	public static String color(String s) {
		return translate(s);
	}

	public static List<String> translate(List<String> input) {
		List<String> ret = new ArrayList<String>();
		for (String line : input) {
			ret.add(ChatColor.translateAlternateColorCodes('&', line));
		}
		return ret;
	}
}
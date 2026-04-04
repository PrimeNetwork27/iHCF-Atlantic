package com.doctordark.util;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public final class LocationUtil {

	public static String serializeLocation(Location location) {
		return "[world=" + location.getWorld().getName() + ",x=" + location.getBlockX() + ",y=" + location.getBlockY()
				+ ",z=" + location.getBlockZ() + ",yaw=" + location.getYaw() + ",pitch=" + location.getPitch() + "]";
	}

	public static Location deserializeLocation(String serialized) {
		if (serialized == null || serialized.isEmpty()) {
			return null;
		}

		serialized = serialized.replace("[", "").replace("]", "");

		Map<String, String> map = new HashMap<>();
		for (String part : serialized.split(",")) {
			String[] entry = part.split("=");
			if (entry.length == 2) {
				map.put(entry[0].trim(), entry[1].trim());
			}
		}

		World world = Bukkit.getWorld(map.get("world"));
		if (world == null) {
			return null;
		}

		double x = Double.parseDouble(map.get("x"));
		double y = Double.parseDouble(map.get("y"));
		double z = Double.parseDouble(map.get("z"));
		float yaw = Float.parseFloat(map.get("yaw"));
		float pitch = Float.parseFloat(map.get("pitch"));

		return new Location(world, x, y, z, yaw, pitch);
	}
}

package me.scifi.hcf.combatlog.type;

import java.util.UUID;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;

import me.scifi.hcf.HCF;

public interface LoggerEntity {

	/**
	 * Spawns this NPC.
	 *
	 * @param plugin the plugin instance
	 */
	void postSpawn(HCF plugin);

	/**
	 * Gets the Bukkit entity view.
	 *
	 * @return the {@link org.bukkit.entity.Entity}
	 */
	CraftPlayer getBukkitEntity();

	/**
	 * Gets the {@link UUID} of the represented.
	 *
	 * @return the represented {@link UUID}
	 */
	UUID getUniqueID();

	/**
	 * Removes this entity.
	 */
	void destroy();
}

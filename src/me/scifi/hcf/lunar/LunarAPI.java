package me.scifi.hcf.lunar;

import org.bukkit.Bukkit;

import lombok.Getter;
import me.scifi.hcf.HCF;

@Getter
public class LunarAPI {

	private NametagManager nametagManager;

	public void init() {
		// LOAD THE MANAGERS

		nametagManager = new NametagManager();

		// NOW LOAD THE LUNAR API WITH THE IMPLEMENTATIONS OF THAT MANAGERS.
		LunarNametag lunarTask = new LunarNametag();

		Bukkit.getScheduler().runTaskTimer(HCF.getPlugin(), lunarTask, 20L, 40L);
	}

}

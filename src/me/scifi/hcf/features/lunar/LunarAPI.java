package me.scifi.hcf.features.lunar;

import org.bukkit.Bukkit;

import lombok.Getter;
import me.scifi.hcf.HCF;
import me.scifi.hcf.features.lunar.listener.LunarListener;
import me.scifi.hcf.features.lunar.modules.BorderManager;
import me.scifi.hcf.features.lunar.modules.NametagManager;
import me.scifi.hcf.features.lunar.modules.TeamManager;
import me.scifi.hcf.features.lunar.modules.WaypointsManager;
import me.scifi.hcf.features.lunar.task.LunarNametag;

@Getter
public class LunarAPI {

	private NametagManager nametagManager;
	private TeamManager teamManager;
	private WaypointsManager waypointsManager;
	private BorderManager borderManager;

	public void init() {

		nametagManager = new NametagManager();
		teamManager = new TeamManager();
		waypointsManager = new WaypointsManager();
		borderManager = new BorderManager();

		LunarNametag lunarTask = new LunarNametag();
		Bukkit.getPluginManager().registerEvents(new LunarListener(), HCF.getPlugin());
		Bukkit.getScheduler().runTaskTimer(HCF.getPlugin(), lunarTask, 20L, 40L);
	}

}

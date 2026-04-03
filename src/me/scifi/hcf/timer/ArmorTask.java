package me.scifi.hcf.timer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import lombok.RequiredArgsConstructor;
import me.scifi.hcf.timer.type.PvpClassWarmupTimer;

/**
 * Class to handle the Warmup Timer because Our spigot doesn't have
 * EquipmentSetEvent
 */
@RequiredArgsConstructor
public class ArmorTask extends BukkitRunnable {

	private final PvpClassWarmupTimer timer;

	@Override
	public void run() {
		for (Player player : Bukkit.getOnlinePlayers().toArray(new Player[0])) {
			timer.attemptEquip(player);
		}
	}

}

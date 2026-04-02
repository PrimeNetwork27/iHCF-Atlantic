package com.doctordark.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.doctordark.util.event.CooldownExpiredEvent;
import com.doctordark.util.event.CooldownStartedEvent;
import com.doctordark.util.event.CooldownStartingEvent;

import lombok.Getter;
import me.scifi.hcf.HCF;

@Getter
public class Cooldowns {

	@Getter
	private static Map<String, Cooldowns> cooldownMap = new HashMap<>();
	private Map<UUID, Long> longMap;
	private Map<UUID, Integer> taskMap;
	private String name, displayName, expiredMessage;
	private long duration;

	public Cooldowns(String name, long duration) {
		this(name, duration, null, null);
	}

	public Cooldowns(String name, long duration, String displayName, String expiredMessage) {
		this.longMap = new HashMap<>();
		this.name = name;
		this.duration = duration;
		this.displayName = ((displayName == null) ? name : displayName);
		this.taskMap = new HashMap<>();
		if (expiredMessage != null) {
			this.expiredMessage = expiredMessage;
		}
		cooldownMap.put(name, this);
	}

	public void setCooldown(Player player) {
		this.setCooldown(player, false);
	}

	public void setCooldown(Player player, boolean announce) {
		CooldownStartingEvent event = new CooldownStartingEvent(player, this);
		if (!event.call()) {
			if (event.getReason() != null) {
				player.sendMessage(CC.translate(event.getReason()));
			}
			return;
		}
		taskMap.remove(player.getUniqueId());
		this.longMap.put(player.getUniqueId(), System.currentTimeMillis() + this.duration);
		if (new CooldownStartedEvent(player, this).call()) {
			if (this.expiredMessage != null && announce) {
				int taskId = new BukkitRunnable() {

					@Override
					public void run() {
						if (!player.isOnline()) {
							return;
						}
						for (String s : expiredMessage.split("\n")) {
							player.sendMessage(CC.translate(s));
						}
						new CooldownExpiredEvent(player, Cooldowns.this).call();
					}
				}.runTaskLater(HCF.getPlugin(), (int) duration / 1000 * 20L).getTaskId();
				taskMap.put(player.getUniqueId(), taskId);

			}
		}
	}

	public long getDuration(Player player) {
		return longMap.getOrDefault(player.getUniqueId(), 0L) - System.currentTimeMillis();
	}

	public boolean isOnCooldown(Player player) {
		return this.getDuration(player) > 0L;
	}

	public boolean remove(Player player) {
		if (isOnCooldown(player)) {
			this.longMap.remove(player.getUniqueId());
			new CooldownExpiredEvent(player, this).setForced(true).call();
			if (taskMap.containsKey(player.getUniqueId())) {
				Bukkit.getServer().getScheduler().cancelTask(taskMap.get(player.getUniqueId()));
			}
		}
		return isOnCooldown(player);
	}
}
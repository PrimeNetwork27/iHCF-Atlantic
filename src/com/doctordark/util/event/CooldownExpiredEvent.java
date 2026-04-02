package com.doctordark.util.event;

import org.bukkit.entity.Player;

import com.doctordark.util.Cooldowns;

import lombok.Getter;

@Getter
public class CooldownExpiredEvent extends PlayerBase {

	private Cooldowns cooldown;
	private boolean forced;

	public CooldownExpiredEvent(Player player, Cooldowns cooldown) {
		super(player);
		this.cooldown = cooldown;
	}

	public BaseEvent setForced(boolean forced) {
		this.forced = forced;
		return this;
	}
}
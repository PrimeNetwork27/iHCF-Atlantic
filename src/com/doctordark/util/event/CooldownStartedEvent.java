package com.doctordark.util.event;

import org.bukkit.entity.Player;

import com.doctordark.util.Cooldowns;

import lombok.Getter;

public class CooldownStartedEvent extends PlayerBase {

	@Getter
	private Cooldowns cooldown;

	public CooldownStartedEvent(Player player, Cooldowns cooldown) {
		super(player);
		this.cooldown = cooldown;
	}
}
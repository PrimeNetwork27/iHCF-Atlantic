package com.doctordark.util.event;

import org.bukkit.entity.Player;

import com.doctordark.util.Cooldowns;

import lombok.Getter;
import lombok.Setter;

@Getter
public class CooldownStartingEvent extends PlayerBase {

	private Cooldowns cooldown;
	@Setter
	private String reason;

	public CooldownStartingEvent(Player player, Cooldowns cooldown) {
		super(player);
		this.cooldown = cooldown;
	}

}
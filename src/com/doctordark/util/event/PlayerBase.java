package com.doctordark.util.event;

import org.bukkit.entity.Player;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class PlayerBase extends BaseEvent {
	@Getter
	Player player;
}

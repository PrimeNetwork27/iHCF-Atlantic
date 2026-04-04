package me.scifi.hcf.eventgame.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameEvent extends Event {
	private static final HandlerList HANDLERS = new HandlerList();
	private boolean cancelled;

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public GameEvent setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
		return this;
	}

	public boolean call() {
		Bukkit.getPluginManager().callEvent(this);
		return !this.isCancelled();
	}

	public boolean isCancelled() {
		return this.cancelled;
	}

}

package com.doctordark.util.scoreboard.tablist.shared;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.doctordark.util.scoreboard.tablist.shared.entry.TabElement;
import com.doctordark.util.scoreboard.tablist.shared.entry.TabElementHandler;

import lombok.Getter;
import lombok.SneakyThrows;

@Getter
public class TabHandler {

	private final TabAdapter adapter;
	private final TabElementHandler handler;

	private final long ticks;

	/**
	 * Constructor to make a new tab handler.
	 *
	 * @param adapter the adapter to send the tab with
	 * @param handler the handler to get the elements from
	 * @param plugin  the plugin to register the thread to
	 * @param ticks   the amount it should update
	 * @deprecated as of Tab API 1.1-SNAPSHOT, replaced by
	 *             {@link TabHandler#TabHandler(TabElementHandler, JavaPlugin, long)}
	 */
	@Deprecated
	public TabHandler(TabAdapter adapter, TabElementHandler handler, JavaPlugin plugin, long ticks) {
		this.adapter = adapter;
		this.handler = handler;
		this.ticks = ticks;

		new TabRunnable(this).runTaskTimer(plugin, 20L, ticks);
	}

	/**
	 * Constructor to make a new tab handler
	 *
	 * @param handler the handler to get the elements from
	 * @param plugin  the plugin to register the thread to
	 * @param ticks   the amount it should update
	 */
	@SneakyThrows
	public TabHandler(TabElementHandler handler, JavaPlugin plugin, long ticks) {
		this.adapter = this.createAdapter();
		this.handler = handler;
		this.ticks = ticks;

		new TabRunnable(this).runTaskTimer(plugin, 20L, ticks);
	}

	/**
	 * Create a new adapter for the disguise handling
	 *
	 * @return the newly created adapter
	 * @throws ClassNotFoundException thrown if the disguise class does not exist or
	 *                                could not be found
	 * @throws IllegalAccessException thrown if the instantiation was invoked from
	 *                                an illegal instance
	 * @throws InstantiationException thrown if something went wrong during class
	 *                                instantiation
	 */
	private TabAdapter createAdapter() throws ClassNotFoundException, IllegalAccessException, InstantiationException {

		return new com.doctordark.util.scoreboard.tablist.v1_8_r3.v1_8_R3TabAdapter();
	}

	/**
	 * Update the tablist for a player
	 *
	 * @param player the player to update it for
	 */
	public void sendUpdate(Player player) {
		final TabElement tabElement = this.handler.getElement(player);

		this.adapter.setupProfiles(player).showRealPlayers(player).addFakePlayers(player).hideRealPlayers(player)
				.handleElement(player, tabElement)
				.sendHeaderFooter(player, tabElement.getHeader(), tabElement.getFooter());
	}
}

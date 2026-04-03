package me.scifi.hcf.lunar.modules;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.common.location.ApolloBlockLocation;
import com.lunarclient.apollo.module.waypoint.Waypoint;
import com.lunarclient.apollo.module.waypoint.WaypointModule;
import com.lunarclient.apollo.player.ApolloPlayer;

/**
 * Manages the creation, display, and removal of Lunar Client waypoints for
 * players connected to the server using the Apollo API.
 * <p>
 * This class acts as a middle layer between Bukkit's {@link Player} and the
 * Lunar Client {@link WaypointModule}, allowing you to dynamically create
 * waypoints (e.g., KOTHs, spawns, or faction HQs) and send them directly to
 * players using the official Lunar API.
 * </p>
 *
 * <h2>Example usage:</h2>
 * 
 * <pre>{@code
 * WaypointsManager manager = new WaypointsManager();
 * 
 * // Create a waypoint at the player's current location
 * Waypoint spawn = manager.createWaypoint("Spawn", player.getLocation(), Color.GREEN);
 * 
 * // Display the waypoint to the player
 * manager.displayWaypoint(player, spawn);
 * }</pre>
 *
 * @author
 * @version 1.0
 * @see <a href="https://lunarclient.dev/apollo/developers/modules/waypoint"
 *      target="_blank"> Lunar Client Apollo Waypoint Module</a>
 */
public class WaypointsManager {

	/** The Lunar Client waypoint module instance. */
	private final WaypointModule waypointModule = Apollo.getModuleManager().getModule(WaypointModule.class);

	/** A local cache storing all waypoints by name. */
	private final Map<String, Waypoint> waypointMap = new HashMap<>();

	/**
	 * Sends a specific waypoint to a player's Lunar Client instance.
	 * <p>
	 * If the player does not have Apollo enabled or connected, this method will do
	 * nothing.
	 * </p>
	 *
	 * @param viewer   the Bukkit player who will see the waypoint
	 * @param waypoint the waypoint instance to display
	 */
	public void displayWaypoint(Player viewer, Waypoint waypoint) {
		Optional<ApolloPlayer> apolloPlayerOpt = Apollo.getPlayerManager().getPlayer(viewer.getUniqueId());

		apolloPlayerOpt.ifPresent(apolloPlayer -> {
			this.waypointModule.displayWaypoint(apolloPlayer, waypoint);
		});
	}

	/**
	 * Creates a new Lunar Client {@link Waypoint} with the provided data.
	 * <p>
	 * The waypoint will be stored locally in the {@code waypointMap} cache for
	 * later reference or removal.
	 * </p>
	 *
	 * @param name     the name of the waypoint (displayed in-game)
	 * @param location the Bukkit location representing the waypoint coordinates
	 * @param color    the waypoint color (e.g., {@link Color#RED})
	 * @return a new {@link Waypoint} instance
	 */
	public Waypoint createWaypoint(String name, Location location, Color color) {
		Waypoint waypoint = Waypoint
				.builder().location(ApolloBlockLocation.builder().world(location.getWorld().getName())
						.x(location.getBlockX()).y(location.getBlockY()).z(location.getBlockZ()).build())
				.name(name).color(color).build();

		waypointMap.put(name, waypoint);
		return waypoint;
	}

	/**
	 * Removes a specific waypoint from a player's view by name.
	 * <p>
	 * This only affects the given player; other players will still see the waypoint
	 * unless it is removed individually for them as well.
	 * </p>
	 *
	 * @param viewer   the player whose waypoint list will be modified
	 * @param waypoint the name of the waypoint to remove
	 */
	public void removeWaypoint(Player viewer, String waypoint) {
		Optional<ApolloPlayer> apolloPlayerOpt = Apollo.getPlayerManager().getPlayer(viewer.getUniqueId());
		apolloPlayerOpt.ifPresent(apolloPlayer -> this.waypointModule.removeWaypoint(apolloPlayer, waypoint));
	}

	/**
	 * Resets all waypoints currently displayed to the specified player.
	 * <p>
	 * Useful when cleaning up or refreshing waypoints, for example when a player
	 * changes world or respawns.
	 * </p>
	 *
	 * @param viewer the player whose waypoints will be cleared
	 */
	public void resetWaypoints(Player viewer) {
		Optional<ApolloPlayer> apolloPlayerOpt = Apollo.getPlayerManager().getPlayer(viewer.getUniqueId());
		apolloPlayerOpt.ifPresent(this.waypointModule::resetWaypoints);
	}
}
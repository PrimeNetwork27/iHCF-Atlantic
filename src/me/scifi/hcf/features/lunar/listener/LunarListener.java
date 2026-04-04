package me.scifi.hcf.features.lunar.listener;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.lunarclient.apollo.module.waypoint.Waypoint;

import me.scifi.hcf.HCF;
import me.scifi.hcf.eventgame.event.type.GameStartEvent;
import me.scifi.hcf.eventgame.event.type.GameStopEvent;
import me.scifi.hcf.eventgame.faction.EventFaction;
import me.scifi.hcf.faction.claim.Claim;
import me.scifi.hcf.faction.event.PlayerJoinedFactionEvent;
import me.scifi.hcf.faction.event.PlayerLeftFactionEvent;
import me.scifi.hcf.faction.type.PlayerFaction;
import me.scifi.hcf.features.lunar.LunarAPI;
import me.scifi.hcf.features.lunar.modules.TeamManager.Team;

// Created to manage Nametag, Waypoints and Teams.
public class LunarListener implements Listener { // TODO: Location Util, Handle Event Start and Stop to Waypoints.

	private final LunarAPI api = HCF.getPlugin().getLunarAPI();
	private final Map<String, Waypoint> waypoints = new HashMap<>();
	private final Location endPortal;
	private final Waypoint spawn;
	private final Waypoint end;

	public LunarListener() {
		this.endPortal = new Location(Bukkit.getServer().getWorld("world_the_end"),
				HCF.getPlugin().getConfig().getInt("ENDPORTAL.X"), HCF.getPlugin().getConfig().getInt("ENDPORTAL.Y"),
				HCF.getPlugin().getConfig().getInt("ENDPORTAL.Z"));
		this.end = createWaypoint("End Exit", endPortal, Color.blue);
		this.spawn = createWaypoint("Spawn", Bukkit.getServer().getWorlds().get(0).getSpawnLocation(), Color.BLUE);
		List<Waypoint> ways = Arrays.asList(end, spawn);

		ways.forEach(waypoint -> {
			waypoints.put(waypoint.getName(), waypoint);
		});
	}

	@EventHandler
	public void onGameStart(GameStartEvent event) {
		EventFaction faction = event.getFaction();

		if (faction.getClaims().isEmpty()) {
			return;
		}

		Claim claim = faction.getClaims().iterator().next();
		Waypoint waypoint = createWaypoint(faction.getName(), claim.getCenter(), Color.BLUE);

		for (Player player : Bukkit.getOnlinePlayers()) {
			displayWaypoint(player, waypoint);
		}
	}

	@EventHandler
	public void onGameStop(GameStopEvent event) {
		EventFaction faction = event.getEventFaction();
		for (Player player : Bukkit.getOnlinePlayers()) {
			api.getWaypointsManager().removeWaypoint(player, faction.getName());
		}

	}

	/**
	 * Called when a player joins a faction. Displays the faction's home waypoint,
	 * sets up the team on Lunar, and updates nametags for all online members.
	 */
	@EventHandler
	public void onPlayerJoined(PlayerJoinedFactionEvent event) {
		PlayerFaction faction = event.getFaction();
		com.google.common.base.Optional<Player> player = event.getPlayer();

		if (!player.isPresent()) {
			return;
		}

		// --- Waypoint setup ---
		if (faction.getHome() != null) {
			Waypoint waypoint = createWaypoint("Faction Home", faction.getHome(), Color.BLUE);
			displayWaypoint(player.get(), waypoint);
		}

		// --- Team setup ---
		Optional<Team> optionalTeam = api.getTeamManager().getByTeamId(faction.getLeader().getUniqueId());

		Team team;

		if (optionalTeam.isPresent()) {
			team = optionalTeam.get();
		} else {
			// No existing team: create one and add all online members
			team = api.getTeamManager().createTeam();
			for (Player member : faction.getOnlinePlayers()) {
				team.addMember(member);
			}
		}

		// Ensure the joining player is added to the team
		team.addMember(player.get());

	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		PlayerFaction faction = HCF.getPlugin().getManagerHandler().getFactionManager().getPlayerFaction(player);

		displayWaypoint(player, spawn);

		if (faction == null) { // Just in case.
			return;
		}

		// --- Waypoint setup ---
		if (faction.getHome() != null) {
			Waypoint waypoint = createWaypoint("Faction Home", faction.getHome(), Color.BLUE);
			displayWaypoint(player, waypoint);
		}

		Optional<Team> optionalTeam = api.getTeamManager().getByTeamId(faction.getLeader().getUniqueId());

		Team team;

		if (optionalTeam.isPresent()) {
			team = optionalTeam.get();
		} else {
			// No existing team: create one and add all online members
			team = api.getTeamManager().createTeam();
			for (Player member : faction.getOnlinePlayers()) {
				team.addMember(member);
			}
		}

		// Ensure the joining player is added to the team
		team.addMember(event.getPlayer());

	}

	/**
	 * Called when a player leaves a faction. Removes their faction waypoint and
	 * removes them from the team.
	 */
	@EventHandler
	public void onPlayerLeaved(PlayerLeftFactionEvent event) {
		PlayerFaction faction = event.getFaction();

		com.google.common.base.Optional<Player> optional = event.getPlayer();
		if (!optional.isPresent()) {
			return;
		}

		Player player = optional.get();

		if (faction.getHome() != null) {
			api.getWaypointsManager().removeWaypoint(player, "Faction Home");
		}

		if (faction.getLeader() == null) {
			return;
		}

		Optional<Team> optionalTeam = api.getTeamManager().getByTeamId(faction.getLeader().getUniqueId());
		optionalTeam.ifPresent(team -> team.removeMember(player));
	}

	private Waypoint createWaypoint(String string, Location location, Color color) {
		waypoints.put(string, api.getWaypointsManager().createWaypoint(string, location, color));
		return api.getWaypointsManager().createWaypoint(string, location, color);
	}

	private void displayWaypoint(Player player, Waypoint waypoint) {
		api.getWaypointsManager().displayWaypoint(player, waypoint);
	}
}
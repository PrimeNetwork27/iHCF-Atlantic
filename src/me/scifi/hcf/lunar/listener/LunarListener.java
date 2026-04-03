package me.scifi.hcf.lunar.listener;

import java.awt.Color;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.lunarclient.apollo.module.waypoint.Waypoint;

import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.event.PlayerJoinedFactionEvent;
import me.scifi.hcf.faction.event.PlayerLeftFactionEvent;
import me.scifi.hcf.faction.type.PlayerFaction;
import me.scifi.hcf.lunar.LunarAPI;
import me.scifi.hcf.lunar.modules.TeamManager.Team;

// Created to manage Nametag, Waypoints and Teams.
public class LunarListener implements Listener { // TODO: Location Util, Handle Event Start and Stop to Waypoints.

	private final LunarAPI api = HCF.getPlugin().getLunarAPI();

	private final Location endPortal = new Location(Bukkit.getServer().getWorld("world_the_end"),
			HCF.getPlugin().getConfig().getInt("ENDPORTAL.X"), HCF.getPlugin().getConfig().getInt("ENDPORTAL.Y"),
			HCF.getPlugin().getConfig().getInt("ENDPORTAL.Z"));
	private final Waypoint spawn = api.getWaypointsManager().createWaypoint("Spawn",
			Bukkit.getServer().getWorlds().get(0).getSpawnLocation(), Color.BLUE);
	private final Waypoint end = api.getWaypointsManager().createWaypoint("End Exit", endPortal, Color.blue);

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
			Waypoint waypoint = api.getWaypointsManager().createWaypoint("Faction Home", faction.getHome(), Color.BLUE);
			api.getWaypointsManager().displayWaypoint(player.get(), waypoint);
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

		api.getWaypointsManager().displayWaypoint(player, spawn);
		api.getWaypointsManager().displayWaypoint(event.getPlayer(), end);
		if (faction == null) { // Just in case.
			return;
		}

		// --- Waypoint setup ---
		if (faction.getHome() != null) {
			Waypoint waypoint = api.getWaypointsManager().createWaypoint("Faction Home", faction.getHome(), Color.BLUE);
			api.getWaypointsManager().displayWaypoint(player, waypoint);
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
		Player player = event.getPlayer().get();

		if (player == null) {
			return;
		}
		if (faction.getHome() != null) {
			api.getWaypointsManager().removeWaypoint(player, "Faction Home");
		}

		// Remove player from their team if applicable
		Optional<Team> optionalTeam = api.getTeamManager().getByTeamId(faction.getLeader().getUniqueId());

		optionalTeam.ifPresent(team -> team.removeMember(player));

	}
}
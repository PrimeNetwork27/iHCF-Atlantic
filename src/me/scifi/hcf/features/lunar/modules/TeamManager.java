package me.scifi.hcf.features.lunar.modules;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.common.location.ApolloLocation;
import com.lunarclient.apollo.module.team.TeamMember;
import com.lunarclient.apollo.module.team.TeamModule;

import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.type.PlayerFaction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Handles synchronization between HCF Factions and Lunar Client TeamModule.
 * Players in the same faction are shown as teammates on Lunar Client.
 */
public class TeamManager implements Listener {

	private final Map<UUID, Team> teamsByTeamId = Maps.newHashMap();
	private final Map<UUID, Team> teamsByPlayerUuid = Maps.newHashMap();
	private final TeamModule teamModule = Apollo.getModuleManager().getModule(TeamModule.class);

	public TeamManager() {
		this.runBukkitTeamUpdateTask();
		Bukkit.getPluginManager().registerEvents(this, HCF.getPlugin());
	}

	/**
	 * When a player joins, synchronize their faction with the Lunar team system.
	 */
	@EventHandler
	private void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		syncPlayerFactionTeam(player);
	}

	/**
	 * When a player quits, remove their team if they were the last member.
	 */
	@EventHandler
	private void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		this.getByPlayerUuid(player.getUniqueId()).ifPresent(team -> {
			if (team.getMembers().size() == 1) {
				this.deleteTeam(team.getTeamId());
			} else {
				team.removeMember(player);
			}
		});
	}

	/**
	 * Synchronizes a player’s faction with their corresponding Lunar team. Creates
	 * or joins a team automatically depending on faction members.
	 */
	public void syncPlayerFactionTeam(Player player) {
		if (HCF.getPlugin().getManagerHandler().getFactionManager().getPlayerFaction(player) == null) {
			return;
		}

		PlayerFaction faction = HCF.getPlugin().getManagerHandler().getFactionManager().getPlayerFaction(player);

		if (faction == null) {
			Bukkit.getLogger().warning("[TeamManager] Faction " + (faction != null ? faction.getName() : "null")
					+ " has no name (Player: " + player.getName() + ")");
			return;
		}

		UUID leaderId = faction.getLeader().getUniqueId();
		Optional<Team> optionalTeam = getByPlayerUuid(leaderId);

		getByPlayerUuid(player.getUniqueId()).ifPresent(oldTeam -> oldTeam.removeMember(player));

		if (optionalTeam.isPresent()) {
			optionalTeam.get().addMember(player);
		} else if (faction.getOnlinePlayers().size() > 1) {
			Team team = createTeam();
			for (Player member : faction.getOnlinePlayers()) {
				team.addMember(member);
			}
		}
	}

	/**
	 * Retrieve a team by player UUID.
	 */
	public Optional<Team> getByPlayerUuid(UUID playerUuid) {
		return Optional.ofNullable(this.teamsByPlayerUuid.get(playerUuid));
	}

	/**
	 * Retrieve a team by team UUID.
	 */
	public Optional<Team> getByTeamId(UUID teamId) {
		return Optional.ofNullable(this.teamsByTeamId.get(teamId));
	}

	/**
	 * Creates a new team instance and registers it.
	 */
	public Team createTeam() {
		Team team = new Team();
		this.teamsByTeamId.put(team.getTeamId(), team);
		return team;
	}

	/**
	 * Deletes a team and removes all its members.
	 */
	public void deleteTeam(UUID teamId) {
		Team team = this.teamsByTeamId.remove(teamId);
		if (team != null) {
			// Reset Lunar data for all players in the team
			team.getMembers().forEach(team::removeMember);
		}
	}

	/**
	 * Task that periodically updates all team members’ locations for Lunar.
	 */
	private void runBukkitTeamUpdateTask() {
		Bukkit.getScheduler().runTaskTimerAsynchronously(HCF.getPlugin(), () -> {
			this.teamsByTeamId.values().forEach(Team::refresh);
		}, 20L, 20L); // Updates every 20 ticks (1 second)
	}

	/**
	 * Represents a single Lunar team synchronized with an HCF faction.
	 */
	public class Team {

		private final UUID teamId;
		private final Set<Player> members;

		public Team() {
			this.teamId = UUID.randomUUID();
			this.members = Sets.newHashSet();
		}

		/**
		 * Adds a player to the team and updates the internal mapping.
		 */
		public void addMember(Player player) {
			if (this.members.add(player)) {
				TeamManager.this.teamsByPlayerUuid.put(player.getUniqueId(), this);
			}
		}

		/**
		 * Removes a player from the team and resets their team data on Lunar.
		 */
		public void removeMember(Player player) {
			if (this.members.remove(player)) {
				TeamManager.this.teamsByPlayerUuid.remove(player.getUniqueId());
				Apollo.getPlayerManager().getPlayer(player.getUniqueId()).ifPresent(teamModule::resetTeamMembers);
			}
		}

		/**
		 * Create a TeamMember representation for Lunar with live coordinates.
		 */
		private TeamMember createTeamMember(Player member) {
			Location location = member.getLocation();

			return TeamMember.builder().playerUuid(member.getUniqueId())
					.displayName(Component.text(member.getName(), NamedTextColor.WHITE)).markerColor(Color.WHITE)
					.location(ApolloLocation.builder().world(location.getWorld().getName()).x(location.getX())
							.y(location.getY()).z(location.getZ()).build())
					.build();
		}

		/**
		 * Refreshes all team members’ locations on Lunar Client.
		 */
		public void refresh() {
			if (this.members.isEmpty()) {
				return;
			}

			List<TeamMember> teammates = this.members.stream().filter(Player::isOnline).map(this::createTeamMember)
					.collect(Collectors.toList());

			this.members.forEach(member -> Apollo.getPlayerManager().getPlayer(member.getUniqueId())
					.ifPresent(apolloPlayer -> teamModule.updateTeamMembers(apolloPlayer, teammates)));
		}

		public UUID getTeamId() {
			return this.teamId;
		}

		public Set<Player> getMembers() {
			return this.members;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (other == null || other.getClass() != this.getClass()) {
				return false;
			}
			Team team = (Team) other;
			return this.teamId.equals(team.getTeamId());
		}

		@Override
		public int hashCode() {
			return this.teamId.hashCode();
		}
	}
}
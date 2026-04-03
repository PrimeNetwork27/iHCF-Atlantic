package me.scifi.hcf.listener;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import com.doctordark.util.CC;
import com.google.common.base.Optional;

import me.scifi.hcf.ConfigurationService;
import me.scifi.hcf.HCF;
import me.scifi.hcf.eventgame.faction.KothFaction;
import me.scifi.hcf.faction.event.CaptureZoneEnterEvent;
import me.scifi.hcf.faction.event.CaptureZoneLeaveEvent;
import me.scifi.hcf.faction.event.FactionCreateEvent;
import me.scifi.hcf.faction.event.FactionRemoveEvent;
import me.scifi.hcf.faction.event.FactionRenameEvent;
import me.scifi.hcf.faction.event.PlayerClaimEnterEvent;
import me.scifi.hcf.faction.event.PlayerJoinFactionEvent;
import me.scifi.hcf.faction.event.PlayerLeaveFactionEvent;
import me.scifi.hcf.faction.event.PlayerLeftFactionEvent;
import me.scifi.hcf.faction.struct.RegenStatus;
import me.scifi.hcf.faction.struct.Relation;
import me.scifi.hcf.faction.type.Faction;
import me.scifi.hcf.faction.type.PlayerFaction;

public class FactionListener implements Listener {

	private static final long FACTION_JOIN_WAIT_MILLIS = TimeUnit.SECONDS.toMillis(30L);
	private static final String FACTION_JOIN_WAIT_WORDS = DurationFormatUtils
			.formatDurationWords(FACTION_JOIN_WAIT_MILLIS, true, true);

	private static final String LAND_CHANGED_META_KEY = "landChangedMessage";
	private static final long LAND_CHANGE_MSG_THRESHOLD = 225L;

	private final HCF plugin;

	public FactionListener(HCF plugin) {
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onFactionRenameMonitor(FactionRenameEvent event) {
		Faction faction = event.getFaction();
		if (faction instanceof KothFaction) {
			((KothFaction) faction).getCaptureZone().setName(event.getNewName());
		}
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		plugin.getManagerHandler().getUserManager().getUserAsync(e.getEntity().getUniqueId()).setDeaths(
				plugin.getManagerHandler().getUserManager().getUserAsync(e.getEntity().getUniqueId()).getDeaths() + 1);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onFactionCreate(FactionCreateEvent event) {
		Faction faction = event.getFaction();
		if (faction instanceof PlayerFaction) {
			CommandSender sender = event.getSender();
			for (Player player : Bukkit.getServer().getOnlinePlayers()) {
				String msg = plugin.getMessagesYML().getString("FACTION-CREATE")
						.replace("{factionName}", (player == null ? faction.getName() : faction.getDisplayName(player)))
						.replace("{player}",
								(sender instanceof Player ? ((Player) sender).getDisplayName() : sender.getName()));
				player.sendMessage(CC.translate(msg));
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onFactionRemove(FactionRemoveEvent event) {
		Faction faction = event.getFaction();
		if (faction instanceof PlayerFaction) {
			CommandSender sender = event.getSender();
			for (Player player : Bukkit.getServer().getOnlinePlayers()) {
				String msg = plugin.getMessagesYML().getString("FACTION-DISBAND")
						.replace("{factionName}", (player == null ? faction.getName() : faction.getDisplayName(player)))
						.replace("{player}",
								(sender instanceof Player ? ((Player) sender).getDisplayName() : sender.getName()));
				player.sendMessage(CC.translate(msg));
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onFactionRename(FactionRenameEvent event) {
		Faction faction = event.getFaction();
		if (faction instanceof PlayerFaction) {
			for (Player player : Bukkit.getServer().getOnlinePlayers()) {
				Relation relation = faction.getRelation(player);
				String msg = plugin.getMessagesYML().getString("FACTION-RENAME")
						.replace("{oldFactionName}", relation.toChatColour() + event.getOriginalName())
						.replace("{factionName}", relation.toChatColour() + event.getNewName());
				player.sendMessage(msg);
			}
		}
	}

	private long getLastLandChangedMeta(Player player) {
		List<MetadataValue> value = player.getMetadata(LAND_CHANGED_META_KEY);
		long millis = System.currentTimeMillis();
		long remaining = value == null || value.isEmpty() ? 0L : value.get(0).asLong() - millis;
		if (remaining <= 0L) { // update the metadata.
			player.setMetadata(LAND_CHANGED_META_KEY,
					new FixedMetadataValue(plugin, millis + LAND_CHANGE_MSG_THRESHOLD));
		}

		return remaining;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCaptureZoneEnter(CaptureZoneEnterEvent event) {
		Player player = event.getPlayer();
		if (getLastLandChangedMeta(player) > 0L) {
			return; // delay before re-messaging.
		}
		if (plugin.getManagerHandler().getUserManager().getUser(player.getUniqueId()).isCapzoneEntryAlerts()) {
			String msg = plugin.getMessagesYML().getString("FACTION-ENTERING-CAPTUREZONE")
					.replace("{captureName}", event.getCaptureZone().getDisplayName())
					.replace("{factionName}", event.getFaction().getName());
			player.sendMessage(CC.translate(msg));
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCaptureZoneLeave(CaptureZoneLeaveEvent event) {
		Player player = event.getPlayer();
		if (getLastLandChangedMeta(player) > 0L) {
			return; // delay before re-messaging.
		}

		if (plugin.getManagerHandler().getUserManager().getUser(player.getUniqueId()).isCapzoneEntryAlerts()) {
			String msg = plugin.getMessagesYML().getString("FACTION-LEAVING-CAPTUREZONE")
					.replace("{captureName}", event.getCaptureZone().getDisplayName())
					.replace("{factionName}", event.getFaction().getName());
			player.sendMessage(CC.translate(msg));
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	private void onPlayerClaimEnter(PlayerClaimEnterEvent event) {
		Faction toFaction = event.getToFaction();
		if (toFaction.isSafezone()) {
			Player player = event.getPlayer();
			player.setHealth(player.getMaxHealth());
			player.setFoodLevel(20);
			player.setFireTicks(0);
			player.setSaturation(4.0F);
		}

		Player player = event.getPlayer();
		if (getLastLandChangedMeta(player) > 0L) {
			return; // delay before re-messaging.
		}
		String leavingstatus = event.getFromFaction().isDeathban()
				? ChatColor.YELLOW + "(" + ChatColor.RED + "Deathban" + ChatColor.YELLOW + ")"
				: ChatColor.YELLOW + "(" + ChatColor.GREEN + "Non-Deathban" + ChatColor.YELLOW + ")";
		String enteringstatus = event.getToFaction().isDeathban()
				? ChatColor.YELLOW + "(" + ChatColor.RED + "Deathban" + ChatColor.YELLOW + ")"
				: ChatColor.YELLOW + "(" + ChatColor.GREEN + "Non-Deathban" + ChatColor.YELLOW + ")";
		String entering = plugin.getMessagesYML().getString("FACTION-ENTERING-CLAIM")
				.replace("{factionName}", event.getToFaction().getDisplayName(player))
				.replace("{deathban}", enteringstatus).replace("{deathbanLeft}", leavingstatus).replace("{factionLeft}",
						(event.getFromFaction() == null ? "" : event.getFromFaction().getDisplayName(player)));
		player.sendMessage(CC.translate(entering));
		String leaving = plugin.getMessagesYML().getString("FACTION-LEAVING-CLAIM")
				.replace("{factionName}", event.getFromFaction().getDisplayName(player))
				.replace("{deathban}", leavingstatus);
		if (!leaving.isEmpty()) {
			player.sendMessage(CC.translate(leaving));
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerLeftFaction(PlayerLeftFactionEvent event) {
		Optional<Player> optionalPlayer = event.getPlayer();
		if (optionalPlayer.isPresent()) {
			plugin.getManagerHandler().getUserManager().getUser(optionalPlayer.get().getUniqueId())
					.setLastFactionLeaveMillis(System.currentTimeMillis());
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onPlayerPreFactionJoin(PlayerJoinFactionEvent event) {
		Faction faction = event.getFaction();
		Optional<Player> optionalPlayer = event.getPlayer();
		if (faction instanceof PlayerFaction && optionalPlayer.isPresent()) {
			Player player = optionalPlayer.get();
			PlayerFaction playerFaction = (PlayerFaction) faction;

			if (!ConfigurationService.KIT_MAP && !plugin.getManagerHandler().getEotwHandler().isEndOfTheWorld()
					&& playerFaction.getRegenStatus() == RegenStatus.PAUSED) {
				event.setCancelled(true);
				player.sendMessage(CC.translate(plugin.getMessagesYML().getString("FACTION-JOIN-REGENERATING")));
				return;
			}

			long difference = (plugin.getManagerHandler().getUserManager().getUser(player.getUniqueId())
					.getLastFactionLeaveMillis() - System.currentTimeMillis()) + FACTION_JOIN_WAIT_MILLIS;
			if (difference > 0L && !player.hasPermission("hcf.faction.argument.staff.forcejoin")) {
				event.setCancelled(true);
				String msg = plugin.getMessagesYML().getString("FACTION-JOIN-COOLDOWN")
						.replace("{timeLeft}", FACTION_JOIN_WAIT_WORDS)
						.replace("{difference}", DurationFormatUtils.formatDurationWords(difference, true, true));
				player.sendMessage(CC.translate(msg));
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onFactionLeave(PlayerLeaveFactionEvent event) {
		if (event.isForce() || event.isKick()) {
			return;
		}

		Faction faction = event.getFaction();
		if (faction instanceof PlayerFaction) {
			Optional<Player> optional = event.getPlayer();
			if (optional.isPresent()) {
				Player player = optional.get();
				if (plugin.getManagerHandler().getFactionManager().getFactionAt(player.getLocation()) == faction) {
					event.setCancelled(true);
					player.sendMessage(
							CC.translate(plugin.getMessagesYML().getString("FACTION-LEAVING-REMAIN-TERRITORY")));
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		PlayerFaction playerFaction = plugin.getManagerHandler().getFactionManager().getPlayerFaction(player);
		if (playerFaction != null) {
			playerFaction.printDetails(player);
			String msg = plugin.getMessagesYML().getString("FACTION-MEMBER-JOIN").replace("{memberName}",
					playerFaction.getMember(player).getRole().getAstrix() + player.getName());
			playerFaction.broadcast(CC.translate(msg), player.getUniqueId());
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		PlayerFaction playerFaction = plugin.getManagerHandler().getFactionManager().getPlayerFaction(player);
		if (playerFaction != null) {
			String msg = plugin.getMessagesYML().getString("FACTION-MEMBER-LEFT").replace("{memberName}",
					playerFaction.getMember(player).getRole().getAstrix() + player.getName());
			playerFaction.broadcast(CC.translate(msg));
		}
	}
}

package me.scifi.hcf.scoreboard;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import com.doctordark.util.Config;
import com.doctordark.util.JavaUtils;
import com.doctordark.util.scoreboard.sidebar.AssembleAdapter;
import com.google.common.collect.Ordering;

import me.scifi.hcf.DateTimeFormats;
import me.scifi.hcf.DurationFormatter;
import me.scifi.hcf.HCF;
import me.scifi.hcf.Utils;
import me.scifi.hcf.economy.EconomyManager;
import me.scifi.hcf.eventgame.EventTimer;
import me.scifi.hcf.eventgame.eotw.EotwHandler;
import me.scifi.hcf.eventgame.faction.ConquestFaction;
import me.scifi.hcf.eventgame.faction.EventFaction;
import me.scifi.hcf.eventgame.faction.KothFaction;
import me.scifi.hcf.eventgame.tracker.ConquestTracker;
import me.scifi.hcf.faction.type.PlayerFaction;
import me.scifi.hcf.features.customtimers.CustomTimer;
import me.scifi.hcf.features.staffmode.Vanish;
import me.scifi.hcf.pvpclass.PvpClass;
import me.scifi.hcf.pvpclass.archer.ArcherClass;
import me.scifi.hcf.pvpclass.archer.ArcherMark;
import me.scifi.hcf.pvpclass.bard.BardClass;
import me.scifi.hcf.sotw.SotwCommand;
import me.scifi.hcf.sotw.SotwTimer;
import me.scifi.hcf.timer.PlayerTimer;
import me.scifi.hcf.timer.Timer;
import me.scifi.hcf.user.UserManager;
import net.minecraft.server.v1_8_R3.MinecraftServer;

public class ScoreboardProvider implements AssembleAdapter { // Conquest change to List<String> :P

	public static final ThreadLocal<DecimalFormat> CONQUEST_FORMATTER = new ThreadLocal<DecimalFormat>() {
		@Override
		protected DecimalFormat initialValue() {
			return new DecimalFormat("00.0");
		}
	};
	private final HCF plugin = HCF.getPlugin();
	private static final Comparator<Map.Entry<UUID, ArcherMark>> ARCHER_MARK_COMPARATOR = (o1, o2) -> o1.getValue()
			.compareTo(o2.getValue());

	private static String handleBardFormat(long millis, boolean trailingZero) {
		return (trailingZero ? DateTimeFormats.REMAINING_SECONDS_TRAILING : DateTimeFormats.REMAINING_SECONDS).get()
				.format(millis * 0.001);
	}

	@Override
	public String getTitle(Player player) {
		return ChatColor.translateAlternateColorCodes('&',
				HCF.getPlugin().getMessagesYML().getString("SCOREBOARD.TITLE"));
	}

	@Override
	public List<String> getLines(Player player) {
		List<String> lines = new ArrayList<>();

		UserManager um = HCF.getPlugin().getManagerHandler().getUserManager();
		Config config = HCF.getPlugin().getConfig();
		Config messages = HCF.getPlugin().getMessagesYML();
		if (config.getBoolean("kit-map")) {
			List<String> kitmap = new ArrayList<>();
			for (String kits : messages.getStringList("SCOREBOARD.KITMAP")) {
				kits = kits.replace("{kills}", Integer.toString(player.getStatistic(Statistic.PLAYER_KILLS)))
						.replace("{deaths}", Integer.toString(player.getStatistic(Statistic.DEATHS)))
						.replace("{balance}", EconomyManager.ECONOMY_SYMBOL + Integer.toString(HCF.getPlugin()
								.getManagerHandler().getEconomyManager().getBalance(player.getUniqueId())));
				kitmap.add(kits);
			}
			if (config.getBoolean("kit-map-stats-in-spawn-only")) {
				if (plugin.getManagerHandler().getFactionManager().getClaimAt(player.getLocation()).getFaction()
						.isSafezone()) {
					lines.addAll(kitmap);
				}
			} else {
				lines.addAll(kitmap);
			}
		}
		if (plugin.getManagerHandler().getFactionManager().getPlayerFaction(player) != null) {
			PlayerFaction team = plugin.getManagerHandler().getFactionManager().getPlayerFaction(player);
			if (team.getTarget() != null) {
				PlayerFaction target = team.getTarget();
				List<String> focus = new ArrayList<>();
				Location home = target.getHome();
				for (String focused : messages.getStringList("SCOREBOARD.FOCUS")) {
					focused = focused.replace("{factionName}", target.getName())
							.replace("{members}", target.getOnlineMembers().size() + "/" + target.getMembers().size())
							.replace("{home}",
									(home == null ? "None" : "(" + home.getBlockX() + " | " + home.getBlockZ()))
							.replace("{dtr}", target.getRegenStatus().getSymbol() + target.getDtrColour()
									+ JavaUtils.format(target.getDeathsUntilRaidable(false)) + ChatColor.YELLOW);
					focus.add(focused);
				}
				lines.addAll(focus);
			}
		}
		if (plugin.getStaffModeManager().getStaffMode().contains(player.getUniqueId())) {
			for (String line : messages.getStringList("SCOREBOARD.STAFF-MODE")) {
				line = line.replace("{status}", Vanish.isPlayerVanished(player) ? "&fEnabled" : "&fDisabled")
						.replace("{online}", String.valueOf(HCF.getOnlinePlayers().size()))
						.replace("{tps}", getTPSColored());
				lines.add(line);
			}

		}
		EotwHandler.EotwRunnable eotwRunnable = plugin.getManagerHandler().getEotwHandler().getRunnable();
		if (eotwRunnable != null) {
			long remaining = eotwRunnable.getMillisUntilStarting();
			if (remaining > 0L) {
				lines.add(ChatColor.RED.toString() + ChatColor.BOLD + "EOTW" + ChatColor.RED + " starts" + " in "
						+ ChatColor.BOLD + DurationFormatter.getRemaining(remaining, true));
			} else if ((remaining = eotwRunnable.getMillisUntilCappable()) > 0L) {
				lines.add(ChatColor.RED.toString() + ChatColor.BOLD + "EOTW" + ChatColor.RED + " cappable" + " in "
						+ ChatColor.BOLD + DurationFormatter.getRemaining(remaining, true));
			}
		}

		SotwTimer.SotwRunnable sotwRunnable = plugin.getSotwTimer().getSotwRunnable();
		if (sotwRunnable != null) {
			if (SotwCommand.enabled.contains(player.getUniqueId())) {
				lines.add(Utils.chat(messages.getString("SCOREBOARD.SOTW.ENABLED").replace("%remain%",
						DurationFormatter.getRemaining(sotwRunnable.getRemaining(), true))));
			} else {
				lines.add(Utils.chat(messages.getString("SCOREBOARD.SOTW.REGULAR").replace("%remaining%",
						DurationFormatter.getRemaining(sotwRunnable.getRemaining(), true))));
			}
		}

		if (plugin.getManagerHandler().getKingManager().isEventActive()) {
			int x = plugin.getManagerHandler().getKingManager().getKingPlayer().getLocation().getBlockX();
			int z = plugin.getManagerHandler().getKingManager().getKingPlayer().getLocation().getBlockZ();
			lines.add("&3&lKing Event&7:");
			lines.add(" &7\u00BB &3King Name&7: &f"
					+ plugin.getManagerHandler().getKingManager().getKingPlayer().getName());
			lines.add(" &7\u00BB &3Coords&7: &f" + x + " , " + z);
		}

		EventTimer eventTimer = plugin.getManagerHandler().getTimerManager().getEventTimer();
		List<String> conquestLines = null;

		EventFaction eventFaction = eventTimer.getEventFaction();
		if (eventFaction instanceof KothFaction) {
			lines.add(eventFaction.getScoreboardName().replace("%remaining%",
					DurationFormatter.getRemaining(eventTimer.getRemaining(), true)));
		} else if (eventFaction instanceof ConquestFaction) {
			ConquestFaction conquestFaction = (ConquestFaction) eventFaction;

			conquestLines = new ArrayList<>();
			conquestLines.add(eventFaction.getScoreboardName());
			conquestLines.add("&c&lRed&7: &f" + conquestFaction.getRed().getScoreboardRemaining());
			conquestLines.add("&e&lYellow&7: &f" + conquestFaction.getYellow().getScoreboardRemaining());
			conquestLines.add("&9&lBlue&7: &f" + conquestFaction.getBlue().getScoreboardRemaining());
			conquestLines.add("&2&lGreen&7: &f" + conquestFaction.getGreen().getScoreboardRemaining());

			// Show the top 3 factions next.
			ConquestTracker conquestTracker = (ConquestTracker) conquestFaction.getEventType().getEventTracker();
			int count = 0;
			for (Map.Entry<PlayerFaction, Integer> entry : conquestTracker.getFactionPointsMap().entrySet()) {
				String factionName = entry.getKey().getName();
				if (factionName.length() > 14) {
					factionName = factionName.substring(0, 14);
				}
				conquestLines.add(ChatColor.GOLD.toString() + "\u00bb " + ChatColor.LIGHT_PURPLE + factionName
						+ ChatColor.GRAY + ": " + ChatColor.WHITE + entry.getValue());
				if (++count == 3) {
					break;
				}
			}
		}

		// Show the current PVP Class statistics of the player.
		PvpClass pvpClass = plugin.getManagerHandler().getPvpClassManager().getEquippedClass(player);
		if (pvpClass != null) {
			lines.add(ChatColor.AQUA.toString() + ChatColor.BOLD + "Active Class" + ChatColor.GRAY.toString() + ": "
					+ ChatColor.WHITE + pvpClass.getName());
			if (pvpClass instanceof BardClass) {
				BardClass bardClass = (BardClass) pvpClass;
				lines.add(ChatColor.GOLD + " \u00bb " + ChatColor.LIGHT_PURPLE + "Energy" + ChatColor.GRAY + ": "
						+ ChatColor.WHITE + handleBardFormat(bardClass.getEnergyMillis(player), true));

				long remaining = bardClass.getRemainingBuffDelay(player);
				if (remaining > 0) {
					lines.add(ChatColor.GOLD.toString() + " \u00bb " + ChatColor.LIGHT_PURPLE + "Buff Delay"
							+ ChatColor.GRAY + ": " + ChatColor.WHITE
							+ DurationFormatter.getRemaining(remaining, true));
				}
			} else if (pvpClass instanceof ArcherClass) {
				ArcherClass archerClass = (ArcherClass) pvpClass;

				List<Map.Entry<UUID, ArcherMark>> entryList = Ordering.from(ARCHER_MARK_COMPARATOR)
						.sortedCopy(archerClass.getSentMarks(player).entrySet());
				entryList = entryList.subList(0, Math.min(entryList.size(), 3));
				for (Map.Entry<UUID, ArcherMark> entry : entryList) {
					ArcherMark archerMark = entry.getValue();
					Player target = Bukkit.getPlayer(entry.getKey());
					if (target != null) {
						ChatColor levelColour;
						switch (archerMark.currentLevel) {
						case 1:
							levelColour = ChatColor.GREEN;
							break;
						case 2:
							levelColour = ChatColor.RED;
							break;
						case 3:
							levelColour = ChatColor.DARK_RED;
							break;

						default:
							levelColour = ChatColor.YELLOW;
							break;
						}

						// Add the current mark level to scoreboard.
						// lines.add(new SidebarEntry(ChatColor.GOLD + "" + ChatColor.BOLD, "Archer
						// Mark" + ChatColor.GRAY + ": ", ""));
						String targetName = target.getName();
						targetName = targetName.substring(0, Math.min(targetName.length(), 15));
						lines.add(ChatColor.GOLD + " \u00bb" + ChatColor.RED + ' ' + targetName
								+ ChatColor.YELLOW.toString() + levelColour + " [Mark " + archerMark.currentLevel
								+ ']');

					}
				}
			}
		}

		Collection<Timer> timers = plugin.getManagerHandler().getTimerManager().getTimers();
		for (Timer timer : timers) {
			if (timer instanceof PlayerTimer) {
				PlayerTimer playerTimer = (PlayerTimer) timer;
				long remaining = playerTimer.getRemaining(player);
				if (remaining <= 0) {
					continue;
				}

				String timerName = playerTimer.getName();
				if (timerName.length() > 14) {
					timerName = timerName.substring(0, timerName.length());
				}
				lines.add(playerTimer.getScoreboardPrefix() + timerName + ChatColor.GRAY + ": " + ChatColor.WHITE
						+ DurationFormatter.getRemaining(remaining, true));
			}
		}

		Collection<CustomTimer> customTimers = plugin.getManagerHandler().getCustomTimerManager().getCustomTimers();
		for (CustomTimer timer : customTimers) {

			lines.add(timer.getScoreboard() + "&7: &f" + DurationFormatter.getRemaining(timer.getRemaining(), true));
		}

		if (conquestLines != null && !conquestLines.isEmpty()) {
			if (!lines.isEmpty()) {
				conquestLines.add("" + "" + "");
			}

			conquestLines.addAll(lines);
			lines = conquestLines;
		}

		if (!lines.isEmpty()) {
			lines.add(0, messages.getString("SCOREBOARD.SEPARATOR"));
			if (config.getBoolean("SCOREBOARD-FOOTER")) {
				lines.add(" ");
				lines.add(messages.getString("SCOREBOARD.FOOTER"));
			}
			lines.add(lines.size(), messages.getString("SCOREBOARD.SEPARATOR"));
		}

		lines.forEach(string -> ChatColor.translateAlternateColorCodes('&', string));
		return lines;
	}

	public String getTPSColored() {
		double tps = MinecraftServer.getServer().recentTps[0];
		String color = tps > 18.0 ? "§a" : (tps > 16.0 ? "§e" : "§c");
		String max = tps > 20.0 ? "*" : "";
		return color + max + Math.min(Math.round(tps * 100.0) / 100.0, 20.0);
	}

}

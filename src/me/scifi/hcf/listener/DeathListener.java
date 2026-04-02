package me.scifi.hcf.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.doctordark.util.JavaUtils;

import me.scifi.hcf.ConfigurationService;
import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.struct.Role;
import me.scifi.hcf.faction.type.Faction;
import me.scifi.hcf.faction.type.PlayerFaction;
import me.scifi.hcf.user.FactionUser;
import net.minecraft.server.v1_8_R3.EntityLightning;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityWeather;
import net.minecraft.server.v1_8_R3.WorldServer;

public class DeathListener implements Listener {

	private final HCF plugin;

	public static Map<UUID, ItemStack[]> armor = new HashMap<>();
	public static Map<UUID, ItemStack[]> contents = new HashMap<>();

	public DeathListener(HCF plugin) {
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onPlayerDeathKillIncrement(PlayerDeathEvent event) {
		Player killer = event.getEntity().getKiller();
		if (killer != null) {
			FactionUser user = plugin.getManagerHandler().getUserManager().getUser(killer.getUniqueId());
			user.setKills(user.getKills() + 1);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		PlayerFaction playerFaction = plugin.getManagerHandler().getFactionManager().getPlayerFaction(player);

		if (event.getEntity().getKiller() != null) {
			if (plugin.getManagerHandler().getFactionManager()
					.getPlayerFaction(event.getEntity().getKiller()) != null) {
				plugin.getManagerHandler().getFactionManager().getPlayerFaction(event.getEntity().getKiller())
						.addPoints(1);
			}
		}

		if (!armor.containsKey(player.getUniqueId()) && !contents.containsKey(player.getUniqueId())) {
			armor.put(player.getUniqueId(), player.getInventory().getArmorContents());
			contents.put(player.getUniqueId(), player.getInventory().getContents());
		}
		armor.remove(player.getUniqueId());
		contents.remove(player.getUniqueId());
		armor.put(player.getUniqueId(), player.getInventory().getArmorContents());
		contents.put(player.getUniqueId(), player.getInventory().getContents());

		if (playerFaction != null) {
			playerFaction.setPoints(playerFaction.getPoints() - 1);

			Faction factionAt = plugin.getManagerHandler().getFactionManager().getFactionAt(player.getLocation());
			double dtrLoss = (1.0D * factionAt.getDtrLossMultiplier());
			double newDtr = playerFaction.setDeathsUntilRaidable(playerFaction.getDeathsUntilRaidable() - dtrLoss);

			Role role = playerFaction.getMember(player.getUniqueId()).getRole();
			playerFaction.setRemainingRegenerationTime(
					TimeUnit.SECONDS.toMillis(plugin.getConfig().getLong("HCF-DTR-REGEN-TIME")));
			playerFaction.broadcast(ChatColor.GOLD + "Member Death: " + ConfigurationService.TEAMMATE_COLOUR
					+ role.getAstrix() + player.getName() + ChatColor.GOLD + ". " + "DTR: (" + ChatColor.WHITE
					+ JavaUtils.format(newDtr, 2) + '/'
					+ JavaUtils.format(playerFaction.getMaximumDeathsUntilRaidable(), 2) + ChatColor.GOLD + ").");

		}

		if (MinecraftServer.getServer().recentTps[0] > 15) { // Prevent unnecessary lag during prime times.
			Location location = player.getLocation();
			WorldServer worldServer = ((CraftWorld) location.getWorld()).getHandle();

			EntityLightning entityLightning = new EntityLightning(worldServer, location.getX(), location.getY(),
					location.getZ(), false);
			PacketPlayOutSpawnEntityWeather packet = new PacketPlayOutSpawnEntityWeather(entityLightning);
			for (Player target : Bukkit.getServer().getOnlinePlayers()) {
				if (plugin.getManagerHandler().getUserManager().getUser(target.getUniqueId()).isShowLightning()) {
					((CraftPlayer) target).getHandle().playerConnection.sendPacket(packet);
					target.playSound(target.getLocation(), Sound.AMBIENCE_THUNDER, 1.0F, 1.0F);
				}
			}
		}
	}
}

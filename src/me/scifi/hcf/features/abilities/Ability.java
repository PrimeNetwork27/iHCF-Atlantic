package me.scifi.hcf.features.abilities;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import com.doctordark.util.Cooldowns;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.scifi.hcf.HCF;
import me.scifi.hcf.features.abilities.type.BigBoy;
import me.scifi.hcf.features.abilities.type.RoadRunner;
import me.scifi.hcf.features.abilities.type.Steroids;

@Getter
@Setter
@AllArgsConstructor
public abstract class Ability implements Listener {

	@Getter
	private static final Map<String, Ability> abilities = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	private String name;
	private Cooldowns cooldown;
	private boolean enable;
	private ItemStack item;

	public static void load() { // Reflection ?
		PluginManager pluginManager = Bukkit.getPluginManager();

		Arrays.asList(new Steroids(), new BigBoy(), new RoadRunner())
				.forEach(listener -> pluginManager.registerEvents(listener, HCF.getPlugin()));
	}

	public void consume(Player player) {
		if (player.getItemInHand().getAmount() > 1) {
			player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
		} else {
			player.setItemInHand(new ItemStack(Material.AIR));
		}
	}

	@EventHandler
	public void onDeath(final PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (player.getInventory().contains(getItem())) {
			event.getDrops().remove(getItem());
		}
	}

}
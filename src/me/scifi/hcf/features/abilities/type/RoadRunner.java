package me.scifi.hcf.features.abilities.type;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.doctordark.util.CC;
import com.doctordark.util.Cooldowns;
import com.doctordark.util.ItemBuilder;
import com.doctordark.util.TimeUtil;

import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.type.SpawnFaction;
import me.scifi.hcf.features.abilities.Ability;

public class RoadRunner extends Ability {

	private final Map<UUID, PotionEffect> oldEffects = new HashMap<>();

	public RoadRunner() {
		super("roadrunner", new Cooldowns("RoadRunnerCooldown", TimeUtil.parse("30s")), true,
				new ItemBuilder(Material.SUGAR).displayName("&aCocaine")
						.lore("Use this to be a road runner for 5 seconds.").build());
		getAbilities().put(getName(), this);
	}

	@EventHandler
	public void onInyect(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (HCF.getPlugin().getManagerHandler().getFactionManager()
				.getFactionAt(player.getLocation()) instanceof SpawnFaction) {
			return;
		}
		ItemStack item = event.getItem();
		if (item == null) {
			return;
		}
		if (!getItem().isSimilar(item)) {
			return;
		}
		event.setCancelled(true);
		if (getCooldown().isOnCooldown(player)) {
			player.sendMessage(CC.translate("&c&lYou can't use this ability now cause you're on cooldown."));
			return;
		}
		PotionEffect speed = getSpeedEffect(player);
		if (speed != null) {
			oldEffects.put(player.getUniqueId(), speed);
		}
		getCooldown().setCooldown(player);
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 5, 2), true);
		consume(player);
		new BukkitRunnable() {
			@Override
			public void run() {
				PotionEffect old = oldEffects.get(player.getUniqueId());
				if (old != null) {
					player.addPotionEffect(old, true);
				}
			}
		}.runTaskLater(HCF.getPlugin(), 20 * 5);
	}

	public static PotionEffect getSpeedEffect(Player player) {
		return player.getActivePotionEffects().stream()
				.filter(effect -> effect.getType().equals(PotionEffectType.SPEED)).findFirst().orElse(null);
	}

}

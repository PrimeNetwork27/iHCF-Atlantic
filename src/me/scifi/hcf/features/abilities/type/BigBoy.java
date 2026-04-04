package me.scifi.hcf.features.abilities.type;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.doctordark.util.CC;
import com.doctordark.util.Cooldowns;
import com.doctordark.util.ItemBuilder;
import com.doctordark.util.TimeUtil;

import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.type.SpawnFaction;
import me.scifi.hcf.features.abilities.Ability;

public class BigBoy extends Ability { // TODO: Global Timer Cooldown.

	public BigBoy() {
		super("BigBoy", new Cooldowns("BigBoyCooldown", TimeUtil.parse("30s")), true,
				new ItemBuilder(Material.IRON_INGOT).displayName("&eBig boy")
						.lore("&aBecome the big boy of the faction for 10s.").build());
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
		getCooldown().setCooldown(player);
		player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 10, 0));
		player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 10, 0));
		player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 10, 0));
		consume(player);
	}

}

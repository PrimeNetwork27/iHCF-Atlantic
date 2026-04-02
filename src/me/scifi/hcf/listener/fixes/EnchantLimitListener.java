package me.scifi.hcf.listener.fixes;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.ImmutableMap;

import me.scifi.hcf.ConfigurationService;
import net.minecraft.server.v1_8_R3.Item.EnumToolMaterial;
import net.minecraft.server.v1_8_R3.ItemArmor.EnumArmorMaterial;

/**
 * Listener that limits the maximum {@link Enchantment} levels for
 * {@link ItemStack}s.
 */
public class EnchantLimitListener implements Listener { // TODO: ENUM TOOL MATERIAL DOESNT HAD DIAMOND

	private final ImmutableMap<Material, EnumToolMaterial> ITEM_TOOL_MAPPING = /* TODO:Maps.immutableEnumMap */(ImmutableMap
			.of(Material.IRON_INGOT, EnumToolMaterial.IRON, Material.GOLD_INGOT, EnumToolMaterial.GOLD));

	private final ImmutableMap<Material, EnumArmorMaterial> ITEM_ARMOUR_MAPPING = /* TODO:Maps.immutableEnumMap */(ImmutableMap
			.of(Material.IRON_INGOT, EnumArmorMaterial.IRON, Material.GOLD_INGOT, EnumArmorMaterial.GOLD,
					Material.DIAMOND, EnumArmorMaterial.DIAMOND));

	/**
	 * Gets the new fixed level for an enchantment.
	 *
	 * @param enchant the enchant to get for
	 * @return the capped level of enchantment
	 */
	public int getMaxLevel(Enchantment enchant) {
		return ConfigurationService.ENCHANTMENT_LIMITS.getOrDefault(enchant, enchant.getMaxLevel());
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onEnchantItem(EnchantItemEvent event) {
		Map<Enchantment, Integer> adding = event.getEnchantsToAdd();
		Iterator<Map.Entry<Enchantment, Integer>> iterator = adding.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Enchantment, Integer> entry = iterator.next();
			Enchantment enchantment = entry.getKey();
			int maxLevel = getMaxLevel(enchantment);
			if (entry.getValue() > maxLevel) {
				if (maxLevel > 0) {
					adding.put(enchantment, maxLevel);
				} else {
					iterator.remove();
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onEntityDeath(EntityDeathEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			for (ItemStack drop : event.getDrops()) {
				this.validateIllegalEnchants(drop);
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onPlayerFishEvent(PlayerFishEvent event) {
		Entity caught = event.getCaught();
		if (caught instanceof Item) {
			validateIllegalEnchants(((Item) caught).getItemStack());
		}
	}

	/**
	 * Validates the {@link Enchantment}s of a {@link ItemStack}, removing any
	 * disallowed ones.
	 *
	 * @param stack the {@link ItemStack} to validate
	 * @return true if was changed during validation
	 */
	private boolean validateIllegalEnchants(ItemStack stack) {
		boolean updated = false;
		if (stack != null && stack.getType() != Material.AIR) {
			ItemMeta meta = stack.getItemMeta();
			Set<Map.Entry<Enchantment, Integer>> entries;

			// Have to use this for books.
			if (meta instanceof EnchantmentStorageMeta) {
				EnchantmentStorageMeta enchantmentStorageMeta = (EnchantmentStorageMeta) meta;
				entries = enchantmentStorageMeta.getStoredEnchants().entrySet();
				for (Map.Entry<Enchantment, Integer> entry : entries) {
					Enchantment enchantment = entry.getKey();
					int maxLevel = getMaxLevel(enchantment);
					if (entry.getValue() > maxLevel) {
						updated = true;
						if (maxLevel > 0) {
							enchantmentStorageMeta.addStoredEnchant(enchantment, maxLevel, false);
						} else {
							enchantmentStorageMeta.removeStoredEnchant(enchantment);
						}
					}
				}

				// Re-apply the ItemMeta.
				stack.setItemMeta(meta);
			} else {
				entries = stack.getEnchantments().entrySet();
				for (Map.Entry<Enchantment, Integer> entry : entries) {
					Enchantment enchantment = entry.getKey();
					int maxLevel = getMaxLevel(enchantment);
					if (entry.getValue() > maxLevel) {
						updated = true;
						stack.removeEnchantment(enchantment);
						if (maxLevel > 0) {
							stack.addEnchantment(enchantment, maxLevel);
						}
					}
				}
			}
		}

		return updated;
	}
}

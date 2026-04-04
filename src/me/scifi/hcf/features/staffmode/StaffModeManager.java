package me.scifi.hcf.features.staffmode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.doctordark.util.CC;
import com.doctordark.util.ItemBuilder;

import lombok.Getter;
import me.scifi.hcf.HCF;

@Getter
public final class StaffModeManager { // Class created to avoid the static abuse.

	private final Set<UUID> staffMode = new HashSet<>();

	private final Map<UUID, ItemStack[]> armorContents = new HashMap<>();

	private final Map<UUID, ItemStack[]> inventoryContents = new HashMap<>();
	public final ItemStack book = new ItemBuilder(Material.BOOK)
			.displayName(CC.translate(HCF.getPlugin().getMessagesYML().getString("STAFFMODE.INSPECT.NAME")))
			.lore(CC.translate(HCF.getPlugin().getMessagesYML().getStringList("STAFFMODE.INSPECT.LORE"))).build();

	public final ItemStack compass = new ItemBuilder(Material.COMPASS)
			.displayName(CC.translate(HCF.getPlugin().getMessagesYML().getString("STAFFMODE.THRUCOMPASS.NAME")))
			.lore(CC.translate(HCF.getPlugin().getMessagesYML().getStringList("STAFFMODE.THRUCOMPASS.LORE"))).build();

	public final ItemStack ice = new ItemBuilder(Material.ICE)
			.displayName(CC.translate(HCF.getPlugin().getMessagesYML().getString("STAFFMODE.FREEZEBLOCK.NAME")))
			.lore(CC.translate(HCF.getPlugin().getMessagesYML().getStringList("STAFFMODE.FREEZEBLOCK.LORE"))).build();

	public final ItemStack carpet = new ItemBuilder(Material.CARPET)
			.displayName(CC.translate(HCF.getPlugin().getMessagesYML().getString("STAFFMODE.BETTERVIEW.NAME")))
			.lore(CC.translate(HCF.getPlugin().getMessagesYML().getStringList("STAFFMODE.BETTERVIEW.LORE"))).build();

	public final ItemStack dye = new ItemBuilder(Material.INK_SACK, 1, (byte) 10)
			.displayName(CC.translate(HCF.getPlugin().getMessagesYML().getString("STAFFMODE.VANISHITEM.NAME.ENABLED")))
			.lore(CC.translate(HCF.getPlugin().getMessagesYML().getStringList("STAFFMODE.VANISHITEM.LORE"))).build();

	public final ItemStack head = new ItemBuilder(Material.SKULL, 1, (byte) 3)
			.displayName(CC.translate(HCF.getPlugin().getMessagesYML().getString("STAFFMODE.RANDOMTP.NAME")))
			.lore(CC.translate(HCF.getPlugin().getMessagesYML().getStringList("STAFFMODE.RANDOMTP.LORE"))).build();

	public void putInStaffMode(final Player p) {
		p.setGameMode(GameMode.CREATIVE);
		staffMode.add(p.getUniqueId());
		armorContents.put(p.getUniqueId(), p.getInventory().getArmorContents());
		inventoryContents.put(p.getUniqueId(), p.getInventory().getContents());
		p.getInventory().setArmorContents(null);
		p.getInventory().clear();
		giveModItems(p);
		p.setHealth(20);
		p.setFoodLevel(20);
		Vanish.setVanished(p);
	}

	public void removeFromStaffMode(final Player p) {
		staffMode.remove(p.getUniqueId());
		p.getInventory().clear();
		p.getInventory().setArmorContents(null);
		p.setGameMode(GameMode.SURVIVAL);
		p.setFoodLevel(20);
		p.setHealth(20);
		p.getInventory().setArmorContents(armorContents.get(p.getUniqueId()));
		p.getInventory().setContents(inventoryContents.get(p.getUniqueId()));
		armorContents.remove(p.getUniqueId());
		inventoryContents.remove(p.getUniqueId());
		Vanish.disableVanish(p);
	}

	public void giveModItems(final Player p) {
		PlayerInventory inv = p.getInventory();
		inv.setItem(HCF.getPlugin().getMessagesYML().getInt("STAFFMODE.INSPECT.SLOT"), book);
		inv.setItem(HCF.getPlugin().getMessagesYML().getInt("STAFFMODE.THRUCOMPASS.SLOT"), compass);
		inv.setItem(HCF.getPlugin().getMessagesYML().getInt("STAFFMODE.FREEZEBLOCK.SLOT"), ice);
		inv.setItem(HCF.getPlugin().getMessagesYML().getInt("STAFFMODE.BETTERVIEW.SLOT"), carpet);
		inv.setItem(HCF.getPlugin().getMessagesYML().getInt("STAFFMODE.VANISHITEM.SLOT"), dye);
		inv.setItem(HCF.getPlugin().getMessagesYML().getInt("STAFFMODE.RANDOMTP.SLOT"), head);
	}

}

package com.doctordark.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Copy from PlayerUtil FOUNDATION.
 * 
 */
public final class PlayerUtil {

	/**
	 * The player inventory size which is 3 rows + 1 row for hotbar meaning 9 * 4 =
	 * 36.
	 */
	public static final int PLAYER_INV_SIZE = 36;

	public static boolean addItemsOrDrop(final Player player, final ItemStack... items) {
		final Map<Integer, ItemStack> leftovers = addItems(player.getInventory(), items);

		final World world = player.getWorld();
		final Location location = player.getLocation();

		for (final ItemStack leftover : leftovers.values()) {
			final Item item = world.dropItem(location, leftover);

			item.setPickupDelay(2 * 20);
		}

		return leftovers.isEmpty();
	}

	public static Player getPlayerByNick(final String name, final boolean ignoreVanished) {
		final Player found = lookupNickedPlayer0(name);

		return found;
	}

	private static Player lookupNickedPlayer0(String name) {
		Player player = null;
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p.getName().equalsIgnoreCase(name)) {
				player = p;
			}
		}
		return player;
	}

	public static Map<Integer, ItemStack> addItems(final Inventory inventory, final Collection<ItemStack> items) {
		return addItems(inventory, items.toArray(new ItemStack[items.size()]));
	}

	/**
	 * Attempts to add items into the inventory, returning what it couldn't store
	 *
	 * @param inventory
	 * @param items
	 * @return
	 */
	public static Map<Integer, ItemStack> addItems(final Inventory inventory, final ItemStack... items) {
		return addItems(inventory, 0, items);
	}

	/**
	 * Attempts to add items into the inventory, returning what it couldn't store
	 * <p>
	 * Set oversizedStack to below normal stack size to disable oversized stacks
	 *
	 * @param inventory
	 * @param oversizedStacks
	 * @param items
	 * @return
	 */
	private static Map<Integer, ItemStack> addItems(final Inventory inventory, final int oversizedStacks,
			final ItemStack... items) {
		if (isCombinedInv(inventory)) {
			final Inventory fakeInventory = makeTruncatedInv((PlayerInventory) inventory);
			final Map<Integer, ItemStack> overflow = addItems(fakeInventory, oversizedStacks, items);
			for (int i = 0; i < fakeInventory.getContents().length; i++) {
				inventory.setItem(i, fakeInventory.getContents()[i]);
			}
			return overflow;
		}

		final Map<Integer, ItemStack> left = new HashMap<>();

		// combine items
		final ItemStack[] combined = new ItemStack[items.length];
		for (final ItemStack item : items) {
			if (item == null || item.getAmount() < 1) {
				continue;
			}
			for (int j = 0; j < combined.length; j++) {
				if (combined[j] == null) {
					combined[j] = item.clone();
					break;
				}
				if (combined[j].isSimilar(item)) {
					combined[j].setAmount(combined[j].getAmount() + item.getAmount());
					break;
				}
			}
		}

		for (int i = 0; i < combined.length; i++) {
			final ItemStack item = combined[i];
			if (item == null || item.getType() == Material.AIR) {
				continue;
			}

			while (true) {
				// Do we already have a stack of it?
				final int maxAmount = oversizedStacks > item.getType().getMaxStackSize() ? oversizedStacks
						: item.getType().getMaxStackSize();
				final int firstPartial = firstPartial(inventory, item, maxAmount);

				// Drat! no partial stack
				if (firstPartial == -1) {
					// Find a free spot!
					final int firstFree = inventory.firstEmpty();

					if (firstFree == -1) {
						// No space at all!
						left.put(i, item);
						break;
					}

					// More than a single stack!
					if (item.getAmount() > maxAmount) {
						final ItemStack stack = item.clone();
						stack.setAmount(maxAmount);
						inventory.setItem(firstFree, stack);
						item.setAmount(item.getAmount() - maxAmount);
					} else {
						// Just store it
						inventory.setItem(firstFree, item);
						break;
					}

				} else {
					// So, apparently it might only partially fit, well lets do just that
					final ItemStack partialItem = inventory.getItem(firstPartial);

					final int amount = item.getAmount();
					final int partialAmount = partialItem.getAmount();

					// Check if it fully fits
					if (amount + partialAmount <= maxAmount) {
						partialItem.setAmount(amount + partialAmount);
						break;
					}

					// It fits partially
					partialItem.setAmount(maxAmount);
					item.setAmount(amount + partialAmount - maxAmount);
				}
			}
		}
		return left;
	}

	private static int firstPartial(final Inventory inventory, final ItemStack item, final int maxAmount) {
		if (item == null) {
			return -1;
		}
		final ItemStack[] stacks = inventory.getContents();
		for (int i = 0; i < stacks.length; i++) {
			final ItemStack cItem = stacks[i];
			if (cItem != null && cItem.getAmount() < maxAmount && cItem.isSimilar(item)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Creates a new inventory of {@link #PLAYER_INV_SIZE} size
	 *
	 * @param playerInventory
	 * @return
	 */
	private static Inventory makeTruncatedInv(final PlayerInventory playerInventory) {
		final Inventory fake = Bukkit.createInventory(null, PLAYER_INV_SIZE);
		fake.setContents(Arrays.copyOf(playerInventory.getContents(), fake.getSize()));

		return fake;
	}

	/**
	 * Return true if the inventory is combined player inventory
	 *
	 * @param inventory
	 * @return
	 */
	private static boolean isCombinedInv(final Inventory inventory) {
		return inventory instanceof PlayerInventory && inventory.getContents().length > PLAYER_INV_SIZE;
	}

}
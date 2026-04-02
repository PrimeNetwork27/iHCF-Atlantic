package com.doctordark.util;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.PacketPlayOutSetSlot;
import net.minecraft.server.v1_8_R3.PlayerInventory;

public class NmsUtils {
	public static int getProtocolVersion(Player player) { // TODO: ViaVersion or other plugin.
		EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
		return nmsPlayer.playerConnection.networkManager.channel
				.attr(io.netty.util.AttributeKey.<Integer>valueOf("protocol_version")).get();
	}

	public static void resendHeldItemPacket(Player player) {
		sendItemPacketAtHeldSlot(player, NmsUtils.getCleanHeldItem(player));
	}

	public static void sendItemPacketAtHeldSlot(Player player, ItemStack stack) {
		sendItemPacketAtSlot(player, stack, player.getInventory().getHeldItemSlot());
	}

	public static void sendItemPacketAtSlot(Player player, ItemStack stack, int index) {
		sendItemPacketAtSlot(player, stack, index, ((CraftPlayer) player).getHandle().defaultContainer.windowId);
	}

	public static void sendItemPacketAtSlot(Player player, ItemStack stack, int index, int windowID) {
		EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
		if (entityPlayer.playerConnection != null) {
			// Safeguarding
			if (index < PlayerInventory.getHotbarSize()) {
				index += 36;
			} else if (index > 35) {
				index = 8 - (index - 36);
			}

			entityPlayer.playerConnection.sendPacket(new PacketPlayOutSetSlot(windowID, index, stack));
		}
	}

	public static ItemStack getCleanItem(Inventory inventory, int slot) {
		return ((CraftInventory) inventory).getInventory().getItem(slot);
	}

	public static ItemStack getCleanItem(Player player, int slot) {
		return getCleanItem(player.getInventory(), slot);
	}

	public static ItemStack getCleanHeldItem(Player player) {
		return getCleanItem(player, player.getInventory().getHeldItemSlot());
	}

	public static net.minecraft.server.v1_8_R3.ItemStack getDirectNmsItemstack(
			final org.bukkit.inventory.ItemStack stack) {
		if (stack == null || stack.getType() == Material.AIR) {
			return null;
		}
		return CraftItemStack.asNMSCopy(stack);
	}
}

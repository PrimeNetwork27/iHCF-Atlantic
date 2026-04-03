package com.doctordark.util.menu;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutOpenWindow;

public class InventoryUtil { // TODO: Avoid to make the class Multi-NMS

	public static void changeTitle(Player player, String title) {
		EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
		int windowId = entityPlayer.activeContainer.windowId;

		IChatBaseComponent chatTitle = new ChatComponentText(title);

		int size = player.getOpenInventory().getTopInventory().getSize();
		String type = getWindowType(size);

		PacketPlayOutOpenWindow packet = new PacketPlayOutOpenWindow(windowId, type, chatTitle, size);

		entityPlayer.playerConnection.sendPacket(packet);
		player.updateInventory();
	}

	private static String getWindowType(int size) {
		switch (size) {
		case 5:
			return "minecraft:hopper";
		case 9:
			return "minecraft:dispenser";
		default:
			return "minecraft:chest"; // 9, 18, 27, 36, 45, 54
		}
	}
}
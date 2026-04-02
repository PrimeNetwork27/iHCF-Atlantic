package me.scifi.hcf.visualise;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.google.common.collect.ImmutableSet;

import me.scifi.hcf.HCF;

/**
 * Reference http://wiki.vg/Protocol
 */
public final class ProtocolLibHook {

	private static final ImmutableSet<String> DISALLOWED_TAB_COMPLETES = ImmutableSet.of("ver", "version", "?", "about",
			"help"); // Empty string is required to check for "/"

	private ProtocolLibHook() {
	}

	/**
	 * Hooks ProtocolLibrary into a {@link JavaPlugin}.
	 *
	 * @param HCF the plugin to hook into
	 */
	public static void hook(HCF plugin) {
		ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
		protocolManager.addPacketListener(
				new PacketAdapter(HCF.getPlugin(), ListenerPriority.NORMAL, PacketType.Play.Client.BLOCK_PLACE) {
					@Override
					public void onPacketReceiving(PacketEvent event) {
						StructureModifier<Integer> modifier = event.getPacket().getIntegers();
						StructureModifier<BlockPosition> blockModifiers = event.getPacket().getBlockPositionModifier();
						Player player = event.getPlayer();

						try {
							int face = modifier.read(3);
							if (face == 255) {
								return;
							}
							BlockPosition blockPosition = blockModifiers.read(0);
							Location location = new Location(player.getWorld(), blockPosition.getX(),
									blockPosition.getY(), blockPosition.getZ());
							VisualBlock visualBlock = HCF.getPlugin().getManagerHandler().getVisualiseHandler()
									.getVisualBlockAt(player, location);
							if (visualBlock == null) {
								return;
							}

							switch (face) {
							case 0: {
								location.add(0.0, -1.0, 0.0);
								break;
							}
							case 1: {
								location.add(0.0, 1.0, 0.0);
								break;
							}
							case 2: {
								location.add(0.0, 0.0, -1.0);
								break;
							}
							case 3: {
								location.add(0.0, 0.0, 1.0);
								break;
							}
							case 4: {
								location.add(-1.0, 0.0, 0.0);
								break;
							}
							case 5: {
								location.add(1.0, 0.0, 0.0);
								break;
							}
							default: {
								return;
							}
							}

							event.setCancelled(true);
							ItemStack stack = event.getPacket().getItemModifier().read(0);
							if (stack != null && (stack.getType().isBlock() || isLiquidSource(stack.getType()))) {
								player.setItemInHand(player.getItemInHand());
							}
							if ((visualBlock = HCF.getPlugin().getManagerHandler().getVisualiseHandler()
									.getVisualBlockAt(player, location)) != null) {
								VisualBlockData visualBlockData = visualBlock.getBlockData();
								player.sendBlockChange(location, visualBlockData.getBlockType(),
										visualBlockData.getData());
							} else {
								new BukkitRunnable() {
									@Override
									public void run() {
										Block block = location.getBlock();
										player.sendBlockChange(location, block.getType(), block.getData());
									}
								}.runTask(HCF.getPlugin());
							}
						} catch (FieldAccessException ex) {
						}
					}
				});

		protocolManager.addPacketListener(
				new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.BLOCK_DIG) {
					@Override
					public void onPacketReceiving(PacketEvent event) {
						StructureModifier<Integer> modifier = event.getPacket().getIntegers();
						try {
							StructureModifier<BlockPosition> blockModifiers = event.getPacket()
									.getBlockPositionModifier();
							StructureModifier<EnumWrappers.PlayerDigType> digTypeModifiers = event.getPacket()
									.getPlayerDigTypes();
							EnumWrappers.PlayerDigType digType = digTypeModifiers.read(0);

							if (digType.name().contains("BLOCK")) {
								Player player = event.getPlayer();
								BlockPosition blockPosition = blockModifiers.read(0);

								Location location = new Location(player.getWorld(), blockPosition.getX(),
										blockPosition.getY(), blockPosition.getZ());
								VisualBlock visualBlock = HCF.getPlugin().getManagerHandler().getVisualiseHandler()
										.getVisualBlockAt(player, location);
								if (visualBlock != null) {
									event.setCancelled(true);
									VisualBlockData data = visualBlock.getBlockData();
									if (player != null && blockPosition != null) {
										event.setCancelled(true);
										player.sendBlockChange(location, data.getBlockType(), data.getData());
									}
								}
							}
						} catch (FieldAccessException ex) {
							ex.printStackTrace();
						}
					}
				});

		protocolManager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Client.TAB_COMPLETE) {
			@Override
			public void onPacketReceiving(PacketEvent event) {
				if (event.getPlayer().hasPermission("hcf.bypassanticommandtab")) {
					return;
				}

				String message = event.getPacket().getSpecificModifier(String.class).read(0).toLowerCase();

				if (message.startsWith("/") && !message.contains(" ")) { // Blocks /<tab>
					event.setCancelled(true);
					return;
				}

				for (String i : DISALLOWED_TAB_COMPLETES) { // Blocks /<command> <tab>
					if (message.startsWith("/" + i) && !message.contains("  ")) {
						event.setCancelled(true);
						return;
					}
				}
			}
		});

	}

	private static boolean isLiquidSource(Material material) {
		switch (material) {
		case LAVA_BUCKET:
		case WATER_BUCKET:
			return true;
		default:
			return false;
		}
	}

}

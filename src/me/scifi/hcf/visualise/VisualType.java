package me.scifi.hcf.visualise;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.cryptomorin.xseries.XMaterial;

import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.struct.Relation;
import me.scifi.hcf.faction.type.Faction;

public enum VisualType {

	// TODO: Figure out a better way for filling blocks than this

	/**
	 * Represents the wall approaching claims when Spawn Tagged.
	 */
	SPAWN_BORDER() {
		private final BlockFiller blockFiller = new BlockFiller() {
			@Override
			VisualBlockData generate(Player player, Location location) {
				return new VisualBlockData(XMaterial.RED_STAINED_GLASS_PANE.parseMaterial(),
						XMaterial.RED_DYE.getData());
			}
		};

		@Override
		BlockFiller blockFiller() {
			return blockFiller;
		}
	},
	/**
	 * Represents the wall approaching claims when PVP Protected.
	 */
	CLAIM_BORDER() {
		private final BlockFiller blockFiller = new BlockFiller() {
			@Override
			VisualBlockData generate(Player player, Location location) {
				return new VisualBlockData(XMaterial.PINK_STAINED_GLASS_PANE.parseMaterial(),
						XMaterial.PINK_DYE.getData());
			}
		};

		@Override
		BlockFiller blockFiller() {
			return blockFiller;
		}
	},
	NONE() {
		@Override
		BlockFiller blockFiller() {
			throw new UnsupportedOperationException();
		}
	},
	/**
	 * Represents claims shown using /faction map.
	 */
	SUBCLAIM_MAP() {
		private final BlockFiller blockFiller = new BlockFiller() {
			@Override
			VisualBlockData generate(Player player, Location location) {
				return new VisualBlockData(XMaterial.BIRCH_LOG.parseMaterial(), (byte) 1);
			}
		};

		@Override
		BlockFiller blockFiller() {
			return blockFiller;
		}
	},
	/**
	 * Represents claims shown using /faction map.
	 */
	CLAIM_MAP() {
		private final BlockFiller blockFiller = new BlockFiller() {
			private final Material[] types = new Material[] { Material.SNOW_BLOCK, Material.SANDSTONE, Material.FURNACE,
					Material.NETHERRACK, Material.GLOWSTONE, Material.LAPIS_BLOCK, Material.NETHER_BRICK,
					Material.DIAMOND_ORE, Material.COAL_ORE, Material.IRON_ORE, Material.GOLD_ORE, Material.LAPIS_ORE,
					Material.REDSTONE_ORE };

			private int materialCounter = 0;

			@Override
			VisualBlockData generate(Player player, Location location) {
				int y = location.getBlockY();
				if (y == 0 || y % 3 == 0) {
					return new VisualBlockData(types[materialCounter]);
				}

				Faction faction = HCF.getPlugin().getManagerHandler().getFactionManager().getFactionAt(location);
				return new VisualBlockData(XMaterial.BLACK_STAINED_GLASS.parseMaterial(),
						(faction != null ? faction.getRelation(player) : Relation.ENEMY).toDyeColour().getDyeData());
			}

			@Override
			ArrayList<VisualBlockData> bulkGenerate(Player player, Iterable<Location> locations) {
				ArrayList<VisualBlockData> result = super.bulkGenerate(player, locations);
				if (++materialCounter == types.length) {
					materialCounter = 0;
				}
				return result;
			}
		};

		@Override
		BlockFiller blockFiller() {
			return blockFiller;
		}
	},

	CREATE_CLAIM_SELECTION() {
		private final BlockFiller blockFiller = new BlockFiller() {
			@Override
			VisualBlockData generate(Player player, Location location) {
				return new VisualBlockData(location.getBlockY() % 3 != 0 ? Material.GLASS : Material.GOLD_BLOCK);
			}
		};

		@Override
		BlockFiller blockFiller() {
			return blockFiller;
		}
	},;

	/**
	 * Gets the {@link BlockFiller} instance.
	 *
	 * @return the filler
	 */
	abstract BlockFiller blockFiller();
}

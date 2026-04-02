package com.doctordark.util;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;

public enum ParticleEffect {

	HUGE_EXPLODE("hugeexplosion", 0, EnumParticle.EXPLOSION_HUGE),
	LARGE_EXPLODE("largeexplode", 1, EnumParticle.EXPLOSION_LARGE),
	FIREWORK_SPARK("fireworksSpark", 2, EnumParticle.FIREWORKS_SPARK),
	AIR_BUBBLE("bubble", 3, EnumParticle.WATER_BUBBLE), SUSPEND("suspend", 4, EnumParticle.SUSPENDED),
	DEPTH_SUSPEND("depthSuspend", 5, EnumParticle.SUSPENDED_DEPTH), TOWN_AURA("townaura", 6, EnumParticle.TOWN_AURA),
	CRITICAL_HIT("crit", 7, EnumParticle.CRIT), MAGIC_CRITICAL_HIT("magicCrit", 8, EnumParticle.CRIT_MAGIC),
	MOB_SPELL("mobSpell", 9, EnumParticle.MOB_APPEARANCE),
	MOB_SPELL_AMBIENT("mobSpellAmbient", 10, EnumParticle.SPELL_MOB_AMBIENT), SPELL("spell", 11, EnumParticle.SPELL),
	INSTANT_SPELL("instantSpell", 12, EnumParticle.SPELL_INSTANT),
	BLUE_SPARKLE("witchMagic", 13, EnumParticle.SPELL_WITCH), NOTE_BLOCK("note", 14, EnumParticle.NOTE),
	ENDER("portal", 15, EnumParticle.PORTAL), ENCHANTMENT_TABLE("enchantmenttable", 16, EnumParticle.ENCHANTMENT_TABLE),
	EXPLODE("explode", 17, EnumParticle.EXPLOSION_NORMAL), FIRE("flame", 18, EnumParticle.FLAME),
	LAVA_SPARK("lava", 19, EnumParticle.LAVA), FOOTSTEP("footstep", 20, EnumParticle.FOOTSTEP),
	SPLASH("splash", 21, EnumParticle.WATER_SPLASH), LARGE_SMOKE("largesmoke", 22, EnumParticle.SMOKE_LARGE),
	CLOUD("cloud", 23, EnumParticle.CLOUD), REDSTONE_DUST("reddust", 24, EnumParticle.REDSTONE),
	SNOWBALL_HIT("snowballpoof", 25, EnumParticle.SNOWBALL), DRIP_WATER("dripWater", 26, EnumParticle.DRIP_WATER),
	DRIP_LAVA("dripLava", 27, EnumParticle.DRIP_LAVA), SNOW_DIG("snowshovel", 28, EnumParticle.SNOW_SHOVEL),
	SLIME("slime", 29, EnumParticle.SLIME), HEART("heart", 30, EnumParticle.HEART),
	ANGRY_VILLAGER("angryVillager", 31, EnumParticle.VILLAGER_ANGRY),
	GREEN_SPARKLE("happyVillager", 32, EnumParticle.VILLAGER_HAPPY),
	ICONCRACK("iconcrack", 33, EnumParticle.ITEM_CRACK), TILECRACK("tilecrack", 34, EnumParticle.BLOCK_CRACK);

	private final String name;
	@Deprecated
	private final int id;
	private final EnumParticle enumParticle;

	private ParticleEffect(final String name, final int id, final EnumParticle enumParticle) {
		this.name = name;
		this.id = id;
		this.enumParticle = enumParticle;
	}

	@Deprecated
	String getName() {
		return this.name;
	}

	@Deprecated
	public int getId() {
		return this.id;
	}

	public EnumParticle getEnumParticle() {
		return this.enumParticle;
	}

	public void display(final Player player, final float x, final float y, final float z, final float speed,
			final int amount) {
		this.display(player, x, y, z, 0.0f, 0.0f, 0.0f, speed, amount);
	}

	public void display(final Player player, final float x, final float y, final float z, final float offsetX,
			final float offsetY, final float offsetZ, final float speed, final int amount) {
		final Packet packet = this.createPacket(x, y, z, offsetX, offsetY, offsetZ, speed, amount);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}

	public void display(final Player player, final Location location, final float speed, final int amount) {
		this.display(player, location, 0.0f, 0.0f, 0.0f, speed, amount);
	}

	public void display(final Player player, final Location location, final float offsetX, final float offsetY,
			final float offsetZ, final float speed, final int amount) {
		final Packet packet = this.createPacket(location, offsetX, offsetY, offsetZ, speed, amount);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}

	public void broadcast(final float x, final float y, final float z, final float offsetX, final float offsetY,
			final float offsetZ, final float speed, final int amount) {
		final Packet packet = this.createPacket(x, y, z, offsetX, offsetY, offsetZ, speed, amount);
		for (final Player player : Bukkit.getOnlinePlayers()) {
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
		}
	}

	public void broadcast(final Location location, final float offsetX, final float offsetY, final float offsetZ,
			final float speed, final int amount) {
		this.broadcast(location, offsetX, offsetY, offsetZ, speed, amount, null, null);
	}

	public void broadcast(final Location location, final float offsetX, final float offsetY, final float offsetZ,
			final float speed, final int amount, @Nullable final Player source) {
		this.broadcast(location, offsetX, offsetY, offsetZ, speed, amount, source, null);
	}

	public void broadcast(final Location location, final float offsetX, final float offsetY, final float offsetZ,
			final float speed, final int amount, @Nullable final Player source,
			@Nullable final Predicate<Player> predicate) {
		final Packet packet = this.createPacket(location, offsetX, offsetY, offsetZ, speed, amount);
		for (final Player player : Bukkit.getOnlinePlayers()) {
			if ((source == null || player.canSee(source)) && (predicate == null || predicate.apply(player))) {
				((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
			}
		}
	}

	public void sphere(@Nullable final Player player, final Location location, final float radius) {
		this.sphere(player, location, radius, 20.0f, 2);
	}

	public void sphere(@Nullable final Player player, final Location location, final float radius, final float density,
			final int intensity) {
		Preconditions.checkNotNull((Object) location, "Location cannot be null");
		Preconditions.checkArgument(radius >= 0.0f, "Radius must be positive");
		Preconditions.checkArgument(density >= 0.0f, "Density must be positive");
		Preconditions.checkArgument(intensity >= 0, "Intensity must be positive");
		final float deltaPitch = 180.0f / density;
		final float deltaYaw = 360.0f / density;
		final World world = location.getWorld();
		for (int i = 0; i < density; ++i) {
			for (int j = 0; j < density; ++j) {
				final float pitch = -90.0f + j * deltaPitch;
				final float yaw = -180.0f + i * deltaYaw;
				final float x = radius * MathHelper.sin(-yaw * 0.017453292f - 3.1415927f)
						* -MathHelper.cos(-pitch * 0.017453292f) + (float) location.getX();
				final float y = radius * MathHelper.sin(-pitch * 0.017453292f) + (float) location.getY();
				final float z = radius * MathHelper.cos(-yaw * 0.017453292f - 3.1415927f)
						* -MathHelper.cos(-pitch * 0.017453292f) + (float) location.getZ();
				final Location target = new Location(world, x, y, z);
				if (player == null) {
					this.broadcast(target, 0.0f, 0.0f, 0.0f, 0.0f, intensity);
				} else {
					this.display(player, target, 0.0f, 0.0f, 0.0f, 0.0f, intensity);
				}
			}
		}
	}

	private PacketPlayOutWorldParticles createPacket(final Location location, final float offsetX, final float offsetY,
			final float offsetZ, final float speed, final int amount) {
		return this.createPacket((float) location.getX(), (float) location.getY(), (float) location.getZ(), offsetX,
				offsetY, offsetZ, speed, amount);
	}

	private PacketPlayOutWorldParticles createPacket(final float x, final float y, final float z, final float offsetX,
			final float offsetY, final float offsetZ, final float speed, final int amount) {
		Preconditions.checkArgument(speed >= 0.0f, "Speed must be positive");
		Preconditions.checkArgument(amount > 0, "Cannot use less than one particle.");
		return new PacketPlayOutWorldParticles(this.enumParticle, true, x, y, z, offsetX, offsetY, offsetZ, speed,
				amount);
	}
}
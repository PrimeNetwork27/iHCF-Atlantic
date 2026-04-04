package com.doctordark.util.scoreboard.nametag;

import java.lang.reflect.Field;
import java.util.Collection;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardTeam;

public final class ScoreboardTeamPacketImpl implements ScoreboardTeamPacket {

	private static final Field aField, bField, cField, dField, eField, gField, hField, iField;

	static {
		try {
			aField = getDeclared("a"); // team name
			bField = getDeclared("b"); // display name
			cField = getDeclared("c"); // prefix
			dField = getDeclared("d"); // suffix
			eField = getDeclared("e"); // name tag visibility (String)
			gField = getDeclared("g"); // players (Collection)
			hField = getDeclared("h"); // mode (int)
			iField = getDeclared("i"); // friendly fire flags (int)
		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	private static Field getDeclared(String name) throws NoSuchFieldException {
		Field f = PacketPlayOutScoreboardTeam.class.getDeclaredField(name);
		f.setAccessible(true);
		return f;
	}

	private final PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();

	public ScoreboardTeamPacketImpl(String name, String prefix, String suffix, Collection<String> players, int mode) {
		try {
			aField.set(packet, name);
			hField.set(packet, mode); // mode va en h, no en f
			if (mode == 0 || mode == 2) {
				bField.set(packet, name);
				cField.set(packet, prefix);
				dField.set(packet, suffix);
				eField.set(packet, "always"); // name tag visibility
				iField.set(packet, 3); // friendly fire flags
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mode == 0 || mode == 3 || mode == 4) {
			addAll(players);
		}
	}

	@SuppressWarnings("unchecked")
	private void addAll(Collection<String> col) {
		if (col == null) {
			return;
		}
		try {
			((Collection<String>) gField.get(packet)).addAll(col);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sendToPlayer(Player player) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}
}
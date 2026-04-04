package com.doctordark.util.scoreboard.nametag;

import java.util.Collection;

import org.bukkit.entity.Player;

public interface ScoreboardTeamPacket {
	void sendToPlayer(Player player);

	static ScoreboardTeamPacket create(String name, String prefix, String suffix, Collection<String> players,
			int mode) {
		return ScoreboardTeamPacketFactory.create(name, prefix, suffix, players, mode);
	}

	static ScoreboardTeamPacket create(String name, Collection<String> players, int mode) {
		return ScoreboardTeamPacketFactory.create(name, players, mode);
	}
}

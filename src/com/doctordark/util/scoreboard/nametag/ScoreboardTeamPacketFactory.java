package com.doctordark.util.scoreboard.nametag;

import java.util.Collection;

public class ScoreboardTeamPacketFactory {

	public static ScoreboardTeamPacket create(String name, String prefix, String suffix, Collection<String> players,
			int mode) {

		return new ScoreboardTeamPacketImpl(name, prefix, suffix, players, mode);

	}

	public static ScoreboardTeamPacket create(String name, Collection<String> players, int mode) {
		return create(name, "", "", players, mode);
	}
}

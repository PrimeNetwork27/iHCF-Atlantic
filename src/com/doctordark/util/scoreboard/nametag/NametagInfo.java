package com.doctordark.util.scoreboard.nametag;

import java.util.ArrayList;
import java.util.Objects;

public final class NametagInfo {
	private final String name;

	private final String prefix;

	private final String suffix;

	private final ScoreboardTeamPacket teamAddPacket;

	protected NametagInfo(String name, String prefix, String suffix) {
		this.name = name;
		this.prefix = prefix;
		this.suffix = suffix;
		this.teamAddPacket = ScoreboardTeamPacket.create(name, prefix, suffix, new ArrayList<>(), 0);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof NametagInfo) {
			NametagInfo otherNametag = (NametagInfo) other;
			return (this.name.equals(otherNametag.name) && this.prefix.equals(otherNametag.prefix)
					&& this.suffix.equals(otherNametag.suffix));
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, prefix, suffix);
	}

	public String getName() {
		return this.name;
	}

	public String getPrefix() {
		return this.prefix;
	}

	public String getSuffix() {
		return this.suffix;
	}

	public ScoreboardTeamPacket getTeamAddPacket() {
		return this.teamAddPacket;
	}
}

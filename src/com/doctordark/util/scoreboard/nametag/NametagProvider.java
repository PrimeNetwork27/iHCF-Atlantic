package com.doctordark.util.scoreboard.nametag;

import java.beans.ConstructorProperties;

import org.bukkit.entity.Player;

public abstract class NametagProvider {
	private final String name;

	private final int weight;

	public abstract NametagInfo fetchNametag(Player paramPlayer1, Player paramPlayer2);

	public static NametagInfo createNametag(String prefix, String suffix) {
		return FrozenNametagHandler.getOrCreate(prefix, suffix);
	}

	@ConstructorProperties({ "name", "weight" })
	public NametagProvider(String name, int weight) {
		this.name = name;
		this.weight = weight;
	}

	public String getName() {
		return this.name;
	}

	public int getWeight() {
		return this.weight;
	}

	protected static final class DefaultNametagProvider extends NametagProvider {
		public DefaultNametagProvider() {
			super("Default Provider", 0);
		}

		public NametagInfo fetchNametag(Player toRefresh, Player refreshFor) {
			return createNametag("", "");
		}
	}
}

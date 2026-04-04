package com.doctordark.util.scoreboard.tablist.shared;

import org.bukkit.Bukkit;

import lombok.Getter;

/**
 * Represents the current Minecraft version the plugin loaded on
 */
public final class MinecraftVersion {

	/**
	 * The string representation of the version, for example V1_13
	 */
	private static String serverVersion;

	/**
	 * The wrapper representation of the version
	 */
	@Getter
	private static V current;

	/**
	 * The subversion such as 5 in 1.22.5
	 */
	@Getter
	private static int subversion;

	/**
	 * The version wrapper
	 */
	public enum V {
		v1_22(22, 769), v1_21(21, 767), v1_20(20, 763), v1_19(19, 759), v1_18(18, 757), v1_17(17, 755), v1_16(16, 735),
		v1_15(15, 573), v1_14(14, 477), v1_13(13, 393), v1_12(12, 335), v1_11(11, 315), v1_10(10, 210), v1_9(9, 107),
		v1_8(8, 47), v1_7(7, 4), v1_6(6, 73), v1_5(5, 60), v1_4(4, 47), v1_3_AND_BELOW(3, 39);

		private final int minorVersionNumber;

		@Getter
		private final int protocolVersion;

		V(int version, int protocol) {
			this.minorVersionNumber = version;
			this.protocolVersion = protocol;
		}

		/**
		 * Attempts to get the version from number
		 *
		 * @param number
		 * @return
		 * @throws RuntimeException if number not found
		 */
		protected static V parse(int number) {
			for (final V v : values()) {
				if (v.minorVersionNumber == number) {
					return v;
				}
			}

			throw new IllegalArgumentException("Invalid version number: " + number);
		}

		/**
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return "1." + this.minorVersionNumber;
		}
	}

	/**
	 * Does the current Minecraft version equal the given version?
	 *
	 * @param version
	 * @return
	 */
	public static boolean equals(V version) {
		return compareWith(version) == 0;
	}

	/**
	 * Is the current Minecraft version older than the given version?
	 *
	 * @param version
	 * @return
	 */
	public static boolean olderThan(V version) {
		return compareWith(version) < 0;
	}

	/**
	 * Is the current Minecraft version newer than the given version?
	 *
	 * @param version
	 * @return
	 */
	public static boolean newerThan(V version) {
		return compareWith(version) > 0;
	}

	/**
	 * Is the current Minecraft version at equals or newer than the given version?
	 *
	 * @param version
	 * @return
	 */
	public static boolean atLeast(V version) {
		return equals(version) || newerThan(version);
	}

	// Compares two versions by the number
	private static int compareWith(V version) {
		try {
			return getCurrent().minorVersionNumber - version.minorVersionNumber;

		} catch (final Throwable t) {
			t.printStackTrace();

			return 0;
		}
	}

	/**
	 * Return the full version such as 1.22.6
	 *
	 * @return
	 */
	public static String getFullVersion() {
		return current.toString() + (subversion > 0 ? "." + subversion : "");
	}

	/**
	 * Return the class versioning such as v1_14_R1 or empty string if not available
	 *
	 * @return
	 * @deprecated use {@link #getFullVersion()} because this returns empty string
	 *             on 1.20.5+ Paper
	 */
	@Deprecated
	public static String getServerVersion() {
		return serverVersion.equals("craftbukkit") ? "" : serverVersion;
	}

	static {

		// Find NMS package version
		final String packageName = Bukkit.getServer() == null ? ""
				: Bukkit.getServer().getClass().getPackage().getName();
		final String curr = packageName.substring(packageName.lastIndexOf('.') + 1);
		serverVersion = !"craftbukkit".equals(curr) && !"".equals(packageName) ? curr : "";

		// Find the Bukkit version
		final String bukkitVersion = Bukkit.getServer().getBukkitVersion(); // 1.20.6-R0.1-SNAPSHOT
		final String versionString = bukkitVersion.split("\\-")[0]; // 1.20.6
		final String[] versions = versionString.split("\\.");
		if (versions.length != 2 && versions.length != 3) {
			throw new IllegalArgumentException("Foundation cannot read Bukkit version: " + versionString
					+ ", expected 2 or 3 parts separated by dots, got " + versions.length + " parts");
		}

		final int version = Integer.parseInt(versions[1]); // 20

		current = version < 3 ? V.v1_3_AND_BELOW : V.parse(version);
		subversion = versions.length == 3 ? Integer.parseInt(versions[2]) : 0;

	}
}
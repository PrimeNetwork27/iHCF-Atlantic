package com.doctordark.util.scoreboard.nametag;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import me.scifi.hcf.HCF;

final class NametagThread extends Thread {
	private static final Map<NametagUpdate, Boolean> pendingUpdates = new ConcurrentHashMap<>();

	public NametagThread() {
		super("qLib - Nametag Thread");
		setDaemon(true);
	}

	@Override
	public void run() {
		while (true) {
			if (HCF.getPlugin() != null) {
				for (NametagUpdate pendingUpdate : pendingUpdates.keySet()) {
					try {
						FrozenNametagHandler.applyUpdate(pendingUpdate);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			try {
				Thread.sleep(FrozenNametagHandler.getUpdateInterval() * 50L);
			} catch (InterruptedException e2) {
				Thread.currentThread().interrupt();
				break;
			}
		}
	}

	public static Map<NametagUpdate, Boolean> getPendingUpdates() {
		return pendingUpdates;
	}
}

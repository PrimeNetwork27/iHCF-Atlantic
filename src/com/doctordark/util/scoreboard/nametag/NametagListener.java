package com.doctordark.util.scoreboard.nametag;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;

import me.scifi.hcf.HCF;

final class NametagListener implements Listener {
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (FrozenNametagHandler.isInitiated()) {
			event.getPlayer().setMetadata("qLibNametag-LoggedIn",
					new FixedMetadataValue(HCF.getPlugin(), Boolean.valueOf(true)));
			FrozenNametagHandler.initiatePlayer(event.getPlayer());
			FrozenNametagHandler.reloadPlayer(event.getPlayer());
			FrozenNametagHandler.reloadOthersFor(event.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		event.getPlayer().removeMetadata("qLibNametag-LoggedIn", HCF.getPlugin());
		FrozenNametagHandler.getTeamMap().remove(event.getPlayer().getName());
	}
}

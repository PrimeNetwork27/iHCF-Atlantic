package me.scifi.hcf.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.FactionManager;
import me.scifi.hcf.faction.type.PlayerFaction;

public class ChatListener implements Listener {

	private HCF plugin;

	public ChatListener(HCF plugin) {
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		String message = event.getMessage();
		Player player = event.getPlayer();
		FactionManager fm = plugin.getManagerHandler().getFactionManager();
		PlayerFaction playerFaction = fm.getPlayerFaction(player);
		String displayName = player.getDisplayName();
		ConsoleCommandSender console = Bukkit.getConsoleSender();
		String defaultFormat = getChatFormat(player, playerFaction, console);

		// Handle the custom messaging here.
		event.setFormat(defaultFormat);
		event.setCancelled(true);
		console.sendMessage(String.format(defaultFormat, displayName, message));
		for (Player recipient : event.getRecipients()) {
			recipient.sendMessage(String.format(getChatFormat(player, playerFaction, recipient), displayName, message));
		}
	}

	private String getChatFormat(Player player, PlayerFaction playerFaction, CommandSender viewer) {
		String factionTag = (playerFaction == null ? ChatColor.RED.toString() + '*'
				: playerFaction.getDisplayName(viewer));
		String result;
		result = ChatColor.GOLD + "[" + factionTag + ChatColor.GOLD + "]" + " %1$s" + ChatColor.GRAY + ": "
				+ ChatColor.WHITE + "%2$s";

		return result;
	}

}

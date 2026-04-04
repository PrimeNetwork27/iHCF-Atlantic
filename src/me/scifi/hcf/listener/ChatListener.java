package me.scifi.hcf.listener;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import me.scifi.hcf.HCF;
import me.scifi.hcf.Utils;
import me.scifi.hcf.command.MuteChatCommand;
import me.scifi.hcf.faction.FactionManager;
import me.scifi.hcf.faction.struct.ChatChannel;
import me.scifi.hcf.faction.type.PlayerFaction;

public class ChatListener implements Listener {

	private HCF plugin;

	public ChatListener(HCF plugin) {
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onChat(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		String message = e.getMessage().replace("%", "%%");
		FactionManager fm = plugin.getManagerHandler().getFactionManager();
		String rankName = plugin.getRank().getGroupPrefix(p);
		PlayerFaction playerFaction = fm.getPlayerFaction(p);

		ChatChannel chatChannel = playerFaction == null ? ChatChannel.PUBLIC
				: playerFaction.getMember(p).getChatChannel();

		if (MuteChatCommand.isLocked && !p.hasPermission("hcf.command.mutechat.bypass")) {
			p.sendMessage(Utils.chat(plugin.getMessagesYML().getString("CHAT-MUTED-TALK")));
			e.setCancelled(true);
			return;
		}

		switch (chatChannel) {
		case PUBLIC: {
			String factionTag = playerFaction == null ? "&c*" : "&c" + playerFaction.getName();
			e.setFormat(Utils.chat("&6[" + factionTag + "&6] " + rankName + p.getName() + "&7: &f") + message);
			break;
		}
		case FACTION: {
			String formatted = String.format(chatChannel.getRawFormat(p), p.getDisplayName(), message);
			e.setCancelled(true);
			playerFaction.getOnlinePlayers().forEach(player -> player.sendMessage(formatted));
			Bukkit.getConsoleSender().sendMessage(formatted);
			break;
		}
		case ALLIANCE: {
			String formatted = String.format(chatChannel.getRawFormat(p), p.getDisplayName(), message);
			Set<Player> recipients = playerFaction.getOnlinePlayers();
			playerFaction.getAlliedFactions().forEach(af -> recipients.addAll(af.getOnlinePlayers()));
			e.setCancelled(true);
			recipients.forEach(player -> player.sendMessage(formatted));
			Bukkit.getConsoleSender().sendMessage(formatted);
			break;
		}
		}
	}

}

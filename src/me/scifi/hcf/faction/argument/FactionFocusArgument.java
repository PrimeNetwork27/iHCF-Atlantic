package me.scifi.hcf.faction.argument;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.doctordark.util.CC;
import com.doctordark.util.command.CommandArgument;

import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.type.Faction;
import me.scifi.hcf.faction.type.PlayerFaction;

public class FactionFocusArgument extends CommandArgument { // Automatic Unfocus 2 hours later?
	private final HCF plugin;

	public FactionFocusArgument(HCF plugin) {
		super("focus", "Focus on a player or argument", new String[] { "unfocus" });
		this.plugin = plugin;
	}

	@Override
	public String getUsage(String s) {
		return CC.translate("&cUsage: /f focus <player | faction>");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-ONLY"));
			return true;
		}

		Player player = (Player) sender;

		if (args.length < 2) {
			sender.sendMessage(getUsage(commandLabel));
			return true;
		}

		PlayerFaction faction = plugin.getManagerHandler().getFactionManager().getPlayerFaction(player);
		if (faction == null) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-NOT-FACTION"));
			return true;
		}

		String name = args[1];

		Player targetPlayer = plugin.getServer().getPlayer(name);

		if (targetPlayer == null) {
			Faction targetFaction = plugin.getManagerHandler().getFactionManager().getFaction(name);

			if (!(targetFaction instanceof PlayerFaction)) {
				sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-FOCUS-PLAYERFACTION")
						.replace("{name}", targetFaction.getName()));
				// faction isn't player faction
				return true;
			}

			handleFactionFocus(sender, faction, (PlayerFaction) targetFaction);
			return true;
		}
		PlayerFaction targetFaction = plugin.getManagerHandler().getFactionManager().getPlayerFaction(targetPlayer);
		if (targetFaction == null) {
			return true;
		}

		if (!(targetFaction instanceof PlayerFaction)) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-FOCUS-PLAYERFACTION").replace("{name}",
					targetFaction.getName()));
			// faction isn't player faction
			return true;
		}
		handleFactionFocus(sender, faction, targetFaction);
		return true;
	}

	private void handleFactionFocus(CommandSender sender, PlayerFaction current, PlayerFaction target) {
		if (current.isFocused(target.getUniqueID())) {
			current.setTarget(null);
			current.broadcast(plugin.getMessagesYML().getString("FACTION-PLAYER-UNFOCUS-HANDLE")
					.replace("{player}", sender.getName()).replace("{focusedFaction}", target.getName()));
			return;
		}

		current.setTarget(target);
		current.broadcast(plugin.getMessagesYML().getString("FACTION-PLAYER-FOCUS-HANDLE")
				.replace("{player}", sender.getName()).replace("{focusedFaction}", target.getName()));
	}

}

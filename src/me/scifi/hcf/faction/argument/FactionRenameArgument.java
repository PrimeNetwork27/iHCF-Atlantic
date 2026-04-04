package me.scifi.hcf.faction.argument;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.doctordark.util.JavaUtils;
import com.doctordark.util.command.CommandArgument;

import me.scifi.hcf.ConfigurationService;
import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.struct.Role;
import me.scifi.hcf.faction.type.PlayerFaction;

public class FactionRenameArgument extends CommandArgument {

	private static final long FACTION_RENAME_DELAY_MILLIS = TimeUnit.SECONDS.toMillis(15L);
	private static final String FACTION_RENAME_DELAY_WORDS = DurationFormatUtils
			.formatDurationWords(FACTION_RENAME_DELAY_MILLIS, true, true);

	private final HCF plugin;

	public FactionRenameArgument(HCF plugin) {
		super("rename", "Change the name of your faction.");
		this.plugin = plugin;
		this.aliases = new String[] { "changename", "setname" };
	}

	@Override
	public String getUsage(String label) {
		return '/' + label + ' ' + getName() + " <newFactionName>";
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-ONLY"));
			return true;
		}

		if (args.length < 2) {
			sender.sendMessage(ChatColor.RED + "Usage: " + getUsage(label));
			return true;
		}

		Player player = (Player) sender;
		PlayerFaction playerFaction = plugin.getManagerHandler().getFactionManager().getPlayerFaction(player);

		if (playerFaction == null) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-NOT-FACTION"));
			return true;
		}

		if (playerFaction.getMember(player.getUniqueId()).getRole() != Role.LEADER) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-RENAME-LEADER"));
			return true;
		}

		String newName = args[1];

		if (ConfigurationService.DISALLOWED_FACTION_NAMES.contains(newName.toLowerCase())) {
			sender.sendMessage(
					plugin.getMessagesYML().getString("FACTION-RENAME-DISALLOWED").replace("{name}", newName));
			return true;
		}

		if (newName.length() < ConfigurationService.FACTION_NAME_CHARACTERS_MIN) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-CREATE-SHORT").replace("{min}",
					String.valueOf(ConfigurationService.FACTION_NAME_CHARACTERS_MIN)));
			return true;
		}

		if (newName.length() > ConfigurationService.FACTION_NAME_CHARACTERS_MAX) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-CREATE-LONG").replace("{max}",
					String.valueOf(ConfigurationService.FACTION_NAME_CHARACTERS_MAX)));
			return true;
		}

		if (!JavaUtils.isAlphanumeric(newName)) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-CREATE-ALPHANUMERIC"));
			return true;
		}

		if (plugin.getManagerHandler().getFactionManager().getFaction(newName) != null) {
			sender.sendMessage(
					plugin.getMessagesYML().getString("FACTION-PLAYER-CREATE-EXISTS").replace("{name}", newName));
			return true;
		}

		long difference = (playerFaction.lastRenameMillis - System.currentTimeMillis()) + FACTION_RENAME_DELAY_MILLIS;

		if (!player.isOp() && difference > 0L) {

			player.sendMessage(plugin.getMessagesYML().getString("FACTION-RENAME-DELAY")
					.replace("{delay}", FACTION_RENAME_DELAY_WORDS)
					.replace("{difference}", DurationFormatUtils.formatDurationWords(difference, true, true)));

			return true;
		}

		playerFaction.setName(args[1], sender);
		return true;
	}
}

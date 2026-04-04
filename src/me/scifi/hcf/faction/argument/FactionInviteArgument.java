package me.scifi.hcf.faction.argument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.doctordark.util.chat.ClickAction;
import com.doctordark.util.chat.Text;
import com.doctordark.util.command.CommandArgument;

import me.scifi.hcf.ConfigurationService;
import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.struct.Relation;
import me.scifi.hcf.faction.struct.Role;
import me.scifi.hcf.faction.type.Faction;
import me.scifi.hcf.faction.type.PlayerFaction;

/**
 * Faction argument used to invite players into {@link Faction}s.
 */
public class FactionInviteArgument extends CommandArgument {

	private static final Pattern USERNAME_REGEX = Pattern.compile("^[a-zA-Z0-9_]{2,16}$");

	private final HCF plugin;

	public FactionInviteArgument(HCF plugin) {
		super("invite", "Invite a player to the faction.");
		this.plugin = plugin;
		this.aliases = new String[] { "inv", "invitemember", "inviteplayer" };
	}

	@Override
	public String getUsage(String label) {
		return '/' + label + ' ' + getName() + " <playerName>";
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

		if (!USERNAME_REGEX.matcher(args[1]).matches()) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-INVITE-USERNAME").replace("{name}", args[1]));
			return true;
		}

		Player player = (Player) sender;
		PlayerFaction playerFaction = plugin.getManagerHandler().getFactionManager().getPlayerFaction(player);

		if (playerFaction == null) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-NOT-FACTION"));
			return true;
		}

		if (playerFaction.getMember(player.getUniqueId()).getRole() == Role.MEMBER) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-INVITE-OFFICER"));
			return true;
		}

		Set<String> invitedPlayerNames = playerFaction.getInvitedPlayerNames();
		String name = args[1];

		if (playerFaction.getMember(name) != null) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-INVITE-ALREADY").replace("{name}", name));
			return true;
		}

		if (!ConfigurationService.KIT_MAP && !plugin.getManagerHandler().getEotwHandler().isEndOfTheWorld()
				&& playerFaction.isRaidable()) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-INVITE-RAIDABLE"));
			return true;
		}

		if (!invitedPlayerNames.add(name)) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-INVITE-ALREADY-INVITED"));
			return true;
		}

		Player target = Bukkit.getPlayer(name);
		if (target != null) {
			name = target.getName(); // fix casing.
			Text text = new Text(sender.getName()).setColor(Relation.ENEMY.toChatColour())
					.append(new Text(" has invited you to join ").setColor(ChatColor.YELLOW));
			text.append(new Text(playerFaction.getName()).setColor(Relation.ENEMY.toChatColour()))
					.append(new Text(". ").setColor(ChatColor.YELLOW));
			text.append(new Text("Click here").setColor(ChatColor.GREEN)
					.setClick(ClickAction.RUN_COMMAND, '/' + label + " accept " + playerFaction.getName())
					.setHoverText(ChatColor.AQUA + "Click to join " + playerFaction.getDisplayName(target)
							+ ChatColor.AQUA + '.'))
					.append(new Text(" to accept this invitation.").setColor(ChatColor.YELLOW));
			text.send(target);
		}
		playerFaction.broadcast(plugin.getMessagesYML().getString("FACTION-INVITE-BROADCAST")
				.replace("{sender}", Relation.MEMBER.toChatColour() + sender.getName())
				.replace("{target}", Relation.ENEMY.toChatColour() + name));
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (args.length != 2 || !(sender instanceof Player)) {
			return Collections.emptyList();
		}

		Player player = (Player) sender;
		PlayerFaction playerFaction = plugin.getManagerHandler().getFactionManager().getPlayerFaction(player);
		if (playerFaction == null || (playerFaction.getMember(player.getUniqueId()).getRole() == Role.MEMBER)) {
			return Collections.emptyList();
		}

		List<String> results = new ArrayList<>();
		for (Player target : Bukkit.getServer().getOnlinePlayers()) {
			if (player.canSee(target) && !results.contains(target.getName())) {
				if (playerFaction != plugin.getManagerHandler().getFactionManager()
						.getPlayerFaction(target.getUniqueId())) {
					results.add(target.getName());
				}
			}
		}

		return results;
	}
}

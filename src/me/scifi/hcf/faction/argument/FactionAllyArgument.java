package me.scifi.hcf.faction.argument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.doctordark.util.command.CommandArgument;

import me.scifi.hcf.ConfigurationService;
import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.event.FactionRelationCreateEvent;
import me.scifi.hcf.faction.struct.Relation;
import me.scifi.hcf.faction.struct.Role;
import me.scifi.hcf.faction.type.Faction;
import me.scifi.hcf.faction.type.PlayerFaction;

/**
 * Faction argument used to request or accept ally {@link Relation} invitations
 * from a {@link Faction}.
 */
public class FactionAllyArgument extends CommandArgument {

	private static final Relation RELATION = Relation.ALLY;

	private final HCF plugin;

	public FactionAllyArgument(HCF plugin) {
		super("ally", "Make an ally pact with other factions.", new String[] { "alliance" });
		this.plugin = plugin;
	}

	@Override
	public String getUsage(String label) {
		return '/' + label + ' ' + getName() + " <factionName>";
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-ONLY"));
			return true;
		}

		if (ConfigurationService.MAX_ALLIES_PER_FACTION <= 0) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-ALLY-DISABLED"));
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

		if (playerFaction.getMember(player.getUniqueId()).getRole() == Role.MEMBER) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-ALLY-MUST-OFFICER"));
			return true;
		}

		Faction containingFaction = plugin.getManagerHandler().getFactionManager().getContainingFaction(args[1]);

		if (!(containingFaction instanceof PlayerFaction)) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-NOT-FOUND1").replace("{1}", args[1]));
			return true;
		}

		PlayerFaction targetFaction = (PlayerFaction) containingFaction;

		if (playerFaction == targetFaction) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-ALLY-REQUEST-OWN").replace("{displayName}",
					RELATION.getDisplayName()));
			return true;
		}

		Collection<UUID> allied = playerFaction.getAllied();

		if (allied.size() >= ConfigurationService.MAX_ALLIES_PER_FACTION) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-ALLY-REACHED").replace("{limit}",
					String.valueOf(ConfigurationService.MAX_ALLIES_PER_FACTION)));
			return true;
		}

		if (targetFaction.getAllied().size() >= ConfigurationService.MAX_ALLIES_PER_FACTION) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-ALLY-TARGET-REACHED")
					.replace("{limit}", String.valueOf(ConfigurationService.MAX_ALLIES_PER_FACTION))
					.replace("{displayName}", targetFaction.getDisplayName(sender)));
			return true;
		}

		if (allied.contains(targetFaction.getUniqueID())) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-ALLY-TARGET-ALREADY")
					.replace("{name}", RELATION.getDisplayName())
					.replace("{displayName}", targetFaction.getDisplayName(playerFaction)));
			return true;
		}

		// Their faction has already requested us, lets' accept.
		if (targetFaction.getRequestedRelations().remove(playerFaction.getUniqueID()) != null) {
			FactionRelationCreateEvent event = new FactionRelationCreateEvent(playerFaction, targetFaction, RELATION);
			Bukkit.getPluginManager().callEvent(event);

			targetFaction.getRelations().put(playerFaction.getUniqueID(), RELATION);
			targetFaction.broadcast(plugin.getMessagesYML().getString("FACTION-ALLY-REQUEST-ACEPTED")
					.replace("{relation}", RELATION.getDisplayName())
					.replace("{displayName}", playerFaction.getDisplayName(targetFaction)));

			playerFaction.getRelations().put(targetFaction.getUniqueID(), RELATION);
			playerFaction.broadcast(plugin.getMessagesYML().getString("FACTION-ALLY-REQUEST-ACEPTED")
					.replace("{relation}", RELATION.getDisplayName())
					.replace("{displayName}", targetFaction.getDisplayName(playerFaction)));
			return true;
		}

		if (playerFaction.getRequestedRelations().putIfAbsent(targetFaction.getUniqueID(), RELATION) != null) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-ALLY-WISH-INFORM")
					.replace("{relation", RELATION.getDisplayName())
					.replace("{displayName}", targetFaction.getDisplayName(playerFaction)));
			return true;
		}

		// Handle the request.
		playerFaction.broadcast(plugin.getMessagesYML().getString("FACTION-ALLY-WISH-HANDLE")
				.replace("{target}", playerFaction.getDisplayName(targetFaction))
				.replace("{allied}", RELATION.getDisplayName()));
		targetFaction.broadcast(plugin.getMessagesYML().getString("FACTION-ALLY-WISH-GETTING")
				.replace("{requesting}", playerFaction.getDisplayName(targetFaction))
				.replace("{name}", playerFaction.getName()));
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (args.length != 2 || !(sender instanceof Player)) {
			return Collections.emptyList();
		}

		Player player = (Player) sender;
		PlayerFaction playerFaction = plugin.getManagerHandler().getFactionManager().getPlayerFaction(player);
		if (playerFaction == null) {
			return Collections.emptyList();
		}

		List<String> results = new ArrayList<>();
		for (Player target : Bukkit.getServer().getOnlinePlayers()) {
			if (!target.equals(player) && player.canSee(target) && !results.contains(target.getName())) {
				Faction targetFaction = plugin.getManagerHandler().getFactionManager().getPlayerFaction(target);
				if (targetFaction != null && playerFaction != targetFaction) {
					if (playerFaction.getRequestedRelations().get(targetFaction.getUniqueID()) != RELATION
							&& playerFaction.getRelations().get(targetFaction.getUniqueID()) != RELATION) {
						results.add(targetFaction.getName());
					}
				}
			}
		}

		return results;
	}
}

package me.scifi.hcf.faction.argument;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.doctordark.util.command.CommandArgument;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import me.scifi.hcf.ConfigurationService;
import me.scifi.hcf.HCF;
import me.scifi.hcf.faction.event.FactionRelationRemoveEvent;
import me.scifi.hcf.faction.struct.Relation;
import me.scifi.hcf.faction.struct.Role;
import me.scifi.hcf.faction.type.Faction;
import me.scifi.hcf.faction.type.PlayerFaction;

public class FactionUnallyArgument extends CommandArgument {

	private final HCF plugin;

	public FactionUnallyArgument(HCF plugin) {
		super("unally", "Remove an ally pact with other factions.");
		this.plugin = plugin;
		this.aliases = new String[] { "unalliance", "neutral" };
	}

	@Override
	public String getUsage(String label) {
		return '/' + label + ' ' + getName() + " <all|factionName>";
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-PLAYER-ONLY"));
			return true;
		}

		if (ConfigurationService.MAX_ALLIES_PER_FACTION <= 0) {
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-ALLY-DISALLOWED"));
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
			sender.sendMessage(plugin.getMessagesYML().getString("FACTION-ALLY-MUST"));
			return true;
		}

		Relation relation = Relation.ALLY;
		Collection<PlayerFaction> targetFactions = new HashSet<>();

		if (args[1].equalsIgnoreCase("all")) {
			Collection<PlayerFaction> allies = playerFaction.getAlliedFactions();
			if (allies.isEmpty()) {
				sender.sendMessage(plugin.getMessagesYML().getString("FACTION-ALLY-NONE"));
				return true;
			}

			targetFactions.addAll(allies);
		} else {
			Faction searchedFaction = plugin.getManagerHandler().getFactionManager().getContainingFaction(args[1]);

			if (!(searchedFaction instanceof PlayerFaction)) {
				sender.sendMessage(
						plugin.getMessagesYML().getString("FACTION-PLAYER-NOT-FOUND1").replace("{1}", args[1]));
				return true;
			}

			targetFactions.add((PlayerFaction) searchedFaction);
		}

		for (PlayerFaction targetFaction : targetFactions) {
			if (playerFaction.getRelations().remove(targetFaction.getUniqueID()) == null
					|| targetFaction.getRelations().remove(playerFaction.getUniqueID()) == null) {
				sender.sendMessage(plugin.getMessagesYML().getString("FACTION-ALLY-NOT")
						.replace("{allied}", relation.getDisplayName())
						.replace("{target}", targetFaction.getDisplayName(playerFaction)));
				return true;
			}

			FactionRelationRemoveEvent event = new FactionRelationRemoveEvent(playerFaction, targetFaction,
					Relation.ALLY);
			Bukkit.getPluginManager().callEvent(event);

			if (event.isCancelled()) {
				sender.sendMessage("Could not drop {allied} with {target}.");
				sender.sendMessage(plugin.getMessagesYML().getString("FACTION-ALLY-CANCELLED")
						.replace("{allied}", relation.getDisplayName())
						.replace("{target}", targetFaction.getDisplayName(playerFaction)));
				return true;
			}

			// Inform the affected factions.
			playerFaction.broadcast(plugin.getMessagesYML().getString("FACTION-ALLY-BROADCAST")
					.replace("{allied}", relation.getDisplayName())
					.replace("{target}", targetFaction.getDisplayName(playerFaction)));

			playerFaction.broadcast(plugin.getMessagesYML().getString("FACTION-ALLY-TARGETBROADCAST")
					.replace("{allied}", relation.getDisplayName())
					.replace("{ally}", playerFaction.getDisplayName(targetFaction)));

		}

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

		return Lists.newArrayList(Iterables.concat(COMPLETIONS,
				playerFaction.getAlliedFactions().stream().map(Faction::getName).collect(Collectors.toList())));
	}

	private static final ImmutableList<String> COMPLETIONS = ImmutableList.of("all");
}

package me.scifi.hcf.features.abilities;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.doctordark.util.CC;
import com.doctordark.util.PlayerUtil;

public class AbilityCommand implements CommandExecutor {

	private final String[] USAGE = { "&7Abilities Commands:", "&7/Ability list", "&7/Ability get (name)",
			"&7/Ability give (name) (target) (amount)" };

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}

		Player player = (Player) sender;

		if (!player.hasPermission("hcf.command.ability")) {
			player.sendMessage("Unknown command. Type \"/help\" for help.");
			return true;
		}

		if (args.length == 0) {
			sendUsage(player);
			return true;
		}

		if (args[0].equalsIgnoreCase("list")) {
			player.sendMessage(CC.translate("&7Abilities disponibles:"));
			for (Ability ability : Ability.getAbilities().values()) {
				player.sendMessage(CC.translate("&7- &e" + ability.getName()));
			}
			return true;
		}

		if (args[0].equalsIgnoreCase("get")) {
			if (args.length < 2) {
				sendUsage(player);
				return true;
			}

			Ability ability = getAbility(args[1]);
			if (ability == null) {
				player.sendMessage(CC.translate("&cAbility &e" + args[1] + "&c not found."));
				return true;
			}

			PlayerUtil.addItemsOrDrop(player, ability.getItem());
			player.sendMessage(CC.translate("&aYou received the ability &e" + ability.getName()));

		} else if (args[0].equalsIgnoreCase("give")) {
			if (args.length < 4) {
				sendUsage(player);
				return true;
			}

			Ability ability = getAbility(args[1]);
			if (ability == null) {
				player.sendMessage(CC.translate("&cAbility &e" + args[1] + "&c not found."));
				return true;
			}

			Player target = Bukkit.getPlayer(args[2]);
			if (target == null) {
				player.sendMessage(CC.translate("&cPlayer &e" + args[2] + "&c not found."));
				return true;
			}

			int amount;
			try {
				amount = Integer.parseInt(args[3]);
			} catch (NumberFormatException e) {
				player.sendMessage(CC.translate("&cAmount must be a number."));
				return true;
			}

			for (int i = 0; i < amount; i++) {
				target.getInventory().addItem(ability.getItem());
			}

			target.sendMessage(CC.translate("&aYou have received the ability &e" + ability.getName()));
			player.sendMessage(CC.translate("&aYou have given the ability &e" + ability.getName() + " &7(x" + amount
					+ ")&a to &e" + target.getName()));

		} else {
			sendUsage(player);
		}

		return true;
	}

	private Ability getAbility(String name) {
		Ability direct = Ability.getAbilities().get(name);
		if (direct != null) {
			return direct;
		}

		for (Ability ability : Ability.getAbilities().values()) {
			if (ability.getName().equalsIgnoreCase(name)) {
				return ability;
			}
		}
		return null;
	}

	private void sendUsage(Player player) {
		for (String line : USAGE) {
			player.sendMessage(CC.translate(line));
		}
	}
}
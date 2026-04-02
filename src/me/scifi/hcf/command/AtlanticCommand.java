package me.scifi.hcf.command;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class AtlanticCommand implements CommandExecutor, TabCompleter { // TODO: Save Data, Reload Data, etc.

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
}

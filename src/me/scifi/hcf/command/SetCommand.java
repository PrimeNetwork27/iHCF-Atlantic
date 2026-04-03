package me.scifi.hcf.command;

import java.util.List;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.scifi.hcf.HCF;
import me.scifi.hcf.Utils;

public class SetCommand implements CommandExecutor {

	private HCF plugin;

	public SetCommand(HCF plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("set")) {
			if (sender instanceof Player) {
				Player p = (Player) sender;
				if (p.hasPermission(command.getPermission())) {
					if (args.length == 0) {
						List<String> setHelp = Utils.list(plugin.getMessagesYML().getStringList("SET-HELP"));
						setHelp.forEach(p::sendMessage);
						return true;
					}
					if (args.length == 1) {
						if (args[0].equalsIgnoreCase("endexit")) {
							if (p.getLocation().getWorld().getEnvironment() == World.Environment.NORMAL) {
								plugin.getConfig().set("ENDEXIT.X", p.getLocation().getBlockX());
								plugin.getConfig().set("ENDEXIT.Y", p.getLocation().getBlockY());
								plugin.getConfig().set("ENDEXIT.Z", p.getLocation().getBlockZ());
								plugin.getConfig().save();
								return true;
							} else {
								p.sendMessage(Utils.chat("&cEnd Exit Can Only Be Set In The Overworld."));
								return true;
							}
						} else if (args[0].equalsIgnoreCase("endspawn")) {
							if (p.getLocation().getWorld().getEnvironment() == World.Environment.THE_END) {
								World world = p.getWorld();
								int setX = p.getLocation().getBlockX();
								int setY = p.getLocation().getBlockY();
								int setZ = p.getLocation().getBlockZ();
								plugin.getConfig().set("ENDSPAWN.X", setX);
								plugin.getConfig().set("ENDSPAWN.Y", setY);
								plugin.getConfig().set("ENDSPAWN.Z", setZ);
								plugin.getConfig().save();
								world.setSpawnLocation(setX, setY, setZ);
								return true;
							} else {
								p.sendMessage(Utils.chat("&cEnd Spawn Can Only Be Set In The End."));
								return true;
							}
						} else if (args[0].equalsIgnoreCase("endportal")) {
							int setX = p.getLocation().getBlockX();
							int setY = p.getLocation().getBlockY();
							int setZ = p.getLocation().getBlockZ();
							plugin.getConfig().set("ENDPORTAL.X", setX);
							plugin.getConfig().set("ENDPORTAL.Y", setY);
							plugin.getConfig().set("ENDPORTAL.Z", setZ);
							plugin.getConfig().save();
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}

package me.scifi.hcf.features.staffmode;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.scifi.hcf.HCF;
import me.scifi.hcf.Utils;

public class StaffModeCommand implements CommandExecutor {

	private HCF plugin;

	public StaffModeCommand(HCF plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("staffmode")) {
			if (sender instanceof Player) {
				Player p = (Player) sender;
				if (p.hasPermission(command.getPermission())) {
					if (plugin.getStaffModeManager().getStaffMode().contains(p.getUniqueId())) {
						plugin.getStaffModeManager().removeFromStaffMode(p);
						p.sendMessage(Utils.chat(plugin.getMessagesYML().getString("STAFFMODE-DISABLED")));
						return true;
					} else {
						plugin.getStaffModeManager().putInStaffMode(p);
						p.sendMessage(Utils.chat(plugin.getMessagesYML().getString("STAFFMODE-ENABLED")));
						return true;
					}
				}
			}
		}
		return false;
	}
}

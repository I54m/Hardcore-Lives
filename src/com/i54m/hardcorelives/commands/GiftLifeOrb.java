package com.i54m.hardcorelives.commands;

import com.i54m.hardcorelives.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiftLifeOrb implements CommandExecutor {

    private final Main plugin = Main.getInstance();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (s.equalsIgnoreCase("giftlifeorb")) {
            if (!commandSender.hasPermission("hardcorelives.giftlifeorb")) {
                commandSender.sendMessage(ChatColor.RED + "You do not have permission to do this!");
                return true;
            }
            if (strings.length < 2) {
                commandSender.sendMessage(ChatColor.RED + "Incorrect Usage: ");
                commandSender.sendMessage(ChatColor.WHITE + "/giftlifeorb <player> <amount>");
                return true;
            } else {
                final Player target = Bukkit.getPlayer(strings[0]);
                if (target != null && target.isOnline()) {
                    int items = 1;
                    try {
                        items = Integer.parseInt(strings[1]);
                    } catch (NumberFormatException nfe) {
                        commandSender.sendMessage(ChatColor.RED + strings[1] + " is not a valid number!");
                        return true;
                    }
                    ItemStack item = plugin.getLifeItem().clone();
                    item.setAmount(items);
                    target.getInventory().addItem(item);
                    target.updateInventory();
                } else {
                    commandSender.sendMessage(ChatColor.RED + strings[0] + " is not an online player's name!");
                    return true;
                }
            }
            return true;
        }
        return false;
    }
}

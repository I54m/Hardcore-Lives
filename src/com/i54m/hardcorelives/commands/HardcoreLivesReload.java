package com.i54m.hardcorelives.commands;

import com.i54m.hardcorelives.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class HardcoreLivesReload implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (s.equalsIgnoreCase("hardcorelivesreload")) {
            Main.getInstance().reloadConfig();
            Main.getInstance().loadVariables();
            commandSender.sendMessage(ChatColor.GREEN + "Config reloaded!");
            return true;
        }
        return false;
    }
}

package com.i54m.hardcorelives.handlers;

import com.i54m.hardcorelives.Main;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ErrorHandler {

    @Getter
    private static final ErrorHandler INSTANCE = new ErrorHandler();
    private Main plugin = Main.getInstance();
    private Throwable previousException = null;
    private ErrorHandler() {
    }

    public void log(Throwable e) {
        if (plugin == null) plugin = Main.getInstance();
        if (isExceptionCausedByPlugin(e)) {
            plugin.getLogger().warning(" ");
            plugin.getLogger().warning(ChatColor.RED + "An error was encountered!");
            plugin.getLogger().warning(ChatColor.RED + "Error Type: " + e.getClass().getName());
            plugin.getLogger().warning(ChatColor.RED + "Error Message: " + e.getMessage());
            StringBuilder stacktrace = new StringBuilder();
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                stacktrace.append(stackTraceElement.toString()).append("\n");
            }
            plugin.getLogger().warning(ChatColor.RED + "Stack Trace: " + stacktrace.toString());
            plugin.getLogger().warning(" ");
        }
    }


    private void detailedAlert(Throwable e, CommandSender sender) {
        if (!isExceptionCausedByPlugin(e))
            return;
        if (e.getMessage().equals(previousException.getMessage()))
            return;
        else
            previousException = e;
        sender.sendMessage(ChatColor.DARK_RED + "ERROR: " + ChatColor.RED + e.getMessage());
        sender.sendMessage(ChatColor.RED + "This error will be logged! Please inform a dev asap!");
    }

    public void alert(Throwable e, CommandSender sender) {
        if (!isExceptionCausedByPlugin(e))
            return;
        if (e.getMessage().equals(previousException.getMessage()))
            return;
        else
            previousException = e;
        if (sender.isOp()) detailedAlert(e, sender);
        else {
            sender.sendMessage(ChatColor.DARK_RED + "ERROR: " + ChatColor.RED + "An unexpected error occurred while trying to perform that action!");
            sender.sendMessage(ChatColor.RED + "This error will be logged! Please inform a dev asap!");
        }
    }

    public void loginError(AsyncPlayerPreLoginEvent event) {
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "ERROR: An error occurred during your login process and we were unable to fetch required data.\n Please inform an admin+ asap!");
    }

    public void loginError(PlayerJoinEvent event) {
        event.getPlayer().kickPlayer(ChatColor.RED + "ERROR: An error occurred during your login process and we were unable to fetch required data.\n Please inform an admin+ asap!");
    }

    public void loginError(PlayerLoginEvent event) {
        event.getPlayer().kickPlayer(ChatColor.RED + "ERROR: An error occurred during your login process and we were unable to fetch required data.\n Please inform an admin+ asap!");
    }

    public boolean isExceptionCausedByPlugin(final Throwable e) {
        final List<StackTraceElement> all = getEverything(e, new ArrayList<>());
        for (final StackTraceElement element : all) {
            if (element.getClassName().toLowerCase().contains("com.i54m"))
                return true;
        }
        return false;
    }

    private List<StackTraceElement> getEverything(final Throwable e, List<StackTraceElement> objects) {
        if (e.getCause() != null)
            objects = getEverything(e.getCause(), objects);
        objects.addAll(Arrays.asList(e.getStackTrace()));
        return objects;
    }

}

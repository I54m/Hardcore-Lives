package com.i54m.hardcorelives;

import com.i54m.hardcorelives.managers.PlayerDataManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class HardcoreLivesPlaceholder extends PlaceholderExpansion {

    private final Main plugin = Main.getInstance();
    private final PlayerDataManager playerDataManager = PlayerDataManager.getINSTANCE();

    @Override
    public String onPlaceholderRequest(Player p, String value) {
        if (value.equals("lives"))
            return String.valueOf(playerDataManager.getPlayerData(p, false).getInt("lives"));
        else if (value.equals("deaths"))
            return String.valueOf(playerDataManager.getPlayerData(p, false).getInt("deaths"));
        else
            return null;
    }

    @Override
    public String getIdentifier() {
        return "hardcorelives";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }
}

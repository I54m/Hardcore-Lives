package com.i54m.hardcorelives.listeners;

import com.i54m.hardcorelives.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlace implements Listener {

    private final Main plugin = Main.getInstance();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() == plugin.getLifeItem().getType()) {
            if (event.getItemInHand().isSimilar(plugin.getLifeItem())) {
                event.setCancelled(true);
            }
        }
    }
}

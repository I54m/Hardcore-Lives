package com.i54m.hardcorelives.listeners;

import com.i54m.hardcorelives.Main;
import com.i54m.hardcorelives.managers.PlayerDataManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerInteract implements Listener {

    private final Main plugin = Main.getInstance();
    private final PlayerDataManager playerDataManager = PlayerDataManager.getINSTANCE();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event){
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack usedItem;
        if (event.getHand() == EquipmentSlot.HAND)
            usedItem = player.getInventory().getItemInMainHand().clone();
        else if (event.getHand() == EquipmentSlot.OFF_HAND)
            usedItem = player.getInventory().getItemInOffHand().clone();
        else return;

        if (usedItem.isSimilar(plugin.getLifeItem())){
            event.setCancelled(true);
            usedItem.setAmount(usedItem.getAmount() - 1);
            if (event.getHand() == EquipmentSlot.HAND)
                player.getInventory().setItemInMainHand(usedItem);
            else if (event.getHand() == EquipmentSlot.OFF_HAND)
                player.getInventory().setItemInOffHand(usedItem);
            player.updateInventory();
            FileConfiguration playerData = playerDataManager.getPlayerData(player.getUniqueId(), false);
            int currentLives = playerData.getInt("lives");
            currentLives++;
            playerData.set("lives", currentLives);
            playerDataManager.savePlayerData(player.getUniqueId());
            String lifeItemName = ChatColor.translateAlternateColorCodes('&', plugin.getLifeItem().getItemMeta().getDisplayName());
            player.sendMessage(ChatColor.GREEN + "You claimed a " + lifeItemName + ChatColor.GREEN + ". You now have: " + currentLives + " lives remaining!");
        }
    }
}

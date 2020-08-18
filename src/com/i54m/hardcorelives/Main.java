package com.i54m.hardcorelives;

import com.i54m.hardcorelives.commands.*;
import com.i54m.hardcorelives.listeners.*;
import com.i54m.hardcorelives.managers.PlayerDataManager;
import com.i54m.hardcorelives.managers.WorkerManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main extends JavaPlugin {

    @Setter(AccessLevel.PRIVATE)
    @Getter
    private static Main instance;
    private final WorkerManager workerManager = WorkerManager.getINSTANCE();
    private final PlayerDataManager playerDataManager = PlayerDataManager.getINSTANCE();
    @Getter(AccessLevel.PUBLIC)
    private final ItemStack Book = new ItemStack(Material.WRITTEN_BOOK, 1);
    @Getter
    private int DefaultLives;
    @Getter
    private Location DeathRespawn;
    @Getter
    private int DeathParticleAmount;
    @Getter
    private int RespawnParticleAmount;
    @Getter
    private ItemStack LifeItem;
    private int maxDistance;

    @Override
    public void onLoad() {
        setInstance(this);
    }

    @Override
    public void onEnable() {
        if (getInstance() == null) setInstance(this);
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) new HardcoreLivesPlaceholder().register();
        else getLogger().warning("Could not find PlaceholderAPI! This plugin is required for placeholders to work!");
        //setup config and config variables
        getConfig().options().copyHeader(true).copyDefaults(true);
        saveDefaultConfig();
        reloadConfig();
        loadVariables();
        //register commands
        Bukkit.getPluginCommand("addlives").setExecutor(new AddLives());
        Bukkit.getPluginCommand("convertlives").setExecutor(new ConvertLives());
        Bukkit.getPluginCommand("giftlives").setExecutor(new GiftLives());
        Bukkit.getPluginCommand("giftlifeorb").setExecutor(new GiftLifeOrb());
        Bukkit.getPluginCommand("resetdeaths").setExecutor(new ResetDeaths());
        Bukkit.getPluginCommand("hardcorelivesreload").setExecutor(new HardcoreLivesReload());
        Bukkit.getPluginCommand("purchaselives").setExecutor(new PurchaseLives());
        Bukkit.getPluginCommand("removelives").setExecutor(new RemoveLives());
        Bukkit.getPluginCommand("setlives").setExecutor(new SetLives());
        //register events
        Bukkit.getPluginManager().registerEvents(new BlockPlace(), this);
        Bukkit.getPluginManager().registerEvents(new EntityDamage(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDeath(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerInteract(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerRespawn(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerTeleport(), this);

        workerManager.start();
        playerDataManager.start();
    }

    @Override
    public void onDisable() {
        if (workerManager.isStarted())
            workerManager.stop();
        if (playerDataManager.isStarted())
            playerDataManager.stop();
    }

    public void loadVariables() {
        try {
            DefaultLives = getConfig().getInt("default-lives", 2);
            String worldString = getConfig().getString("death-respawn.world");
            if (worldString == null) {
                getLogger().warning("Could not find " + worldString + " world! Using world \"world\" as death world!");
                worldString = "world";
            }
            World world = getServer().getWorld(worldString);
            if (world == null) {
                getLogger().warning("Could not find " + worldString + " world! Using world 0 as death world!");
                world = getServer().getWorlds().get(0);
            }
            DeathRespawn = new Location(
                    world,
                    getConfig().getDouble("death-respawn.x", 0.0),
                    getConfig().getDouble("death-respawn.y", 60.0),
                    getConfig().getDouble("death-respawn.z", 0.0),
                    (float) getConfig().getDouble("death-respawn.yaw", 0.0),
                    (float) getConfig().getDouble("death-respawn.pitch", 0.0));
            DeathParticleAmount = getConfig().getInt("death-particle-amount", 25);
            RespawnParticleAmount = getConfig().getInt("respawn-particle-amount", 25);
            maxDistance = getConfig().getInt("respawn-max-distance", 30000);
            LifeItem = new ItemStack(Material.valueOf(getConfig().getString("life-item.Material", "CONDUIT")), 1);
            ItemMeta lifeItemMeta = LifeItem.getItemMeta();
            lifeItemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', getConfig().getString("life-item.Name", "&6Life Orb")));
            List<String> lore = new ArrayList<>();
            getConfig().getStringList("life-item.Lore").forEach((loreItem) -> lore.add(ChatColor.translateAlternateColorCodes('&', loreItem)));
            lifeItemMeta.setLore(lore);
            if (getConfig().getBoolean("life-item.Enchanted")) {
                LifeItem.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                lifeItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            LifeItem.setItemMeta(lifeItemMeta);
            if (getConfig().getBoolean("life-item.Craftable") && Bukkit.getRecipe(new NamespacedKey(this, "life_item_key")) == null)
                setupRecipe();
            else if (!getConfig().getBoolean("life-item.Craftable") && Bukkit.getRecipe(new NamespacedKey(this, "life_item_key")) != null)
                Bukkit.removeRecipe(new NamespacedKey(this, "life_item_key"));
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        //setup info book meta
        BookMeta meta = (BookMeta) Book.getItemMeta();
        meta.setAuthor("Vorplex");
        meta.setTitle("Hardcore Survival Intro");
        meta.addPage("Welcome to Hardcore Survival!\n\nThis book contains some basic starting info that you should know!");
        meta.addPage("- If you have a life left you will respawn back at a bed or respawn anchor when you die!\n- Lives can be gifted through /giftlives or brought on our store!");
        meta.addPage("You may also craft lives with a nether star and 4 Netherite Ingots arranged in a cross shape with blaze rods in the corners.");
        meta.addPage(LifeItem.getItemMeta().getDisplayName() + ChatColor.RESET + " Ingredients List:\n- Blaze Rods: 4\n- Netherite Ingots: 4\n- Nether Star: 1");
        Book.setItemMeta(meta);
    }

    public void respawn(Player target, FileConfiguration targetData) {
        target.sendMessage(ChatColor.GREEN + "Respawning you in 5s...");
        target.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 10 * 20, 1));
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (target.isOnline()) {
                target.sendMessage(ChatColor.GREEN + "Respawning you now....");
                targetData.set("alive", true);
                playerDataManager.savePlayerData(target.getUniqueId());
                if (target.getBedSpawnLocation() != null) {
                    //found bed/anchor to respawn you at
                    target.sendMessage(ChatColor.GREEN + "Found a bed/charged respawn anchor to respawn you at!");
                    target.teleport(target.getBedSpawnLocation());
                } else {
                    //missing bed/anchor to respawn you at
                    target.sendMessage(ChatColor.RED + "Could not find a bed/charged respawn anchor to respawn you at!");
                    target.teleport(getRandomLocation());
                }
                target.playSound(target.getLocation(), Sound.ITEM_TOTEM_USE, 1, 1);
                target.spawnParticle(Particle.TOTEM, target.getLocation(), getRespawnParticleAmount());
                EntityDamage.setInvincible(target, 10);
            }
        }, 5 * 20);
    }


    private void setupRecipe() {
        NamespacedKey lifeItemKey = new NamespacedKey(this, "life_item_key");
        ShapedRecipe lifeItemRecipe = new ShapedRecipe(lifeItemKey, LifeItem);
        lifeItemRecipe.shape("BIB", "INI", "BIB");
        lifeItemRecipe.setIngredient('B', Material.BLAZE_ROD);
        lifeItemRecipe.setIngredient('I', Material.NETHERITE_INGOT);
        lifeItemRecipe.setIngredient('N', Material.NETHER_STAR);
        Bukkit.addRecipe(lifeItemRecipe);
    }

    public Location getRandomLocation() {
        Location location = getRandomLocationUnSafe();
        if (!isSafeLocation(location)) {
            double x = location.getBlockX() + 0.5;
            double z = location.getBlockZ() + 0.5;
            for (double y = location.getBlockY(); y > 0; y--) {
                Location feet = new Location(location.getWorld(), x, y, z);
                if (isSafeLocation(feet))
                    return feet;
            }
            for (int x2 = location.getBlockX() - 10; x <= location.getBlockX() + 10; x++) {
                for (int z2 = location.getBlockZ() - 10; z <= location.getBlockZ() + 10; z++) {
                    Location feet = location.getWorld().getHighestBlockAt(x2, z2).getLocation();
                    if (isSafeLocation(feet)) return feet;
                }
            }
            return getRandomLocation();
        } else return location;
    }

    public Location getRandomLocationUnSafe() {
        World world = getServer().getWorld("world");
        if (world == null) world = getServer().getWorlds().get(0);
        Random rndGen = new Random();
        int r = rndGen.nextInt(maxDistance);
        int x = rndGen.nextInt(r);
        int z = (int) Math.sqrt(Math.pow(r, 2) - Math.pow(x, 2));
        if (rndGen.nextBoolean()) x *= -1;
        if (rndGen.nextBoolean()) z *= -1;
        Location randomLoc = new Location(world, x, 0, z);
        return world.getHighestBlockAt(randomLoc).getLocation().add(0, 1, 0);
    }

    public boolean isSafeLocation(Location location) {
        Block feet = location.getBlock();
        if (feet.getType().isSolid() && feet.getLocation().add(0, 1, 0).getBlock().getType().isSolid())
            return false; // solid feet space (not ideal location and potential to suffocate)
        Block head = feet.getRelative(BlockFace.UP);
        if (head.getType().isSolid()) return false; // solid head space (will suffocate)
        Block ground = feet.getRelative(BlockFace.DOWN);
        if (!ground.getType().isSolid()) return false; // ground not solid (will fall)

        if (ground.getBiome().name().contains("OCEAN")) return new Random().nextBoolean(); // 50% chance to allow ocean locations if location is an ocean

        return head.getType() != Material.LAVA &&
                feet.getType() != Material.LAVA; // make sure the air gaps aren't lava
    }
}
/*
placeholder for amount of lives left
lives - on buycraft can be crafted from soul gems + nether star + netherite ingot - /giftlives, /addlives, /setlives /removelives
respawn - they just lose inv and exp and respawn at bed/random & reset advancements
start at 2 lives
first spawn = random spawn
claiming
 */
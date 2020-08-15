package com.i54m.hardcorelives.managers;

import com.i54m.hardcorelives.Main;
import com.i54m.hardcorelives.exceptions.ManagerNotStartedException;
import com.sun.istack.internal.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The PlayerDataManager handles all player data files and caches the required ones
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@NoArgsConstructor
public class PlayerDataManager implements Listener, Manager {
    private Main PLUGIN = Main.getInstance();
    /**
     * Private manager instance with a getter method.
     */
    @Getter
    private static final PlayerDataManager INSTANCE = new PlayerDataManager();
    /**
     * Cache to keep all loaded player data configuration files.
     */
    private final Map<UUID, FileConfiguration> playerDataCache = new HashMap<>();

    /**
     * Whether the manager is started or not,
     * true if the manager should be locked and no operations allowed, else false.
     */
    private boolean locked = true;

    /**
     * Whether the manager is started or not.
     *
     * @return true if the manager should be locked and no operations allowed, else false.
     */
    @Override
    public boolean isStarted() {
        return !locked;
    }

    /**
     * Used to start the PlayerDataManager and register it's listeners.
     */
    @Override
    public void start() {
        if (!locked) {
            ERROR_HANDLER.log(new Exception("Player Data Manager Already started!"));
            return;
        }
        if (PLUGIN == null) PLUGIN = Main.getInstance();
        File dataDir = new File(PLUGIN.getDataFolder() + "/playerdata/");
        if (!dataDir.exists()) dataDir.mkdir();
        PLUGIN.getServer().getPluginManager().registerEvents(this, PLUGIN);
        locked = false;
        PLUGIN.getLogger().info(ChatColor.GREEN + "Started Player Data Manager!");
    }

    /**
     * Used to stop the PlayerDataManager and register it's listeners.
     */
    @Override
    public void stop() {
        if (locked) {
            ERROR_HANDLER.log(new ManagerNotStartedException(WorkerManager.getINSTANCE(), this));
            return;
        }
        playerDataCache.clear();
        locked = true;
    }

    /**
     * This is the event listener to load playerdata when a player joins, it uses {@link WorkerManager}
     * to run it on a separate thread so that the player's login isn't slowed down.
     *
     * @param e the LoginEvent.
     * @see WorkerManager
     * @see com.i54m.hardcorelives.managers.WorkerManager.Worker
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(@NotNull final PlayerJoinEvent e) {
        final Player player = e.getPlayer();
        WorkerManager.getINSTANCE().runWorker(new WorkerManager.Worker(() -> {
            try {
                loadPlayerData(player.getUniqueId());
            } catch (Exception pde) {
                ERROR_HANDLER.log(pde);
                ERROR_HANDLER.loginError(e);
            }
            final FileConfiguration playerData = getPlayerData(player, false);
            if (playerData.getBoolean("alive", true)) {
                if (!PLUGIN.isSafeLocation(player.getLocation())) {
                    double x = player.getLocation().getBlockX() + 0.5;
                    double z = player.getLocation().getBlockZ() + 0.5;
                    for (double y = player.getLocation().getBlockY(); y > 0; y--) {
                        Location feet = new Location(player.getWorld(), x, y, z);
                        if (PLUGIN.isSafeLocation(feet)) {
                            Bukkit.getScheduler().callSyncMethod(PLUGIN, () -> player.teleport(feet));
                            player.sendMessage(ChatColor.GREEN + "You would have taken damage on login, you have been teleported to a safer location!");
                            break;
                        }
                    }
                }
                player.setInvulnerable(true);
                player.sendMessage(ChatColor.RED + "You have 5 seconds of invincibility! Use them Wisely!");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
                if (player.isOnline()) {
                    player.setInvulnerable(false);
                    player.sendMessage(org.bukkit.ChatColor.RED + "You are no longer invincible, don't die!");
                    player.playEffect(player.getLocation(), Effect.MOBSPAWNER_FLAMES, null);
                }
            } else if (!playerData.getBoolean("alive") && playerData.getInt("lives") > 0) {
                player.sendMessage(ChatColor.GREEN + "It seems you are dead and have an extra life!");
                Bukkit.getScheduler().callSyncMethod(PLUGIN, () -> {
                    PLUGIN.respawn(player, playerData);
                    return null;
                });
            }
        }));
    }


    /**
     * This is the event listener to remove playerdata from the cache when they leave and set the last logout details,
     * it uses {@link WorkerManager} to run it on a separate thread so that the player's disconnect isn't slowed down.
     *
     * @param e the DisconnectEvent.
     * @see WorkerManager
     * @see com.i54m.hardcorelives.managers.WorkerManager.Worker
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(@NotNull final PlayerQuitEvent e) {
        WorkerManager.getINSTANCE().runWorker(new WorkerManager.Worker(() -> playerDataCache.remove(e.getPlayer().getUniqueId())));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerKick(@NotNull final PlayerKickEvent e) {
        WorkerManager.getINSTANCE().runWorker(new WorkerManager.Worker(() -> playerDataCache.remove(e.getPlayer().getUniqueId())));
    }

    /**
     * Fetch the player's data configuration file.
     *
     * @param player the player to fetch the data for
     * @return a configuration file of the stored data
     */
    public FileConfiguration getPlayerData(@NotNull Player player, boolean create) {
        if (locked) {
            ERROR_HANDLER.log(new ManagerNotStartedException(WorkerManager.getINSTANCE(), this));
            return null;
        }
        return getPlayerData(player.getUniqueId(), create);
    }

    /**
     * Fetch the player's data configuration file from their uuid.
     * If the player's data is not already loaded we will load it into the cache and return it
     *
     * @param uuid the uuid of the player to fetch the data for
     * @return a configuration file of the stored data
     */
    public FileConfiguration getPlayerData(@NotNull UUID uuid, boolean create) {
        if (locked) {
            ERROR_HANDLER.log(new ManagerNotStartedException(WorkerManager.getINSTANCE(), this));
            return null;
        }
        if (isPlayerDataLoaded(uuid))
            return playerDataCache.get(uuid);
        else if (create) return loadPlayerData(uuid);
        else return loadPlayerDataNoCreate(uuid);
    }

    /**
     * Load player's data configuration file from their uuid into the cache.
     * Use {@link #getPlayerData(UUID, boolean)} to get a player's data.
     *
     * @param uuid the uuid of the player to load the data for
     * @return a configuration file of the stored data
     */
    public FileConfiguration loadPlayerData(@NotNull UUID uuid) {
        if (locked) {
            ERROR_HANDLER.log(new ManagerNotStartedException(WorkerManager.getINSTANCE(), this));
            return null;
        }
        try {
            boolean newPlayer = false;
            if (isPlayerDataLoaded(uuid)) return getPlayerData(uuid, true);
            File dataFile = new File(PLUGIN.getDataFolder() + "/playerdata/", uuid.toString() + ".yml");
            if (!dataFile.exists()) {
                dataFile.createNewFile();
                newPlayer = true;
            }
            FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(dataFile);
            if (newPlayer) {
                saveDefaultData(playerConfig, dataFile, uuid);
                firstJoin(uuid);
            }
            playerDataCache.put(uuid, playerConfig);
            return playerConfig;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
            // data create/save exception
        }
    }

    private void firstJoin(@NotNull final UUID uuid) {
        WorkerManager.getINSTANCE().runWorker(new WorkerManager.Worker(() -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;
            player.sendMessage(ChatColor.GREEN + "Welcome to hardcore Survival! You have 2 lives left!");
            player.getInventory().addItem(PLUGIN.getBook());
            player.updateInventory();
            Bukkit.getScheduler().callSyncMethod(PLUGIN, () -> {
                player.teleport(PLUGIN.getRandomLocation());
                player.openBook(PLUGIN.getBook());
                return null;
            });
        }));
    }

    /**
     * Load player's data configuration file from their uuid into the cache.
     * Use {@link #getPlayerData(UUID, boolean)} to get a player's data.
     *
     * @param uuid the uuid of the player to load the data for
     * @return a configuration file of the stored data
     */
    public FileConfiguration loadPlayerDataNoCreate(@NotNull UUID uuid) {
        if (locked) {
            ERROR_HANDLER.log(new ManagerNotStartedException(WorkerManager.getINSTANCE(), this));
            return null;
        }
        if (isPlayerDataLoaded(uuid)) return getPlayerData(uuid, false);
        File dataFile = new File(PLUGIN.getDataFolder() + "/playerdata/", uuid.toString() + ".yml");
        if (!dataFile.exists())
            return null;
        FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(dataFile);
        playerDataCache.put(uuid, playerConfig);
        return playerConfig;
    }

    private void saveDefaultData(@NotNull FileConfiguration config, @NotNull File dataFile, @NotNull UUID uuid) throws IOException {
        config.set("lives", PLUGIN.getDefaultLives());
        config.set("alive", true);
        config.set("deaths", 0);
        config.save(dataFile);
    }

    /**
     * Save player's data configuration file
     *
     * @param uuid uuid of the player to get the data file of
     */
    public void savePlayerData(@NotNull UUID uuid) {
        if (locked) {
            ERROR_HANDLER.log(new ManagerNotStartedException(WorkerManager.getINSTANCE(), this));
            return;
        }
        try {
            if (!isPlayerDataLoaded(uuid)) return;
            File dataFile = new File(PLUGIN.getDataFolder() + "/playerdata/", uuid.toString() + ".yml");
            if (!dataFile.exists()) {
                dataFile.createNewFile();
                return;
            }
            playerDataCache.get(uuid).save(dataFile);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            // data create/save exception
        }
    }

    /**
     * @param uuid uuid of the player to check for a loaded data config
     * @return true if the player's data config is loaded in the cache, false if the player's data config is not loaded in the cache
     */
    public boolean isPlayerDataLoaded(@NotNull UUID uuid) {
        if (locked) {
            ERROR_HANDLER.log(new ManagerNotStartedException(WorkerManager.getINSTANCE(), this));
            return false;
        }
        return playerDataCache.containsKey(uuid);
    }
}

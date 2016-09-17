/*
 * This file is part of DeltaSkins.
 *
 * DeltaSkins is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DeltaSkins is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DeltaSkins.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gmail.tracebachi.DeltaSkins.Spigot;

import com.gmail.tracebachi.DeltaSkins.Shared.Interfaces.IDeltaSkins;
import com.gmail.tracebachi.DeltaSkins.Shared.MojangApi;
import com.gmail.tracebachi.DeltaSkins.Shared.Runnables.BatchUuidRunnable;
import com.gmail.tracebachi.DeltaSkins.Shared.Runnables.SaveFilesRunnable;
import com.gmail.tracebachi.DeltaSkins.Shared.Runnables.SkinFetchRunnable;
import com.gmail.tracebachi.DeltaSkins.Shared.Settings;
import com.gmail.tracebachi.DeltaSkins.Shared.Storage.PlayerProfile;
import com.gmail.tracebachi.DeltaSkins.Shared.Storage.PlayerProfileStorage;
import com.gmail.tracebachi.DeltaSkins.Shared.Storage.SkinData;
import com.gmail.tracebachi.DeltaSkins.Shared.Storage.SkinDataStorage;
import com.gmail.tracebachi.DeltaSkins.Spigot.Commands.MainCommands;
import com.gmail.tracebachi.DeltaSkins.Spigot.Listeners.BungeeListener;
import com.gmail.tracebachi.DeltaSkins.Spigot.Listeners.LoginListener;
import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.spigotmc.SpigotConfig;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;

import static com.gmail.tracebachi.DeltaSkins.Shared.JsonHelper.readJsonElementFromFile;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee).
 */
public class DeltaSkins extends JavaPlugin implements IDeltaSkins, Listener
{
    private final String PROFILES_FILENAME = "Profiles.json";
    private final String SKIN_DATA_FILENAME = "SkinData.json";

    private int debugLevel;
    private LoginListener loginListener;
    private MainCommands commands;

    private MojangApi mojangApi;
    private SkinApplier skinApplier;
    private PlayerProfileStorage playerProfileStorage;
    private SkinDataStorage skinDataStorage;
    private BungeeListener bungeeListener;

    @Override
    public void onLoad()
    {
        File file = new File(getDataFolder(), "config.yml");
        if(!file.exists())
        {
            saveDefaultConfig();
        }
    }

    public void onEnable()
    {
        if(SpigotConfig.bungee)
        {
            skinApplier = new SkinApplier(this);
            bungeeListener = new BungeeListener(skinApplier);

            getServer().getMessenger().registerIncomingPluginChannel(
                this,
                "DeltaSkins",
                bungeeListener);

            info("DeltaSkins enabled in BungeeCord mode.");
            return;
        }

        reloadConfig();

        debugLevel = getConfig().getInt("DebugLevel", 2);
        long batchRequestIntervalSeconds = getConfig().getLong("BatchRequestIntervalSeconds", 90);
        long skinCacheDurationMinutes = getConfig().getLong("SkinCacheDurationMinutes", 180);
        long fileSaveInterval = getConfig().getLong("FileSaveInterval", 1800);
        loadFormats(getConfig());

        mojangApi = new MojangApi(this);
        skinApplier = new SkinApplier(this);
        playerProfileStorage = new PlayerProfileStorage();
        skinDataStorage = new SkinDataStorage(skinCacheDurationMinutes);

        // Read player profiles
        File profileFile = new File(getDataFolder(), PROFILES_FILENAME);
        JsonArray playerProfilesArray = readJsonElementFromFile(
            profileFile,
            new JsonArray()).getAsJsonArray();
        playerProfileStorage.deserialize(playerProfilesArray);

        // Read skin data
        File skinDataFile = new File(getDataFolder(), SKIN_DATA_FILENAME);
        JsonArray skinDataArray = readJsonElementFromFile(
            skinDataFile,
            new JsonArray()).getAsJsonArray();
        skinDataStorage.deserialize(skinDataArray);

        // Schedule repeating tasks
        BukkitScheduler scheduler = getServer().getScheduler();
        BatchUuidRunnable uuidRunnable = new BatchUuidRunnable(batchRequestIntervalSeconds, this);
        SaveFilesRunnable fileSaveRunnable = new SaveFilesRunnable(profileFile, skinDataFile, this);
        scheduler.runTaskTimerAsynchronously(this, uuidRunnable, 20, 20);
        scheduler.runTaskTimer(this, fileSaveRunnable, fileSaveInterval, fileSaveInterval);

        loginListener = new LoginListener(skinApplier, this);
        getServer().getPluginManager().registerEvents(loginListener, this);

        commands = new MainCommands(skinApplier, this);
        getCommand("deltaskins").setExecutor(commands);
    }

    public void onDisable()
    {
        if(SpigotConfig.bungee)
        {
            getServer().getMessenger().unregisterIncomingPluginChannel(this);

            skinApplier = null;
            bungeeListener = null;
            return;
        }

        getServer().getScheduler().cancelTasks(this);

        commands = null;
        loginListener = null;

        // Save profiles by reusing the sync runnable
        new SaveFilesRunnable(
            new File(getDataFolder(), PROFILES_FILENAME),
            new File(getDataFolder(), SKIN_DATA_FILENAME),
            this).run();

        skinDataStorage = null;
        playerProfileStorage = null;
        skinApplier = null;

        mojangApi.close();
        mojangApi = null;
    }

    @Override
    public MojangApi getMojangApi()
    {
        return mojangApi;
    }

    @Override
    public PlayerProfileStorage getPlayerProfileStorage()
    {
        return playerProfileStorage;
    }

    @Override
    public SkinDataStorage getSkinDataStorage()
    {
        return skinDataStorage;
    }

    @Override
    public void onProfileFound(PlayerProfile profile)
    {
        Preconditions.checkNotNull(profile, "Profile was null.");

        // Store the profile (and overwrite if a profile previously existed).
        playerProfileStorage.put(profile);
        debug("Added/Updated PlayerProfile for {name: " + profile.getName() + "}.");

        // Fetch the skin based on the profile.
        fetchSkin(profile);
    }

    @Override
    public void fetchSkin(PlayerProfile profile)
    {
        Preconditions.checkNotNull(profile, "Profile was null.");

        // Fetch the skin based on the profile.
        SkinFetchRunnable runnable = new SkinFetchRunnable(profile, this);
        getServer().getScheduler().runTaskAsynchronously(this, runnable);

        debug("Fetching SkinData for {" +
            "name: " + profile.getName() + ", " +
            "playerUuid: " + profile.getPlayerUuid() + ", " +
            "skinUuid: " + profile.getSkinUuid() + "}.");
    }

    @Override
    public void onSkinFetched(PlayerProfile profile, SkinData skinData)
    {
        Preconditions.checkNotNull(profile, "Profile was null.");
        Preconditions.checkNotNull(skinData, "SkinData was null.");

        // Save the results in the storage.
        skinDataStorage.put(skinData.getUuid(), skinData);
        debug("Found new SkinData for {" +
            "name: " + profile.getName() + ", " +
            "playerUuid: " + profile.getPlayerUuid() + ", " +
            "skinUuid: " + skinData.getUuid() + "}.");

        // If the player is online, apply the skin.
        Player player = Bukkit.getPlayer(profile.getName());
        if(player != null)
        {
            skinApplier.apply(player, skinData);
        }
    }

    @Override
    public void info(String message)
    {
        getLogger().info(message);
    }

    @Override
    public void severe(String message)
    {
        getLogger().severe(message);
    }

    @Override
    public void debug(String message)
    {
        if(debugLevel >= 1)
        {
            getLogger().info("[Debug-L1] " + message);
        }
    }

    @Override
    public void debugApi(String message)
    {
        if(debugLevel >= 2)
        {
            getLogger().info("[Debug-L2]" + message);
        }
    }

    private void loadFormats(ConfigurationSection configuration)
    {
        Preconditions.checkNotNull(configuration, "Configuration was null.");

        HashMap<String, MessageFormat> formatHashMap = new HashMap<>();
        ConfigurationSection formats = configuration.getConfigurationSection("Formats");

        for(String key : formats.getKeys(false))
        {
            String formatToAdd = ChatColor.translateAlternateColorCodes('&', formats.getString(key));
            formatHashMap.put(key, new MessageFormat(formatToAdd));
        }

        Settings.setFormatMap(formatHashMap);
    }
}

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
package com.gmail.tracebachi.DeltaSkins.Bungee;

import com.gmail.tracebachi.DeltaSkins.Bungee.Commands.MainCommands;
import com.gmail.tracebachi.DeltaSkins.Bungee.Listeners.LoginListener;
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
import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonArray;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static com.gmail.tracebachi.DeltaSkins.Shared.JsonHelper.readJsonElementFromFile;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee).
 */
public class DeltaSkins extends Plugin implements IDeltaSkins
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

    public void onEnable()
    {
        Configuration config = loadConfig();
        if(config == null) { return; }

        debugLevel = config.getInt("DebugLevel", 2);
        long batchRequestIntervalSeconds = config.getLong("BatchRequestIntervalSeconds", 90);
        long skinCacheDurationMinutes = config.getLong("SkinCacheDurationMinutes", 180);
        long fileSaveInterval = config.getLong("FileSaveInterval", 1800);
        loadFormats(config);

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
        TaskScheduler scheduler = this.getProxy().getScheduler();
        BatchUuidRunnable uuidRunnable = new BatchUuidRunnable(batchRequestIntervalSeconds, this);
        SaveFilesRunnable fileSaveRunnable = new SaveFilesRunnable(profileFile, skinDataFile, this);
        scheduler.schedule(this, uuidRunnable, 60, 15, TimeUnit.SECONDS);
        scheduler.schedule(this, fileSaveRunnable, 600, fileSaveInterval, TimeUnit.SECONDS);

        // Register login listener
        loginListener = new LoginListener(skinApplier, this);
        getProxy().getPluginManager().registerListener(this, loginListener);

        // Register commands
        commands = new MainCommands(skinApplier, this);
        getProxy().getPluginManager().registerCommand(this, commands);
    }

    public void onDisable()
    {
        getProxy().getScheduler().cancel(this);

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
        debug("Added/Updated profile for " + profile.getName());

        // Fetch the skin based on the profile.
        fetchSkin(profile);
    }

    @Override
    public void fetchSkin(PlayerProfile profile)
    {
        Preconditions.checkNotNull(profile, "Profile was null.");

        // Fetch the skin based on the profile.
        SkinFetchRunnable runnable = new SkinFetchRunnable(profile, this);
        getProxy().getScheduler().runAsync(this, runnable);

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
        debug("Found new skin data for {" +
            "name: " + profile.getName() + ", " +
            "playerUuid: " + profile.getPlayerUuid() + ", " +
            "skinUuid: " + skinData.getUuid() + "}.");

        // If the player is online, apply the skin.
        ProxiedPlayer player = getProxy().getPlayer(profile.getName());
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

    private Configuration loadConfig()
    {
        try
        {
            return ConfigurationProvider
                .getProvider(YamlConfiguration.class)
                .load(loadResource(this, "config.yml"));
        }
        catch(IOException e)
        {
            getLogger().severe("Failed to load configuration file. Report this error and the following stacktrace.");
            e.printStackTrace();
            return null;
        }
    }

    private void loadFormats(Configuration configuration)
    {
        Preconditions.checkNotNull(configuration, "Configuration was null.");

        HashMap<String, MessageFormat> formatHashMap = new HashMap<>();
        Configuration formats = configuration.getSection("Formats");

        for(String key : formats.getKeys())
        {
            String formatToAdd = ChatColor.translateAlternateColorCodes('&', formats.getString(key));
            formatHashMap.put(key, new MessageFormat(formatToAdd));
        }

        Settings.setFormatMap(formatHashMap);
    }

    /***
     * Source for this method found at:
     * https://www.spigotmc.org/threads/bungeecords-configuration-api.11214/#post-119017
     *
     * Originally authored by: vemacs, Feb 15, 2014
     */
    private File loadResource(Plugin plugin, String resource)
    {
        File folder = plugin.getDataFolder();
        if (!folder.exists())
        {
            folder.mkdir();
        }

        File destinationFile = new File(folder, resource);
        try
        {
            if(!destinationFile.exists())
            {
                destinationFile.createNewFile();
                try (InputStream in = plugin.getResourceAsStream(resource);
                     OutputStream out = new FileOutputStream(destinationFile))
                {
                    ByteStreams.copy(in, out);
                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return destinationFile;
    }
}

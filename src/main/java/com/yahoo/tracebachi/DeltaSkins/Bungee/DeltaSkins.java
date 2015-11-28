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
package com.yahoo.tracebachi.DeltaSkins.Bungee;

import com.google.common.io.ByteStreams;
import com.yahoo.tracebachi.DeltaSkins.Bungee.Commands.MainCommands;
import com.yahoo.tracebachi.DeltaSkins.Bungee.Listeners.LoginListener;
import com.yahoo.tracebachi.DeltaSkins.Shared.Interfaces.IDeltaSkins;
import com.yahoo.tracebachi.DeltaSkins.Shared.MojangApi;
import com.yahoo.tracebachi.DeltaSkins.Shared.PlayerProfile;
import com.yahoo.tracebachi.DeltaSkins.Shared.PlayerProfileFileReader;
import com.yahoo.tracebachi.DeltaSkins.Shared.PlayerProfileStorage;
import com.yahoo.tracebachi.DeltaSkins.Shared.Runnables.BatchUuidRunnable;
import com.yahoo.tracebachi.DeltaSkins.Shared.Runnables.ProfileFileSaveRunnable;
import com.yahoo.tracebachi.DeltaSkins.Shared.Runnables.SkinFetchRunnable;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class DeltaSkins extends Plugin implements IDeltaSkins, Listener
{
    private final String profileFilename = "profiles.json";

    private boolean debugCode;
    private boolean debugRequests;

    private LoginListener loginListener;
    private MainCommands commands;

    private MojangApi mojangApi;
    private PlayerProfileStorage storage;

    public void onEnable()
    {
        Configuration config;
        try
        {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class)
                .load(loadResource(this, "config.yml"));
        }
        catch(IOException e)
        {
            e.printStackTrace();
            // TODO Log failed to start
            return;
        }

        debugCode = config.getBoolean("debug_code", false);
        debugRequests = config.getBoolean("debug_mojang_api", false);
        long batchRequestInterval = config.getLong("batch_request_interval", 90000);
        long cacheExpiration = config.getLong("cache_expiration", 1000 * 60 * 60 * 12);
        long cacheSaveInterval = config.getLong("cache_save_interval", 36000);
        long cacheDiscardOlderThan = config.getLong("cache_discard_older_than", 604800000);
        int connectTimeout = config.getInt("connect_timeout", 5000);
        int readTimeout = config.getInt("read_timeout", 5000);

        mojangApi = new MojangApi(connectTimeout, readTimeout, this);
        storage = new PlayerProfileStorage(this);

        // Read profile file
        File file = new File(getDataFolder(), profileFilename);
        PlayerProfileFileReader.read(cacheDiscardOlderThan, file, this);

        // Schedule repeating tasks
        BatchUuidRunnable uuidRunnable = new BatchUuidRunnable(batchRequestInterval, this);
        ProfileFileSaveRunnable fileSaveRunnable = new ProfileFileSaveRunnable(file, this);

        this.getProxy().getScheduler().schedule(this, uuidRunnable, 15, 15, TimeUnit.SECONDS);
        this.getProxy().getScheduler().schedule(this, fileSaveRunnable, 600, cacheSaveInterval, TimeUnit.SECONDS);

        loginListener = new LoginListener(cacheExpiration, this);
        getProxy().getPluginManager().registerListener(this, loginListener);

        commands = new MainCommands(this);
        getProxy().getPluginManager().registerCommand(this, commands);
    }

    public void onDisable()
    {
        commands = null;
        loginListener = null;

        getProxy().getScheduler().cancel(this);

        // Save profiles by reusing the sync runnable
        new ProfileFileSaveRunnable(new File(getDataFolder(), profileFilename), this).run();
        storage = null;

        mojangApi.cleanupAndClose();
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
        return storage;
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
        if(debugCode)
        {
            getLogger().info("[Debug] " + message);
        }
    }

    @Override
    public void debugApi(String message)
    {
        if(debugRequests)
        {
            getLogger().info("[Debug-Mojang-Api]" + message);
        }
    }

    @Override
    public void runAsyncSkinFetch(PlayerProfile profile, String uuidToUse)
    {
        if(profile == null) { return; }

        SkinFetchRunnable runnable = new SkinFetchRunnable(profile, uuidToUse, this);
        getProxy().getScheduler().runAsync(this, runnable);
    }

    @Override
    public void sendDelayedMessage(String name, String message)
    {
        if(name == null || message == null) { return; }

        getProxy().getScheduler().schedule(this, () ->
        {
            ProxiedPlayer player = getProxy().getPlayer(name);
            if(player != null)
            {
                player.sendMessage(TextComponent.fromLegacyText(message));
            }
        }, 5, TimeUnit.SECONDS);
    }

    /***
     * Source for this method found at:
     * https://www.spigotmc.org/threads/bungeecords-configuration-api.11214/#post-119017
     *
     * Originally authored by: vemacs, Feb 15, 2014
     */
    public static File loadResource(Plugin plugin, String resource)
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

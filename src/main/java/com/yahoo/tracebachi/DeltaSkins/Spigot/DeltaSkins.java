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
package com.yahoo.tracebachi.DeltaSkins.Spigot;

import com.yahoo.tracebachi.DeltaSkins.Shared.Interfaces.IDeltaSkins;
import com.yahoo.tracebachi.DeltaSkins.Shared.MojangApi;
import com.yahoo.tracebachi.DeltaSkins.Shared.PlayerProfile;
import com.yahoo.tracebachi.DeltaSkins.Shared.PlayerProfileFileReader;
import com.yahoo.tracebachi.DeltaSkins.Shared.PlayerProfileStorage;
import com.yahoo.tracebachi.DeltaSkins.Shared.Runnables.BatchUuidRunnable;
import com.yahoo.tracebachi.DeltaSkins.Shared.Runnables.ProfileFileSaveRunnable;
import com.yahoo.tracebachi.DeltaSkins.Shared.Runnables.SkinFetchRunnable;
import com.yahoo.tracebachi.DeltaSkins.Spigot.Commands.MainCommands;
import com.yahoo.tracebachi.DeltaSkins.Spigot.Listeners.LoginListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;

public class DeltaSkins extends JavaPlugin implements IDeltaSkins, Listener
{
    private final String profileFilename = "profiles.json";

    private boolean debugCode;
    private boolean debugRequests;

    private LoginListener loginListener;
    private MainCommands commands;

    private MojangApi mojangApi;
    private PlayerProfileStorage storage;

    private BukkitTask asyncBatchSkinTask;
    private BukkitTask syncSaveCacheTask;

    @Override
    public void onLoad()
    {
        saveDefaultConfig();
    }

    public void onEnable()
    {
        reloadConfig();
        debugCode = getConfig().getBoolean("debug_code", false);
        debugRequests = getConfig().getBoolean("debug_mojang_api", false);
        long batchRequestInterval = getConfig().getLong("batch_request_interval", 90000);
        long cacheExpiration = getConfig().getLong("cache_expiration", 1000 * 60 * 60 * 12);
        long cacheSaveInterval = getConfig().getLong("cache_save_interval", 36000);
        long cacheDiscardOlderThan = getConfig().getLong("cache_discard_older_than", 604800000);
        int connectTimeout = getConfig().getInt("connect_timeout", 5000);
        int readTimeout = getConfig().getInt("read_timeout", 5000);

        mojangApi = new MojangApi(connectTimeout, readTimeout, this);
        storage = new PlayerProfileStorage(this);

        // Read profile file
        File file = new File(getDataFolder(), profileFilename);
        PlayerProfileFileReader.read(cacheDiscardOlderThan, file, this);

        // Schedule repeating tasks
        BukkitScheduler scheduler = getServer().getScheduler();
        asyncBatchSkinTask = scheduler.runTaskTimerAsynchronously(this,
            new BatchUuidRunnable(batchRequestInterval, this), 5, 20);
        syncSaveCacheTask = scheduler.runTaskTimer(this,
            new ProfileFileSaveRunnable(file, this), 1200, cacheSaveInterval);

        loginListener = new LoginListener(cacheExpiration, this);
        getServer().getPluginManager().registerEvents(loginListener, this);

        commands = new MainCommands(this);
        getCommand("deltaskins").setExecutor(commands);
    }

    public void onDisable()
    {
        commands = null;
        loginListener = null;

        getServer().getScheduler().cancelTasks(this);
        if(asyncBatchSkinTask != null)
        {
            asyncBatchSkinTask.cancel();
            asyncBatchSkinTask = null;
        }
        if(syncSaveCacheTask != null)
        {
            syncSaveCacheTask.cancel();
            syncSaveCacheTask = null;
        }

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

        getServer().getScheduler().runTaskAsynchronously(this, () ->
        {
            String playerName = profile.getName();
            SkinFetchRunnable runnable = new SkinFetchRunnable(profile, uuidToUse, this);
            runnable.run();

            Bukkit.getScheduler().runTask(this, () ->
            {
                Player player = Bukkit.getPlayer(playerName);
                if(player != null)
                {
                    player.sendMessage(Prefixes.INFO + "Your skin has been fetched! Relog to apply it.");
                }
            });
        });
    }
}

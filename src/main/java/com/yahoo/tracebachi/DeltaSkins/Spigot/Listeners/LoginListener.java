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
package com.yahoo.tracebachi.DeltaSkins.Spigot.Listeners;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.yahoo.tracebachi.DeltaSkins.Shared.Interfaces.IDeltaSkins;
import com.yahoo.tracebachi.DeltaSkins.Shared.MojangApi;
import com.yahoo.tracebachi.DeltaSkins.Shared.PlayerProfile;
import com.yahoo.tracebachi.DeltaSkins.Shared.SkinData;
import com.yahoo.tracebachi.DeltaSkins.Spigot.Prefixes;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LoginListener implements Listener
{
    private static final String TEXTURES_PROPERTY = "textures";

    private final long cacheExpiration;
    private final IDeltaSkins plugin;
    private final MojangApi mojangApi;

    public LoginListener(long cacheExpiration, IDeltaSkins plugin)
    {
        this.cacheExpiration = cacheExpiration;
        this.plugin = plugin;
        this.mojangApi = plugin.getMojangApi();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLoginEvent(PlayerLoginEvent event)
    {
        if(event.getResult() != PlayerLoginEvent.Result.ALLOWED) { return; }

        Player player = event.getPlayer();
        String name = player.getName();
        PlayerProfile profile = plugin.getPlayerProfileStorage().getProfile(name);
        SkinData skinData;

        if(profile == null)
        {
            mojangApi.addUuidRequest(name);
            return;
        }

        skinData = profile.getSkinData();
        if(skinData == null)
        {
            if(profile.getPlayerUuid().equals(PlayerProfile.UNKNOWN_UUID))
            {
                plugin.runAsyncSkinFetch(profile, profile.getSkinUuid());
            }
            else
            {
                plugin.runAsyncSkinFetch(profile, profile.getPlayerUuid());
            }

            plugin.debug("Profile for " + name + " does not have skin data. Queued to update.");
            return;
        }

        if((System.currentTimeMillis() - skinData.getCreatedAt()) > cacheExpiration)
        {
            if(profile.getPlayerUuid().equals(PlayerProfile.UNKNOWN_UUID))
            {
                plugin.runAsyncSkinFetch(profile, profile.getSkinUuid());
            }
            else
            {
                plugin.runAsyncSkinFetch(profile, profile.getPlayerUuid());
            }

            plugin.debug("Cached profile for " + name + " expired. Queued to update.");

            plugin.sendDelayedMessage(name, Prefixes.INFO + "Looks like your skin needs to refresh! " +
                "Relog in a minute to fix it.");
        }
        else
        {
            WrappedGameProfile wrappedprofile = WrappedGameProfile.fromPlayer(player);
            WrappedSignedProperty property = WrappedSignedProperty.fromValues(TEXTURES_PROPERTY,
                skinData.getValue(), skinData.getSignature());
            wrappedprofile.getProperties().put(TEXTURES_PROPERTY, property);

            plugin.debug("Found profile for " + name + " in cache.");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuitEvent(PlayerQuitEvent event)
    {
        String name = event.getPlayer().getName();
        mojangApi.removeUuidRequest(name);
    }
}

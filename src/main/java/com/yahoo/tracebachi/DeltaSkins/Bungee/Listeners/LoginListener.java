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
package com.yahoo.tracebachi.DeltaSkins.Bungee.Listeners;

import com.yahoo.tracebachi.DeltaSkins.Shared.Interfaces.IDeltaSkins;
import com.yahoo.tracebachi.DeltaSkins.Shared.MojangApi;
import com.yahoo.tracebachi.DeltaSkins.Shared.PlayerProfile;
import com.yahoo.tracebachi.DeltaSkins.Shared.SkinData;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.lang.reflect.Field;

public class LoginListener implements Listener
{
    private static final String TEXTURES_PROPERTY = "textures";
    private static Field profileField = null;

    private final long cacheExpiration;
    private final IDeltaSkins plugin;
    private final MojangApi mojangApi;

    public LoginListener(long cacheExpiration, IDeltaSkins plugin)
    {
        this.cacheExpiration = cacheExpiration;
        this.plugin = plugin;
        this.mojangApi = plugin.getMojangApi();

        if(profileField == null)
        {
            try
            {
                profileField = InitialHandler.class.getDeclaredField("loginProfile");
                profileField.setAccessible(true);
            }
            catch(NoSuchFieldException e)
            {
                plugin.severe("Failed to reflect loginProfile field. Report this error and stacktrace below.");
                e.printStackTrace();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLoginEvent(PostLoginEvent event)
    {
        ProxiedPlayer player = event.getPlayer();
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
        }
        else
        {
            updateSkinFor(player, profile.getPlayerUuid(), skinData);
            plugin.debug("Found profile for " + name + " in cache.");
        }
    }

    /**************************************************************************
     * Original source for this method is from the SkinRestorer plugin
     * Author: Th3Tr0LLeR
     * https://www.spigotmc.org/resources/skinsrestorer.2124/
     *************************************************************************/
    private void updateSkinFor(ProxiedPlayer player, String playerUuid, SkinData skinData)
    {
        try
        {
            LoginResult.Property textures = new LoginResult.Property(TEXTURES_PROPERTY,
                skinData.getValue(), skinData.getSignature());
            InitialHandler handler = (InitialHandler) player.getPendingConnection();
            LoginResult loginResult = (LoginResult) LoginListener.profileField.get(handler);

            if(loginResult == null)
            {
                String newLoginResultUuid;

                if(playerUuid.equals(PlayerProfile.UNKNOWN_UUID))
                {
                    newLoginResultUuid = player.getUniqueId().toString();
                }
                else
                {
                    newLoginResultUuid = playerUuid;
                }

                loginResult = new LoginResult(newLoginResultUuid, new LoginResult.Property[]{textures});
            }
            else
            {
                LoginResult.Property[] present = loginResult.getProperties();
                boolean alreadyHasSkin = false;

                for(LoginResult.Property property : present)
                {
                    if(property.getName().equals(TEXTURES_PROPERTY))
                    {
                        alreadyHasSkin = true;
                    }
                }

                if(!alreadyHasSkin)
                {
                    LoginResult.Property[] newProperties = new LoginResult.Property[present.length + 1];
                    System.arraycopy(present, 0, newProperties, 0, present.length);
                    newProperties[present.length] = textures;
                    loginResult.setProperties(newProperties);
                }
            }

            LoginListener.profileField.set(handler, loginResult);
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuitEvent(PlayerDisconnectEvent event)
    {
        String name = event.getPlayer().getName();
        mojangApi.removeUuidRequest(name);
    }
}

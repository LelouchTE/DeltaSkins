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
package com.gmail.tracebachi.DeltaSkins.Bungee.Listeners;

import com.gmail.tracebachi.DeltaSkins.Bungee.SkinApplier;
import com.gmail.tracebachi.DeltaSkins.Shared.Interfaces.IDeltaSkins;
import com.gmail.tracebachi.DeltaSkins.Shared.MojangApi;
import com.gmail.tracebachi.DeltaSkins.Shared.Storage.PlayerProfile;
import com.gmail.tracebachi.DeltaSkins.Shared.Storage.PlayerProfileStorage;
import com.gmail.tracebachi.DeltaSkins.Shared.Storage.SkinData;
import com.gmail.tracebachi.DeltaSkins.Shared.Storage.SkinDataStorage;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 9/10/16.
 */
public class LoginListener implements Listener
{
    private final SkinApplier skinApplier;
    private final MojangApi mojangApi;
    private final IDeltaSkins plugin;

    public LoginListener(SkinApplier skinApplier, IDeltaSkins plugin)
    {
        this.skinApplier = skinApplier;
        this.plugin = plugin;
        this.mojangApi = plugin.getMojangApi();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onQuitEvent(PlayerDisconnectEvent event)
    {
        String name = event.getPlayer().getName();
        mojangApi.removeUuidRequest(name);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLoginEvent(PostLoginEvent event)
    {
        ProxiedPlayer player = event.getPlayer();
        String name = player.getName();
        PlayerProfileStorage playerProfileStorage = plugin.getPlayerProfileStorage();
        PlayerProfile profile = playerProfileStorage.get(name);

        // If the profile is not found, add a UUID request.
        if(profile == null)
        {
            mojangApi.addUuidRequest(name);
            return;
        }

        SkinDataStorage skinDataStorage = plugin.getSkinDataStorage();
        SkinData skinData = skinDataStorage.get(profile.getSkinUuid());

        if(skinData != null)
        {
            skinApplier.apply(player, skinData);
        }
        else
        {
            plugin.fetchSkin(profile);
        }
    }
}

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
package com.gmail.tracebachi.DeltaSkins.Bungee.Commands;

import com.gmail.tracebachi.DeltaSkins.Bungee.SkinApplier;
import com.gmail.tracebachi.DeltaSkins.Shared.Interfaces.IDeltaSkins;
import com.gmail.tracebachi.DeltaSkins.Shared.Storage.PlayerProfile;
import com.gmail.tracebachi.DeltaSkins.Shared.Storage.PlayerProfileStorage;
import com.gmail.tracebachi.DeltaSkins.Shared.Storage.SkinData;
import com.gmail.tracebachi.DeltaSkins.Shared.Storage.SkinDataStorage;
import com.google.common.base.Preconditions;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import static com.gmail.tracebachi.DeltaSkins.Shared.Settings.format;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee).
 */
public class ResetCommand
{
    private final SkinApplier skinApplier;
    private final IDeltaSkins plugin;

    public ResetCommand(SkinApplier skinApplier, IDeltaSkins plugin)
    {
        this.skinApplier = skinApplier;
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("DeltaSkins.Reset"))
        {
            sender.sendMessage(format("NoPermission", "DeltaSkins.Reset"));
            return true;
        }

        if(args.length < 2)
        {
            sender.sendMessage(format("CommandHelp", "/ds reset <name>"));
            return true;
        }

        PlayerProfileStorage storage = plugin.getPlayerProfileStorage();
        PlayerProfile oldPlayerProfile = storage.remove(args[1]);

        if(oldPlayerProfile == null)
        {
            sender.sendMessage(format("PlayerProfileNotFound", args[1]));
            return true;
        }

        if(oldPlayerProfile.hasPlayerUuid())
        {
            PlayerProfile newPlayerProfile = new PlayerProfile(
                oldPlayerProfile.getName(),
                oldPlayerProfile.getPlayerUuid(),
                oldPlayerProfile.getPlayerUuid());

            storage.put(newPlayerProfile);

            SkinDataStorage skinDataStorage = plugin.getSkinDataStorage();
            SkinData skinData = skinDataStorage.get(newPlayerProfile.getSkinUuid());

            if(skinData != null)
            {
                applyNewSkin(args[1], skinData);
            }
            else
            {
                plugin.fetchSkin(newPlayerProfile);
            }

            sender.sendMessage(format("PlayerProfileResetToOwn", args[1]));
        }
        else
        {
            plugin.getMojangApi().addUuidRequest(args[1]);

            sender.sendMessage(format("PlayerProfileResetRemoved", args[1]));
        }

        plugin.debug("[ResetCommand] Reset PlayerProfile {" +
            "name: " + oldPlayerProfile.getName() + ", " +
            "playerUuid: " + oldPlayerProfile.getPlayerUuid() + "}.");
        return true;
    }

    private void applyNewSkin(String playerName, SkinData skinData)
    {
        Preconditions.checkNotNull(skinData, "SkinData was null.");

        ProxiedPlayer player = BungeeCord.getInstance().getPlayer(playerName);

        if(player != null)
        {
            skinApplier.apply(player, skinData);
        }
    }
}

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
package com.gmail.tracebachi.DeltaSkins.Spigot.Commands;

import com.gmail.tracebachi.DeltaSkins.Shared.Interfaces.IDeltaSkins;
import com.gmail.tracebachi.DeltaSkins.Shared.Storage.PlayerProfile;
import com.gmail.tracebachi.DeltaSkins.Shared.Storage.PlayerProfileStorage;
import com.gmail.tracebachi.DeltaSkins.Shared.Storage.SkinData;
import com.gmail.tracebachi.DeltaSkins.Spigot.SkinApplier;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.gmail.tracebachi.DeltaSkins.Shared.Settings.format;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee).
 */
public class SetCommand
{
    private SkinApplier skinApplier;
    private final IDeltaSkins plugin;

    public SetCommand(SkinApplier skinApplier, IDeltaSkins plugin)
    {
        this.skinApplier = skinApplier;
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("DeltaSkins.Set"))
        {
            sender.sendMessage(format("NoPermission", "DeltaSkins.Set"));
            return true;
        }

        if(args.length < 3)
        {
            sender.sendMessage(format("CommandHelp", "/ds set <dest> <src>"));
            return true;
        }

        if(args[1].equalsIgnoreCase(args[2]))
        {
            sender.sendMessage(format("UseResetInsteadOfSet", args[1]));
            return true;
        }

        PlayerProfileStorage storage = plugin.getPlayerProfileStorage();
        PlayerProfile destProfile = storage.get(args[1]);
        PlayerProfile sourceProfile = storage.get(args[2]);

        if(sourceProfile == null)
        {
            plugin.getMojangApi().addUuidRequest(args[2]);
            plugin.debug("[SetCommand] Added UUID request for " + args[2]);

            if(destProfile == null)
            {
                plugin.getMojangApi().addUuidRequest(args[1]);
                plugin.debug("[SetCommand] Added UUID request for " + args[1]);
            }

            sender.sendMessage(format("PlayerProfileNotFoundForSet", args[2]));
            return true;
        }

        SkinData skinData = plugin.getSkinDataStorage().get(sourceProfile.getSkinUuid());

        if(destProfile != null)
        {
            PlayerProfile newDestProfile = new PlayerProfile(
                destProfile.getName(),
                destProfile.getPlayerUuid(),
                sourceProfile.getSkinUuid());

            storage.put(newDestProfile);

            plugin.debug("[SetCommand] Updated PlayerProfile for " + args[1] +
                " using PlayerProfile from " + sourceProfile.getName());

            if(skinData != null)
            {
                applyNewSkin(destProfile.getName(), skinData);
            }
            else
            {
                plugin.fetchSkin(newDestProfile);
            }
        }
        else
        {
            PlayerProfile newDestProfile = new PlayerProfile(
                args[1],
                PlayerProfile.NULL_UUID,
                sourceProfile.getSkinUuid());

            storage.put(newDestProfile);

            plugin.debug("[SetCommand] Added PlayerProfile for " + args[1] +
                " using PlayerProfile from " + sourceProfile.getName());

            if(skinData != null)
            {
                applyNewSkin(args[1], skinData);
            }
            else
            {
                plugin.fetchSkin(newDestProfile);
            }
        }

        sender.sendMessage(format("PlayerProfileUpdated", args[1], args[2]));
        return true;
    }

    private void applyNewSkin(String playerName, SkinData skinData)
    {
        Preconditions.checkNotNull(skinData, "SkinData was null.");

        Player player = Bukkit.getPlayerExact(playerName);

        if(player != null)
        {
            skinApplier.apply(player, skinData);
        }
    }
}

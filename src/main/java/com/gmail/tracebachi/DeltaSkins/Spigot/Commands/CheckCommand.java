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

import com.gmail.tracebachi.DeltaSkins.Shared.Storage.PlayerProfile;
import com.gmail.tracebachi.DeltaSkins.Shared.Storage.PlayerProfileStorage;
import org.bukkit.command.CommandSender;

import static com.gmail.tracebachi.DeltaSkins.Shared.Settings.format;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee).
 */
public class CheckCommand
{
    private PlayerProfileStorage profileStorage;

    public CheckCommand(PlayerProfileStorage profileStorage)
    {
        this.profileStorage = profileStorage;
    }

    public boolean onCommand(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("DeltaSkins.Check"))
        {
            sender.sendMessage(format("NoPermission", "DeltaSkins.Check"));
            return true;
        }

        if(args.length < 2)
        {
            sender.sendMessage(format("CommandHelp", "/ds check <name>"));
            return true;
        }

        PlayerProfile sourceProfile = profileStorage.get(args[1]);
        String found = (sourceProfile != null) ? "Yes" : "No";

        sender.sendMessage(format("PlayerProfileCheck", args[1], found));
        return true;
    }
}

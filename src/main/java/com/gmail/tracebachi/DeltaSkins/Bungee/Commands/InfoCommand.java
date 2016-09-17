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

import com.gmail.tracebachi.DeltaSkins.Shared.Interfaces.IDeltaSkins;
import net.md_5.bungee.api.CommandSender;

import static com.gmail.tracebachi.DeltaSkins.Shared.Settings.format;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee).
 */
public class InfoCommand
{
    private final IDeltaSkins plugin;

    public InfoCommand(IDeltaSkins plugin)
    {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender)
    {
        if(!sender.hasPermission("DeltaSkins.Info"))
        {
            sender.sendMessage(format("NoPermission", "DeltaSkins.Info"));
        }
        else
        {
            sender.sendMessage(format("PlayerProfileInfo",
                String.valueOf(plugin.getPlayerProfileStorage().size()),
                String.valueOf(plugin.getMojangApi().getUuidRequestCount())));
        }
        return true;
    }
}

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
import org.bukkit.command.CommandSender;

import static com.gmail.tracebachi.DeltaSkins.Shared.Settings.format;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee).
 */
public class DropCommand
{
    private final IDeltaSkins plugin;

    public DropCommand(IDeltaSkins plugin)
    {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("DeltaSkins.Drop"))
        {
            sender.sendMessage(format("NoPermission", "DeltaSkins.Drop"));
            return true;
        }

        if(args.length < 2)
        {
            sender.sendMessage(format("CommandHelp", "/ds drop <name>"));
            return true;
        }

        plugin.getPlayerProfileStorage().remove(args[1]);
        sender.sendMessage(format("PlayerProfileDropped", args[1]));

        plugin.debug("[DropCommand] Removed player profile for " + args[1]);
        return true;
    }
}

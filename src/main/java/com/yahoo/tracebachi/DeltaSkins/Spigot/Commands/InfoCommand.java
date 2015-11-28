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
package com.yahoo.tracebachi.DeltaSkins.Spigot.Commands;

import com.yahoo.tracebachi.DeltaSkins.Shared.Interfaces.IDeltaSkins;
import com.yahoo.tracebachi.DeltaSkins.Spigot.Prefixes;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class InfoCommand
{
    private final String PERM_MESSAGE = ChatColor.translateAlternateColorCodes('&',
        "&8[&4!&8] &4Error &8[&4!&8]&7 You do not have the 'DeltaSkins.info' permission.");

    private final IDeltaSkins plugin;

    public InfoCommand(IDeltaSkins plugin)
    {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("DeltaSkins.info"))
        {
            sender.sendMessage(PERM_MESSAGE);
            return true;
        }

        sender.sendMessage(Prefixes.INFO + "Profiles in cache: " + ChatColor.WHITE +
            plugin.getPlayerProfileStorage().size());
        sender.sendMessage(Prefixes.INFO + "Pending UUID requests: " + ChatColor.WHITE +
            plugin.getMojangApi().getUuidRequestCount());
        return true;
    }
}

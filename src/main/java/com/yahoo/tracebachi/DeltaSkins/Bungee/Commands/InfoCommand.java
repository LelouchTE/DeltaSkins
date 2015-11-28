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
package com.yahoo.tracebachi.DeltaSkins.Bungee.Commands;

import com.yahoo.tracebachi.DeltaSkins.Bungee.Prefixes;
import com.yahoo.tracebachi.DeltaSkins.Shared.Interfaces.IDeltaSkins;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

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
            sender.sendMessage(TextComponent.fromLegacyText(PERM_MESSAGE));
            return true;
        }

        sender.sendMessage(TextComponent.fromLegacyText(Prefixes.INFO + "Profiles in cache: " +
            ChatColor.WHITE + plugin.getPlayerProfileStorage().size()));
        sender.sendMessage(TextComponent.fromLegacyText(Prefixes.INFO + "Pending UUID requests: " +
            ChatColor.WHITE + plugin.getMojangApi().getUuidRequestCount()));
        return true;
    }
}

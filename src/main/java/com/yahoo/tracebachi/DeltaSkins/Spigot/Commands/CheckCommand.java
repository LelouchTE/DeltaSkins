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

import com.yahoo.tracebachi.DeltaSkins.Shared.PlayerProfile;
import com.yahoo.tracebachi.DeltaSkins.Shared.PlayerProfileStorage;
import com.yahoo.tracebachi.DeltaSkins.Spigot.Prefixes;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CheckCommand
{
    private final String PERM_MESSAGE = ChatColor.translateAlternateColorCodes('&',
        "&8[&4!&8] &4Error &8[&4!&8]&7 You do not have the 'DeltaSkins.check' permission.");

    private final String HELP_MESSAGE = ChatColor.translateAlternateColorCodes('&',
        "&8[&9!&8] &9Help &8[&9!&8]&7 /deltaskins check <player name>");

    private PlayerProfileStorage profileStorage;

    public CheckCommand(PlayerProfileStorage profileStorage)
    {
        this.profileStorage = profileStorage;
    }

    public boolean onCommand(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("DeltaSkins.check"))
        {
            sender.sendMessage(PERM_MESSAGE);
            return true;
        }

        if(args.length < 2)
        {
            sender.sendMessage(HELP_MESSAGE);
            return true;
        }

        PlayerProfile sourceProfile = profileStorage.getProfile(args[1]);

        if(sourceProfile != null)
        {
            sender.sendMessage(Prefixes.SUCCESS + "Found player profile in cache for " + args[1]);
        }
        else
        {
            sender.sendMessage(Prefixes.FAILURE + "No player profile in cache for " + args[1]);
        }
        return true;
    }
}

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
import com.yahoo.tracebachi.DeltaSkins.Shared.PlayerProfile;
import com.yahoo.tracebachi.DeltaSkins.Shared.PlayerProfileStorage;
import com.yahoo.tracebachi.DeltaSkins.Spigot.Prefixes;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class SetCommand
{
    private final String PERM_MESSAGE = ChatColor.translateAlternateColorCodes('&',
        "&8[&4!&8] &4Error &8[&4!&8]&7 You do not have the 'DeltaSkins.set' permission.");

    private final String HELP_MESSAGE = ChatColor.translateAlternateColorCodes('&',
        "&8[&9!&8] &9Help &8[&9!&8]&7 /deltaskins set <destination> <source>");

    private final IDeltaSkins plugin;

    public SetCommand(IDeltaSkins plugin)
    {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("DeltaSkins.set"))
        {
            sender.sendMessage(PERM_MESSAGE);
            return true;
        }

        if(args.length < 3)
        {
            sender.sendMessage(HELP_MESSAGE);
            return true;
        }

        if(args[1].equalsIgnoreCase(args[2]))
        {
            sender.sendMessage(Prefixes.FAILURE + "Use update instead to fetch the original skin.");
            return true;
        }

        PlayerProfileStorage storage = plugin.getPlayerProfileStorage();
        PlayerProfile destProfile = storage.getProfile(args[1]);
        PlayerProfile sourceProfile = storage.getProfile(args[2]);

        if(sourceProfile == null)
        {
            plugin.getMojangApi().addUuidRequest(args[2]);
            plugin.debug("[SetCommand] Added UUID request for " + args[2]);

            if(destProfile == null)
            {
                plugin.getMojangApi().addUuidRequest(args[1]);
                plugin.debug("[SetCommand] Added UUID request for " + args[1]);
            }

            sender.sendMessage(Prefixes.FAILURE + "No data found for " + args[2] +
                ", but UUID request added. Try again later.");
        }
        else
        {
            if(destProfile != null)
            {
                destProfile.setSkinUuid(sourceProfile.getSkinUuid());
                destProfile.setSkinData(sourceProfile.getSkinData());

                plugin.debug("[SetCommand] Updated non-premium profile for " + args[1] +
                    " using skin data from " + sourceProfile.getName());
            }
            else
            {
                destProfile = new PlayerProfile(args[1], PlayerProfile.UNKNOWN_UUID);
                destProfile.setSkinUuid(sourceProfile.getSkinUuid());
                destProfile.setSkinData(sourceProfile.getSkinData());

                storage.addProfile(args[1], destProfile);

                plugin.debug("[SetCommand] Added non-premium profile for " + args[1] +
                    " using skin data from " + sourceProfile.getName());
            }

            sender.sendMessage(Prefixes.SUCCESS + "Copied skin data (" + args[1] + " <- " + args[2] + "). " +
                args[1] + " must relog for the skin to take effect.");
        }
        return true;
    }
}

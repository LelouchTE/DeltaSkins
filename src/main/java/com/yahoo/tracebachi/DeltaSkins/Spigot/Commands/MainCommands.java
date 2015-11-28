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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MainCommands implements CommandExecutor
{
    private CheckCommand checkCommand;
    private DropCommand dropCommand;
    private InfoCommand infoCommand;
    private SetCommand setCommand;
    private UpdateCommand updateCommand;

    public MainCommands(IDeltaSkins plugin)
    {
        this.checkCommand = new CheckCommand(plugin.getPlayerProfileStorage());
        this.dropCommand = new DropCommand(plugin);
        this.infoCommand = new InfoCommand(plugin);
        this.setCommand = new SetCommand(plugin);
        this.updateCommand = new UpdateCommand(plugin);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if(args.length >= 1)
        {
            switch(args[0].toLowerCase())
            {
                case "check":
                    checkCommand.onCommand(sender, args);
                    return true;
                case "drop":
                    dropCommand.onCommand(sender, args);
                    return true;
                case "set":
                    setCommand.onCommand(sender, args);
                    return true;
                case "update":
                    updateCommand.onCommand(sender, args);
                    return true;
                case "info":
                    infoCommand.onCommand(sender, args);
                    return true;
                default:
            }
        }

        sender.sendMessage(Prefixes.INFO + "-- " + ChatColor.BLUE + "DeltaSkins" + ChatColor.DARK_GRAY + "--");
        sender.sendMessage(Prefixes.INFO + "/deltaskins check <name>");
        sender.sendMessage(Prefixes.INFO + "/deltaskins drop <name>");
        sender.sendMessage(Prefixes.INFO + "/deltaskins set <dest> <source>");
        sender.sendMessage(Prefixes.INFO + "/deltaskins update <name>");
        sender.sendMessage(Prefixes.INFO + "/deltaskins info");
        return true;
    }
}

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
import com.gmail.tracebachi.DeltaSkins.Spigot.SkinApplier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import static org.bukkit.ChatColor.*;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee).
 */
public class MainCommands implements CommandExecutor
{
    private final String INFO = translateAlternateColorCodes('&',
        "&8[&9!&8] &9Info &8[&9!&8]&7 ");

    private CheckCommand checkCommand;
    private DropCommand dropCommand;
    private InfoCommand infoCommand;
    private ResetCommand resetCommand;
    private SetCommand setCommand;

    public MainCommands(SkinApplier skinApplier, IDeltaSkins plugin)
    {
        this.checkCommand = new CheckCommand(plugin.getPlayerProfileStorage());
        this.dropCommand = new DropCommand(plugin);
        this.infoCommand = new InfoCommand(plugin);
        this.resetCommand = new ResetCommand(skinApplier, plugin);
        this.setCommand = new SetCommand(skinApplier, plugin);
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
                case "reset":
                    resetCommand.onCommand(sender, args);
                    return true;
                case "set":
                    setCommand.onCommand(sender, args);
                    return true;
                case "info":
                    infoCommand.onCommand(sender);
                    return true;
                default:
            }
        }

        sender.sendMessage(INFO + "-- " + BLUE + "DeltaSkins" + DARK_GRAY + "--");
        sender.sendMessage(INFO + "/deltaskins check <name>");
        sender.sendMessage(INFO + "/deltaskins drop <name>");
        sender.sendMessage(INFO + "/deltaskins set <dest> <source>");
        sender.sendMessage(INFO + "/deltaskins reset <name>");
        sender.sendMessage(INFO + "/deltaskins info");
        return true;
    }
}

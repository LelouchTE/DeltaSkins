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
import net.md_5.bungee.api.plugin.Command;

public class MainCommands extends Command
{
    private CheckCommand checkCommand;
    private DropCommand dropCommand;
    private InfoCommand infoCommand;
    private SetCommand setCommand;
    private UpdateCommand updateCommand;

    public MainCommands(IDeltaSkins plugin)
    {
        super("deltaskins");
        this.checkCommand = new CheckCommand(plugin.getPlayerProfileStorage());
        this.dropCommand = new DropCommand(plugin);
        this.infoCommand = new InfoCommand(plugin);
        this.setCommand = new SetCommand(plugin);
        this.updateCommand = new UpdateCommand(plugin);
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if(args.length >= 1)
        {
            switch(args[0].toLowerCase())
            {
                case "check":
                    checkCommand.onCommand(sender, args);
                    return;
                case "drop":
                    dropCommand.onCommand(sender, args);
                    return;
                case "set":
                    setCommand.onCommand(sender, args);
                    return;
                case "update":
                    updateCommand.onCommand(sender, args);
                    return;
                case "info":
                    infoCommand.onCommand(sender, args);
                    return;
                default:
            }
        }

        sender.sendMessage(TextComponent.fromLegacyText(
            Prefixes.INFO + "-- " + ChatColor.BLUE + "DeltaSkins" + ChatColor.DARK_GRAY + "--"));
        sender.sendMessage(TextComponent.fromLegacyText(
            Prefixes.INFO + "/deltaskins check <name>"));
        sender.sendMessage(TextComponent.fromLegacyText(
            Prefixes.INFO + "/deltaskins drop <name>"));
        sender.sendMessage(TextComponent.fromLegacyText(
            Prefixes.INFO + "/deltaskins set <dest> <source>"));
        sender.sendMessage(TextComponent.fromLegacyText(
            Prefixes.INFO + "/deltaskins update <name>"));
        sender.sendMessage(TextComponent.fromLegacyText(
            Prefixes.INFO + "/deltaskins info"));
    }
}

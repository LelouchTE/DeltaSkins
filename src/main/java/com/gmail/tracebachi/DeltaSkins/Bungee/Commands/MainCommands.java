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

import com.gmail.tracebachi.DeltaSkins.Bungee.SkinApplier;
import com.gmail.tracebachi.DeltaSkins.Shared.Interfaces.IDeltaSkins;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import static net.md_5.bungee.api.ChatColor.*;
import static net.md_5.bungee.api.chat.TextComponent.fromLegacyText;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee).
 */
public class MainCommands extends Command
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
        super("deltaskins", null, "ds");
        this.checkCommand = new CheckCommand(plugin.getPlayerProfileStorage());
        this.dropCommand = new DropCommand(plugin);
        this.infoCommand = new InfoCommand(plugin);
        this.resetCommand = new ResetCommand(skinApplier, plugin);
        this.setCommand = new SetCommand(skinApplier, plugin);
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
                case "reset":
                    resetCommand.onCommand(sender, args);
                    return;
                case "set":
                    setCommand.onCommand(sender, args);
                    return;
                case "info":
                    infoCommand.onCommand(sender);
                    return;
                default:
            }
        }

        sender.sendMessage(fromLegacyText(
            INFO + "-- " + BLUE + "DeltaSkins" + DARK_GRAY + "--"));
        sender.sendMessage(fromLegacyText(
            INFO + "/deltaskins check <name>"));
        sender.sendMessage(fromLegacyText(
            INFO + "/deltaskins drop <name>"));
        sender.sendMessage(fromLegacyText(
            INFO + "/deltaskins set <dest> <source>"));
        sender.sendMessage(fromLegacyText(
            INFO + "/deltaskins reset <name>"));
        sender.sendMessage(fromLegacyText(
            INFO + "/deltaskins info"));
    }
}

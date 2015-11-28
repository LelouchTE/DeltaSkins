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
package com.yahoo.tracebachi.DeltaSkins.Shared.Runnables;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.yahoo.tracebachi.DeltaSkins.Shared.Interfaces.IDeltaSkins;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 10/30/15.
 */
public class ProfileFileSaveRunnable implements Runnable
{
    private final File destination;
    private final IDeltaSkins plugin;

    public ProfileFileSaveRunnable(File destination, IDeltaSkins plugin)
    {
        this.plugin = plugin;
        this.destination = destination;
    }

    @Override
    public void run()
    {
        try
        {
            plugin.debug("Write profiles to file ...");
            JsonArray array = plugin.getPlayerProfileStorage().serialize();

            try(BufferedWriter writer = new BufferedWriter(new FileWriter(destination)))
            {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(array, writer);
                writer.close();
            }

            plugin.debug("... Finished.");
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}

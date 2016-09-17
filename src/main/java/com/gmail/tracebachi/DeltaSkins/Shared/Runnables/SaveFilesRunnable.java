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
package com.gmail.tracebachi.DeltaSkins.Shared.Runnables;

import com.gmail.tracebachi.DeltaSkins.Shared.Interfaces.IDeltaSkins;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 10/30/15.
 */
public class SaveFilesRunnable implements Runnable
{
    private final File profileFile;
    private final File skinDataFile;
    private final IDeltaSkins plugin;

    public SaveFilesRunnable(File profileFile, File skinDataFile, IDeltaSkins plugin)
    {
        this.profileFile = Preconditions.checkNotNull(profileFile, "ProfileFile was null.");
        this.skinDataFile = Preconditions.checkNotNull(skinDataFile, "SkinDataFile was null.");
        this.plugin = Preconditions.checkNotNull(plugin, "Plugin was null.");
    }

    @Override
    public void run()
    {
        try
        {
            try(BufferedWriter writer = new BufferedWriter(new FileWriter(profileFile)))
            {
                JsonArray serialized = plugin.getPlayerProfileStorage().serialize();
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(serialized, writer);
                writer.close();
            }

            plugin.debug("Finished writing to " + profileFile.getAbsolutePath() + '.');
        }
        catch(IOException e)
        {
            plugin.severe("Failed writing to " + profileFile.getAbsolutePath() + '.');
            e.printStackTrace();
        }

        try
        {
            try(BufferedWriter writer = new BufferedWriter(new FileWriter(skinDataFile)))
            {
                JsonArray serialized = plugin.getSkinDataStorage().serialize();
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(serialized, writer);
                writer.close();
            }

            plugin.debug("Finished writing to " + skinDataFile.getAbsolutePath() + '.');
        }
        catch(IOException e)
        {
            plugin.severe("Failed writing to " + skinDataFile.getAbsolutePath() + '.');
            e.printStackTrace();
        }
    }
}

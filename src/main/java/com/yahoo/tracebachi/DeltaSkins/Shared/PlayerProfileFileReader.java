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
package com.yahoo.tracebachi.DeltaSkins.Shared;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yahoo.tracebachi.DeltaSkins.Shared.Interfaces.IDeltaSkins;

import java.io.*;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 11/27/15.
 */
public interface PlayerProfileFileReader
{
    static boolean read(long discardOlderThan, File file, IDeltaSkins plugin)
    {
        BufferedReader reader = null;
        JsonElement fileElement;
        PlayerProfileStorage storage = plugin.getPlayerProfileStorage();

        try
        {
            reader = new BufferedReader(new FileReader(file));
            JsonParser parser = new JsonParser();
            fileElement = parser.parse(reader);
            reader.close();
        }
        catch(FileNotFoundException e)
        {
            plugin.info(file.getName() + " was not found. Starting with an empty file.");
            e.printStackTrace();
            return false;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            try { reader.close(); }
            catch(IOException ee) { ee.printStackTrace(); }
            return false;
        }

        if(fileElement == null || !fileElement.isJsonArray())
        {
            plugin.info(file.getName() + " is not a JsonArray. Starting with an empty file.");
            return false;
        }

        plugin.debug("Deserializing player profiles ...");
        long currentTime = System.currentTimeMillis();
        for(JsonElement element : fileElement.getAsJsonArray())
        {
            try
            {
                JsonObject object = element.getAsJsonObject();
                PlayerProfile profile = PlayerProfile.deserialize(object);
                SkinData skinData = profile.getSkinData();

                if(skinData != null)
                {
                    long timeDiff = currentTime - skinData.getCreatedAt();
                    boolean ignoreTimeDiff = profile.getPlayerUuid().equals(PlayerProfile.UNKNOWN_UUID);

                    if(ignoreTimeDiff || timeDiff < discardOlderThan)
                    {
                        storage.addProfile(profile.getName(), profile);
                    }
                    else
                    {
                        plugin.debug("Removed profile for " + profile.getName() +
                            " due to inactivity.");
                    }
                }
            }
            catch(NullPointerException ex)
            {
                ex.printStackTrace();
                plugin.info("Failed to parse object:\n" + element);
            }
        }
        plugin.debug("... Finished.");

        return true;
    }
}

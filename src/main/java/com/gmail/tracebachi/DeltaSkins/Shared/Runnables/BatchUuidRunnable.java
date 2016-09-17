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

import com.gmail.tracebachi.DeltaSkins.Shared.Exceptions.RateLimitedException;
import com.gmail.tracebachi.DeltaSkins.Shared.Interfaces.IDeltaSkins;
import com.gmail.tracebachi.DeltaSkins.Shared.Storage.PlayerProfile;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 10/30/15.
 */
public class BatchUuidRunnable implements Runnable
{
    private final long requestIntervalMillis;
    private final IDeltaSkins plugin;

    private long lastRunTime = 0L;

    public BatchUuidRunnable(long requestIntervalSeconds, IDeltaSkins plugin)
    {
        this.requestIntervalMillis = requestIntervalSeconds * 1000;
        this.plugin = plugin;
    }

    @Override
    public void run()
    {
        long currentTime = System.currentTimeMillis();
        if((currentTime - lastRunTime) < requestIntervalMillis) { return; }

        lastRunTime = currentTime;

        JsonArray jsonArray;

        try
        {
            jsonArray = plugin.getMojangApi().getUuids();

            if(jsonArray == null) { return; }
        }
        catch(RateLimitedException e)
        {
            plugin.info("Rate limited in fetching UUIDs.");
            return;
        }
        catch(IOException e)
        {
            plugin.severe("Failed to fetch UUIDs.");
            e.printStackTrace();
            return;
        }

        for(JsonElement element : jsonArray)
        {
            JsonObject object = element.getAsJsonObject();
            String uuid = object.get("id").getAsString();
            String name = object.get("name").getAsString();
            PlayerProfile profile = new PlayerProfile(name, uuid, uuid);

            // Update skin based on the profile.
            plugin.onProfileFound(profile);
        }
    }
}

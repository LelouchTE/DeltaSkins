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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.yahoo.tracebachi.DeltaSkins.Shared.Exceptions.RateLimitedException;
import com.yahoo.tracebachi.DeltaSkins.Shared.Interfaces.IDeltaSkins;
import com.yahoo.tracebachi.DeltaSkins.Shared.PlayerProfile;
import com.yahoo.tracebachi.DeltaSkins.Shared.PlayerProfileStorage;

import java.io.IOException;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 10/30/15.
 */
public class BatchUuidRunnable implements Runnable
{
    private final long requestInterval;
    private final IDeltaSkins plugin;

    private long lastRunTime = 0L;

    public BatchUuidRunnable(long requestInterval, IDeltaSkins plugin)
    {
        this.requestInterval = requestInterval;
        this.plugin = plugin;
    }

    @Override
    public void run()
    {
        JsonArray jsonArray;
        PlayerProfileStorage storage = plugin.getPlayerProfileStorage();

        if((System.currentTimeMillis() - lastRunTime) > requestInterval)
        {
            lastRunTime = System.currentTimeMillis();
        }
        else { return; }

        try
        {
            jsonArray = plugin.getMojangApi().getUuids();
            if(jsonArray == null) { return; }
        }
        catch(IOException | RateLimitedException e)
        {
            e.printStackTrace();
            return;
        }

        for(JsonElement element : jsonArray)
        {
            JsonObject object = element.getAsJsonObject();
            String uuid = object.get("id").getAsString();
            String name = object.get("name").getAsString();

            PlayerProfile profile = storage.getProfile(name);

            if(profile == null)
            {
                profile = new PlayerProfile(name, uuid);
                storage.addProfile(name, profile);
                plugin.runAsyncSkinFetch(profile, uuid);
            }
            else if(profile.getPlayerUuid().equals(PlayerProfile.UNKNOWN_UUID))
            {
                PlayerProfile updatedProfile = new PlayerProfile(name, uuid);

                updatedProfile.setSkinUuid(profile.getSkinUuid());
                updatedProfile.setSkinData(profile.getSkinData());
                storage.addProfile(name, updatedProfile);
            }
        }
    }
}

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
package com.gmail.tracebachi.DeltaSkins.Shared.Storage;

import com.gmail.tracebachi.DeltaSkins.Shared.Interfaces.JsonSerializable;
import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.concurrent.ConcurrentHashMap;

/**
 * GeeItsZee (tracebachi@gmail.com)
 */
public class SkinDataStorage implements JsonSerializable
{
    private final ConcurrentHashMap<String, SkinData> skinDataMap = new ConcurrentHashMap<>();

    private final long removeOlderThanMinutes;

    public SkinDataStorage(long removeOlderThanMinutes)
    {
        this.removeOlderThanMinutes = removeOlderThanMinutes;
    }

    public SkinData get(String uuid)
    {
        Preconditions.checkNotNull(uuid, "UUID was null.");

        SkinData skinData = skinDataMap.get(uuid);

        if(skinData == null) { return null; }

        if((System.currentTimeMillis() - skinData.getCreatedAt()) >=
            removeOlderThanMinutes * 60 * 1000)
        {
            skinDataMap.remove(uuid);
            return null;
        }

        return skinData;
    }

    public SkinData remove(String uuid)
    {
        Preconditions.checkNotNull(uuid, "UUID was null.");
        return skinDataMap.remove(uuid);
    }

    public SkinData put(String uuid, SkinData skinData)
    {
        Preconditions.checkNotNull(uuid, "UUID was null.");
        Preconditions.checkNotNull(skinData, "SkinData was null.");

        return skinDataMap.put(uuid, skinData);
    }

    public int size()
    {
        return skinDataMap.size();
    }

    @Override
    public JsonArray serialize()
    {
        JsonArray array = new JsonArray();

        synchronized(skinDataMap)
        {
            for(SkinData skinData : skinDataMap.values())
            {
                if((System.currentTimeMillis() - skinData.getCreatedAt()) <
                    removeOlderThanMinutes * 60 * 1000)
                {
                    array.add(skinData.serialize());
                }
            }
        }

        return array;
    }

    public int deserialize(JsonArray array)
    {
        Preconditions.checkNotNull(array, "Array was null.");

        int loaded = 0;

        for(JsonElement element : array)
        {
            if(element.isJsonObject())
            {
                try
                {
                    SkinData skinData = SkinData.deserialize(element.getAsJsonObject());

                    skinDataMap.put(skinData.getUuid(), skinData);
                    loaded += 1;
                }
                catch(NullPointerException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return loaded;
    }
}

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
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 9/10/16.
 */
public class PlayerProfileStorage implements JsonSerializable
{
    private final ConcurrentHashMap<String, PlayerProfile> profileMap = new ConcurrentHashMap<>();

    public PlayerProfile get(String name)
    {
        Preconditions.checkNotNull(name, "Name was null.");
        return profileMap.get(name.toLowerCase());
    }

    public PlayerProfile remove(String name)
    {
        Preconditions.checkNotNull(name, "Name was null.");
        return profileMap.remove(name.toLowerCase());
    }

    public PlayerProfile put(PlayerProfile profile)
    {
        Preconditions.checkNotNull(profile, "Profile was null.");

        return profileMap.put(profile.getName().toLowerCase(), profile);
    }

    public int size()
    {
        return profileMap.size();
    }

    @Override
    public JsonArray serialize()
    {
        JsonArray array = new JsonArray();

        synchronized(profileMap)
        {
            for(PlayerProfile profile : profileMap.values())
            {
                array.add(profile.serialize());
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
                    PlayerProfile profile = PlayerProfile.deserialize(element.getAsJsonObject());

                    profileMap.put(profile.getName().toLowerCase(), profile);
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

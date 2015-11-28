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

import com.google.gson.JsonArray;
import com.yahoo.tracebachi.DeltaSkins.Shared.Interfaces.IDeltaSkins;
import com.yahoo.tracebachi.DeltaSkins.Shared.Interfaces.JsonSerializable;

import java.util.concurrent.ConcurrentHashMap;

public class PlayerProfileStorage implements JsonSerializable
{
    private final ConcurrentHashMap<String, PlayerProfile> profileMap = new ConcurrentHashMap<>();
    private final IDeltaSkins plugin;

    public PlayerProfileStorage(IDeltaSkins plugin)
    {
        this.plugin = plugin;
    }

    public PlayerProfile getProfile(String name)
    {
        return profileMap.get(name.toLowerCase());
    }

    public PlayerProfile removeProfile(String name)
    {
        return profileMap.remove(name.toLowerCase());
    }

    public PlayerProfile addProfile(String name, PlayerProfile profile)
    {
        return profileMap.put(name.toLowerCase(), profile);
    }

    public int size()
    {
        return profileMap.size();
    }

    @Override
    public JsonArray serialize()
    {
        JsonArray array = new JsonArray();

        plugin.debug("Serializing player profiles ...");
        synchronized(profileMap)
        {
            for(PlayerProfile profile : profileMap.values())
            {
                try
                {
                    // If at least one of the UUIDs is known
                    if(!profile.getPlayerUuid().equals(PlayerProfile.UNKNOWN_UUID) ||
                        !profile.getSkinUuid().equals(PlayerProfile.UNKNOWN_UUID))
                    {
                        array.add(profile.serialize());
                    }
                }
                catch(NullPointerException ex)
                {
                    ex.printStackTrace();
                    plugin.info("Failed to serialize profile for (" + profile.getName());
                }
            }
        }
        plugin.debug("... Finished.");

        return array;
    }
}

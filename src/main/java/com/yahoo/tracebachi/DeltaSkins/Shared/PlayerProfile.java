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
import com.yahoo.tracebachi.DeltaSkins.Shared.Interfaces.JsonSerializable;

public class PlayerProfile implements JsonSerializable
{
    public static final String UNKNOWN_UUID = "00000000000000000000000000000000";

    private final String name;
    private final String playerUuid;
    private String skinUuid = UNKNOWN_UUID;
    private SkinData skinData;

    public PlayerProfile(String name, String playerUuid)
    {
        this.name = name.toLowerCase();
        this.playerUuid = playerUuid != null ? playerUuid : UNKNOWN_UUID;
    }

    public String getName()
    {
        return name;
    }

    public String getPlayerUuid()
    {
        return playerUuid;
    }

    public synchronized String getSkinUuid()
    {
        return skinUuid;
    }

    public synchronized SkinData getSkinData()
    {
        return skinData;
    }

    public synchronized void setSkinUuid(String skinUuid)
    {
        if(skinUuid == null)
        {
            skinUuid = UNKNOWN_UUID;
        }
        this.skinUuid = skinUuid;
    }

    public synchronized void setSkinData(SkinData skinData)
    {
        this.skinData = skinData.clone();
    }

    @Override
    public synchronized JsonObject serialize()
    {
        JsonObject object = new JsonObject();
        object.addProperty("name", name);
        object.addProperty("playerUuid", playerUuid);
        object.addProperty("skinUuid", skinUuid);

        if(skinData != null)
        {
            object.add("skinData", skinData.serialize());
        }
        return object;
    }

    public static PlayerProfile deserialize(JsonObject object)
    {
        String name = object.get("name").getAsString();
        String playerUuid = object.get("playerUuid").getAsString();
        String skinUuid = object.get("skinUuid").getAsString();
        JsonElement jsonSkinData = object.get("skinData");

        PlayerProfile result = new PlayerProfile(name, playerUuid);
        result.skinUuid = skinUuid;

        if(jsonSkinData != null)
        {
            result.skinData = SkinData.deserialize(jsonSkinData.getAsJsonObject());
        }
        return result;
    }
}

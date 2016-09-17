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
import com.google.gson.JsonObject;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 9/10/16.
 */
public class PlayerProfile implements JsonSerializable
{
    public static final String NULL_UUID = "00000000000000000000000000000000";

    private final String name;
    private final String playerUuid;
    private final String skinUuid;

    public PlayerProfile(String name, String playerUuid, String skinUuid)
    {
        this.name = Preconditions.checkNotNull(name, "Name was null.");
        this.playerUuid = (playerUuid == null) ? NULL_UUID : playerUuid;
        this.skinUuid = Preconditions.checkNotNull(skinUuid, "SkinUuid was null.");
    }

    public String getName()
    {
        return name;
    }

    public String getPlayerUuid()
    {
        return playerUuid;
    }

    public String getSkinUuid()
    {
        return skinUuid;
    }

    public boolean hasPlayerUuid()
    {
        return !NULL_UUID.equals(playerUuid);
    }

    @Override
    public String toString()
    {
        return "{" +
            "name: " + name + ", " +
            "playerUuid: " + playerUuid + ", " +
            "skinUuid: " + skinUuid + "}";
    }

    @Override
    public synchronized JsonObject serialize()
    {
        JsonObject object = new JsonObject();
        object.addProperty("name", name);
        object.addProperty("playerUuid", playerUuid);
        object.addProperty("skinUuid", skinUuid);
        return object;
    }

    public static PlayerProfile deserialize(JsonObject object)
    {
        String name = object.get("name").getAsString();
        String playerUuid = object.get("playerUuid").getAsString();
        String skinUuid = object.get("skinUuid").getAsString();
        return new PlayerProfile(name, playerUuid, skinUuid);
    }
}

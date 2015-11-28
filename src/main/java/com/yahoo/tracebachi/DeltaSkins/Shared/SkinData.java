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

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 10/29/15.
 */
public class SkinData implements JsonSerializable
{
    private final long createdAt;
    private final String value;
    private final String signature;

    public SkinData(String value, String signature)
    {
        this(System.currentTimeMillis(), value, signature);
    }

    public SkinData(long createdAt, String value, String signature)
    {
        this.createdAt = createdAt;
        this.value = value;
        this.signature = signature;
    }

    public long getCreatedAt()
    {
        return createdAt;
    }

    public String getValue()
    {
        return value;
    }

    public String getSignature()
    {
        return signature;
    }

    public SkinData clone()
    {
        return new SkinData(this.createdAt, this.value, this.signature);
    }

    @Override
    public JsonObject serialize()
    {
        JsonObject object = new JsonObject();
        object.addProperty("createdAt", createdAt);
        object.addProperty("value", value);
        object.addProperty("signature", signature);
        return object;
    }

    public static SkinData deserialize(JsonObject object)
    {
        JsonElement createdAt = object.get("createdAt");
        String value = object.get("value").getAsString();
        String signature = object.get("signature").getAsString();

        if(createdAt != null)
        {
            return new SkinData(createdAt.getAsLong(), value, signature);
        }
        else
        {
            return new SkinData(value, signature);
        }
    }
}

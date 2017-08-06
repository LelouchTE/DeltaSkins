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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * GeeItsZee (tracebachi@gmail.com)
 */
public class SkinData implements JsonSerializable
{
    private final String uuid;
    private final String value;
    private final String signature;
    private final long createdAt;

    public SkinData(String uuid, String value, String signature)
    {
        this(uuid, value, signature, System.currentTimeMillis());
    }

    public SkinData(String uuid, String value, String signature, long createdAt)
    {
        this.uuid = Preconditions.checkNotNull(uuid, "UUID was null.");
        this.value = Preconditions.checkNotNull(value, "Value was null.");
        this.signature = Preconditions.checkNotNull(signature, "Signature was null.");
        this.createdAt = createdAt;
    }

    public String getUuid()
    {
        return uuid;
    }

    public String getValue()
    {
        return value;
    }

    public String getSignature()
    {
        return signature;
    }

    public long getCreatedAt()
    {
        return createdAt;
    }

    @Override
    public JsonObject serialize()
    {
        JsonObject object = new JsonObject();
        object.addProperty("uuid", uuid);
        object.addProperty("value", value);
        object.addProperty("signature", signature);
        object.addProperty("createdAt", createdAt);
        return object;
    }

    public static SkinData deserialize(JsonObject object)
    {
        String uuid = object.get("uuid").getAsString();
        String value = object.get("value").getAsString();
        String signature = object.get("signature").getAsString();
        JsonElement createdAt = object.get("createdAt");

        if(createdAt != null)
        {
            return new SkinData(uuid, value, signature, createdAt.getAsLong());
        }
        else
        {
            return new SkinData(uuid, value, signature);
        }
    }
}

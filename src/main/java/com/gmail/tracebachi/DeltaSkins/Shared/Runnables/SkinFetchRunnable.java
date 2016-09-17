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
import com.gmail.tracebachi.DeltaSkins.Shared.Storage.SkinData;
import com.google.gson.JsonObject;

import java.io.IOException;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 10/30/15.
 */
public class SkinFetchRunnable implements Runnable
{
    private final PlayerProfile profile;
    private final IDeltaSkins plugin;

    public SkinFetchRunnable(PlayerProfile profile, IDeltaSkins plugin)
    {
        this.plugin = plugin;
        this.profile = profile;
    }

    @Override
    public void run()
    {
        String skinUuid = profile.getSkinUuid();

        if(skinUuid.equals(PlayerProfile.NULL_UUID))
        {
            plugin.severe(
                "SkinUuid in profile for " + profile.getName() + " is null. " +
                "Skin cannot be fetched with invalid profile.");
            return;
        }

        try
        {
            JsonObject skinObject = plugin.getMojangApi().getSkin(skinUuid);

            if(skinObject != null && skinObject.has("properties"))
            {
                JsonObject skinProperties = skinObject
                    .get("properties")
                    .getAsJsonArray()
                    .get(0)
                    .getAsJsonObject();
                SkinData skinData = new SkinData(
                    skinUuid,
                    skinProperties.get("value").getAsString(),
                    skinProperties.get("signature").getAsString());

                plugin.onSkinFetched(profile, skinData);
            }
        }
        catch(RateLimitedException ex)
        {
            plugin.info("Rate limited in fetching skin for {" +
                "name: " + profile.getName() + ", " +
                "skinUuid: " + skinUuid + "}.");
        }
        catch(NullPointerException ex)
        {
            plugin.severe("Failed to deserialize skin data for {" +
                "name: " + profile.getName() + ", " +
                "skinUuid: " + skinUuid + "}.");
            ex.printStackTrace();
        }
        catch(IOException ex)
        {
            plugin.severe("Failed to fetch skin data for {" +
                "name: " + profile.getName() + ", " +
                "skinUuid: " + skinUuid + "}.");
            ex.printStackTrace();
        }
    }
}

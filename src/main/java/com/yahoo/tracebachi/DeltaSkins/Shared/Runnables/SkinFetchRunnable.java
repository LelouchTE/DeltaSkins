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

import com.google.gson.JsonObject;
import com.yahoo.tracebachi.DeltaSkins.Shared.Exceptions.RateLimitedException;
import com.yahoo.tracebachi.DeltaSkins.Shared.Interfaces.IDeltaSkins;
import com.yahoo.tracebachi.DeltaSkins.Shared.PlayerProfile;
import com.yahoo.tracebachi.DeltaSkins.Shared.SkinData;

import java.io.IOException;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 10/30/15.
 */
public class SkinFetchRunnable implements Runnable
{
    private final IDeltaSkins plugin;
    private final PlayerProfile profile;
    private final String uuidToUse;

    public SkinFetchRunnable(PlayerProfile profile, String uuidToUse, IDeltaSkins plugin)
    {
        this.plugin = plugin;
        this.profile = profile;
        this.uuidToUse = uuidToUse;
    }

    @Override
    public void run()
    {
        if(profile.getPlayerUuid() == null)
        {
            plugin.severe("UUID in profile is null. " +
                "Skin cannot be fetched with invalid profile.");
            return;
        }

        if(profile.getName() == null)
        {
            plugin.severe("UUID in profile is null. " +
                "Skin cannot be fetched with invalid profile.");
            return;
        }

        try
        {
            JsonObject skinObject = plugin.getMojangApi().getSkin(uuidToUse);

            if(skinObject != null && skinObject.has("properties"))
            {
                JsonObject skinProperties = skinObject.get("properties")
                    .getAsJsonArray().get(0).getAsJsonObject();

                profile.setSkinUuid(uuidToUse);
                profile.setSkinData(SkinData.deserialize(skinProperties));
            }
        }
        catch(RateLimitedException ex)
        {
            plugin.info("Rate limited in fetching skin for " +
                profile.getName() + '.');
        }
        catch(NullPointerException ex)
        {
            plugin.severe("Failed to deserialize skin data for " +
                profile.getName() + '.');
            ex.printStackTrace();
        }
        catch(IOException ex)
        {
            plugin.severe("Failed to fetch skin data for " +
                profile.getName() + '.');
            ex.printStackTrace();
        }
    }
}

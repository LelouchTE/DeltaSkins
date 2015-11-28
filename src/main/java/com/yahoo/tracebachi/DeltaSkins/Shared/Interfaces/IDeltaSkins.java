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
package com.yahoo.tracebachi.DeltaSkins.Shared.Interfaces;

import com.yahoo.tracebachi.DeltaSkins.Shared.MojangApi;
import com.yahoo.tracebachi.DeltaSkins.Shared.PlayerProfile;
import com.yahoo.tracebachi.DeltaSkins.Shared.PlayerProfileStorage;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 11/27/15.
 */
public interface IDeltaSkins
{
    MojangApi getMojangApi();

    PlayerProfileStorage getPlayerProfileStorage();

    void info(String message);

    void severe(String message);

    void debug(String message);

    void debugApi(String message);

    void runAsyncSkinFetch(PlayerProfile profile, String uuidToUse);

    void sendDelayedMessage(String name, String message);
}

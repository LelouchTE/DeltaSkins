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
package com.gmail.tracebachi.DeltaSkins.Shared.Interfaces;

import com.gmail.tracebachi.DeltaSkins.Shared.MojangApi;
import com.gmail.tracebachi.DeltaSkins.Shared.Storage.PlayerProfile;
import com.gmail.tracebachi.DeltaSkins.Shared.Storage.PlayerProfileStorage;
import com.gmail.tracebachi.DeltaSkins.Shared.Storage.SkinData;
import com.gmail.tracebachi.DeltaSkins.Shared.Storage.SkinDataStorage;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 11/27/15.
 */
public interface IDeltaSkins extends IDeltaSkinsLogger
{
    MojangApi getMojangApi();

    PlayerProfileStorage getPlayerProfileStorage();

    SkinDataStorage getSkinDataStorage();

    void onProfileFound(PlayerProfile profile);

    void fetchSkin(PlayerProfile profile);

    void onSkinFetched(PlayerProfile profile, SkinData skinData);
}

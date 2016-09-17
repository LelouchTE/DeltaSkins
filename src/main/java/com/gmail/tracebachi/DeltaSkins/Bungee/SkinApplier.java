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
package com.gmail.tracebachi.DeltaSkins.Bungee;

import com.gmail.tracebachi.DeltaSkins.Shared.Interfaces.IDeltaSkinsLogger;
import com.gmail.tracebachi.DeltaSkins.Shared.Storage.SkinData;
import com.google.common.base.Preconditions;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 9/16/16.
 */
public class SkinApplier
{
    private static final String TEXTURES_PROPERTY_NAME = "textures";

    private final Field loginProfileField;
    private final IDeltaSkinsLogger logger;

    public SkinApplier(IDeltaSkinsLogger logger)
    {
        loginProfileField = getLoginProfileField();
        this.logger = logger;
    }

    public void apply(ProxiedPlayer player, SkinData skinData)
    {
        Preconditions.checkNotNull(player, "Player was null.");
        Preconditions.checkNotNull(skinData, "SkinData was null.");

        InitialHandler handler = (InitialHandler) player.getPendingConnection();
        String uuidString = player.getUniqueId().toString();
        LoginResult newLoginResult = addTextureProperty(handler, uuidString, skinData);

        try
        {
            loginProfileField.set(handler, newLoginResult);
            applySkinOnServer(player, skinData);
        }
        catch(IllegalAccessException e)
        {
            logger.debug("Failed to apply SkinData {" +
                "playerName: " + player.getName() + ", " +
                "skinUuid: " + skinData.getUuid() + "}.");
            e.printStackTrace();
        }
    }

    private LoginResult addTextureProperty(InitialHandler handler, String uuid, SkinData skinData)
    {
        LoginResult.Property textureProperty = new LoginResult.Property(
            TEXTURES_PROPERTY_NAME,
            skinData.getValue(),
            skinData.getSignature());

        if(handler == null ||
            handler.getLoginProfile() == null ||
            handler.getLoginProfile().getProperties() == null)
        {
            return new LoginResult(uuid, new LoginResult.Property[] { textureProperty });
        }

        LoginResult.Property[] oldProperties = handler.getLoginProfile().getProperties();

        for(int i = 0; i < oldProperties.length; i++)
        {
            if(oldProperties[i].getName().equals(TEXTURES_PROPERTY_NAME))
            {
                oldProperties[i] = textureProperty;
                return new LoginResult(uuid, oldProperties);
            }
        }

        LoginResult.Property[] newProperties = new LoginResult.Property[oldProperties.length + 1];

        System.arraycopy(oldProperties, 0, newProperties, 0, oldProperties.length);
        newProperties[oldProperties.length] = textureProperty;
        return new LoginResult(uuid, newProperties);
    }

    private static Field getLoginProfileField()
    {
        try
        {
            Field loginProfileField = InitialHandler.class.getDeclaredField("loginProfile");
            loginProfileField.setAccessible(true);
            return loginProfileField;
        }
        catch(NoSuchFieldException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    private static void applySkinOnServer(ProxiedPlayer player, SkinData skinData)
    {
        Preconditions.checkNotNull(player, "Player was null.");
        Preconditions.checkNotNull(skinData, "SkinData was null.");

        // If the server is null, the player is not yet connected to a server.
        // We don't need to do anything after this to update their skin.
        if(player.getServer() == null) { return; }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(byteArrayOutputStream);

        try
        {
            out.writeUTF(player.getName());
            out.writeUTF(skinData.getUuid());
            out.writeUTF(skinData.getValue());
            out.writeUTF(skinData.getSignature());

            player.getServer().sendData(
                "DeltaSkins",
                byteArrayOutputStream.toByteArray());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}

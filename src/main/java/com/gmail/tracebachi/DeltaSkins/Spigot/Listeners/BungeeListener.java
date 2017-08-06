package com.gmail.tracebachi.DeltaSkins.Spigot.Listeners;

import com.gmail.tracebachi.DeltaSkins.Shared.Storage.SkinData;
import com.gmail.tracebachi.DeltaSkins.Spigot.SkinApplier;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * GeeItsZee (tracebachi@gmail.com)
 */
public class BungeeListener implements PluginMessageListener
{
    private final SkinApplier skinApplier;

    public BungeeListener(SkinApplier skinApplier)
    {
        this.skinApplier = skinApplier;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes)
    {
        if (!channel.equals("DeltaSkins")) { return; }

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        DataInputStream in = new DataInputStream(byteArrayInputStream);

        try
        {
            String name = in.readUTF();
            String uuid = in.readUTF();
            String value = in.readUTF();
            String signature = in.readUTF();
            SkinData skinData = new SkinData(uuid, value, signature);
            Player targetPlayer = Bukkit.getPlayerExact(name);

            if(targetPlayer != null)
            {
                skinApplier.apply(targetPlayer, skinData);
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}

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
package com.gmail.tracebachi.DeltaSkins.Spigot;

import com.gmail.tracebachi.DeltaSkins.Shared.Storage.SkinData;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_10_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;

import static net.minecraft.server.v1_10_R1.PacketPlayOutEntity.*;

/**
 * GeeItsZee (tracebachi@gmail.com)
 */
public class SkinApplier
{
    private static final String TEXTURES_PROPERTY_NAME = "textures";

    private final JavaPlugin plugin;

    public SkinApplier(JavaPlugin plugin)
    {
        this.plugin = plugin;
    }

    public void apply(Player player, SkinData skinData)
    {
        Location location = player.getLocation();
        PlayerInventory playerInventory = player.getInventory();
        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer entityPlayer = craftPlayer.getHandle();
        World entityPlayerWorld = entityPlayer.getWorld();
        int entityId = entityPlayer.getId();
        GameProfile profile = entityPlayer.getProfile();
        Property textureProperty = new Property(
            TEXTURES_PROPERTY_NAME,
            skinData.getValue(),
            skinData.getSignature());

        profile.getProperties().clear();
        profile.getProperties().put(TEXTURES_PROPERTY_NAME, textureProperty);

        PacketPlayOutPlayerInfo removeInfo = new PacketPlayOutPlayerInfo(
            PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,
            entityPlayer);

        PacketPlayOutEntityDestroy entityDestroy = new PacketPlayOutEntityDestroy(
            entityId);

        PacketPlayOutPlayerInfo addInfo = new PacketPlayOutPlayerInfo(
            PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER,
            entityPlayer);

        PacketPlayOutRespawn respawn = new PacketPlayOutRespawn(
            entityPlayerWorld.worldProvider.getDimensionManager().getDimensionID(),
            entityPlayerWorld.getDifficulty(),
            entityPlayerWorld.worldData.getType(),
            EnumGamemode.getById(player.getGameMode().getValue()));

        PacketPlayOutPosition position = new PacketPlayOutPosition(
            location.getX(),
            location.getY(),
            location.getZ(),
            location.getYaw(),
            location.getPitch(),
            Collections.emptySet(),
            0);

        PacketPlayOutEntityHeadRotation headLook = new PacketPlayOutEntityHeadRotation(
            entityPlayer,
            (byte) ((int) (location.getYaw() * 256.0F / 360.0F)));

        PacketPlayOutEntityLook bodyLook = new PacketPlayOutEntityLook(
            entityId,
            (byte) ((int) (location.getYaw() * 256.0F / 360.0F)),
            (byte) ((int) (location.getPitch() * 256.0F / 360.0F)),
            entityPlayer.onGround);

        PacketPlayOutHeldItemSlot heldItemSlot = new PacketPlayOutHeldItemSlot(
            playerInventory.getHeldItemSlot());

        PacketPlayOutNamedEntitySpawn namedEntitySpawn = new PacketPlayOutNamedEntitySpawn(
            entityPlayer);

        PacketPlayOutEntityEquipment mainHand = new PacketPlayOutEntityEquipment(
            entityId,
            EnumItemSlot.MAINHAND,
            CraftItemStack.asNMSCopy(playerInventory.getItemInMainHand()));

        PacketPlayOutEntityEquipment offHand = new PacketPlayOutEntityEquipment(
            entityId,
            EnumItemSlot.OFFHAND,
            CraftItemStack.asNMSCopy(playerInventory.getItemInOffHand()));

        PacketPlayOutEntityEquipment helmet = new PacketPlayOutEntityEquipment(
            entityId,
            EnumItemSlot.HEAD,
            CraftItemStack.asNMSCopy(playerInventory.getHelmet()));

        PacketPlayOutEntityEquipment chestplate = new PacketPlayOutEntityEquipment(
            entityId,
            EnumItemSlot.CHEST,
            CraftItemStack.asNMSCopy(playerInventory.getChestplate()));

        PacketPlayOutEntityEquipment leggings = new PacketPlayOutEntityEquipment(
            entityId,
            EnumItemSlot.LEGS,
            CraftItemStack.asNMSCopy(playerInventory.getLeggings()));

        PacketPlayOutEntityEquipment boots = new PacketPlayOutEntityEquipment(
            entityId,
            EnumItemSlot.FEET,
            CraftItemStack.asNMSCopy(playerInventory.getBoots()));

        for(Player iterPlayer : Bukkit.getOnlinePlayers())
        {
            CraftPlayer iterCraftPlayer = (CraftPlayer) iterPlayer;
            PlayerConnection playerConnection = iterCraftPlayer.getHandle().playerConnection;

            if(iterPlayer.getUniqueId().equals(player.getUniqueId()))
            {
                playerConnection.sendPacket(removeInfo);
                playerConnection.sendPacket(addInfo);

                playerConnection.sendPacket(respawn);
                playerConnection.sendPacket(position);
                playerConnection.sendPacket(heldItemSlot);

                craftPlayer.updateScaledHealth();
                craftPlayer.getHandle().triggerHealthUpdate();
                craftPlayer.updateInventory();

                plugin.getServer().getScheduler().runTask(
                    plugin,
                    () -> craftPlayer.getHandle().updateAbilities());
            }
            else
            {
                playerConnection.sendPacket(entityDestroy);
                playerConnection.sendPacket(removeInfo);
                playerConnection.sendPacket(addInfo);
                playerConnection.sendPacket(namedEntitySpawn);

                playerConnection.sendPacket(headLook);
                playerConnection.sendPacket(bodyLook);
                playerConnection.sendPacket(mainHand);
                playerConnection.sendPacket(offHand);
                playerConnection.sendPacket(helmet);
                playerConnection.sendPacket(chestplate);
                playerConnection.sendPacket(leggings);
                playerConnection.sendPacket(boots);
            }
        }
    }
}

package com.zerofall.ezstorage.network.S2C;

import com.zerofall.ezstorage.EZStorageAddon;

import emi.dev.emi.emi.network.EmiPacket;
import emi.shims.java.net.minecraft.network.PacketByteBuf;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.src.CompressedStreamTools;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Minecraft;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.ResourceLocation;

import java.io.*;

/**
 * S2C: Syncs the cursor (held) item after a storage slot click.
 */
public class S2CCursorItemPacket implements EmiPacket {

    public static final ResourceLocation CHANNEL = new ResourceLocation(EZStorageAddon.MOD_ID, "s2c_cursor_item");

    private final byte[] itemData; // empty array = no item on cursor

    public S2CCursorItemPacket(ItemStack cursorItem)
    {
        byte[] data = new byte[0];

        if (cursorItem != null)
        {
            try
            {
                NBTTagCompound tag = new NBTTagCompound();
                cursorItem.writeToNBT(tag);
                data = CompressedStreamTools.compress(tag);
            }
            catch (Exception exception)
            {
                EZStorageAddon.instance.LOG.error("S2CCursorItemPacket: failed to serialize cursor item", exception);
            }
        }

        this.itemData = data;
    }

    public S2CCursorItemPacket(PacketByteBuf buf)
    {
        int len = buf.readVarInt();
        byte[] bytes = new byte[Math.max(0, len)];

        if (len > 0)
        {
            for (int i = 0; i < len; i++)
            {
                bytes[i] = buf.readByte();
            }
        }

        this.itemData = bytes;
    }

    @Override
    public void write(PacketByteBuf buf)
    {
        buf.writeVarInt(itemData.length);

        if (itemData.length > 0)
        {
            for (byte value : itemData)
            {
                buf.writeByte(value);
            }
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void apply(EntityPlayer player)
    {
        ItemStack cursor = null;

        if (itemData.length > 0)
        {
            try
            {
                NBTTagCompound tag = CompressedStreamTools.readCompressed(new ByteArrayInputStream(itemData));
                cursor = ItemStack.loadItemStackFromNBT(tag);
            }
            catch (Exception exception)
            {
                // ignore — cursor stays null
            }
        }
        player.inventory.setItemStack(cursor);
        Minecraft mc = Minecraft.getMinecraft();

        if (mc != null && mc.thePlayer != null)
        {
            mc.thePlayer.inventory.setItemStack(cursor);
        }
    }

    @Override
    public ResourceLocation getId() {
        return CHANNEL;
    }
}
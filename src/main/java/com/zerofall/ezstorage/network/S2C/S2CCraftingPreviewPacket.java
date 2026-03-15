package com.zerofall.ezstorage.network.S2C;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.zerofall.ezstorage.EZStorageAddon;
import com.zerofall.ezstorage.tileentity.TileEntityStorageCore;

import emi.dev.emi.emi.network.EmiPacket;
import emi.shims.java.net.minecraft.network.PacketByteBuf;
import net.minecraft.src.CompressedStreamTools;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.ResourceLocation;
import net.minecraft.src.TileEntity;

/**
 * S2C: Sync crafting preview matrix for world rendering without requiring an open container.
 */
public class S2CCraftingPreviewPacket implements EmiPacket {

    public static final ResourceLocation CHANNEL = new ResourceLocation(EZStorageAddon.MOD_ID, "s2c_crafting_preview");

    private final int coreX;
    private final int coreY;
    private final int coreZ;
    private final boolean hasCraftBox;
    private final int craftBoxX;
    private final int craftBoxY;
    private final int craftBoxZ;
    private final byte[] nbtBytes;

    public S2CCraftingPreviewPacket(TileEntityStorageCore core)
    {
        this.coreX = core.xCoord;
        this.coreY = core.yCoord;
        this.coreZ = core.zCoord;
        this.hasCraftBox = core.hasCraftBox;
        this.craftBoxX = core.craftBoxX;
        this.craftBoxY = core.craftBoxY;
        this.craftBoxZ = core.craftBoxZ;

        byte[] bytes = new byte[0];

        try
        {
            NBTTagCompound tag = new NBTTagCompound();
            NBTTagList gridList = new NBTTagList();
            ItemStack[] matrix = core.craftMatrixPreview;

            for (int i = 0; i < 9; i++)
            {
                NBTTagCompound slotTag = new NBTTagCompound();
                slotTag.setByte("Slot", (byte)i);

                if (matrix != null && matrix[i] != null)
                {
                    matrix[i].writeToNBT(slotTag);
                }

                gridList.appendTag(slotTag);
            }

            tag.setTag("CraftMatrixPreview", gridList);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            CompressedStreamTools.writeCompressed(tag, output);
            bytes = output.toByteArray();
        }
        catch (Exception exception)
        {
            EZStorageAddon.instance.LOG.error("Failed to serialize crafting preview", exception);
        }

        this.nbtBytes = bytes;
    }

    public S2CCraftingPreviewPacket(PacketByteBuf buf)
    {
        this.coreX = buf.readVarInt();
        this.coreY = buf.readVarInt();
        this.coreZ = buf.readVarInt();
        this.hasCraftBox = buf.readBoolean();
        this.craftBoxX = buf.readVarInt();
        this.craftBoxY = buf.readVarInt();
        this.craftBoxZ = buf.readVarInt();

        this.nbtBytes = readByteArray(buf);
    }

    @Override
    public void write(PacketByteBuf buf)
    {
        buf.writeVarInt(this.coreX);
        buf.writeVarInt(this.coreY);
        buf.writeVarInt(this.coreZ);
        buf.writeBoolean(this.hasCraftBox);
        buf.writeVarInt(this.craftBoxX);
        buf.writeVarInt(this.craftBoxY);
        buf.writeVarInt(this.craftBoxZ);

        buf.writeInt(this.nbtBytes.length);

        if (this.nbtBytes.length > 0)
        {
            writeByteArray(buf, this.nbtBytes);
        }
    }

    @Override
    public void apply(EntityPlayer player)
    {
        if (player == null || player.worldObj == null)
        {
            return;
        }

        TileEntity tileEntity = player.worldObj.getBlockTileEntity(this.coreX, this.coreY, this.coreZ);

        if (!(tileEntity instanceof TileEntityStorageCore core))
        {
            return;
        }

        core.hasCraftBox = this.hasCraftBox;
        core.craftBoxX = this.craftBoxX;
        core.craftBoxY = this.craftBoxY;
        core.craftBoxZ = this.craftBoxZ;

        ItemStack[] nextMatrix = new ItemStack[9];

        if (this.nbtBytes.length <= 0)
        {
            core.craftMatrixPreview = nextMatrix;
            return;
        }

        try
        {
            NBTTagCompound tag = CompressedStreamTools.readCompressed(new ByteArrayInputStream(this.nbtBytes));

            if (tag == null || !tag.hasKey("CraftMatrixPreview"))
            {
                return;
            }

            NBTTagList gridList = tag.getTagList("CraftMatrixPreview");

            for (int i = 0; i < gridList.tagCount(); i++)
            {
                NBTTagCompound slotTag = (NBTTagCompound)gridList.tagAt(i);
                int slotIndex = slotTag.getByte("Slot") & 255;

                if (slotIndex >= 0 && slotIndex < 9)
                {
                    nextMatrix[slotIndex] = ItemStack.loadItemStackFromNBT(slotTag);
                }
            }

            core.craftMatrixPreview = nextMatrix;
        }
        catch (Exception exception)
        {
            EZStorageAddon.instance.LOG.error("Failed to deserialize crafting preview", exception);
        }
    }

    @Override
    public ResourceLocation getId() {
        return CHANNEL;
    }

    private static byte[] readByteArray(PacketByteBuf buf)
    {
        int len = buf.readInt();

        if (len <= 0)
        {
            return new byte[0];
        }

        byte[] bytes = new byte[len];

        for (int i = 0; i < len; i++)
        {
            bytes[i] = buf.readByte();
        }

        return bytes;
    }

    private static void writeByteArray(PacketByteBuf buf, byte[] bytes)
    {
        for (byte value : bytes)
        {
            buf.writeByte(value);
        }
    }
}
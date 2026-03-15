package com.zerofall.ezstorage.block;

import net.minecraft.src.ChatMessageComponent;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

import com.zerofall.ezstorage.tileentity.TileEntityStorageCore;
import com.zerofall.ezstorage.util.BlockRef;
import com.zerofall.ezstorage.util.EZInventory;

public abstract class StorageUserInterface extends EZBlockContainer {

    protected StorageUserInterface(int id, String name, Material materialIn) {
        super(id, name, materialIn);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int facing, float hitX, float hitY, float hitZ)
    {
        if (!world.isRemote && player instanceof EntityPlayerMP entityPlayerMP)
        {
            TileEntityStorageCore core;
            TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

            if (tileEntity instanceof TileEntityStorageCore)
            {
                core = (TileEntityStorageCore) tileEntity;
            }
            else
            {
                BlockRef blockRef = new BlockRef(this, x, y, z);
                core = findCore(blockRef, world, null);
            }

            if (core == null)
            {
                return sendNoCoreMessage(entityPlayerMP);
            }

            EZInventory inventory = core.getInventory();
            if (inventory != null)
            {
                openPlayerInventoryGui(entityPlayerMP, inventory, world, x, y, z, core);
            }
        }

        return true;
    }

    private boolean sendNoCoreMessage(EntityPlayerMP entityPlayerMP) {
        entityPlayerMP.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("chat.msg.storagecore_not_found"));
        return true;
    }
}
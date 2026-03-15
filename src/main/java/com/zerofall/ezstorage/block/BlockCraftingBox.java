package com.zerofall.ezstorage.block;

import com.zerofall.ezstorage.tileentity.TileEntityStorageCore;
import com.zerofall.ezstorage.util.BlockRef;
import com.zerofall.ezstorage.util.EZInventory;
import net.minecraft.src.*;

public class BlockCraftingBox extends StorageUserInterface {

    public BlockCraftingBox(int id) {
        super(id, "crafting_box", Material.iron);
    }

    @Override
    public void breakBlock(World worldIn, int x, int y, int z, int blockId, int meta) {
        super.breakBlock(worldIn, x, y, z, blockId, meta);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int facing, float hitX, float hitY, float hitZ)
    {
        if (world.isRemote) return true;

        if (!(player instanceof EntityPlayerMP entityPlayerMP)) return true;

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
            entityPlayerMP.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("chat.msg.storagecore_not_found"));
            return true;
        }

        EZInventory inventory = core.getInventory();

        if (inventory != null)
        {
            openPlayerInventoryGui(entityPlayerMP, inventory, world, x, y, z, core);
        }

        return true;
    }
}
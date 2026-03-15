package com.zerofall.ezstorage.block;

import net.minecraft.src.Material;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EnumChatFormatting;
import net.minecraft.src.ChatMessageComponent;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

import com.zerofall.ezstorage.tileentity.TileEntityStorageCore;
import com.zerofall.ezstorage.util.EZInventoryManager;

public class BlockStorageCore extends StorageUserInterface {

    public BlockStorageCore(int id) {
        super(id, "storage_core", Material.wood);
        this.setResistance(6000.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World world) {
        return new TileEntityStorageCore();
    }

    @Override
    public void breakBlock(World worldIn, int x, int y, int z, int blockId, int meta)
    {
        TileEntity te = worldIn.getBlockTileEntity(x, y, z);
        if (te instanceof TileEntityStorageCore core) {
            if (!core.hasStoredItems()) {
                var inventory = core.getInventory();
                if (inventory != null) {
                    EZInventoryManager.deleteInventory(inventory);
                }
            }
        }

        super.breakBlock(worldIn, x, y, z, blockId, meta);
    }

    @Override
    public float getBlockHardness(World world, int x, int y, int z)
    {
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

        if (tileEntity instanceof TileEntityStorageCore core)
        {

            if (core.hasStoredItems())
            {
                return -1.0F;
            }
        }

        return super.getBlockHardness(world, x, y, z);
    }

    @Override
    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player)
    {
        if (!world.isRemote)
        {
            TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

            if (tileEntity instanceof TileEntityStorageCore core && core.hasStoredItems())
            {
                player.sendChatToPlayer(ChatMessageComponent
                    .createFromTranslationKey("chat.msg.storagecore_break_blocked_nonempty")
                    .setColor(EnumChatFormatting.YELLOW));
            }
        }

        super.onBlockClicked(world, x, y, z, player);
    }

}
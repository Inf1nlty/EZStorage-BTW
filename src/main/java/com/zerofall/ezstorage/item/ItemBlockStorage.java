package com.zerofall.ezstorage.item;

import java.util.List;

import net.minecraft.src.*;

import com.zerofall.ezstorage.block.BlockStorage;

public class ItemBlockStorage extends ItemBlock {

    private final int blockId;

    public ItemBlockStorage(int itemId) {
        super(itemId);
        this.blockId = itemId + 256;
    }

    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack item_stack, EntityPlayer player, List info, boolean extended_info)
    {
        Block block = Block.blocksList[this.blockId];

        if (block instanceof BlockStorage blockStorage)
        {
            info.add(StatCollector.translateToLocalFormatted("hud.msg.ezstorage.storage.capacity", blockStorage.getCapacity()));
        }
    }
}
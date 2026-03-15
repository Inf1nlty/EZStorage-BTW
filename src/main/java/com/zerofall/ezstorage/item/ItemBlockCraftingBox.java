package com.zerofall.ezstorage.item;

import net.minecraft.src.*;

public class ItemBlockCraftingBox extends ItemBlock {

    public ItemBlockCraftingBox(int itemId) {
        super(itemId);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return super.getUnlocalizedName(stack);
    }
}
package com.zerofall.ezstorage.block;

import net.minecraft.src.Block;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.Material;

import com.zerofall.ezstorage.Reference;

public class EZBlock extends Block {

    protected EZBlock(int id, String name, Material materialIn) {
        super(id, materialIn);
        this.setUnlocalizedName(name);
        this.setTextureName(Reference.MOD_ID + ":" + name);
        this.setCreativeTab(CreativeTabs.tabDecorations);
        this.setHardness(2.25F);
    }

}
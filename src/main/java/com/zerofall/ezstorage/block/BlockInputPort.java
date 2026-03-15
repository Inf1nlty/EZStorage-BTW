package com.zerofall.ezstorage.block;

import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

import com.zerofall.ezstorage.tileentity.TileEntityInventoryProxy;

public class BlockInputPort extends EZBlockContainer {

    public BlockInputPort(int id) {
        super(id, "input_port", Material.iron);
    }

    @Override
    public TileEntity createNewTileEntity(World world) {
        return new TileEntityInventoryProxy();
    }
}
package com.zerofall.ezstorage.mixin;

import btw.block.blocks.HopperBlock;
import btw.block.tileentity.HopperTileEntity;
import com.zerofall.ezstorage.block.EZBlockContainer;
import com.zerofall.ezstorage.block.StorageMultiblock;
import com.zerofall.ezstorage.init.EZBlocks;
import com.zerofall.ezstorage.tileentity.TileEntityInventoryProxy;
import com.zerofall.ezstorage.tileentity.TileEntityStorageCore;
import com.zerofall.ezstorage.util.BlockRef;
import com.zerofall.ezstorage.util.EZInventory;
import net.minecraft.src.Block;
import net.minecraft.src.ChatMessageComponent;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * If a world contains an EZStorage proxy tile at that position, guard and route safely.
 */
@Mixin(HopperBlock.class)
public abstract class HopperBlockMixin {

    @Unique
    private static boolean ezstorage$isInputPortAt(World world, int x, int y, int z)
    {
        return world.getBlockId(x, y, z) == EZBlocks.input_port.blockID;
    }

    @Unique
    private static void ezstorage$restoreHopperTile(World world, int x, int y, int z)
    {
        // Recover from stale tile data left at hopper coordinates.
        world.removeBlockTileEntity(x, y, z);
        world.setBlockTileEntity(x, y, z, new HopperTileEntity());
    }

    @Inject(method = "onNeighborBlockChange", at = @At("HEAD"), cancellable = true)
    private void ezstorage$guardNeighborCast(World world, int x, int y, int z, int neighborId, CallbackInfo ci)
    {
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

        if (tileEntity == null || tileEntity instanceof HopperTileEntity)
        {
            return;
        }

        if (!world.isRemote)
        {
            ezstorage$restoreHopperTile(world, x, y, z);
        }

        ci.cancel();
    }

    @Inject(method = "onBlockActivated", at = @At("HEAD"), cancellable = true)
    private void ezstorage$handleProxyTile(World world, int x, int y, int z, EntityPlayer player, int facing, float hitX, float hitY, float hitZ, CallbackInfoReturnable<Boolean> cir)
    {
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

        if (!(tileEntity instanceof TileEntityInventoryProxy proxy))
        {
            return;
        }

        if (!ezstorage$isInputPortAt(world, x, y, z))
        {
            if (!world.isRemote)
            {
                ezstorage$restoreHopperTile(world, x, y, z);
            }

            cir.setReturnValue(false);
            return;
        }

        if (world.isRemote)
        {
            cir.setReturnValue(true);
            return;
        }

        if (!(player instanceof EntityPlayerMP playerMP))
        {
            cir.setReturnValue(true);
            return;
        }

        EZInventory inventory = proxy.getInventory();
        TileEntityStorageCore core = proxy.core;

        if (core == null)
        {
            Block block = Block.blocksList[world.getBlockId(x, y, z)];

            if (block instanceof StorageMultiblock multiblock)
            {
                core = multiblock.findCore(new BlockRef(block, x, y, z), world, null);
                proxy.core = core;
                inventory = core != null ? core.getInventory() : inventory;
            }
        }

        if (inventory == null || core == null)
        {
            playerMP.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("chat.msg.storagecore_not_found"));
            cir.setReturnValue(true);
            return;
        }

        if (EZBlocks.input_port instanceof EZBlockContainer uiBlock)
        {
            uiBlock.openPlayerInventoryGui(playerMP, inventory, world, x, y, z, core);
        }

        cir.setReturnValue(true);
    }
}

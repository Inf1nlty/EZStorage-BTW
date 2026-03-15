package com.zerofall.ezstorage.mixin;

import com.zerofall.ezstorage.container.ContainerStorageCoreCrafting;
import com.zerofall.ezstorage.configuration.EZConfiguration;
import com.zerofall.ezstorage.util.EZInventoryManager;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.SlotCrafting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SlotCrafting.class)
public abstract class SlotCraftingMixin {

    @Final @Shadow private IInventory craftMatrix;
    @Shadow private EntityPlayer thePlayer;
    @Shadow private int amountCrafted;

    @Shadow protected abstract void onCrafting(ItemStack par1ItemStack);

    @Inject(method = "onPickupFromSlot", at = @At("HEAD"), cancellable = true)
    private void ezstorage$routeCraftingOutput(EntityPlayer player, ItemStack craftedStack, CallbackInfo ci)
    {
        if (!(this.thePlayer.openContainer instanceof ContainerStorageCoreCrafting container))
        {
            return;
        }

        if (craftedStack == null || craftedStack.stackSize <= 0)
        {
            ci.cancel();
            return;
        }

        container.beginSuppressCraftingReset();

        try
        {
            ItemStack[][] recipe = new ItemStack[9][];

            for (int i = 0; i < recipe.length; i++)
            {
                ItemStack matrixStack = this.craftMatrix.getStackInSlot(i);

                if (matrixStack != null)
                {
                    recipe[i] = new ItemStack[] { matrixStack.copy() };
                }
            }

            int consumption = 1;
            this.amountCrafted = craftedStack.stackSize;
            this.onCrafting(craftedStack);


            for (int i = 0; i < this.craftMatrix.getSizeInventory(); ++i)
            {
                ItemStack matrixStack = this.craftMatrix.getStackInSlot(i);

                if (matrixStack == null)
                {
                    continue;
                }

                this.craftMatrix.decrStackSize(i, consumption);

                if (matrixStack.getItem().hasContainerItem())
                {
                    ItemStack containerItem = new ItemStack(matrixStack.getItem().getContainerItem());
                    Item containerItemType = containerItem.getItem();

                    if (containerItemType.getClass() == craftedStack.getItem().getClass()
                        || matrixStack.getItem().doesContainerItemLeaveCraftingGrid(matrixStack)
                        && this.thePlayer.inventory.addItemStackToInventory(containerItem))
                    {
                        continue;
                    }

                    if (this.craftMatrix.getStackInSlot(i) == null)
                    {
                        this.craftMatrix.setInventorySlotContents(i, containerItem);
                        continue;
                    }

                    this.thePlayer.dropPlayerItem(containerItem);
                }

            }


            if (EZConfiguration.guiAutoRefill.getBooleanValue())
            {
                container.tryToPopulateCraftingGrid(recipe, player, false);
            }

            container.saveGrid();
            EZInventoryManager.sendToClients(container.inventory);
            container.detectAndSendChanges();
        }
        finally
        {
            container.endSuppressCraftingReset();
        }

        ci.cancel();
    }
}
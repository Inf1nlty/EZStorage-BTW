package com.zerofall.ezstorage.container;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.InventoryCraftResult;
import net.minecraft.src.InventoryCrafting;
import net.minecraft.src.Slot;
import net.minecraft.src.SlotCrafting;
import net.minecraft.src.ItemStack;
import net.minecraft.src.IRecipe;
import net.minecraft.src.CraftingManager;
import net.minecraft.src.EntityOtherPlayerMP;
import net.minecraft.src.World;

import com.zerofall.ezstorage.configuration.EZConfiguration;

import com.zerofall.ezstorage.util.EZInventory;
import com.zerofall.ezstorage.util.EZInventoryManager;

public class ContainerStorageCoreCrafting extends ContainerStorageCore {

    public InventoryCrafting craftMatrix = new InventoryCrafting(this, 3, 3);
    public IInventory craftResult = new InventoryCraftResult();
    /** Last computed crafting result item. */
    public ItemStack current_crafting_result;
    private IRecipe currentRecipe;
    private ItemStack previous_crafting_result;
    /** Public so GuiCraftingCore can read the world when its own world field is null. */
    public World worldObj;
    public int craftBoxX;
    public int craftBoxY;
    public int craftBoxZ;
    private boolean hasCraftBoxPos;
    private int suppressCraftingResetDepth;
    private int resumeTicks;
    private int resumePeriod;

    public ContainerStorageCoreCrafting(EntityPlayer player, World world, EZInventory inventory)
    {
        this(player, world, 0, 0, 0, false);
        this.inventory = inventory;

        if (this.inventory != null && this.inventory.craftMatrix != null)
        {
            boolean loaded = false;

            for (int k = 0; k < 9; k++)
            {
                if (this.inventory.craftMatrix[k] != null)
                {
                    this.craftMatrix.setInventorySlotContents(k, this.inventory.craftMatrix[k]);
                    loaded = true;
                }
            }
            if (loaded)
            {
                this.onCraftMatrixChanged(this.craftMatrix);
            }
        }
    }

    public ContainerStorageCoreCrafting(EntityPlayer player, World world, EZInventory inventory, int craftBoxX, int craftBoxY, int craftBoxZ)
    {
        this(player, world, craftBoxX, craftBoxY, craftBoxZ, true);
        this.inventory = inventory;

        if (this.inventory != null && this.inventory.craftMatrix != null)
        {
            boolean loaded = false;

            for (int k = 0; k < 9; k++)
            {
                if (this.inventory.craftMatrix[k] != null)
                {
                    this.craftMatrix.setInventorySlotContents(k, this.inventory.craftMatrix[k]);
                    loaded = true;
                }
            }
            if (loaded)
            {
                this.onCraftMatrixChanged(this.craftMatrix);
            }
        }
    }

    public ContainerStorageCoreCrafting(EntityPlayer player, World world)
    {
        this(player, world, 0, 0, 0, false);
    }

    public ContainerStorageCoreCrafting(EntityPlayer player, World world, int craftBoxX, int craftBoxY, int craftBoxZ)
    {
        this(player, world, craftBoxX, craftBoxY, craftBoxZ, true);
    }

    private ContainerStorageCoreCrafting(EntityPlayer player, World world, int craftBoxX, int craftBoxY, int craftBoxZ, boolean hasCraftBoxPos)
    {
        super(player);

        this.worldObj = world;
        this.craftBoxX = craftBoxX;
        this.craftBoxY = craftBoxY;
        this.craftBoxZ = craftBoxZ;
        this.hasCraftBoxPos = hasCraftBoxPos;
        this.suppressCraftingResetDepth = 0;
        this.resumeTicks = -1;
        this.resumePeriod = -1;
        this.addSlotToContainer(new SlotCrafting(player, this.craftMatrix, this.craftResult, 0, 118, 132));
        int i;
        int j;

        for (i = 0; i < 3; ++i)
        {
            for (j = 0; j < 3; ++j)
            {
                this.addSlotToContainer(new Slot(this.craftMatrix, j + i * 3, 44 + j * 18, 114 + i * 18));
            }
        }

        this.onCraftMatrixChanged(this.craftMatrix);
    }

    public void onCraftMatrixChanged(IInventory inventoryIn)
    {
        this.currentRecipe = CraftingManager.getInstance().findMatchingIRecipe(this.craftMatrix, this.worldObj);
        this.current_crafting_result = CraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.worldObj);

        if (!ItemStack.areItemStacksEqual(this.current_crafting_result, this.previous_crafting_result)
            && !this.isSuppressingCraftingReset())
        {
            this.detectAndSendChanges();
        }

        // Always refresh the output slot contents; equivalent result does not imply slot is still populated.
        this.refreshCraftingSlotResult();

        this.previous_crafting_result = this.current_crafting_result == null ? null : this.current_crafting_result.copy();

        tryRestoreCraftingProgressOnClient();
    }

    public void beginSuppressCraftingReset()
    {
        this.suppressCraftingResetDepth++;
    }

    public void endSuppressCraftingReset()
    {
        if (this.suppressCraftingResetDepth > 0)
        {
            this.suppressCraftingResetDepth--;
        }
    }

    public boolean isSuppressingCraftingReset()
    {
        return this.suppressCraftingResetDepth > 0;
    }

    private static long makeCraftBoxKey(int dim, int x, int y, int z)
    {
        long key = 1469598103934665603L;
        key = (key ^ dim) * 1099511628211L;
        key = (key ^ x) * 1099511628211L;
        key = (key ^ y) * 1099511628211L;
        key = (key ^ z) * 1099511628211L;
        return key;
    }

    private int computeMatrixHash()
    {
        int hash = 1;

        for (int i = 0; i < 9; i++)
        {
            ItemStack stack = this.craftMatrix.getStackInSlot(i);

            if (stack == null)
            {
                hash = 31 * hash;
                continue;
            }

            hash = 31 * hash + stack.itemID;
            hash = 31 * hash + stack.getItemDamage();
            hash = 31 * hash + stack.stackSize;
        }

        return hash;
    }

    private void tryRestoreCraftingProgressOnClient()
    {
        // BTW port: no progressive crafting state to restore.
    }

    private void captureCraftingProgressSnapshotOnClient()
    {
        // BTW port: no progressive crafting state to snapshot.
    }

    private SlotCrafting getCraftingSlot()
    {
        return (SlotCrafting)this.getSlot(this.rowCount() * 9 + 36);
    }

    private void refreshCraftingSlotResult()
    {
        SlotCrafting craftingSlot = this.getCraftingSlot();

        if (craftingSlot == null)
        {
            return;
        }

        if (this.current_crafting_result == null || this.current_crafting_result.getItem() == null)
        {
            craftingSlot.setRecipe(null);
            this.craftResult.setInventorySlotContents(0, null);
            return;
        }

        craftingSlot.setRecipe(this.currentRecipe);
        this.craftResult.setInventorySlotContents(0, this.current_crafting_result.copy());
    }

    /** Mirrors MITEContainerCrafting.getRecipe() so GUI tooltip code can call it. */
    public IRecipe getRecipe() {
        return this.currentRecipe;
    }

    public int getCraftingTier()
    {
        return 0;
    }

    public boolean isCurrentRecipeBlockedByTier()
    {
        return false;
    }

    public void onUpdate()
    {
        if (this.player instanceof EntityOtherPlayerMP)
        {
            return;
        }

        this.onCraftMatrixChanged(this.craftMatrix);

        this.detectAndSendChanges();
    }

    // Shift clicking
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        if (playerIn.worldObj.isRemote)
        {
            return null;
        }

        Slot slotObject = (Slot) inventorySlots.get(index);

        if (slotObject != null && slotObject.getHasStack())
        {
            if (slotObject instanceof SlotCrafting)
            {
                ItemStack[][] recipe = new ItemStack[9][];

                for (int i = 0; i < 9; i++)
                {
                    ItemStack matrixStack = this.craftMatrix.getStackInSlot(i);
                    recipe[i] = new ItemStack[] { matrixStack == null ? null : matrixStack.copy() };
                }

                // Craft exactly once, then route output by priority:
                // player inventory -> EZ storage -> drop overflow.
                if (slotObject.getHasStack())
                {
                    ItemStack output = slotObject.getStack();

                    if (output != null && output.getItem() != null)
                    {
                        ItemStack produced = output.copy();
                        slotObject.onSlotChange(output, produced);
                        slotObject.onPickupFromSlot(playerIn, produced);

                        int playerInvStart = this.rowCount() * 9;
                        int playerInvEnd = playerInvStart + 36;
                        ItemStack remaining = produced.copy();

                        this.mergeItemStack(remaining, playerInvStart, playerInvEnd, true);

                        if (remaining.stackSize > 0)
                        {
                            ItemStack overflow = this.inventory.input(remaining);

                            if (overflow != null && overflow.stackSize > 0)
                            {
                                playerIn.dropPlayerItemWithRandomChoice(overflow, false);
                            }
                        }

                        if (EZConfiguration.guiAutoRefill.getBooleanValue())
                        {
                            tryToPopulateCraftingGrid(recipe, playerIn, false);
                        }

                        saveGrid();
                        EZInventoryManager.sendToClients(inventory);
                        this.detectAndSendChanges();
                        return produced;
                    }
                }

                return null;

            }
            else
            {
                ItemStack stackInSlot = slotObject.getStack();
                ItemStack movedStack = stackInSlot.copy();
                ItemStack remainingStack = this.inventory.input(stackInSlot);

                slotObject.putStack(remainingStack);
                slotObject.onSlotChanged();

                saveGrid();
                EZInventoryManager.sendToClients(inventory);
                this.detectAndSendChanges();

                if (remainingStack == null || remainingStack.stackSize != movedStack.stackSize)
                {
                    return movedStack;
                }
            }
        }

        return null;
    }

    @Override
    public ItemStack customSlotClick(int slotId, int clickedButton, int mode, EntityPlayer playerIn)
    {
        if (playerIn.worldObj.isRemote)
        {
            return null;
        }

        if (slotId > 0 && mode == 0 && clickedButton == 0)
        {
            if (inventorySlots.size() > slotId)
            {
                Slot slotObject = (Slot) inventorySlots.get(slotId);

                if (slotObject != null)
                {
                    if (slotObject instanceof SlotCrafting)
                    {
                        ItemStack[][] recipe = new ItemStack[9][];

                        for (int i = 0; i < 9; i++)
                        {
                            ItemStack matrixStack = this.craftMatrix.getStackInSlot(i);
                            recipe[i] = new ItemStack[] { matrixStack == null ? null : matrixStack.copy() };
                        }
                        ItemStack result = super.customSlotClick(slotId, clickedButton, mode, playerIn);

                        if (result != null
                            && EZConfiguration.guiAutoRefill.getBooleanValue()
                            && tryToPopulateCraftingGrid(recipe, playerIn, false))
                        {
                            saveGrid();
                            EZInventoryManager.sendToClients(inventory);
                            this.detectAndSendChanges();
                        }
                        else if (result != null)
                        {
                            saveGrid();
                            EZInventoryManager.sendToClients(inventory);
                            this.detectAndSendChanges();
                        }

                        return result;
                    }
                }
            }

        }


        return super.customSlotClick(slotId, clickedButton, mode, playerIn);
    }

    public boolean tryToPopulateCraftingGrid(ItemStack[][] recipe, EntityPlayer playerIn, boolean usePlayerInv)
    {
        boolean hasChanges = false;
        int autoRefillAmount = getConfiguredAutoRefillAmount();
        // Maps playerInv slot index -> list of crafting grid slots that need an item from that player slot
        HashMap<Integer, ArrayList<Slot>> playerInvSlotsMapping = new HashMap<>();
        final int craftingSlotsStartIndex = inventorySlots.size() - 3 * 3;

        for (int j = 0; j < recipe.length; j++)
        {
            ItemStack[] recipeItems = recipe[j];

            Slot slot = getSlotFromInventory(this.craftMatrix, j);
            if (slot == null)
            {
                continue;
            }

            ItemStack stackInSlot = slot.getStack();

            if (stackInSlot != null)
            {
                ItemStack recipeMatch = getMatchingItemStackForRecipe(recipeItems, stackInSlot);

                if (recipeMatch != null)
                {
                    int targetStackSize = getTargetStackSizeForRefill(recipeMatch, autoRefillAmount);

                    if (stackInSlot.stackSize < targetStackSize)
                    {
                        ItemStack request = stackInSlot.copy();
                        request.stackSize = targetStackSize - stackInSlot.stackSize;

                        ItemStack topUp = getMatchingItemFromStorage(request);

                        if (topUp != null)
                        {
                            stackInSlot.stackSize += topUp.stackSize;
                            hasChanges = true;
                        }
                    }

                    // Already has a valid item — force GUI update
                    inventoryItemStacks.set(craftingSlotsStartIndex + j, null);
                    continue;
                }
                // Return wrong item to storage
                ItemStack result = this.inventory.input(stackInSlot);

                if (result != null)
                {
                    playerIn.dropPlayerItemWithRandomChoice(result, false);
                }
                slot.putStack(null);
                hasChanges = true;
            }

            if (recipeItems == null || recipeItems.length == 0)
            {
                slot.putStack(null);
                continue;
            }

            // --- Try to find the item ---
            ItemStack retrieved = null;
            boolean foundInPlayerInv = false;

            for (int k = 0; k < recipeItems.length; k++)
            {
                ItemStack recipeItem = recipeItems[k];

                if (recipeItem == null) continue;

                // Pull configured amount per slot (clamped by item stack max)
                ItemStack recipeItemOne = recipeItem.copy();
                recipeItemOne.stackSize = getTargetStackSizeForRefill(recipeItemOne, autoRefillAmount);

                // 1) Try storage first
                retrieved = getMatchingItemFromStorage(recipeItemOne);
                if (retrieved != null)
                {
                    hasChanges = true;
                    break;
                }

                // 2) Try player inventory if allowed
                if (usePlayerInv)
                {
                    Integer playerInvSize = playerIn.inventory.mainInventory.length;
                    for (int i = 0; i < playerInvSize; i++)
                    {
                        ItemStack playerItem = playerIn.inventory.mainInventory[i];

                        if (playerItem != null && isRecipeItemValid(recipeItemOne, playerItem))
                        {
                            ArrayList<Slot> targetSlots = playerInvSlotsMapping.get(i);
                            if (targetSlots == null)
                            {
                                targetSlots = new ArrayList<>();
                                playerInvSlotsMapping.put(i, targetSlots);
                            }
                            // Check we haven't already consumed all copies of this item
                            if (playerItem.stackSize > targetSlots.size())
                            {
                                targetSlots.add(slot);
                                foundInPlayerInv = true;
                                break;
                            }
                        }
                    }

                    if (foundInPlayerInv) break;
                }
            }

            if (retrieved != null)
            {
                slot.putStack(retrieved);
            }
            else if (!foundInPlayerInv)
            {
                // Nothing found anywhere — clear slot
                slot.putStack(null);
            }
            // If foundInPlayerInv==true, the second loop below will fill the slot
        }

        // Second pass: transfer items from player inventory into crafting grid slots
        if (usePlayerInv && !playerInvSlotsMapping.isEmpty())
        {
            Set<Entry<Integer, ArrayList<Slot>>> set = playerInvSlotsMapping.entrySet();

            for (Entry<Integer, ArrayList<Slot>> entry : set)
            {
                Integer playerInvSlotId = entry.getKey();
                ArrayList<Slot> targetSlots = entry.getValue();
                int targetSlotsCount = targetSlots.size();

                ItemStack playerInvSlot = playerIn.inventory.mainInventory[playerInvSlotId];
                if (playerInvSlot == null) continue;

                // Distribute evenly, last slot gets the remainder
                int itemsToRequest = playerInvSlot.stackSize / targetSlotsCount;
                if (itemsToRequest < 1) itemsToRequest = 1;

                for (int j = 0; j < targetSlotsCount; j++)
                {
                    Slot targetSlot = targetSlots.get(j);
                    if (targetSlot == null) continue;

                    // Re-fetch in case previous iteration consumed some
                    playerInvSlot = playerIn.inventory.mainInventory[playerInvSlotId];
                    if (playerInvSlot == null) break;

                    int toTake = (j == targetSlotsCount - 1) ? playerInvSlot.stackSize : itemsToRequest;
                    if (toTake < 1) toTake = 1;

                    ItemStack taken = playerIn.inventory.decrStackSize(playerInvSlotId, toTake);
                    if (taken != null && taken.stackSize > 0)
                    {
                        // If slot already has a compatible item (e.g. from a previous grid state), merge
                        ItemStack existing = targetSlot.getStack();
                        if (existing != null && EZInventory.stacksEqual(existing, taken))
                        {
                            existing.stackSize += taken.stackSize;
                        }
                        else
                        {
                            targetSlot.putStack(taken);
                        }
                        hasChanges = true;
                    }
                    else
                    {
                        if (targetSlot.getStack() == null)
                        {
                            targetSlot.putStack(null);
                        }
                    }
                }
            }
        }

        return hasChanges;
    }

    private static int getConfiguredAutoRefillAmount()
    {
        int amount = EZConfiguration.guiAutoRefillAmount.getIntegerValue();

        if (amount < 1)
        {
            return 1;
        }

        return Math.min(64, amount);
    }

    private static int getTargetStackSizeForRefill(ItemStack stack, int configuredAmount)
    {
        if (stack == null)
        {
            return 1;
        }

        int maxStack = stack.getMaxStackSize();

        if (maxStack < 1)
        {
            maxStack = 1;
        }

        return Math.max(1, Math.min(configuredAmount, maxStack));
    }

    private ItemStack getMatchingItemFromStorage(ItemStack recipeItem)
    {
        for (int i = 0; i < this.inventory.inventory.size(); i++)
        {
            ItemStack group = this.inventory.inventory.get(i);

            if (isRecipeItemValid(recipeItem, group))
            {
                if (group.stackSize >= recipeItem.stackSize)
                {
                    ItemStack stack = group.copy();
                    stack.stackSize = recipeItem.stackSize;
                    group.stackSize -= recipeItem.stackSize;

                    if (group.stackSize <= 0)
                    {
                        this.inventory.inventory.remove(i);
                    }
                    this.inventory.setHasChanges();
                    return stack;
                }
            }
        }

        return null;
    }

    private static boolean isRecipeItemValid(ItemStack recipeItem, ItemStack candidate)
    {
        if (recipeItem == null || candidate == null || recipeItem.getItem() == null || candidate.getItem() == null)
            return false;
        // Check item ID match
        if (recipeItem.itemID == candidate.itemID)
        {
            // Wildcard damage (32767) or exact damage match or damageable
            if (recipeItem.getItemDamage() == Short.MAX_VALUE
                || recipeItem.getItemDamage() == candidate.getItemDamage()
                || recipeItem.isItemStackDamageable()) {
                return true;
            }
        }

        return EZInventory.stacksEqual(recipeItem, candidate);
    }

    private static ItemStack getMatchingItemStackForRecipe(ItemStack[] recipeItems, ItemStack stack)
    {
        if (recipeItems == null)
        {
            return null;
        }
        for (ItemStack recipeItem : recipeItems)
        {
            if (isRecipeItemValid(recipeItem, stack))
            {
                return recipeItem;
            }
        }

        return null;
    }

    @Override
    protected int playerInventoryY() {
        return 174;
    }

    @Override
    protected int rowCount() {
        return 5;
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        captureCraftingProgressSnapshotOnClient();
        saveGrid();
        super.onContainerClosed(playerIn);
    }

    public void saveGrid() {
        if (this.inventory != null)
        {
            if (this.inventory.craftMatrix == null)
            {
                this.inventory.craftMatrix = new ItemStack[9];
            }

            boolean hasChanges = false;

            for (int i = 0; i < 9; i++)
            {
                ItemStack current = this.craftMatrix.getStackInSlot(i);
                if (!areCraftGridStacksEqual(this.inventory.craftMatrix[i], current))
                {
                    hasChanges = true;
                }
                this.inventory.craftMatrix[i] = current == null ? null : current.copy();
            }
            if (hasChanges)
            {
                this.inventory.setHasChanges();
                EZInventoryManager.saveInventory(this.inventory);
            }
        }
    }

    private static boolean areCraftGridStacksEqual(ItemStack left, ItemStack right)
    {
        if (!EZInventory.stacksEqual(left, right))
        {
            return false;
        }

        if (left == null)
        {
            return true;
        }

        return right != null && left.stackSize == right.stackSize;
    }

    public void clearGrid(EntityPlayer playerIn)
    {
        boolean cleared = false;

        for (int i = 0; i < 9; i++)
        {
            ItemStack stack = this.craftMatrix.getStackInSlot(i);

            if (stack != null)
            {
                ItemStack result = this.inventory.input(stack);
                this.craftMatrix.setInventorySlotContents(i, null);

                if (result != null)
                {
                    playerIn.dropPlayerItemWithRandomChoice(result, false);
                }
                cleared = true;
            }
        }

        if (cleared && !playerIn.worldObj.isRemote)
        {
            saveGrid(); // persist cleared matrix to inventory before broadcast
            EZInventoryManager.sendToClients(inventory);
        }
    }
}


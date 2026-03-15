package com.zerofall.ezstorage.tileentity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.src.*;

import com.zerofall.ezstorage.block.BlockCraftingBox;
import com.zerofall.ezstorage.block.BlockInputPort;
import com.zerofall.ezstorage.block.BlockStorage;
import com.zerofall.ezstorage.block.BlockStorageCore;
import com.zerofall.ezstorage.block.StorageMultiblock;
import com.zerofall.ezstorage.init.EZBlocks;
import com.zerofall.ezstorage.network.S2C.S2CCraftingPreviewPacket;
import com.zerofall.ezstorage.util.BlockRef;
import com.zerofall.ezstorage.util.EZInventory;
import com.zerofall.ezstorage.util.EZInventoryManager;
import com.zerofall.ezstorage.util.EZStorageUtils;
import emi.dev.emi.emi.network.EmiNetwork;

public class TileEntityStorageCore extends TileEntity {

    private EZInventory inventory;

    Set<BlockRef> multiblock = new HashSet<>();
    private int ticks;
    public boolean hasCraftBox = false;
    public int craftBoxX = 0, craftBoxY = 0, craftBoxZ = 0;
    public String inventoryId = "";

    public long inventoryItemsStored;
    public long inventoryItemsMax;
    public int inventoryTypesStored;
    public int inventoryTypesMax;
    public ItemStack[] craftMatrixPreview;
    private int lastCraftPreviewHash = Integer.MIN_VALUE;

    public EZInventory getInventory() {
        boolean allowCreate = this.worldObj != null && !this.worldObj.isRemote;
        return getInventory(allowCreate);
    }

    public boolean hasStoredItems()
    {
        EZInventory inventory = getInventory(false);

        if (inventory == null)
        {
            return false;
        }

        if (inventory.getTotalCount() > 0)
        {
            return true;
        }

        if (inventory.craftMatrix != null)
        {
            for (ItemStack stack : inventory.craftMatrix)
            {
                if (stack != null && stack.stackSize > 0)
                {
                    return true;
                }
            }
        }

        return false;
    }

    private EZInventory getInventory(boolean allowCreate)
    {
        if (inventory == null)
        {
            inventory = EZInventoryManager.getInventory(inventoryId);

            if (inventory == null && allowCreate)
            {
                inventory = EZInventoryManager.createInventory();
                inventoryId = inventory.id;
            }
        }

        return inventory;
    }

    public void updateTileEntity(boolean sendInventoryToClients)
    {
        if (this.craftMatrixPreview == null)
        {
            this.craftMatrixPreview = new ItemStack[9];
        }

        if (this.worldObj != null && !this.worldObj.isRemote)
        {
            EZInventory inventory = getInventory(false);

            for (int i = 0; i < 9; i++)
            {
                ItemStack stack = inventory != null && inventory.craftMatrix != null ? inventory.craftMatrix[i] : null;
                this.craftMatrixPreview[i] = stack == null ? null : stack.copy();
            }

            this.broadcastCraftPreviewIfChanged();
        }

        this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        this.onInventoryChanged();

        if (sendInventoryToClients)
        {
            EZInventoryManager.sendToClients(inventory, false);
        }
    }

    public void updateTileEntity() {
        updateTileEntity(true);
    }

    private void broadcastCraftPreviewIfChanged()
    {
        if (!(this.worldObj instanceof WorldServer worldServer))
        {
            return;
        }

        int hash = 1;
        hash = 31 * hash + (this.hasCraftBox ? 1 : 0);
        hash = 31 * hash + this.craftBoxX;
        hash = 31 * hash + this.craftBoxY;
        hash = 31 * hash + this.craftBoxZ;

        for (int i = 0; i < 9; i++)
        {
            ItemStack stack = this.craftMatrixPreview != null ? this.craftMatrixPreview[i] : null;

            if (stack == null)
            {
                hash = 31 * hash;
                continue;
            }

            hash = 31 * hash + stack.itemID;
            hash = 31 * hash + stack.getItemDamage();
            hash = 31 * hash + stack.stackSize;
        }

        if (hash == this.lastCraftPreviewHash)
        {
            return;
        }

        this.lastCraftPreviewHash = hash;
        S2CCraftingPreviewPacket packet = new S2CCraftingPreviewPacket(this);

        for (Object object : worldServer.playerEntities)
        {
            if (object instanceof EntityPlayerMP entityPlayerMP)
            {
                EmiNetwork.sendToClient(entityPlayerMP, packet);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound paramNBTTagCompound)
    {
        super.writeToNBT(paramNBTTagCompound);

        EZInventory inventory = getInventory(false);

        if (inventory != null)
        {
            // Persist external inventory file eagerly so full shutdown doesn't lose the binding target.
            EZInventoryManager.saveInventory(inventory);

            // Keep an embedded backup for recovery if the external file is missing/corrupted.
            NBTTagCompound internal = new NBTTagCompound();
            inventory.writeToNBT(internal);
            paramNBTTagCompound.setTag("Internal", internal);
        }

        paramNBTTagCompound.setString("inventoryId", inventoryId);
        paramNBTTagCompound.setBoolean("hasCraftBox", this.hasCraftBox);
        paramNBTTagCompound.setInteger("craftBoxX", this.craftBoxX);
        paramNBTTagCompound.setInteger("craftBoxY", this.craftBoxY);
        paramNBTTagCompound.setInteger("craftBoxZ", this.craftBoxZ);

        if (this.craftMatrixPreview != null)
        {
            NBTTagList gridList = new NBTTagList();

            for (int i = 0; i < 9; i++)
            {
                NBTTagCompound slotTag = new NBTTagCompound();
                slotTag.setByte("Slot", (byte)i);

                if (this.craftMatrixPreview[i] != null)
                {
                    this.craftMatrixPreview[i].writeToNBT(slotTag);
                }

                gridList.appendTag(slotTag);
            }

            paramNBTTagCompound.setTag("CraftMatrixPreview", gridList);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound paramNBTTagCompound)
    {
        super.readFromNBT(paramNBTTagCompound);

        inventoryId = paramNBTTagCompound.getString("inventoryId");
        this.hasCraftBox = paramNBTTagCompound.getBoolean("hasCraftBox");
        this.craftBoxX = paramNBTTagCompound.getInteger("craftBoxX");
        this.craftBoxY = paramNBTTagCompound.getInteger("craftBoxY");
        this.craftBoxZ = paramNBTTagCompound.getInteger("craftBoxZ");

        if (paramNBTTagCompound.hasKey("CraftMatrixPreview"))
        {
            NBTTagList gridList = paramNBTTagCompound.getTagList("CraftMatrixPreview");
            this.craftMatrixPreview = new ItemStack[9];

            for (int i = 0; i < gridList.tagCount(); i++)
            {
                NBTTagCompound slotTag = (NBTTagCompound)gridList.tagAt(i);
                int slotIndex = slotTag.getByte("Slot") & 255;

                if (slotIndex >= 0 && slotIndex < 9)
                {
                    this.craftMatrixPreview[slotIndex] = ItemStack.loadItemStackFromNBT(slotTag);
                }
            }
        }
        else if (this.craftMatrixPreview == null)
        {
            this.craftMatrixPreview = new ItemStack[9];
        }

        // Recover embedded backup data if present.
        if (paramNBTTagCompound.hasKey("Internal"))
        {
            String id = this.inventoryId;
            EZInventory existing = id == null || id.isEmpty() ? null : EZInventoryManager.getInventory(id);

            if (existing == null)
            {
                NBTTagCompound internal = paramNBTTagCompound.getCompoundTag("Internal");
                EZInventory restored = new EZInventory();
                restored.readFromNBT(internal);

                if (id == null || id.isEmpty())
                {
                    restored = EZInventoryManager.createInventory(restored);
                    this.inventoryId = restored.id;
                }
                else
                {
                    restored = EZInventoryManager.registerInventory(id, restored);
                }

                this.inventory = restored;
            }
        }
    }

    /**
     * Scans the multiblock structure for valid blocks
     */
    public void scanMultiblock(EntityLivingBase entity) {
        scanMultiblock(entity, true);
    }

    /**
     * Scans the multiblock structure for valid blocks
     */
    public void scanMultiblock(EntityLivingBase entity, boolean force)
    {
        EZInventory inventory = getInventory(true);
        int maxItems = 0;
        boolean oldHasCraftBox = this.hasCraftBox;
        int oldCraftBoxX = this.craftBoxX;
        int oldCraftBoxY = this.craftBoxY;
        int oldCraftBoxZ = this.craftBoxZ;
        inventory.maxItems = 0;
        this.hasCraftBox = false;
        this.craftBoxX = 0;
        this.craftBoxY = 0;
        this.craftBoxZ = 0;
        multiblock = new HashSet<BlockRef>();
        BlockRef ref = new BlockRef(this);
        multiblock.add(ref);
        getValidNeighbors(ref, entity);

        for (BlockRef blockRef : multiblock)
        {
            if (blockRef.block instanceof BlockStorage blockStorage) {
                maxItems += blockStorage.getCapacity();
            }
        }

        boolean craftBoxChanged = oldHasCraftBox != this.hasCraftBox
            || oldCraftBoxX != this.craftBoxX
            || oldCraftBoxY != this.craftBoxY
            || oldCraftBoxZ != this.craftBoxZ;

        if (!force && inventory.maxItems == maxItems && !craftBoxChanged)
        {
            return;
        }
        inventory.maxItems = maxItems;
        updateTileEntity(false);
    }

    /**
     * Recursive function that scans a block's neighbors, and adds valid blocks to the multiblock list
     * @param br
     */
    private void getValidNeighbors(BlockRef br, EntityLivingBase entity)
    {
        List<BlockRef> neighbors = EZStorageUtils.getNeighbors(br.posX, br.posY, br.posZ, worldObj);

        for (BlockRef blockRef : neighbors)
        {
            if (blockRef.block instanceof StorageMultiblock)
            {
                if (multiblock.add(blockRef) && validateSystem(entity))
                {
                    if (blockRef.block instanceof BlockInputPort)
                    {
                        TileEntity tileEntity = worldObj.getBlockTileEntity(blockRef.posX, blockRef.posY, blockRef.posZ);
                        if (tileEntity instanceof TileEntityInventoryProxy teInvProxy)
                        {
                            teInvProxy.core = this;
                        }
                    }

                    if (blockRef.block instanceof BlockCraftingBox)
                    {
                        hasCraftBox = true;
                        craftBoxX = blockRef.posX;
                        craftBoxY = blockRef.posY;
                        craftBoxZ = blockRef.posZ;
                    }
                    getValidNeighbors(blockRef, entity);
                }
            }
        }
    }

    public boolean validateSystem(EntityLivingBase entity)
    {
        int count = 0;

        for (BlockRef ref : multiblock)
        {
            if (ref.block instanceof BlockStorageCore)
            {
                count++;
            }

            Block thisBlock = Block.blocksList[worldObj.getBlockId(xCoord, yCoord, zCoord)];

            if (count > 1 && thisBlock instanceof BlockStorageCore)
            {
                worldObj.setBlockToAir(xCoord, yCoord, zCoord);
                worldObj.spawnEntityInWorld(new EntityItem(worldObj, xCoord, yCoord, zCoord, new ItemStack(EZBlocks.storage_core)));
                return false;
            }
        }

        return true;
    }

    public boolean isPartOfMultiblock(BlockRef blockRef)
    {
        if (multiblock != null)
        {
            if (multiblock.contains(blockRef))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public void updateEntity()
    {
        // First scan
        if (ticks == 0 && worldObj != null && !worldObj.isRemote)
        {
            scanMultiblock(null);
        }

        // 400 ticks = 20 ticks * 20 seconds
        if (ticks >= 400)
        {
            // Periodical scan
            if (!worldObj.isRemote)
            {
                scanMultiblock(null, false);
            }

            // Reset
            ticks = 0;
        }

        // Increment
        ticks += 1;
    }
}
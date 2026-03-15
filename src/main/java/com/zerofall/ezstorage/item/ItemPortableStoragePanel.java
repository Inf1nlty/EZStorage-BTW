package com.zerofall.ezstorage.item;

import com.zerofall.ezstorage.Reference;
import com.zerofall.ezstorage.block.EZBlockContainer;
import com.zerofall.ezstorage.configuration.EZConfiguration;
import com.zerofall.ezstorage.enums.PortableStoragePanelTier;
import com.zerofall.ezstorage.init.EZBlocks;
import com.zerofall.ezstorage.tileentity.TileEntityStorageCore;
import com.zerofall.ezstorage.util.EZInventory;
import com.zerofall.ezstorage.util.EZInventoryManager;
import com.zerofall.ezstorage.util.EZInventoryReference;
import net.minecraft.src.ChatMessageComponent;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.StatCollector;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.WorldServer;

import java.util.List;

/** Portable terminal item that can bind to a Storage Core and open it remotely. */
public class ItemPortableStoragePanel extends Item {

    public ItemPortableStoragePanel(int id)
    {
        super(id);
        this.setUnlocalizedName("portable_storage_terminal");
        this.setTextureName(Reference.MOD_ID + ":portable_storage_terminal");
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
        this.setCreativeTab(CreativeTabs.tabTools);
    }

    @Override
    public int getMetadata(int meta)
    {
        return meta;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        if (world.isRemote || !(player instanceof EntityPlayerMP playerMP))
        {
            return false;
        }

        TileEntity te = world.getBlockTileEntity(x, y, z);

        if (!(te instanceof TileEntityStorageCore core))
        {
            return false;
        }

        setInventoryReference(stack, core);
        playerMP.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("chat.msg.storagecore_connected"));
        return true;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        if (world.isRemote || !(player instanceof EntityPlayerMP playerMP))
        {
            return stack;
        }

        EZInventoryReference reference = getInventoryReference(stack);

        if (reference == null || !validateReference(stack))
        {
            playerMP.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("chat.msg.storagecore_not_found"));
            return stack;
        }

        if (!isInRange(stack, reference, playerMP))
        {
            playerMP.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("chat.msg.storagecore_out_of_range"));
            return stack;
        }

        WorldServer dim = reference.getWorld();

        if (dim == null)
        {
            playerMP.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("chat.msg.storagecore_not_found"));
            return stack;
        }

        EZInventory inventory = EZInventoryManager.getInventory(reference.inventoryId);

        if (inventory == null)
        {
            playerMP.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("chat.msg.storagecore_not_found"));
            return stack;
        }

        TileEntity tileEntity = dim.getBlockTileEntity(reference.blockX, reference.blockY, reference.blockZ);
        TileEntityStorageCore core = tileEntity instanceof TileEntityStorageCore storageCore ? storageCore : null;

        if (EZBlocks.storage_panel instanceof EZBlockContainer uiBlock)
        {
            uiBlock.openPlayerInventoryGui(playerMP, inventory, dim, reference.blockX, reference.blockY, reference.blockZ, core);
        }

        return stack;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced)
    {
        EZInventoryReference reference = getInventoryReference(stack);
        PortableStoragePanelTier tier = getTier(stack);
        int range = tier.isInfinity ? 0 : EZConfiguration.portableTerminalRange.getIntegerValue();

        String rangeText = tier.isInfinity
            ? StatCollector.translateToLocal("hud.msg.ezstorage.portable.range.infinity")
            : String.valueOf(range);
        tooltip.add(StatCollector.translateToLocalFormatted("hud.msg.ezstorage.portable.range", rangeText));

        String statusKey = reference == null
            ? "hud.msg.ezstorage.portable.status.notconnected"
            : "hud.msg.ezstorage.portable.status.connected";
        tooltip.add(StatCollector.translateToLocalFormatted("hud.msg.ezstorage.portable.status", StatCollector.translateToLocal(statusKey)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void getSubItems(int itemId, CreativeTabs tab, List subItems)
    {
        subItems.add(new ItemStack(itemId, 1, PortableStoragePanelTier.TIER_1.meta));
        subItems.add(new ItemStack(itemId, 1, PortableStoragePanelTier.TIER_INFINITY.meta));
    }

    public PortableStoragePanelTier getTier(ItemStack stack)
    {
        PortableStoragePanelTier tier = PortableStoragePanelTier.getTierFromMeta(stack.getItemDamage());
        return tier == null ? PortableStoragePanelTier.TIER_1 : tier;
    }

    public EZInventoryReference getInventoryReference(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();

        if (tag != null && tag.hasKey("reference"))
        {
            NBTTagCompound refTag = tag.getCompoundTag("reference");
            return new EZInventoryReference(
                refTag.getString("inventoryId"),
                refTag.getInteger("blockDimId"),
                refTag.getInteger("blockX"),
                refTag.getInteger("blockY"),
                refTag.getInteger("blockZ"));
        }

        return null;
    }

    public void setInventoryReference(ItemStack stack, TileEntityStorageCore core)
    {
        NBTTagCompound tag = stack.getTagCompound();

        if (tag == null)
        {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }

        if (core == null || core.worldObj == null || core.getInventory() == null)
        {
            tag.removeTag("reference");
            return;
        }

        NBTTagCompound refTag = new NBTTagCompound();
        refTag.setString("inventoryId", core.getInventory().id);
        refTag.setInteger("blockDimId", core.worldObj.provider.dimensionId);
        refTag.setInteger("blockX", core.xCoord);
        refTag.setInteger("blockY", core.yCoord);
        refTag.setInteger("blockZ", core.zCoord);
        tag.setTag("reference", refTag);
    }

    public boolean validateReference(ItemStack stack)
    {
        EZInventoryReference reference = getInventoryReference(stack);

        if (reference == null || reference.inventoryId == null || reference.inventoryId.isEmpty())
        {
            return false;
        }

        WorldServer dim = reference.getWorld();

        if (dim == null)
        {
            return false;
        }

        // Keep binding valid even when the chunk is not currently loaded.
        if (!dim.blockExists(reference.blockX, reference.blockY, reference.blockZ))
        {
            return true;
        }

        TileEntity te = dim.getBlockTileEntity(reference.blockX, reference.blockY, reference.blockZ);

        if (!(te instanceof TileEntityStorageCore core))
        {
            return false;
        }

        return reference.inventoryId.equals(core.inventoryId);
    }

    public static boolean isInRange(ItemStack stack, EZInventoryReference reference, EntityPlayerMP player)
    {
        if (!(stack.getItem() instanceof ItemPortableStoragePanel panel) || reference == null)
        {
            return false;
        }

        PortableStoragePanelTier tier = panel.getTier(stack);

        if (tier.isInfinity)
        {
            return true;
        }

        if (reference.blockDimId != player.worldObj.provider.dimensionId)
        {
            return false;
        }

        double dx = reference.blockX - player.posX;
        double dy = reference.blockY - player.posY;
        double dz = reference.blockZ - player.posZ;
        double dist2 = dx * dx + dy * dy + dz * dz;
        double range = EZConfiguration.portableTerminalRange.getIntegerValue();

        return dist2 <= range * range;
    }
}
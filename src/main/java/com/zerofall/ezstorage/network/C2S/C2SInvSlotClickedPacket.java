package com.zerofall.ezstorage.network.C2S;

import com.zerofall.ezstorage.container.ContainerStorageCore;
import com.zerofall.ezstorage.container.ContainerStorageCoreCrafting;
import com.zerofall.ezstorage.network.S2C.S2CCursorItemPacket;
import com.zerofall.ezstorage.EZStorageAddon;
import com.zerofall.ezstorage.util.EZInventoryManager;

import emi.dev.emi.emi.network.EmiNetwork;
import emi.dev.emi.emi.network.EmiPacket;
import emi.shims.java.net.minecraft.network.PacketByteBuf;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ResourceLocation;
import net.minecraft.src.EntityPlayerMP;

/**
 * C2S: Client clicks a storage slot.
 */
public class C2SInvSlotClickedPacket implements EmiPacket {

    public static final ResourceLocation CHANNEL = new ResourceLocation(EZStorageAddon.MOD_ID, "c2s_slot_click");

    private final int index;
    private final int button;
    private final int mode;

    public C2SInvSlotClickedPacket(int index, int button, int mode) {
        this.index = index;
        this.button = button;
        this.mode = mode;
    }

    public C2SInvSlotClickedPacket(PacketByteBuf buf) {
        this.index = buf.readVarInt();
        this.button = buf.readVarInt();
        this.mode = buf.readVarInt();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(index);
        buf.writeVarInt(button);
        buf.writeVarInt(mode);
    }

    @Override
    public void apply(EntityPlayer player)
    {
        if (player.openContainer instanceof ContainerStorageCore storageContainer)
        {
            storageContainer.customSlotClick(index, button, mode, player);

            // Persist crafting grid before broadcast so S2C payload includes the latest matrix.
            if (storageContainer instanceof ContainerStorageCoreCrafting craftingContainer)
            {
                craftingContainer.saveGrid();
            }

            // Always push authoritative storage state; avoids stale client grid during rapid shift moves.
            EZInventoryManager.sendToClients(storageContainer.inventory);

            // Sync cursor item back to the client so it appears immediately
            if (player instanceof EntityPlayerMP entityPlayerMP)
            {
                EmiNetwork.sendToClient(entityPlayerMP, new S2CCursorItemPacket(player.inventory.getItemStack()));
            }

            // Force sync container slots (including player inventory) to client
            storageContainer.detectAndSendChanges();

            // During rapid shift transfers, vanilla incremental slot updates can be dropped/arrive late.
            // Push a full container snapshot so client inventory visuals stay in lock-step.
            if (player instanceof EntityPlayerMP entityPlayerMP && mode == 1)
            {
                entityPlayerMP.sendContainerToPlayer(storageContainer);
                entityPlayerMP.sendContainerToPlayer(entityPlayerMP.inventoryContainer);
            }
        }
    }

    @Override
    public ResourceLocation getId() {
        return CHANNEL;
    }
}
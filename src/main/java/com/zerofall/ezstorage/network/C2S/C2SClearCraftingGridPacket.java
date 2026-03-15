package com.zerofall.ezstorage.network.C2S;

import com.zerofall.ezstorage.EZStorageAddon;
import com.zerofall.ezstorage.container.ContainerStorageCoreCrafting;
import emi.dev.emi.emi.network.EmiPacket;
import emi.shims.java.net.minecraft.network.PacketByteBuf;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ResourceLocation;

/**
 * C2S: Client requests clearing the crafting grid.
 */
public class C2SClearCraftingGridPacket implements EmiPacket {

    public static final ResourceLocation CHANNEL = new ResourceLocation(EZStorageAddon.MOD_ID, "c2s_clear_crafting");

    public C2SClearCraftingGridPacket() {}

    public C2SClearCraftingGridPacket(PacketByteBuf buf) {}

    @Override
    public void write(PacketByteBuf buf) {}

    @Override
    public void apply(EntityPlayer player) {
        if (player.openContainer instanceof ContainerStorageCoreCrafting container) {
            container.clearGrid(player);
        }
    }

    @Override
    public ResourceLocation getId() {
        return CHANNEL;
    }
}
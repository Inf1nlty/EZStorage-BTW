package com.zerofall.ezstorage.network;

import com.zerofall.ezstorage.network.C2S.C2SClearCraftingGridPacket;
import com.zerofall.ezstorage.network.C2S.C2SInvSlotClickedPacket;
import com.zerofall.ezstorage.network.C2S.C2SReqCraftingPacket;
import com.zerofall.ezstorage.network.S2C.S2CCraftingPreviewPacket;
import com.zerofall.ezstorage.network.S2C.S2CCursorItemPacket;
import com.zerofall.ezstorage.network.S2C.S2COpenGuiPacket;
import com.zerofall.ezstorage.network.S2C.S2CStoragePacket;
import emi.dev.emi.emi.PacketReader;

public class EZStoragePacketHandler {

    public static void registerAllPackets() {
        // Server-bound (C2S)
        PacketReader.registerServerPacketReader(C2SInvSlotClickedPacket.CHANNEL, C2SInvSlotClickedPacket::new);
        PacketReader.registerServerPacketReader(C2SReqCraftingPacket.CHANNEL, C2SReqCraftingPacket::new);
        PacketReader.registerServerPacketReader(C2SClearCraftingGridPacket.CHANNEL, C2SClearCraftingGridPacket::new);

        // Client-bound (S2C)
        PacketReader.registerClientPacketReader(S2CStoragePacket.CHANNEL, buf -> new S2CStoragePacket(buf));
        PacketReader.registerClientPacketReader(S2COpenGuiPacket.CHANNEL, buf -> new S2COpenGuiPacket(buf));
        PacketReader.registerClientPacketReader(S2CCursorItemPacket.CHANNEL, buf -> new S2CCursorItemPacket(buf));
        PacketReader.registerClientPacketReader(S2CCraftingPreviewPacket.CHANNEL, buf -> new S2CCraftingPreviewPacket(buf));
    }
}
package com.zerofall.ezstorage;

import api.BTWAddon;
import com.zerofall.ezstorage.configuration.EZConfiguration;
import com.zerofall.ezstorage.init.EZBlocks;
import com.zerofall.ezstorage.init.EZItems;
import com.zerofall.ezstorage.network.EZStoragePacketHandler;
import fi.dy.masa.malilib.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.zerofall.ezstorage.client.gui.GuiHandler;

public class EZStorageAddon extends BTWAddon {

    public static final String MOD_ID = Reference.MOD_ID;

    public static EZStorageAddon instance;

    public final Logger LOG = LogManager.getLogger(Reference.MOD_ID);
    public final GuiHandler guiHandler = new GuiHandler();

    @Override
    public void initialize() {
        instance = this;

        EZConfiguration.init();
        ConfigManager.getInstance().registerConfig(EZConfiguration.getInstance());

        EZBlocks.registerBlocks();
        EZItems.registerItems();
        EZBlocks.registerRecipes();
        EZItems.registerRecipes();
        EZStoragePacketHandler.registerAllPackets();
    }
}
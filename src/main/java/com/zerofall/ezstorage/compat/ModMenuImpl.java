package com.zerofall.ezstorage.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.zerofall.ezstorage.configuration.EZConfiguration;

public class ModMenuImpl implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> EZConfiguration.getInstance().getConfigScreen(parent);
    }
}

package com.zalithboost;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.zalithboost.ui.MenuScreen;

/** Integración con Mod Menu: abre nuestro menú desde la lista de mods. */
public class ZbModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return MenuScreen::new;
    }
}

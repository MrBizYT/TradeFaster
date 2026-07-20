package com.tradefaster;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TradeFasterMod implements ModInitializer {
    public static final String MOD_ID = "tradefaster";
    public static final Logger LOGGER = LoggerFactory.getLogger("TradeFaster");

    @Override
    public void onInitialize() {
        LOGGER.info("[TradeFaster] Initializing - buy all villager trades with one click.");
    }
}

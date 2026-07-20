package com.tradefaster.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;

public class TradeFasterClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (TradeFasterState.isBuying && client.screen instanceof MerchantScreen merchantScreen) {
                MerchantMenu menu = merchantScreen.getMenu();

                // Check if the result slot still has an item
                ItemStack resultItem = menu.getSlot(2).getItem();
                if (resultItem.isEmpty()) {
                    // Result slot is empty — but it might be a brief gap between server updates.
                    // Wait for several consecutive empty ticks before confirming exhaustion.
                    TradeFasterState.emptyTickCount++;
                    if (TradeFasterState.emptyTickCount >= TradeFasterState.EMPTY_TICK_GRACE) {
                        // Trade truly exhausted — stop buying
                        TradeFasterState.isBuying = false;
                        TradeFasterState.emptyTickCount = 0;
                        if (TradeFasterState.button != null) {
                            TradeFasterState.button.active = true;
                        }
                    }
                    return;
                }

                // Result slot has an item — reset empty counter and perform one trade
                TradeFasterState.emptyTickCount = 0;

                if (client.player != null && client.gameMode != null) {
                    client.gameMode.handleContainerInput(
                        menu.containerId,
                        2,                          // Result slot index
                        0,                          // Primary action button
                        ContainerInput.QUICK_MOVE,  // Shift-click to inventory
                        client.player
                    );
                }
            }

            // If screen closed, reset state
            if (TradeFasterState.isBuying && !(client.screen instanceof MerchantScreen)) {
                TradeFasterState.isBuying = false;
                TradeFasterState.emptyTickCount = 0;
                TradeFasterState.button = null;
            }
        });
    }
}

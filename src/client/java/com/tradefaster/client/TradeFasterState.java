package com.tradefaster.client;

import net.minecraft.client.gui.components.Button;

public class TradeFasterState {
    public static boolean isBuying = false;
    public static Button button = null;
    // Counter for consecutive ticks where the result slot appears empty.
    // Prevents premature stop due to brief empty state between server updates.
    public static int emptyTickCount = 0;
    // How many consecutive empty ticks before we consider the trade truly exhausted.
    public static final int EMPTY_TICK_GRACE = 5;
}

package com.tradefaster.client.mixin;

import com.tradefaster.TradeFasterMod;
import com.tradefaster.client.TradeFasterScheduler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin {

    @Unique
    private static final int TRADEFASTER_RESULT_SLOT_INDEX = 2;

    @Unique
    private static final int TRADEFASTER_MAX_ATTEMPTS = 32;

    @Unique
    private static final long TRADEFASTER_CLICK_DELAY_MS = 100L;

    @Unique
    private Button tradefaster$buyAllButton;

    @Unique
    private MerchantMenu tradefaster$getMenu() {
        AbstractContainerScreen<?> self = (AbstractContainerScreen<?>) this;
        return (MerchantMenu) self.getMenu();
    }

    @Inject(method = "init", at = @At("TAIL"), require = 0)
    private void tradefaster$addBuyAllButton(CallbackInfo ci) {
        TradeFasterMod.LOGGER.info("[TradeFaster] MerchantScreen.init() injected - creating button");
        this.tradefaster$createButton();
    }

    @Inject(method = "extractContents", at = @At("HEAD"), require = 0)
    private void tradefaster$updateButtonState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (this.tradefaster$buyAllButton == null) {
            this.tradefaster$createButton();
            if (this.tradefaster$buyAllButton == null) {
                return;
            }
        }

        MerchantMenu menu = this.tradefaster$getMenu();
        ItemStack result = menu.getSlot(TRADEFASTER_RESULT_SLOT_INDEX).getItem();
        boolean hasTrade = !result.isEmpty();
        this.tradefaster$buyAllButton.active = hasTrade;
    }

    @Unique
    private void tradefaster$createButton() {
        AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) (Object) this;
        int leftPos = accessor.tradefaster$getLeftPos();
        int topPos = accessor.tradefaster$getTopPos();

        int buttonW = 90;
        int buttonH = 14;
        int rightPanelLeft = leftPos + 99;
        int rightPanelWidth = 177;
        int buttonX = rightPanelLeft + (rightPanelWidth - buttonW) / 2;
        int buttonY = topPos + 72 - buttonH - 2;

        this.tradefaster$buyAllButton = Button.builder(
            Component.literal("\u21bb Buy All"),
            b -> this.tradefaster$buyAll()
        ).bounds(buttonX, buttonY, buttonW, buttonH).build();

        this.tradefaster$buyAllButton.active = false;

        Screen self = (Screen) this;
        ScreenAccessor screenAccessor = (ScreenAccessor) self;
        screenAccessor.tradefaster$addRenderableWidget(this.tradefaster$buyAllButton);

        TradeFasterMod.LOGGER.info("[TradeFaster] Buy All button created at ({}, {}) size {}x{}",
            buttonX, buttonY, buttonW, buttonH);
    }

    @Unique
    private void tradefaster$buyAll() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null) {
            return;
        }

        MerchantMenu menu = this.tradefaster$getMenu();
        int containerId = menu.containerId;
        Screen expectedScreen = (Screen) this;

        MerchantScreenAccessor selfAccessor = (MerchantScreenAccessor) (Object) this;
        int shopItem = selfAccessor.tradefaster$getShopItem();

        Slot resultSlot = menu.getSlot(TRADEFASTER_RESULT_SLOT_INDEX);
        if (resultSlot.getItem().isEmpty()) {
            return;
        }

        TradeFasterMod.LOGGER.info("[TradeFaster] Buy All triggered - trade index {}, max {} attempts",
            shopItem, TRADEFASTER_MAX_ATTEMPTS);

        for (int i = 0; i < TRADEFASTER_MAX_ATTEMPTS; i++) {
            long delayMs = (long) i * TRADEFASTER_CLICK_DELAY_MS;
            int attempt = i;
            TradeFasterScheduler.schedule(() -> mc.execute(() -> {
                if (mc.player == null || mc.gameMode == null) return;
                if (mc.screen != expectedScreen) return;

                try {
                    menu.tryMoveItems(shopItem);
                } catch (Throwable t) {
                    TradeFasterMod.LOGGER.warn("[TradeFaster] tryMoveItems failed on attempt {}", attempt, t);
                }

                Slot rs = menu.getSlot(TRADEFASTER_RESULT_SLOT_INDEX);
                if (rs.getItem().isEmpty()) {
                    TradeFasterMod.LOGGER.info("[TradeFaster] Result slot empty after tryMoveItems on attempt {} - stopping", attempt);
                    return;
                }

                try {
                    mc.gameMode.handleContainerInput(containerId, TRADEFASTER_RESULT_SLOT_INDEX, 0,
                        ContainerInput.QUICK_MOVE, (Player) mc.player);
                } catch (Throwable t) {
                    TradeFasterMod.LOGGER.warn("[TradeFaster] Click attempt {} failed", attempt, t);
                }
            }), delayMs);
        }
    }
}

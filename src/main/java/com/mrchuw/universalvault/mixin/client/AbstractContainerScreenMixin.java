package com.mrchuw.universalvault.mixin.client;

import com.mrchuw.universalvault.client.EncodedPatternOverlay;
import javax.annotation.Nullable;

//? if <26.1 {
/*import net.minecraft.client.gui.GuiGraphics;
*///?} else {
import net.minecraft.client.gui.GuiGraphicsExtractor;
 //?}
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
    @Shadow @Nullable protected Slot hoveredSlot;

    //? if <1.21.11 {
    /*@Inject(method = "renderSlot", at = @At("HEAD"), cancellable = true, require = 0)
    private void uv$substituteSlot(GuiGraphics p_281607_, Slot p_282613_, CallbackInfo ci) {
        var self = (AbstractContainerScreen<?>) (Object) this;
        if (EncodedPatternOverlay.renderForSlot(p_281607_, self, hoveredSlot, p_282613_)) {
            ci.cancel();
        }
    }
    *///?} elif =1.21.11 {
    /*@Inject(method = "renderSlot", at = @At("HEAD"), cancellable = true, require = 0)
    private void uv$substituteSlot(GuiGraphics p_281607_, Slot p_282613_, int p_470717_, int p_470566_, CallbackInfo ci) {
        var self = (AbstractContainerScreen<?>) (Object) this;
        if (EncodedPatternOverlay.renderForSlot(p_281607_, self, hoveredSlot, p_282613_)) {
            ci.cancel();
        }
    }
    *///?} else {
    @Inject(method = "extractSlot", at = @At("HEAD"),
            cancellable = true, require = 0)
    private void uv$substituteSlot(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        var self = (AbstractContainerScreen<?>) (Object) this;
        if (EncodedPatternOverlay.renderForSlot(graphics, self, hoveredSlot, slot)) {
            ci.cancel();
        }
    }
    //?}
}
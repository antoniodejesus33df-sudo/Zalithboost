package com.zalithboost.mixin;

import com.zalithboost.config.ZbConfig;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Único mixin del mod: oculta la mira vanilla cuando usamos la mira personalizada. */
@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void zb$ocultarMiraVanilla(CallbackInfo ci) {
        if (ZbConfig.I.xhEnabled) ci.cancel();
    }
}

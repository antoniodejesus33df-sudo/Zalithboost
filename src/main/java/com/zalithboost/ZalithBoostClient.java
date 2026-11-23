package com.zalithboost;

import com.zalithboost.config.ZbConfig;
import com.zalithboost.hud.AlertsHud;
import com.zalithboost.hud.CrosshairHud;
import com.zalithboost.hud.HudManager;
import com.zalithboost.perf.Perf;
import com.zalithboost.ui.FloatButton;
import com.zalithboost.ui.HudEditorScreen;
import com.zalithboost.ui.MenuScreen;
import com.zalithboost.util.CpsTracker;
import com.zalithboost.util.SystemMonitor;
import com.zalithboost.util.Targeting;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/** Punto de entrada del cliente. Mod 100% cliente: no toca red ni servidor. */
public class ZalithBoostClient implements ClientModInitializer {
    public static final String ID = "zalithboost";
    public static KeyBinding openKey;

    @Override
    public void onInitializeClient() {
        ZbConfig.load();

        // Tecla configurable (en Zalith puedes asignarla a un botón de tus controles)
        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.zalithboost.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, "category.zalithboost"));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openKey.wasPressed()) {
                if (mc.currentScreen == null) mc.setScreen(new MenuScreen(null));
            }
            CpsTracker.tick(mc);
            Targeting.tick(mc);
            AlertsHud.tick(mc);
            SystemMonitor.tick();
            Perf.tick(mc);
        });

        // Dibujo del HUD (alertas -> mira -> elementos movibles)
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.options.hudHidden || mc.currentScreen instanceof HudEditorScreen) return;
            AlertsHud.render(ctx, mc);
            CrosshairHud.render(ctx, mc);
            HudManager.renderAll(ctx, mc, false, null);
        });

        // Botón táctil flotante (pantallas de pausa / inventario)
        FloatButton.register();

        // Al cerrar el juego: restaurar FPS si el modo anti-calor los había limitado
        ClientLifecycleEvents.CLIENT_STOPPING.register(c -> {
            Perf.restoreThrottle();
            ZbConfig.save();
        });
    }
}

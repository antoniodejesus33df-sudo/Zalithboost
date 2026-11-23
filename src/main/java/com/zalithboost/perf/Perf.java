package com.zalithboost.perf;

import com.zalithboost.config.ZbConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.text.Text;

/**
 * Optimización: perfiles rápidos, auto-ajuste dinámico, modo anti-calor y limpieza de memoria.
 * Solo se tocan opciones vanilla, así que no entra en conflicto con Sodium/Lithium/VulkanMod.
 */
public final class Perf {
    private static int tickC, low, high, baseRender = -1, lastAuto = -1, gcC;
    private static ParticlesMode baseParticles;
    private static boolean throttled;
    private static int savedFps;

    private Perf() {}

    public static void tick(MinecraftClient mc) {
        if (mc.world == null) return;
        if (++tickC % 20 == 0) { auto(mc); thermal(mc); }
        gc(mc);
    }

    // ---------- Auto-ajuste dinámico ----------
    private static void auto(MinecraftClient mc) {
        ZbConfig z = ZbConfig.I;
        if (!z.autoAdjust || mc.currentScreen != null || throttled) return;
        GameOptions o = mc.options;
        int cur = o.getViewDistance().getValue();
        if (cur != lastAuto) baseRender = cur; // el usuario lo cambió a mano
        int fps = mc.getCurrentFps();
        if (fps < z.targetFps) { low++; high = 0; }
        else if (fps > z.targetFps * 1.4) { high++; low = 0; }
        else { low = 0; high = 0; }

        if (low >= 3) {
            if (cur > z.minRender) { cur--; o.getViewDistance().setValue(cur); }
            else if (baseParticles == null) {
                baseParticles = o.getParticles().getValue();
                o.getParticles().setValue(ParticlesMode.MINIMAL);
            }
            low = 0;
        } else if (high >= 8) {
            if (baseParticles != null) { o.getParticles().setValue(baseParticles); baseParticles = null; }
            else if (cur < baseRender) { cur++; o.getViewDistance().setValue(cur); }
            high = 0;
        }
        lastAuto = cur;
    }

    // ---------- Modo anti-calor ----------
    private static void thermal(MinecraftClient mc) {
        ZbConfig z = ZbConfig.I;
        if (!z.thermalMode) { if (throttled) restoreThrottle(); return; }
        float t = com.zalithboost.util.SystemMonitor.tempC;
        if (Float.isNaN(t)) return; // Android no deja leer la temperatura
        if (!throttled && t >= z.thermalLimit) {
            savedFps = mc.options.getMaxFps().getValue();
            mc.options.getMaxFps().setValue(30);
            throttled = true;
            msg(mc, "zalithboost.msg.thermal_on");
        } else if (throttled && t < z.thermalLimit - 3) {
            restoreThrottle();
        }
    }

    public static void restoreThrottle() {
        if (!throttled) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.options.getMaxFps().setValue(savedFps);
        throttled = false;
        msg(mc, "zalithboost.msg.thermal_off");
    }

    // ---------- Limpieza de memoria (solo en el menú de pausa, para no causar tirones) ----------
    private static void gc(MinecraftClient mc) {
        int mins = ZbConfig.I.gcMinutes;
        if (mins <= 0) return;
        if (++gcC >= mins * 1200 && mc.currentScreen instanceof GameMenuScreen) {
            System.gc(); // es solo una sugerencia a la JVM
            gcC = 0;
        }
    }

    // ---------- Perfiles rápidos ----------
    public static void applyProfile(int p) {
        switch (p) {
            // vista, simulación, fps máx, partículas, nubes, gráficos, sombras, escala entidades, mezcla bioma, bobbing
            case 0 -> apply(6, 5, 120, ParticlesMode.MINIMAL, CloudRenderMode.OFF, GraphicsMode.FAST, false, 0.5, 0, false);   // Ultra rendimiento
            case 1 -> apply(10, 8, 90, ParticlesMode.DECREASED, CloudRenderMode.FAST, GraphicsMode.FAST, false, 1.0, 1, true); // Equilibrado
            case 2 -> apply(14, 10, 60, ParticlesMode.ALL, CloudRenderMode.FANCY, GraphicsMode.FANCY, true, 1.5, 3, true);     // Calidad
            default -> apply(4, 5, 60, ParticlesMode.MINIMAL, CloudRenderMode.OFF, GraphicsMode.FAST, false, 0.5, 0, false);   // Mínimo (1 clic)
        }
    }

    private static void apply(int view, int sim, int fps, ParticlesMode pm, CloudRenderMode cm, GraphicsMode gm,
                              boolean shadows, double entScale, int blend, boolean bob) {
        MinecraftClient mc = MinecraftClient.getInstance();
        GameOptions o = mc.options;
        o.getViewDistance().setValue(view);
        o.getSimulationDistance().setValue(sim);
        o.getMaxFps().setValue(fps);
        o.getParticles().setValue(pm);
        o.getCloudRenderMode().setValue(cm);
        o.getGraphicsMode().setValue(gm);
        o.getEntityShadows().setValue(shadows);
        o.getEntityDistanceScaling().setValue(entScale);
        o.getBiomeBlendRadius().setValue(blend);
        o.getBobView().setValue(bob);
        baseRender = -1; lastAuto = -1; baseParticles = null; // el auto-ajuste toma estos valores como base
        o.write();
        o.sendClientSettings();
        msg(mc, "zalithboost.msg.profile_applied");
    }

    /** Menos movimiento: sin bobbing y sin efectos de FOV/distorsión. */
    public static void applyReduceMotion(boolean on) {
        GameOptions o = MinecraftClient.getInstance().options;
        o.getBobView().setValue(!on);
        o.getFovEffectScale().setValue(on ? 0.0 : 1.0);
        o.getDistortionEffectScale().setValue(on ? 0.0 : 1.0);
    }

    private static void msg(MinecraftClient mc, String key) {
        if (mc.player != null) mc.player.sendMessage(Text.translatable(key), true);
    }
}

package com.zalithboost.perf;

import com.zalithboost.util.RendererInfo;
import com.zalithboost.util.SystemMonitor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.particle.ParticlesMode;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Textos de la pestaña de diagnóstico: renderer, memoria, JVM recomendada, qué cuesta más FPS y compatibilidad. */
public final class Diagnostics {
    private Diagnostics() {}

    private record Cost(String id, double value) {}

    private static String t(String key, Object... a) { return Text.translatable(key, a).getString(); }

    public static String renderer() {
        return t("zalithboost.diag.renderer", RendererInfo.full()) + "\n" + t("zalithboost.diag.renderer_note");
    }

    public static String memory() {
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) >> 20, max = rt.maxMemory() >> 20;
        long dev = SystemMonitor.deviceRamMb();
        String s = t("zalithboost.diag.heap", used, max);
        s += "\n" + (dev > 0 ? t("zalithboost.diag.devram", dev) : t("zalithboost.diag.devram_na"));
        s += "\n" + (SystemMonitor.battery >= 0
                ? t("zalithboost.diag.battery", SystemMonitor.battery,
                    Float.isNaN(SystemMonitor.tempC) ? "?" : String.format(Locale.ROOT, "%.1f", SystemMonitor.tempC))
                : t("zalithboost.diag.battery_na"));
        return s;
    }

    /** Recomendación de argumentos JVM según la RAM del celular. */
    public static String jvm() {
        long dev = SystemMonitor.deviceRamMb();
        long base = dev > 0 ? dev : Runtime.getRuntime().maxMemory() >> 20;
        int xmx = base <= 3500 ? 1024 : base <= 5500 ? 1536 : base <= 7500 ? 2048 : base <= 11500 ? 3072 : 4096;
        return t("zalithboost.diag.jvm", "-Xmx" + xmx + "M -Xms" + (xmx / 2) + "M -XX:+UseG1GC -XX:MaxGCPauseMillis=50");
    }

    /** Estima qué opciones te cuestan más FPS y sugiere un ajuste (heurística, no un benchmark real). */
    public static String costs() {
        GameOptions o = MinecraftClient.getInstance().options;
        List<Cost> l = new ArrayList<>();
        int view = o.getViewDistance().getValue();
        l.add(new Cost("view", view * view / 4.0));
        int sim = o.getSimulationDistance().getValue();
        l.add(new Cost("sim", sim * sim / 8.0));
        ParticlesMode pm = o.getParticles().getValue();
        l.add(new Cost("particles", pm == ParticlesMode.ALL ? 12 : pm == ParticlesMode.DECREASED ? 6 : 1));
        CloudRenderMode cm = o.getCloudRenderMode().getValue();
        l.add(new Cost("clouds", cm == CloudRenderMode.FANCY ? 10 : cm == CloudRenderMode.FAST ? 3 : 0));
        l.add(new Cost("shadows", o.getEntityShadows().getValue() ? 6 : 0));
        l.add(new Cost("entscale", o.getEntityDistanceScaling().getValue() * 6));
        int b = o.getBiomeBlendRadius().getValue();
        l.add(new Cost("blend", (2 * b + 1) * (2 * b + 1) / 4.0));
        GraphicsMode gm = o.getGraphicsMode().getValue();
        l.add(new Cost("graphics", gm == GraphicsMode.FABULOUS ? 40 : gm == GraphicsMode.FANCY ? 8 : 0));
        l.sort(Comparator.comparingDouble(Cost::value).reversed());

        StringBuilder sb = new StringBuilder(t("zalithboost.diag.costs_title"));
        for (int i = 0; i < 3; i++) {
            Cost c = l.get(i);
            sb.append("\n").append(i + 1).append(". ")
              .append(t("zalithboost.opt." + c.id())).append(" - ")
              .append(t("zalithboost.tip." + c.id()));
        }
        return sb.toString();
    }

    public static String compat() {
        FabricLoader f = FabricLoader.getInstance();
        StringBuilder sb = new StringBuilder(t("zalithboost.diag.compat_title"));
        for (String id : new String[]{"sodium", "lithium", "vulkanmod", "entityculling", "modmenu"}) {
            sb.append("\n").append(id).append(": ")
              .append(t(f.isModLoaded(id) ? "zalithboost.diag.mod_on" : "zalithboost.diag.mod_off"));
        }
        sb.append("\n").append(t("zalithboost.diag.compat_note"));
        return sb.toString();
    }
}

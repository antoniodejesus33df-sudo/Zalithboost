package com.zalithboost.ui;

import com.zalithboost.config.ZbConfig;
import com.zalithboost.hud.HudElement;
import com.zalithboost.hud.HudManager;
import com.zalithboost.perf.Diagnostics;
import com.zalithboost.perf.Perf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.particle.ParticlesMode;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Definición de todas las opciones del menú, agrupadas por pestaña. */
public final class MenuOptions {
    public static final int INFO = 0, COMBAT = 1, PERF = 2, LOOK = 3, PROFILES = 4, DIAG = 5;

    private MenuOptions() {}

    private static ZbConfig c() { return ZbConfig.I; }
    private static String k(String id) { return "zalithboost.opt." + id; }
    private static String n(double v) { return String.valueOf((int) Math.round(v)); }
    private static String en(Enum<?> e) { return Text.translatable("zalithboost.val." + e.name().toLowerCase(Locale.ROOT)).getString(); }
    private static void toast(MinecraftClient mc, String key) {
        if (mc.player != null) mc.player.sendMessage(Text.translatable(key), true);
    }

    public static List<Opt> build(MenuScreen menu) {
        MinecraftClient mc = MinecraftClient.getInstance();
        GameOptions o = mc.options;
        List<Opt> l = new ArrayList<>();

        // ===================== INFORMACIÓN =====================
        l.add(new Opt.Header(INFO, k("sec.hud")));
        for (HudElement e : HudManager.ALL) {
            String id = e.id;
            l.add(new Opt.Toggle(INFO, "zalithboost.hud." + id, () -> c().elem(id).enabled, v -> c().elem(id).enabled = v));
        }
        l.add(new Opt.Action(INFO, k("edit_hud"), () -> mc.setScreen(new HudEditorScreen(menu))));
        l.add(new Opt.Header(INFO, k("sec.enemy")));
        l.add(new Opt.Toggle(INFO, k("enemy_equip"), () -> c().enemyEquip, v -> c().enemyEquip = v));
        l.add(new Opt.Toggle(INFO, k("enemy_fx"), () -> c().enemyEffects, v -> c().enemyEffects = v));
        l.add(new Opt.Slider(INFO, k("enemy_range"), 4, 16, 1, () -> c().enemyRange, v -> c().enemyRange = (int) Math.round(v), MenuOptions::n));
        l.add(new Opt.Toggle(INFO, k("fps_graph"), () -> c().fpsGraph, v -> c().fpsGraph = v));

        // ===================== COMBATE =====================
        l.add(new Opt.Header(COMBAT, k("sec.crosshair")));
        l.add(new Opt.Toggle(COMBAT, k("xh_on"), () -> c().xhEnabled, v -> c().xhEnabled = v));
        l.add(new Opt.Cycle(COMBAT, k("xh_shape"), 4, () -> c().xhShape, v -> c().xhShape = v,
                v -> Text.translatable("zalithboost.val.shape." + v).getString()));
        l.add(new Opt.Slider(COMBAT, k("xh_size"), 4, 16, 1, () -> c().xhSize, v -> c().xhSize = (int) Math.round(v), MenuOptions::n));
        l.add(new Opt.Slider(COMBAT, k("xh_thick"), 1, 4, 1, () -> c().xhThick, v -> c().xhThick = (int) Math.round(v), MenuOptions::n));
        l.add(new Opt.Cycle(COMBAT, k("xh_color"), Ui.PALETTE.length, () -> c().xhColor, v -> c().xhColor = v,
                v -> Text.translatable("zalithboost.val.color." + v).getString()));
        l.add(new Opt.Toggle(COMBAT, k("xh_dynamic"), () -> c().xhDynamic, v -> c().xhDynamic = v));
        l.add(new Opt.Cycle(COMBAT, k("xh_in"), Ui.PALETTE.length, () -> c().xhInColor, v -> c().xhInColor = v,
                v -> Text.translatable("zalithboost.val.color." + v).getString()));
        l.add(new Opt.Cycle(COMBAT, k("xh_out"), Ui.PALETTE.length, () -> c().xhOutColor, v -> c().xhOutColor = v,
                v -> Text.translatable("zalithboost.val.color." + v).getString()));
        l.add(new Opt.Slider(COMBAT, k("xh_offx"), -20, 20, 1, () -> c().xhOffX, v -> c().xhOffX = (int) Math.round(v), MenuOptions::n));
        l.add(new Opt.Slider(COMBAT, k("xh_offy"), -20, 20, 1, () -> c().xhOffY, v -> c().xhOffY = (int) Math.round(v), MenuOptions::n));

        l.add(new Opt.Header(COMBAT, k("sec.alerts")));
        l.add(new Opt.Toggle(COMBAT, k("low_hp"), () -> c().lowHealthAlert, v -> c().lowHealthAlert = v));
        l.add(new Opt.Slider(COMBAT, k("low_hp_hearts"), 2, 10, 1, () -> c().lowHealthHearts, v -> c().lowHealthHearts = (int) Math.round(v), MenuOptions::n));
        l.add(new Opt.Toggle(COMBAT, k("dura_alert"), () -> c().durabilityAlert, v -> c().durabilityAlert = v));

        l.add(new Opt.Header(COMBAT, k("sec.comfort")));
        l.add(new Opt.Toggle(COMBAT, k("sprint"), () -> o.getSprintToggled().getValue(), v -> o.getSprintToggled().setValue(v)));
        l.add(new Opt.Toggle(COMBAT, k("sneak"), () -> o.getSneakToggled().getValue(), v -> o.getSneakToggled().setValue(v)));
        l.add(new Opt.Toggle(COMBAT, k("reduce_motion"), () -> c().reduceMotion, v -> { c().reduceMotion = v; Perf.applyReduceMotion(v); }));

        // ===================== RENDIMIENTO =====================
        l.add(new Opt.Header(PERF, k("sec.profiles")));
        l.add(new Opt.Action(PERF, k("prof_ultra"), () -> Perf.applyProfile(0)));
        l.add(new Opt.Action(PERF, k("prof_balanced"), () -> Perf.applyProfile(1)));
        l.add(new Opt.Action(PERF, k("prof_quality"), () -> Perf.applyProfile(2)));
        l.add(new Opt.Action(PERF, k("prof_min"), () -> Perf.applyProfile(3)));

        l.add(new Opt.Header(PERF, k("sec.auto")));
        l.add(new Opt.Toggle(PERF, k("auto"), () -> c().autoAdjust, v -> c().autoAdjust = v));
        l.add(new Opt.Slider(PERF, k("target_fps"), 20, 120, 5, () -> c().targetFps, v -> c().targetFps = (int) Math.round(v), MenuOptions::n));
        l.add(new Opt.Slider(PERF, k("min_render"), 2, 12, 1, () -> c().minRender, v -> c().minRender = (int) Math.round(v), MenuOptions::n));

        l.add(new Opt.Header(PERF, k("sec.manual")));
        l.add(new Opt.Slider(PERF, k("view"), 2, 32, 1, () -> o.getViewDistance().getValue(), v -> o.getViewDistance().setValue((int) Math.round(v)), MenuOptions::n));
        l.add(new Opt.Slider(PERF, k("sim"), 5, 32, 1, () -> o.getSimulationDistance().getValue(), v -> o.getSimulationDistance().setValue((int) Math.round(v)), MenuOptions::n));
        l.add(new Opt.Slider(PERF, k("maxfps"), 10, 260, 10, () -> o.getMaxFps().getValue(), v -> o.getMaxFps().setValue((int) Math.round(v)),
                v -> v >= 260 ? Text.translatable("zalithboost.val.unlimited").getString() : n(v)));
        l.add(new Opt.Cycle(PERF, k("particles"), ParticlesMode.values().length, () -> o.getParticles().getValue().ordinal(),
                v -> o.getParticles().setValue(ParticlesMode.values()[v]), v -> en(ParticlesMode.values()[v])));
        l.add(new Opt.Cycle(PERF, k("clouds"), CloudRenderMode.values().length, () -> o.getCloudRenderMode().getValue().ordinal(),
                v -> o.getCloudRenderMode().setValue(CloudRenderMode.values()[v]), v -> en(CloudRenderMode.values()[v])));
        l.add(new Opt.Cycle(PERF, k("graphics"), GraphicsMode.values().length, () -> o.getGraphicsMode().getValue().ordinal(),
                v -> o.getGraphicsMode().setValue(GraphicsMode.values()[v]), v -> en(GraphicsMode.values()[v])));
        l.add(new Opt.Toggle(PERF, k("shadows"), () -> o.getEntityShadows().getValue(), v -> o.getEntityShadows().setValue(v)));
        l.add(new Opt.Slider(PERF, k("entscale"), 0.5, 5.0, 0.5, () -> o.getEntityDistanceScaling().getValue(),
                v -> o.getEntityDistanceScaling().setValue(v), v -> String.format(Locale.ROOT, "%.1f", v)));
        l.add(new Opt.Slider(PERF, k("blend"), 0, 7, 1, () -> o.getBiomeBlendRadius().getValue(), v -> o.getBiomeBlendRadius().setValue((int) Math.round(v)), MenuOptions::n));
        l.add(new Opt.Toggle(PERF, k("bob"), () -> o.getBobView().getValue(), v -> o.getBobView().setValue(v)));

        l.add(new Opt.Header(PERF, k("sec.battery")));
        l.add(new Opt.Toggle(PERF, k("thermal"), () -> c().thermalMode, v -> c().thermalMode = v));
        l.add(new Opt.Slider(PERF, k("thermal_limit"), 38, 50, 1, () -> c().thermalLimit, v -> c().thermalLimit = (int) Math.round(v), v -> n(v) + " \u00b0C"));
        l.add(new Opt.Slider(PERF, k("gc"), 0, 30, 5, () -> c().gcMinutes, v -> c().gcMinutes = (int) Math.round(v),
                v -> v == 0 ? Text.translatable("zalithboost.val.off").getString() : n(v) + " min"));

        // ===================== APARIENCIA =====================
        l.add(new Opt.Header(LOOK, k("sec.theme")));
        l.add(new Opt.Cycle(LOOK, k("theme"), 5, () -> c().theme, v -> c().theme = v,
                v -> Text.translatable("zalithboost.val.theme." + v).getString()));
        l.add(new Opt.Slider(LOOK, k("r"), 0, 255, 5, () -> c().customR, v -> { c().customR = (int) Math.round(v); c().theme = 4; }, MenuOptions::n));
        l.add(new Opt.Slider(LOOK, k("g"), 0, 255, 5, () -> c().customG, v -> { c().customG = (int) Math.round(v); c().theme = 4; }, MenuOptions::n));
        l.add(new Opt.Slider(LOOK, k("b"), 0, 255, 5, () -> c().customB, v -> { c().customB = (int) Math.round(v); c().theme = 4; }, MenuOptions::n));
        l.add(new Opt.Slider(LOOK, k("opacity"), 0.2, 1.0, 0.05, () -> c().hudOpacity, v -> c().hudOpacity = (float) v,
                v -> Math.round(v * 100) + "%"));
        l.add(new Opt.Header(LOOK, k("sec.float")));
        l.add(new Opt.Toggle(LOOK, k("float_on"), () -> c().floatButton, v -> c().floatButton = v));
        l.add(new Opt.Slider(LOOK, k("float_size"), 16, 40, 2, () -> c().floatSize, v -> c().floatSize = (int) Math.round(v), MenuOptions::n));
        l.add(new Opt.Action(LOOK, k("float_reset"), () -> { c().floatX = 0.93f; c().floatY = 0.40f; }));
        l.add(new Opt.Action(LOOK, k("edit_hud"), () -> mc.setScreen(new HudEditorScreen(menu))));

        // ===================== PERFILES =====================
        l.add(new Opt.Header(PROFILES, k("sec.slots")));
        l.add(new Opt.Cycle(PROFILES, k("slot"), 3, () -> c().slot - 1, v -> c().slot = v + 1, v -> "#" + (v + 1)));
        l.add(new Opt.Action(PROFILES, k("slot_save"), () -> {
            ZbConfig.save();
            toast(mc, ZbConfig.saveSlot(c().slot) ? "zalithboost.msg.saved" : "zalithboost.msg.error");
        }));
        l.add(new Opt.Action(PROFILES, k("slot_load"), () -> {
            if (ZbConfig.loadSlot(c().slot)) { toast(mc, "zalithboost.msg.loaded"); menu.reopen(); }
            else toast(mc, "zalithboost.msg.error");
        }));
        l.add(new Opt.Action(PROFILES, k("export"), () -> {
            mc.keyboard.setClipboard(ZbConfig.toJson());
            toast(mc, "zalithboost.msg.exported");
        }));
        l.add(new Opt.Action(PROFILES, k("import"), () -> {
            if (ZbConfig.replace(mc.keyboard.getClipboard())) { toast(mc, "zalithboost.msg.loaded"); menu.reopen(); }
            else toast(mc, "zalithboost.msg.error");
        }));
        l.add(new Opt.Action(PROFILES, k("reset_all"), () -> { ZbConfig.I = new ZbConfig(); ZbConfig.save(); menu.reopen(); }));

        // ===================== DIAGNÓSTICO =====================
        l.add(new Opt.Info(DIAG, Diagnostics::renderer));
        l.add(new Opt.Info(DIAG, Diagnostics::memory));
        l.add(new Opt.Info(DIAG, Diagnostics::costs));
        l.add(new Opt.Info(DIAG, Diagnostics::jvm));
        l.add(new Opt.Info(DIAG, Diagnostics::compat));
        return l;
    }
}

package com.zalithboost.hud;

import com.zalithboost.config.ZbConfig;
import com.zalithboost.ui.Ui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;

/** Registro de elementos del HUD y su dibujo (posición y escala vienen de la config). */
public final class HudManager {
    public static final List<HudElement> ALL = List.of(
            new Elements.Fps(), new Elements.Cps(), new Elements.Coords(), new Elements.Enemy(),
            new Elements.Armor(), new Elements.Effects(), new Elements.Sys());

    private HudManager() {}

    public static ZbConfig.Elem defaultFor(String id) {
        for (HudElement e : ALL) if (e.id.equals(id)) return new ZbConfig.Elem(e.defOn, e.defX, e.defY);
        return new ZbConfig.Elem(false, 0.05f, 0.05f);
    }

    public static void renderAll(DrawContext c, MinecraftClient mc, boolean editing, HudElement selected) {
        int sw = c.getScaledWindowWidth(), sh = c.getScaledWindowHeight();
        for (HudElement e : ALL) {
            ZbConfig.Elem cfg = ZbConfig.I.elem(e.id);
            if (!cfg.enabled && !editing) continue;
            MatrixStack m = c.getMatrices();
            m.push();
            m.translate(cfg.x * sw, cfg.y * sh, 0f);
            m.scale(cfg.scale, cfg.scale, 1f);
            e.render(c, mc, editing);
            m.pop();
            if (editing) {
                int x = (int) (cfg.x * sw), y = (int) (cfg.y * sh);
                int ww = Math.round(e.w * cfg.scale), hh = Math.round(e.h * cfg.scale);
                int col = e == selected ? Ui.theme().a() : (cfg.enabled ? 0x99FFFFFF : 0x66FF5555);
                Ui.outline(c, x - 1, y - 1, ww + 2, hh + 2, col);
                String label = e.name() + (cfg.enabled ? "" : " (off)");
                c.drawText(mc.textRenderer, label, x, y - 10, col | 0xFF000000, true);
            }
        }
    }
}

package com.zalithboost.hud;

import com.zalithboost.config.ZbConfig;
import com.zalithboost.ui.Ui;
import com.zalithboost.util.Targeting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/** Mira personalizada. Cambia de color según si lo que apuntas está al alcance normal de golpe. */
public final class CrosshairHud {
    private CrosshairHud() {}

    public static void render(DrawContext c, MinecraftClient mc) {
        ZbConfig z = ZbConfig.I;
        if (!z.xhEnabled || mc.currentScreen != null || !mc.options.getPerspective().isFirstPerson()) return;

        int col = Ui.PALETTE[Math.floorMod(z.xhColor, Ui.PALETTE.length)];
        if (z.xhDynamic && Targeting.aimed) {
            col = Ui.PALETTE[Math.floorMod(Targeting.inReach ? z.xhInColor : z.xhOutColor, Ui.PALETTE.length)];
        }
        int cx = c.getScaledWindowWidth() / 2 + z.xhOffX, cy = c.getScaledWindowHeight() / 2 + z.xhOffY;
        int s = Math.max(4, z.xhSize), t = Math.max(1, z.xhThick), h = t / 2;

        switch (z.xhShape) {
            case 1 -> { // punto
                int d = Math.max(2, t * 2);
                c.fill(cx - d / 2, cy - d / 2, cx - d / 2 + d, cy - d / 2 + d, col);
            }
            case 2 -> { // círculo
                int n = Math.max(16, s * 6);
                for (int i = 0; i < n; i++) {
                    double a = 2 * Math.PI * i / n;
                    int px = cx + (int) Math.round(Math.cos(a) * s), py = cy + (int) Math.round(Math.sin(a) * s);
                    c.fill(px, py, px + t, py + t, col);
                }
            }
            case 3 -> { // cruz con hueco al centro
                int g = 3;
                c.fill(cx - s, cy - h, cx - g, cy - h + t, col);
                c.fill(cx + g + 1, cy - h, cx + s + 1, cy - h + t, col);
                c.fill(cx - h, cy - s, cx - h + t, cy - g, col);
                c.fill(cx - h, cy + g + 1, cx - h + t, cy + s + 1, col);
            }
            default -> { // cruz completa
                c.fill(cx - s, cy - h, cx + s + 1, cy - h + t, col);
                c.fill(cx - h, cy - s, cx - h + t, cy + s + 1, col);
            }
        }
    }
}

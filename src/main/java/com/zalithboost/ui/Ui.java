package com.zalithboost.ui;

import com.zalithboost.config.ZbConfig;
import net.minecraft.client.gui.DrawContext;

/** Utilidades de dibujo: degradados, esquinas redondeadas, temas de color. */
public final class Ui {
    private Ui() {}

    /** Colores a (acento 1), b (acento 2), bg1/bg2 (degradado del panel). */
    public record Theme(int a, int b, int bg1, int bg2) {}

    private static final Theme[] THEMES = {
            new Theme(0xFFFF3DDB, 0xFF00E5FF, 0xF0150A28, 0xF0070B1E), // neón
            new Theme(0xFF00B4DB, 0xFF0077B6, 0xF0072B40, 0xF0031623), // océano
            new Theme(0xFFFF7E5F, 0xFFFD3A69, 0xF03D1B30, 0xF01B0B1B), // atardecer
            new Theme(0xFF8AB4F8, 0xFF6B7280, 0xF01F1F23, 0xF0111114)  // oscuro
    };

    /** Paleta para la mira: blanco, rojo, verde, cian, amarillo, magenta, naranja. */
    public static final int[] PALETTE = {0xFFFFFFFF, 0xFFFF3B3B, 0xFF3BFF6B, 0xFF3BE8FF, 0xFFFFE83B, 0xFFFF3BE0, 0xFFFF9A3B};

    public static Theme theme() {
        ZbConfig z = ZbConfig.I;
        if (z.theme == 4) { // personalizado
            int c = 0xFF000000 | (z.customR << 16) | (z.customG << 8) | z.customB;
            return new Theme(c, lerp(c, 0xFF000000, 0.45f), 0xF0141420, 0xF00A0A12);
        }
        return THEMES[Math.floorMod(z.theme, THEMES.length)];
    }

    public static int lerp(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = 0;
        for (int s = 24; s >= 0; s -= 8) {
            int ca = (a >> s) & 0xFF, cb = (b >> s) & 0xFF;
            r |= (((int) (ca + (cb - ca) * t)) & 0xFF) << s;
        }
        return r;
    }

    public static int withAlpha(int color, int alpha) { return (alpha << 24) | (color & 0xFFFFFF); }

    /** Rectángulo redondeado con degradado vertical top -> bottom. */
    public static void rrect(DrawContext c, int x, int y, int w, int h, int r, int top, int bottom) {
        if (w <= 0 || h <= 0) return;
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        if (top == bottom) { // camino rápido: 2r filas de esquinas + 1 relleno central
            for (int i = 0; i < r; i++) {
                int in = inset(i, r);
                c.fill(x + in, y + i, x + w - in, y + i + 1, top);
                c.fill(x + in, y + h - 1 - i, x + w - in, y + h - i, top);
            }
            c.fill(x, y + r, x + w, y + h - r, top);
            return;
        }
        for (int i = 0; i < h; i++) {
            int in = inset(Math.min(i, h - 1 - i), r);
            c.fill(x + in, y + i, x + w - in, y + i + 1, lerp(top, bottom, h <= 1 ? 0f : (float) i / (h - 1)));
        }
    }

    private static int inset(int row, int r) {
        if (row >= r) return 0;
        double dy = r - (row + 0.5);
        return (int) Math.ceil(r - Math.sqrt(r * r - dy * dy));
    }

    public static void outline(DrawContext c, int x, int y, int w, int h, int col) {
        c.fill(x, y, x + w, y + 1, col);
        c.fill(x, y + h - 1, x + w, y + h, col);
        c.fill(x, y, x + 1, y + h, col);
        c.fill(x + w - 1, y, x + w, y + h, col);
    }

    /** Fondo estándar de los elementos del HUD (opacidad configurable + franja de acento). */
    public static void panel(DrawContext c, int w, int h) {
        int al = (int) (Math.max(0.1f, Math.min(1f, ZbConfig.I.hudOpacity)) * 255) & 0xFF;
        int bg = (al << 24) | 0x101018;
        rrect(c, 0, 0, w, h, 4, bg, bg);
        c.fill(0, 3, 2, h - 3, theme().a());
    }

    public static String fmtTime(int ticks) {
        int s = Math.max(0, ticks / 20);
        return String.format(java.util.Locale.ROOT, "%d:%02d", s / 60, s % 60);
    }
}

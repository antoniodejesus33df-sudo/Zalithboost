package com.zalithboost.ui;

import com.zalithboost.config.ZbConfig;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

/**
 * Botón táctil flotante, movible (arrástralo) y ocultable (opción en el menú).
 * Aparece en la pantalla de pausa y en inventarios: durante el juego el cursor está "capturado",
 * así que un botón dibujado en el HUD no se puede tocar. Para abrir el menú en pleno juego,
 * asigna la tecla del mod a un botón de tus controles de Zalith.
 */
public final class FloatButton {
    private static boolean dragging, moved;
    private static double sx, sy;

    private FloatButton() {}

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (!(screen instanceof GameMenuScreen || screen instanceof HandledScreen<?>)) return;
            ScreenEvents.afterRender(screen).register((s, ctx, mx, my, delta) -> render(ctx, s, mx, my));
            ScreenMouseEvents.allowMouseClick(screen).register((s, mx, my, b) -> !onClick(s, mx, my, b));
            ScreenMouseEvents.allowMouseRelease(screen).register((s, mx, my, b) -> !onRelease(s, b));
        });
    }

    private static int bx(Screen s) { return (int) (ZbConfig.I.floatX * (s.width - ZbConfig.I.floatSize)); }
    private static int by(Screen s) { return (int) (ZbConfig.I.floatY * (s.height - ZbConfig.I.floatSize)); }

    private static boolean inside(Screen s, double mx, double my) {
        int z = ZbConfig.I.floatSize;
        return mx >= bx(s) && mx < bx(s) + z && my >= by(s) && my < by(s) + z;
    }

    private static void render(DrawContext ctx, Screen s, int mx, int my) {
        ZbConfig z = ZbConfig.I;
        if (!z.floatButton) return;
        if (dragging) {
            if (Math.hypot(mx - sx, my - sy) > 3) moved = true;
            if (moved) {
                z.floatX = (float) Math.max(0, Math.min(1, (mx - z.floatSize / 2.0) / (s.width - z.floatSize)));
                z.floatY = (float) Math.max(0, Math.min(1, (my - z.floatSize / 2.0) / (s.height - z.floatSize)));
            }
        }
        Ui.Theme t = Ui.theme();
        int x = bx(s), y = by(s), sz = z.floatSize;
        Ui.rrect(ctx, x - 1, y - 1, sz + 2, sz + 2, (sz + 2) / 2, 0x80000000, 0x80000000);
        Ui.rrect(ctx, x, y, sz, sz, sz / 2, t.a(), t.b());
        MinecraftClient mc = MinecraftClient.getInstance();
        ctx.drawText(mc.textRenderer, "Z", x + (sz - mc.textRenderer.getWidth("Z")) / 2, y + (sz - 8) / 2, 0xFFFFFFFF, true);
    }

    /** @return true si consumimos el clic. */
    private static boolean onClick(Screen s, double mx, double my, int button) {
        if (!ZbConfig.I.floatButton || button != 0 || !inside(s, mx, my)) return false;
        dragging = true; moved = false; sx = mx; sy = my;
        return true;
    }

    private static boolean onRelease(Screen s, int button) {
        if (!dragging) return false;
        dragging = false;
        if (moved) ZbConfig.save();
        else MinecraftClient.getInstance().setScreen(new MenuScreen(s)); // toque corto = abrir menú
        return true;
    }
}

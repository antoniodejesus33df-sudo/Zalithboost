package com.zalithboost.ui;

import com.zalithboost.config.ZbConfig;
import com.zalithboost.hud.HudElement;
import com.zalithboost.hud.HudManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Modo "editar HUD": arrastra cada elemento con el dedo; botones +/- (o rueda) para escalar. */
public class HudEditorScreen extends Screen {
    private final Screen parent;
    private HudElement sel;
    private double offX, offY;

    public HudEditorScreen(Screen parent) {
        super(Text.translatable("zalithboost.edit_hud.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int y = height - 26, x = width / 2 - 125;
        addDrawableChild(ButtonWidget.builder(Text.literal("-"), b -> scale(-0.1f)).dimensions(x, y, 30, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("+"), b -> scale(0.1f)).dimensions(x + 32, y, 30, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("zalithboost.edit_hud.toggle"), b -> {
            if (sel != null) { ZbConfig.Elem c = ZbConfig.I.elem(sel.id); c.enabled = !c.enabled; }
        }).dimensions(x + 64, y, 60, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("zalithboost.edit_hud.reset"), b -> {
            if (sel != null) { ZbConfig.Elem c = ZbConfig.I.elem(sel.id); c.x = sel.defX; c.y = sel.defY; c.scale = 1f; }
        }).dimensions(x + 126, y, 60, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("zalithboost.edit_hud.done"), b -> close())
                .dimensions(x + 188, y, 62, 20).build());
    }

    private void scale(float d) {
        if (sel == null) return;
        ZbConfig.Elem c = ZbConfig.I.elem(sel.id);
        c.scale = Math.max(0.5f, Math.min(3f, Math.round((c.scale + d) * 10f) / 10f));
    }

    @Override public void renderBackground(DrawContext ctx, int mx, int my, float delta) { ctx.fill(0, 0, width, height, 0x66000000); }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        renderBackground(ctx, mx, my, delta);
        HudManager.renderAll(ctx, client, true, sel);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.translatable("zalithboost.edit_hud.hint"), width / 2, 6, 0xFFFFFFFF);
        super.render(ctx, mx, my, delta);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (super.mouseClicked(mx, my, button)) return true;
        for (int i = HudManager.ALL.size() - 1; i >= 0; i--) {
            HudElement e = HudManager.ALL.get(i);
            ZbConfig.Elem c = ZbConfig.I.elem(e.id);
            double x = c.x * width, y = c.y * height;
            if (mx >= x && mx <= x + e.w * c.scale && my >= y && my <= y + e.h * c.scale) {
                sel = e; offX = mx - x; offY = my - y;
                return true;
            }
        }
        sel = null;
        return true;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (sel != null) {
            ZbConfig.Elem c = ZbConfig.I.elem(sel.id);
            c.x = (float) Math.max(0, Math.min(0.98, (mx - offX) / width));
            c.y = (float) Math.max(0, Math.min(0.98, (my - offY) / height));
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmount, double vAmount) {
        scale(vAmount > 0 ? 0.1f : -0.1f);
        return true;
    }

    @Override
    public void close() {
        ZbConfig.save();
        client.setScreen(parent);
    }
}

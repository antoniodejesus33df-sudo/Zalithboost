package com.zalithboost.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/** Elemento movible/escalable del HUD. Se dibuja en coordenadas locales (0,0 = esquina superior izquierda). */
public abstract class HudElement {
    public final String id;
    public final float defX, defY;
    public final boolean defOn;
    public int w = 80, h = 20; // tamaño calculado en el último dibujo (sin escala)

    protected HudElement(String id, float defX, float defY, boolean defOn) {
        this.id = id; this.defX = defX; this.defY = defY; this.defOn = defOn;
    }

    public String name() { return Text.translatable("zalithboost.hud." + id).getString(); }

    /** @param preview true en el editor de HUD: dibuja datos de ejemplo. */
    public abstract void render(DrawContext c, MinecraftClient mc, boolean preview);
}

package com.zalithboost.ui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoubleSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/** Fila del menú: interruptor, slider, selector, botón, encabezado o texto informativo. */
public abstract class Opt {
    public static final int ROW = 26, GAP = 3; // filas altas = fáciles de tocar con el dedo

    public final int tab;
    public final String key;
    protected float anim; // animación suave del hover

    protected Opt(int tab, String key) { this.tab = tab; this.key = key; }

    public String label() { return Text.translatable(key).getString(); }
    public boolean header() { return false; }
    public boolean matches(String q) { return label().toLowerCase(Locale.ROOT).contains(q); }
    public int height(TextRenderer tr, int w) { return ROW; }
    public boolean isSlider() { return false; }
    public void click(double mx, int x, int w) {}
    public void drag(double mx, int x, int w) {}
    public abstract void render(DrawContext c, TextRenderer tr, int x, int y, int w, int h, boolean hover);

    protected void rowBg(DrawContext c, int x, int y, int w, int h, boolean hover) {
        anim += ((hover ? 1f : 0f) - anim) * 0.25f;
        int col = Ui.lerp(0x30FFFFFF, (Ui.theme().a() & 0xFFFFFF) | 0x66000000, anim);
        Ui.rrect(c, x, y, w, h, 5, col, col);
    }

    // ------------------------------------------------------------------
    public static final class Header extends Opt {
        public Header(int tab, String key) { super(tab, key); }
        @Override public boolean header() { return true; }
        @Override public int height(TextRenderer tr, int w) { return 16; }
        @Override public void render(DrawContext c, TextRenderer tr, int x, int y, int w, int h, boolean hover) {
            c.drawText(tr, label(), x + 4, y + 5, Ui.theme().a(), true);
            c.fill(x + 4, y + h - 2, x + w - 4, y + h - 1, Ui.withAlpha(Ui.theme().a(), 0x60));
        }
    }

    // ------------------------------------------------------------------
    public static final class Toggle extends Opt {
        private final BooleanSupplier get;
        private final Consumer<Boolean> set;
        private float knob = -1f;
        public Toggle(int tab, String key, BooleanSupplier get, Consumer<Boolean> set) { super(tab, key); this.get = get; this.set = set; }
        @Override public void click(double mx, int x, int w) { set.accept(!get.getAsBoolean()); }
        @Override public void render(DrawContext c, TextRenderer tr, int x, int y, int w, int h, boolean hover) {
            rowBg(c, x, y, w, h, hover);
            c.drawText(tr, label(), x + 8, y + (h - 9) / 2, 0xFFFFFFFF, true);
            float target = get.getAsBoolean() ? 1f : 0f;
            if (knob < 0) knob = target;
            knob += (target - knob) * 0.3f; // animación suave del interruptor
            int sw = 28, sh = 12, sx = x + w - sw - 8, sy = y + (h - sh) / 2;
            int bg = Ui.lerp(0xFF55596A, Ui.theme().a(), knob);
            Ui.rrect(c, sx, sy, sw, sh, 6, bg, bg);
            Ui.rrect(c, sx + 1 + (int) ((sw - sh) * knob), sy + 1, sh - 2, sh - 2, 5, 0xFFFFFFFF, 0xFFFFFFFF);
        }
    }

    // ------------------------------------------------------------------
    public static final class Slider extends Opt {
        private final double min, max, step;
        private final DoubleSupplier get;
        private final DoubleConsumer set;
        private final DoubleFunction<String> fmt;
        public Slider(int tab, String key, double min, double max, double step, DoubleSupplier get, DoubleConsumer set, DoubleFunction<String> fmt) {
            super(tab, key); this.min = min; this.max = max; this.step = step; this.get = get; this.set = set; this.fmt = fmt;
        }
        @Override public boolean isSlider() { return true; }
        @Override public void click(double mx, int x, int w) { drag(mx, x, w); }
        @Override public void drag(double mx, int x, int w) {
            double f = Math.max(0, Math.min(1, (mx - (x + 8)) / (w - 16)));
            double v = Math.round((min + f * (max - min)) / step) * step;
            set.accept(Math.max(min, Math.min(max, v)));
        }
        @Override public void render(DrawContext c, TextRenderer tr, int x, int y, int w, int h, boolean hover) {
            rowBg(c, x, y, w, h, hover);
            double v = get.getAsDouble();
            c.drawText(tr, label(), x + 8, y + 4, 0xFFFFFFFF, true);
            String s = fmt.apply(v);
            c.drawText(tr, s, x + w - 8 - tr.getWidth(s), y + 4, 0xFFDDE3EE, true);
            int bx = x + 8, bw = w - 16, by = y + h - 8;
            Ui.rrect(c, bx, by, bw, 4, 2, 0x66000000, 0x66000000);
            double f = Math.max(0, Math.min(1, (v - min) / (max - min)));
            Ui.rrect(c, bx, by, Math.max(4, (int) (bw * f)), 4, 2, Ui.theme().a(), Ui.theme().b());
            Ui.rrect(c, bx + (int) (bw * f) - 3, by - 2, 6, 8, 3, 0xFFFFFFFF, 0xFFFFFFFF);
        }
    }

    // ------------------------------------------------------------------
    public static final class Cycle extends Opt {
        private final int count;
        private final IntSupplier get;
        private final IntConsumer set;
        private final IntFunction<String> name;
        public Cycle(int tab, String key, int count, IntSupplier get, IntConsumer set, IntFunction<String> name) {
            super(tab, key); this.count = count; this.get = get; this.set = set; this.name = name;
        }
        @Override public void click(double mx, int x, int w) { set.accept((get.getAsInt() + 1) % count); }
        @Override public void render(DrawContext c, TextRenderer tr, int x, int y, int w, int h, boolean hover) {
            rowBg(c, x, y, w, h, hover);
            c.drawText(tr, label(), x + 8, y + (h - 9) / 2, 0xFFFFFFFF, true);
            String s = "< " + name.apply(Math.floorMod(get.getAsInt(), count)) + " >";
            c.drawText(tr, s, x + w - 8 - tr.getWidth(s), y + (h - 9) / 2, Ui.theme().a(), true);
        }
    }

    // ------------------------------------------------------------------
    public static final class Action extends Opt {
        private final Runnable run;
        public Action(int tab, String key, Runnable run) { super(tab, key); this.run = run; }
        @Override public void click(double mx, int x, int w) { run.run(); }
        @Override public void render(DrawContext c, TextRenderer tr, int x, int y, int w, int h, boolean hover) {
            anim += ((hover ? 1f : 0f) - anim) * 0.25f;
            int a = Ui.lerp(Ui.withAlpha(Ui.theme().a(), 0x90), Ui.withAlpha(Ui.theme().a(), 0xF0), anim);
            int b = Ui.lerp(Ui.withAlpha(Ui.theme().b(), 0x90), Ui.withAlpha(Ui.theme().b(), 0xF0), anim);
            Ui.rrect(c, x, y, w, h, 6, a, b);
            String s = label();
            c.drawText(tr, s, x + (w - tr.getWidth(s)) / 2, y + (h - 9) / 2, 0xFFFFFFFF, true);
        }
    }

    // ------------------------------------------------------------------
    /** Texto informativo de varias líneas (diagnóstico). El texto se pide a un Supplier en cada frame. */
    public static final class Info extends Opt {
        private final Supplier<String> text;
        private List<OrderedText> lines = new ArrayList<>();
        public Info(int tab, Supplier<String> text) { super(tab, ""); this.text = text; }
        @Override public String label() { return text.get(); }
        @Override public int height(TextRenderer tr, int w) {
            lines = new ArrayList<>();
            for (String part : text.get().split("\n")) lines.addAll(tr.wrapLines(StringVisitable.plain(part), w - 12));
            return lines.size() * 10 + 10;
        }
        @Override public void render(DrawContext c, TextRenderer tr, int x, int y, int w, int h, boolean hover) {
            Ui.rrect(c, x, y, w, h, 5, 0x30FFFFFF, 0x30FFFFFF);
            for (int i = 0; i < lines.size(); i++) c.drawText(tr, lines.get(i), x + 6, y + 5 + i * 10, i == 0 ? 0xFFFFFFFF : 0xFFCFD6E4, true);
        }
    }
}

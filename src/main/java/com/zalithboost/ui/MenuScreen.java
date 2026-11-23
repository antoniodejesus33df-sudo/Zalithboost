package com.zalithboost.ui;

import com.zalithboost.ZalithBoostClient;
import com.zalithboost.config.ZbConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Menú principal "fancy": degradados, esquinas redondeadas, animación de entrada, pestañas con iconos,
 * buscador y lista con scroll táctil (arrastra con el dedo). Todas las filas son altas para tocarlas fácil.
 */
public class MenuScreen extends Screen {
    private static final Item[] TAB_ICONS = {Items.COMPASS, Items.IRON_SWORD, Items.REDSTONE, Items.PAINTING, Items.BOOK, Items.SPYGLASS};
    private static final String[] TAB_KEYS = {"info", "combat", "perf", "look", "profiles", "diag"};

    private final Screen parent;
    private final List<Opt> all;
    private int tab;
    private double scroll;
    private String query = "";
    private TextFieldWidget search;
    private int px, py, pw, ph, tabY, lx, ly, lw, lh;
    private int age;

    // Lista visible (se recalcula cada frame)
    private final List<Opt> vis = new ArrayList<>();
    private int[] visY = new int[0], visH = new int[0];
    private int contentH;

    // Estado del gesto táctil
    private Opt pending, active;
    private boolean moved, listDrag;
    private double dragAcc;

    public MenuScreen(Screen parent) {
        super(Text.translatable("zalithboost.title"));
        this.parent = parent;
        this.all = MenuOptions.build(this);
    }

    /** Reabre el menú (tras cargar un perfil, para refrescar todos los valores). */
    public void reopen() { client.setScreen(new MenuScreen(parent)); }

    @Override
    protected void init() {
        pw = Math.min(width - 8, 380);
        ph = height - 8;
        px = (width - pw) / 2;
        py = 4;
        tabY = py + 26;
        lx = px + 6;
        lw = pw - 12;
        ly = tabY + 26;
        lh = py + ph - ly - 4;

        int sx = px + 12 + textRenderer.getWidth(title) + 6;
        int sw = px + pw - 26 - sx;
        search = new TextFieldWidget(textRenderer, sx, py + 5, Math.max(40, sw), 16, Text.empty());
        search.setPlaceholder(Text.translatable("zalithboost.search"));
        search.setMaxLength(24);
        search.setText(query);
        search.setChangedListener(s -> { query = s.toLowerCase(Locale.ROOT).trim(); scroll = 0; });
        addDrawableChild(search);
    }

    @Override public void tick() { age++; }

    @Override
    public void renderBackground(DrawContext ctx, int mx, int my, float delta) {
        float e = ease(Math.min(1f, (age + delta) / 8f));
        ctx.fill(0, 0, width, height, ((int) (0xA0 * e) << 24));
    }

    private static float ease(float t) { float u = 1 - t; return 1 - u * u * u; }

    private void layout() {
        vis.clear();
        for (Opt o : all) {
            if (query.isEmpty() ? o.tab == tab : (!o.header() && o.matches(query))) vis.add(o);
        }
        visY = new int[vis.size()];
        visH = new int[vis.size()];
        int y = 0;
        for (int i = 0; i < vis.size(); i++) {
            visH[i] = vis.get(i).height(textRenderer, lw - 8);
            visY[i] = y;
            y += visH[i] + Opt.GAP;
        }
        contentH = y;
        scroll = Math.max(0, Math.min(scroll, Math.max(0, contentH - lh)));
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        renderBackground(ctx, mx, my, delta);
        float e = ease(Math.min(1f, (age + delta) / 8f));
        Ui.Theme th = Ui.theme();
        layout();

        ctx.getMatrices().push();
        ctx.getMatrices().translate(0f, (1 - e) * 14f, 0f); // animación de entrada (desliza hacia arriba)

        Ui.rrect(ctx, px, py, pw, ph, 8, th.bg1(), th.bg2());
        Ui.rrect(ctx, px + 8, py, pw - 16, 2, 1, th.a(), th.b());
        ctx.drawText(textRenderer, title, px + 10, py + 9, th.a(), true);
        // botón cerrar
        boolean overX = mx >= px + pw - 22 && mx < px + pw - 6 && my >= py + 5 && my < py + 21;
        Ui.rrect(ctx, px + pw - 22, py + 5, 16, 16, 5, overX ? 0xFFE04848 : 0x55FFFFFF, overX ? 0xFFB03030 : 0x33FFFFFF);
        ctx.drawText(textRenderer, "x", px + pw - 17, py + 9, 0xFFFFFFFF, true);

        // pestañas con icono
        int tw = (pw - 12) / TAB_KEYS.length;
        for (int i = 0; i < TAB_KEYS.length; i++) {
            int tx = px + 6 + i * tw;
            boolean sel = i == tab && query.isEmpty();
            if (sel) Ui.rrect(ctx, tx + 1, tabY, tw - 2, 22, 6, Ui.withAlpha(th.a(), 0xC0), Ui.withAlpha(th.b(), 0xC0));
            else Ui.rrect(ctx, tx + 1, tabY, tw - 2, 22, 6, 0x25FFFFFF, 0x25FFFFFF);
            boolean wide = tw >= 66;
            int ix = wide ? tx + 5 : tx + (tw - 16) / 2;
            ctx.drawItem(new ItemStack(TAB_ICONS[i]), ix, tabY + 3);
            if (wide) ctx.drawText(textRenderer, Text.translatable("zalithboost.tab." + TAB_KEYS[i]), tx + 24, tabY + 7, 0xFFFFFFFF, true);
        }

        // lista con scroll
        ctx.enableScissor(lx, ly, lx + lw, ly + lh);
        int rowW = lw - 8;
        for (int i = 0; i < vis.size(); i++) {
            int y = ly - (int) scroll + visY[i];
            if (y + visH[i] < ly || y > ly + lh) continue;
            boolean hover = mx >= lx && mx < lx + rowW && my >= Math.max(y, ly) && my < Math.min(y + visH[i], ly + lh);
            vis.get(i).render(ctx, textRenderer, lx, y, rowW, visH[i], hover);
        }
        if (vis.isEmpty()) ctx.drawText(textRenderer, Text.translatable("zalithboost.no_results"), lx + 8, ly + 8, 0xFFB8C0D0, true);
        ctx.disableScissor();

        // barra de scroll
        if (contentH > lh) {
            int bh = Math.max(16, lh * lh / contentH);
            int by = ly + (int) ((lh - bh) * (scroll / (contentH - lh)));
            Ui.rrect(ctx, lx + lw - 4, by, 3, bh, 1, Ui.withAlpha(th.a(), 0xB0), Ui.withAlpha(th.a(), 0xB0));
        }

        super.render(ctx, mx, my, delta); // dibuja el buscador
        ctx.getMatrices().pop();
    }

    // ----------------------------- Entrada táctil -----------------------------
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (search.isMouseOver(mx, my)) return super.mouseClicked(mx, my, button);
        search.setFocused(false);

        if (mx >= px + pw - 22 && mx < px + pw - 6 && my >= py + 5 && my < py + 21) { close(); return true; }

        int tw = (pw - 12) / TAB_KEYS.length;
        if (my >= tabY && my < tabY + 22 && mx >= px + 6) {
            int i = (int) ((mx - px - 6) / tw);
            if (i >= 0 && i < TAB_KEYS.length) { tab = i; scroll = 0; search.setText(""); }
            return true;
        }

        pending = null; active = null; moved = false; dragAcc = 0; listDrag = false;
        if (my >= ly && my < ly + lh && mx >= lx && mx < lx + lw) {
            listDrag = true;
            for (int i = 0; i < vis.size() && i < visY.length; i++) {
                int y = ly - (int) scroll + visY[i];
                if (my >= y && my < y + visH[i] && mx < lx + lw - 8) {
                    Opt o = vis.get(i);
                    if (o.isSlider()) { active = o; o.drag(mx, lx, lw - 8); }
                    else pending = o; // se activa al soltar, solo si no fue un arrastre (scroll)
                    break;
                }
            }
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (active != null) { active.drag(mx, lx, lw - 8); return true; }
        if (listDrag) {
            dragAcc += Math.abs(dy);
            if (dragAcc > 4) moved = true;
            if (moved) scroll = Math.max(0, Math.min(scroll - dy, Math.max(0, contentH - lh)));
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (pending != null && !moved) pending.click(mx, lx, lw - 8);
        pending = null; active = null; listDrag = false;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmount, double vAmount) {
        scroll = Math.max(0, Math.min(scroll - vAmount * 18, Math.max(0, contentH - lh)));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!search.isFocused() && ZalithBoostClient.openKey.matchesKey(keyCode, scanCode)) { close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        ZbConfig.save();
        client.options.write();
        client.setScreen(parent);
    }
}

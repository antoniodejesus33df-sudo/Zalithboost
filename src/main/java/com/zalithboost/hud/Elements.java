package com.zalithboost.hud;

import com.zalithboost.config.ZbConfig;
import com.zalithboost.ui.Ui;
import com.zalithboost.util.CpsTracker;
import com.zalithboost.util.FrameStats;
import com.zalithboost.util.RendererInfo;
import com.zalithboost.util.SystemMonitor;
import com.zalithboost.util.Targeting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/** Elementos del HUD: FPS, CPS, coordenadas, tarjeta del enemigo, armadura, efectos y sistema. */
public final class Elements {
    private Elements() {}

    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private static String effectText(StatusEffectInstance e) {
        String n = e.getEffectType().value().getName().getString();
        if (e.getAmplifier() > 0) n += " " + (e.getAmplifier() + 1);
        return n + "  " + (e.isInfinite() ? "inf" : Ui.fmtTime(e.getDuration()));
    }

    private static int effectColor(StatusEffectInstance e) { return 0xFF000000 | e.getEffectType().value().getColor(); }

    // =====================================================================================
    public static final class Fps extends HudElement {
        public Fps() { super("fps", 0.01f, 0.02f, true); }

        @Override public void render(DrawContext c, MinecraftClient mc, boolean preview) {
            TextRenderer tr = mc.textRenderer;
            FrameStats.frame();
            int fps = mc.getCurrentFps();
            String l1 = "FPS " + fps;
            String l2 = "avg " + FrameStats.avg() + "  min " + FrameStats.min() + "  max " + FrameStats.max();
            boolean g = ZbConfig.I.fpsGraph;
            int gh = 22;
            w = Math.max(Math.max(tr.getWidth(l1), tr.getWidth(l2)) + 12, g ? 100 : 0);
            h = 4 + 20 + 4 + (g ? gh + 4 : 0);
            Ui.panel(c, w, h);
            int col = fps >= 50 ? 0xFF4CFF7A : fps >= 30 ? 0xFFFFD84C : 0xFFFF5252;
            c.drawText(tr, l1, 6, 4, col, true);
            c.drawText(tr, l2, 6, 14, 0xFFB8C0D0, true);
            if (g) { // gráfica de frametime (alto máx = 33 ms)
                int gy = 28;
                int n = Math.min(FrameStats.count(), w - 8);
                for (int i = 0; i < n; i++) {
                    float ms = FrameStats.back(n - 1 - i);
                    int bh = Math.max(1, (int) (Math.min(ms / 33.3f, 1f) * gh));
                    int cc = ms <= 16.7f ? 0xFF4CFF7A : ms <= 33.4f ? 0xFFFFD84C : 0xFFFF5252;
                    c.fill(4 + i, gy + gh - bh, 5 + i, gy + gh, cc);
                }
            }
        }
    }

    // =====================================================================================
    public static final class Cps extends HudElement {
        public Cps() { super("cps", 0.01f, 0.14f, true); }

        @Override public void render(DrawContext c, MinecraftClient mc, boolean preview) {
            String s = "CPS " + CpsTracker.cps();
            w = mc.textRenderer.getWidth(s) + 12; h = 16;
            Ui.panel(c, w, h);
            c.drawText(mc.textRenderer, s, 6, 4, 0xFFFFFFFF, true);
        }
    }

    // =====================================================================================
    public static final class Coords extends HudElement {
        public Coords() { super("coords", 0.01f, 0.22f, true); }

        @Override public void render(DrawContext c, MinecraftClient mc, boolean preview) {
            if (mc.player == null || mc.world == null) return;
            TextRenderer tr = mc.textRenderer;
            var p = mc.player;
            String dir = Text.translatable("zalithboost.dir." + p.getHorizontalFacing().asString()).getString();
            Identifier id = mc.world.getBiome(p.getBlockPos()).getKey().map(RegistryKey::getValue).orElse(null);
            String biome = id == null ? "?" : Text.translatable("biome." + id.getNamespace() + "." + id.getPath()).getString();
            long t = mc.world.getTimeOfDay() % 24000L;
            String clock = String.format(Locale.ROOT, "%02d:%02d", (int) ((t / 1000 + 6) % 24), (int) ((t % 1000) * 60 / 1000));
            String[] ls = {
                    String.format(Locale.ROOT, "XYZ %.1f / %.1f / %.1f", p.getX(), p.getY(), p.getZ()),
                    dir + "  (" + Math.round(((p.getYaw() % 360) + 360) % 360) + "\u00b0)",
                    biome,
                    clock};
            int mw = 0;
            for (String s : ls) mw = Math.max(mw, tr.getWidth(s));
            w = mw + 12; h = 4 + ls.length * 10 + 2;
            Ui.panel(c, w, h);
            for (int i = 0; i < ls.length; i++) c.drawText(tr, ls[i], 6, 4 + i * 10, i == 0 ? 0xFFFFFFFF : 0xFFB8C0D0, true);
        }
    }

    // =====================================================================================
    /** Tarjeta del enemigo: nombre, vida (barra + número), armadura, equipo, distancia y efectos. */
    public static final class Enemy extends HudElement {
        public Enemy() { super("enemy", 0.36f, 0.04f, true); }

        @Override public void render(DrawContext c, MinecraftClient mc, boolean preview) {
            TextRenderer tr = mc.textRenderer;
            String name; float hp, max, smooth; int armor; double dist;
            ItemStack[] items; Collection<StatusEffectInstance> fx;
            LivingEntity e = Targeting.shown;
            if (preview) {
                name = "Zombie"; hp = 14f; max = 20f; smooth = hp; armor = 12; dist = 5.3;
                items = new ItemStack[]{new ItemStack(Items.DIAMOND_SWORD), new ItemStack(Items.IRON_HELMET),
                        new ItemStack(Items.IRON_CHESTPLATE), new ItemStack(Items.IRON_LEGGINGS), new ItemStack(Items.IRON_BOOTS)};
                fx = List.of(new StatusEffectInstance(StatusEffects.SPEED, 600));
            } else if (e != null) {
                name = e.getDisplayName().getString(); hp = e.getHealth(); max = e.getMaxHealth();
                smooth = Targeting.smoothHp; armor = e.getArmor(); dist = Targeting.dist;
                items = new ItemStack[]{e.getMainHandStack(), e.getEquippedStack(ARMOR[0]), e.getEquippedStack(ARMOR[1]),
                        e.getEquippedStack(ARMOR[2]), e.getEquippedStack(ARMOR[3])};
                fx = e.getStatusEffects();
            } else return;

            final int W = 150;
            ZbConfig z = ZbConfig.I;
            boolean showFx = z.enemyEffects && !fx.isEmpty();
            int y = 4;
            int h0 = 4 + 11 + 12 + 11 + (z.enemyEquip ? 20 : 0) + (showFx ? 2 + 10 * Math.min(fx.size(), 3) : 0) + 2;
            w = W; h = h0;
            Ui.panel(c, W, h0);

            c.drawText(tr, tr.trimToWidth(name, W - 12), 6, y, 0xFFFFFFFF, true); y += 11;

            // Barra de vida (animada)
            float ratio = Math.max(0f, Math.min(1f, smooth / Math.max(max, 1f)));
            Ui.rrect(c, 6, y, W - 12, 9, 3, 0xFF1A1A22, 0xFF1A1A22);
            int fw = ratio > 0 ? Math.max(4, (int) ((W - 12) * ratio)) : 0;
            if (fw > 0) {
                int col = Ui.lerp(0xFFE53935, 0xFF3DDC84, ratio);
                Ui.rrect(c, 6, y, fw, 9, 3, col, Ui.lerp(col, 0xFF000000, 0.3f));
            }
            String hpTxt = String.format(Locale.ROOT, "%.1f / %.0f", hp, max);
            c.drawText(tr, hpTxt, (W - tr.getWidth(hpTxt)) / 2, y + 1, 0xFFFFFFFF, true);
            y += 12;

            c.drawText(tr, Text.translatable("zalithboost.enemy.info", armor, String.format(Locale.ROOT, "%.1f", dist)).getString(),
                    6, y, 0xFFB8C0D0, true);
            y += 11;

            if (z.enemyEquip) { // arma en mano + 4 piezas de armadura
                int x = 6;
                for (ItemStack s : items) {
                    Ui.rrect(c, x, y, 17, 17, 3, 0x30FFFFFF, 0x30FFFFFF);
                    c.drawItem(s, x + 1, y + 1);
                    x += 19;
                }
                y += 20;
            }
            if (showFx) {
                y += 2;
                int n = 0;
                for (StatusEffectInstance fi : fx) {
                    if (n++ >= 3) break;
                    c.fill(6, y + 1, 10, y + 5, effectColor(fi));
                    c.drawText(tr, effectText(fi), 14, y, 0xFFDDE3EE, true);
                    y += 10;
                }
            }
        }
    }

    // =====================================================================================
    /** Armadura y mano principal con su durabilidad. */
    public static final class Armor extends HudElement {
        public Armor() { super("armor", 0.88f, 0.40f, true); }

        @Override public void render(DrawContext c, MinecraftClient mc, boolean preview) {
            TextRenderer tr = mc.textRenderer;
            List<ItemStack> list = new ArrayList<>();
            if (preview) {
                ItemStack a = new ItemStack(Items.DIAMOND_HELMET); a.setDamage(100); list.add(a);
                list.add(new ItemStack(Items.DIAMOND_CHESTPLATE));
                ItemStack b = new ItemStack(Items.DIAMOND_BOOTS); b.setDamage(300); list.add(b);
            } else if (mc.player != null) {
                for (EquipmentSlot s : ARMOR) { ItemStack st = mc.player.getEquippedStack(s); if (!st.isEmpty()) list.add(st); }
                ItemStack main = mc.player.getMainHandStack();
                if (!main.isEmpty()) list.add(main);
            }
            if (list.isEmpty()) { w = 60; h = 20; return; }
            String[] txt = new String[list.size()];
            int[] cols = new int[list.size()];
            int mw = 0;
            for (int i = 0; i < txt.length; i++) {
                ItemStack s = list.get(i);
                if (s.isDamageable()) {
                    int left = s.getMaxDamage() - s.getDamage();
                    txt[i] = left + "/" + s.getMaxDamage();
                    cols[i] = Ui.lerp(0xFFFF5252, 0xFF4CFF7A, (float) left / s.getMaxDamage());
                } else { txt[i] = s.getCount() > 1 ? "x" + s.getCount() : ""; cols[i] = 0xFFFFFFFF; }
                mw = Math.max(mw, tr.getWidth(txt[i]));
            }
            w = 4 + 18 + mw + 8; h = 2 + list.size() * 18 + 2;
            Ui.panel(c, w, h);
            for (int i = 0; i < txt.length; i++) {
                c.drawItem(list.get(i), 4, 2 + i * 18 + 1);
                c.drawText(tr, txt[i], 24, 2 + i * 18 + 5, cols[i], true);
            }
        }
    }

    // =====================================================================================
    public static final class Effects extends HudElement {
        public Effects() { super("effects", 0.80f, 0.04f, true); }

        @Override public void render(DrawContext c, MinecraftClient mc, boolean preview) {
            TextRenderer tr = mc.textRenderer;
            Collection<StatusEffectInstance> fx;
            if (preview) fx = List.of(new StatusEffectInstance(StatusEffects.SPEED, 1200), new StatusEffectInstance(StatusEffects.STRENGTH, 400));
            else if (mc.player != null) fx = mc.player.getStatusEffects();
            else return;
            if (fx.isEmpty()) { w = 60; h = 20; return; }
            int mw = 0;
            for (StatusEffectInstance e : fx) mw = Math.max(mw, tr.getWidth(effectText(e)));
            w = mw + 18; h = 4 + fx.size() * 10 + 2;
            Ui.panel(c, w, h);
            int y = 4;
            for (StatusEffectInstance e : fx) {
                c.fill(6, y + 1, 10, y + 5, effectColor(e));
                c.drawText(tr, effectText(e), 14, y, 0xFFFFFFFF, true);
                y += 10;
            }
        }
    }

    // =====================================================================================
    /** RAM de la JVM, batería/temperatura (si Android lo permite), ping y renderer. */
    public static final class Sys extends HudElement {
        public Sys() { super("sys", 0.01f, 0.40f, false); }

        @Override public void render(DrawContext c, MinecraftClient mc, boolean preview) {
            TextRenderer tr = mc.textRenderer;
            Runtime rt = Runtime.getRuntime();
            List<String> ls = new ArrayList<>();
            ls.add("RAM " + ((rt.totalMemory() - rt.freeMemory()) >> 20) + "/" + (rt.maxMemory() >> 20) + " MB");
            if (SystemMonitor.battery >= 0) {
                ls.add("BAT " + SystemMonitor.battery + "%" + (Float.isNaN(SystemMonitor.tempC) ? "" : String.format(Locale.ROOT, "  %.1f\u00b0C", SystemMonitor.tempC)));
            }
            if (mc.getNetworkHandler() != null && mc.player != null) {
                PlayerListEntry pe = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
                if (pe != null) ls.add("PING " + pe.getLatency() + " ms");
            }
            ls.add(RendererInfo.shortName());
            int mw = 0;
            for (String s : ls) mw = Math.max(mw, tr.getWidth(s));
            w = mw + 12; h = 4 + ls.size() * 10 + 2;
            Ui.panel(c, w, h);
            for (int i = 0; i < ls.size(); i++) c.drawText(tr, ls.get(i), 6, 4 + i * 10, i == 0 ? 0xFFFFFFFF : 0xFFB8C0D0, true);
        }
    }
}

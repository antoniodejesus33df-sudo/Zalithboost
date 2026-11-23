package com.zalithboost.hud;

import com.zalithboost.config.ZbConfig;
import com.zalithboost.ui.Ui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;

/** Alerta de vida baja (bordes rojos pulsando) y aviso de herramienta/armadura a punto de romperse. */
public final class AlertsHud {
    private static String banner = "";
    private static long bannerUntil;
    private static final Map<Integer, Long> LAST = new HashMap<>();

    private AlertsHud() {}

    public static void tick(MinecraftClient mc) {
        if (mc.player == null || !ZbConfig.I.durabilityAlert || mc.player.age % 20 != 0) return;
        check(mc.player.getMainHandStack(), 0);
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (int i = 0; i < slots.length; i++) check(mc.player.getEquippedStack(slots[i]), i + 1);
    }

    private static void check(ItemStack s, int key) {
        if (s.isEmpty() || !s.isDamageable()) return;
        int max = s.getMaxDamage(), left = max - s.getDamage();
        if (left * 10 > max) return; // solo cuando queda 10% o menos
        long now = System.currentTimeMillis();
        Long last = LAST.get(key);
        if (last != null && now - last < 20000) return; // no spamear
        LAST.put(key, now);
        banner = Text.translatable("zalithboost.alert.durability", s.getName()).getString();
        bannerUntil = now + 2500;
    }

    public static void render(DrawContext c, MinecraftClient mc) {
        ZbConfig z = ZbConfig.I;
        int w = c.getScaledWindowWidth(), h = c.getScaledWindowHeight();

        if (z.lowHealthAlert && mc.player.isAlive() && !mc.player.isCreative() && mc.player.getHealth() <= z.lowHealthHearts * 2f) {
            boolean critical = mc.player.getHealth() <= 4f;
            double pulse = 0.5 + 0.5 * Math.sin(System.currentTimeMillis() / (critical ? 160.0 : 280.0));
            int a = (int) (35 + 60 * pulse);
            int hh = h / 6, hw = w / 10;
            c.fillGradient(0, 0, w, hh, (a << 24) | 0xFF0000, 0x00FF0000);
            c.fillGradient(0, h - hh, w, h, 0x00FF0000, (a << 24) | 0xFF0000);
            for (int i = 0; i < hw; i++) {
                int al = (int) (a * (1 - (float) i / hw));
                c.fill(i, 0, i + 1, h, (al << 24) | 0xFF0000);
                c.fill(w - 1 - i, 0, w - i, h, (al << 24) | 0xFF0000);
            }
        }

        if (System.currentTimeMillis() < bannerUntil) {
            int tw = mc.textRenderer.getWidth(banner);
            int bx = (w - tw) / 2 - 8, by = 18;
            Ui.rrect(c, bx, by, tw + 16, 16, 6, 0xDD3A1010, 0xDD1A0505);
            c.drawText(mc.textRenderer, banner, bx + 8, by + 4, 0xFFFFC857, true);
        }
    }
}

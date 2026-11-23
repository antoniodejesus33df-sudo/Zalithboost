package com.zalithboost.util;

import net.minecraft.client.MinecraftClient;

import java.util.ArrayDeque;

/** Cuenta clics de ataque por segundo (en Zalith los toques se convierten en clics). */
public final class CpsTracker {
    private static final ArrayDeque<Long> CLICKS = new ArrayDeque<>();
    private static boolean prev;

    private CpsTracker() {}

    public static void tick(MinecraftClient mc) {
        boolean now = mc.options.attackKey.isPressed();
        if (now && !prev) CLICKS.addLast(System.currentTimeMillis());
        prev = now;
        trim();
    }

    private static void trim() {
        long limit = System.currentTimeMillis() - 1000;
        while (!CLICKS.isEmpty() && CLICKS.peekFirst() < limit) CLICKS.pollFirst();
    }

    public static int cps() { trim(); return CLICKS.size(); }
}

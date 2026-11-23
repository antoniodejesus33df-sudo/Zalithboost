package com.zalithboost.util;

/** Registro de frametimes (ms) de los últimos 120 frames: promedio, mínimo, máximo y gráfica. */
public final class FrameStats {
    private static final float[] R = new float[120];
    private static int idx, cnt;
    private static long last;

    private FrameStats() {}

    /** Llamar una vez por frame dibujado. */
    public static void frame() {
        long n = System.nanoTime();
        if (last != 0) {
            float ms = (n - last) / 1_000_000f;
            if (ms < 1000f) { R[idx] = ms; idx = (idx + 1) % R.length; if (cnt < R.length) cnt++; }
        }
        last = n;
    }

    public static int count() { return cnt; }

    /** Frametime de hace k frames (0 = el más reciente). */
    public static float back(int k) { return R[Math.floorMod(idx - 1 - k, R.length)]; }

    public static int avg() {
        if (cnt == 0) return 0;
        double s = 0;
        for (int i = 0; i < cnt; i++) s += back(i);
        return (int) Math.round(1000.0 / (s / cnt));
    }

    public static int min() { // FPS mínimo = frame más lento
        float m = 0;
        for (int i = 0; i < cnt; i++) m = Math.max(m, back(i));
        return m <= 0 ? 0 : Math.round(1000f / m);
    }

    public static int max() { // FPS máximo = frame más rápido
        float m = Float.MAX_VALUE;
        for (int i = 0; i < cnt; i++) m = Math.min(m, Math.max(back(i), 0.05f));
        return cnt == 0 ? 0 : Math.round(1000f / m);
    }
}

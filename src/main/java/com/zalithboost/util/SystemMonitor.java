package com.zalithboost.util;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Lee batería/temperatura desde /sys (si Android lo permite a la app) y la RAM del dispositivo.
 * Si el sistema bloquea la lectura, los valores quedan como "no disponible".
 */
public final class SystemMonitor {
    public static int battery = -1;                 // % o -1
    public static float tempC = Float.NaN;          // °C o NaN
    private static int t;

    private SystemMonitor() {}

    public static void tick() {
        if (t++ % 100 != 0) return; // cada ~5 s
        battery = readInt("/sys/class/power_supply/battery/capacity", -1);
        int raw = readInt("/sys/class/power_supply/battery/temp", Integer.MIN_VALUE);
        tempC = raw == Integer.MIN_VALUE ? Float.NaN : raw / 10f;
    }

    private static int readInt(String p, int def) {
        try { return Integer.parseInt(Files.readString(Path.of(p)).trim()); }
        catch (Exception e) { return def; }
    }

    /** RAM total del dispositivo en MB, o -1 si no se puede leer. */
    public static long deviceRamMb() {
        try {
            for (String line : Files.readAllLines(Path.of("/proc/meminfo"))) {
                if (line.startsWith("MemTotal:")) {
                    String n = line.replaceAll("[^0-9]", "");
                    return Long.parseLong(n) / 1024;
                }
            }
        } catch (Exception ignored) {}
        return -1;
    }
}

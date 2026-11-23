package com.zalithboost.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zalithboost.hud.HudManager;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** Configuración en JSON (config/zalithboost.json). Todo es público para que Gson lo (de)serialice. */
public class ZbConfig {

    /** Posición (fracción 0..1 de la pantalla), escala y estado de un elemento del HUD. */
    public static class Elem {
        public boolean enabled;
        public float x, y, scale = 1f;
        public Elem() {}
        public Elem(boolean enabled, float x, float y) { this.enabled = enabled; this.x = x; this.y = y; }
    }

    public static ZbConfig I = new ZbConfig();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // ---- Apariencia ----
    public int theme = 0;                       // 0 neón, 1 océano, 2 atardecer, 3 oscuro, 4 personalizado
    public int customR = 0, customG = 200, customB = 255;
    public float hudOpacity = 0.7f;
    public boolean floatButton = true;
    public float floatX = 0.93f, floatY = 0.40f;
    public int floatSize = 24;
    public int slot = 1;                        // ranura de perfil seleccionada (1..3)

    // ---- HUD ----
    public Map<String, Elem> elems = new LinkedHashMap<>();
    public boolean enemyEquip = true, enemyEffects = true, fpsGraph = true;
    public int enemyRange = 8;

    // ---- Combate / mira ----
    public boolean xhEnabled = true, xhDynamic = true;
    public int xhShape = 0, xhSize = 6, xhThick = 1, xhColor = 0, xhInColor = 2, xhOutColor = 1, xhOffX = 0, xhOffY = 0;
    public boolean lowHealthAlert = true, durabilityAlert = true;
    public int lowHealthHearts = 6;
    public boolean reduceMotion = false;

    // ---- Rendimiento ----
    public boolean autoAdjust = false;
    public int targetFps = 45, minRender = 4;
    public boolean thermalMode = false;
    public int thermalLimit = 43;
    public int gcMinutes = 0;

    public Elem elem(String id) {
        if (elems == null) elems = new LinkedHashMap<>();
        return elems.computeIfAbsent(id, HudManager::defaultFor);
    }

    // ---------- Archivo ----------
    private static Path path() { return FabricLoader.getInstance().getConfigDir().resolve("zalithboost.json"); }

    public static void load() {
        try {
            Path p = path();
            if (Files.exists(p)) {
                ZbConfig c = GSON.fromJson(Files.readString(p), ZbConfig.class);
                if (c != null) { if (c.elems == null) c.elems = new LinkedHashMap<>(); I = c; }
            }
        } catch (Exception e) {
            System.err.println("[ZalithBoost] No se pudo leer la config, uso valores por defecto: " + e);
        }
        save();
    }

    public static void save() {
        try { Files.writeString(path(), GSON.toJson(I)); }
        catch (Exception e) { System.err.println("[ZalithBoost] No se pudo guardar la config: " + e); }
    }

    // ---------- Perfiles ----------
    public static String toJson() { return GSON.toJson(I); }

    /** Reemplaza la config actual con un JSON (portapapeles/perfil). Devuelve false si es inválido. */
    public static boolean replace(String json) {
        try {
            ZbConfig c = GSON.fromJson(json, ZbConfig.class);
            if (c == null) return false;
            if (c.elems == null) c.elems = new LinkedHashMap<>();
            I = c;
            save();
            return true;
        } catch (Exception e) { return false; }
    }

    private static Path slotPath(int n) {
        return FabricLoader.getInstance().getConfigDir().resolve("zalithboost_profiles").resolve("slot" + n + ".json");
    }

    public static boolean saveSlot(int n) {
        try {
            Path p = slotPath(n);
            Files.createDirectories(p.getParent());
            Files.writeString(p, toJson());
            return true;
        } catch (Exception e) { return false; }
    }

    public static boolean loadSlot(int n) {
        try {
            Path p = slotPath(n);
            return Files.exists(p) && replace(Files.readString(p));
        } catch (Exception e) { return false; }
    }
}

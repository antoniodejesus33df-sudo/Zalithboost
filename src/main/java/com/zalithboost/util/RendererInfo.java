package com.zalithboost.util;

import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.opengl.GL11;

import java.util.Locale;

/**
 * Detecta el renderer en uso. OJO: un mod NO puede cambiar el renderer;
 * eso se elige en Zalith Launcher (ajustes del perfil). Aquí solo detectamos y avisamos.
 */
public final class RendererInfo {
    private static String full, shortName;

    private RendererInfo() {}

    private static void init() {
        if (full != null) return;
        String env = System.getenv("POJAV_RENDERER"); // Zalith/Pojav lo exporta
        String envL = env == null ? "" : env.toLowerCase(Locale.ROOT);
        boolean vulkanMod = FabricLoader.getInstance().isModLoaded("vulkanmod");
        String gl = null;
        if (!vulkanMod) { // VulkanMod no usa contexto OpenGL
            try { gl = GL11.glGetString(GL11.GL_RENDERER); } catch (Throwable ignored) {}
        }
        String glL = gl == null ? "" : gl.toLowerCase(Locale.ROOT);
        String kind;
        if (vulkanMod) kind = "VulkanMod (Vulkan)";
        else if (envL.contains("zink") || envL.contains("vulkan") || glL.contains("zink")) kind = "OpenGL sobre Vulkan (Zink)";
        else if (envL.contains("angle") || glL.contains("angle")) kind = "OpenGL ES via ANGLE";
        else if (envL.contains("ltw")) kind = "LTW (GL sobre GLES)";
        else if (envL.contains("gl4es") || envL.contains("opengles2")) kind = "GL4ES (OpenGL ES 2)";
        else kind = "OpenGL";
        shortName = kind;
        full = kind + (env != null ? " | POJAV_RENDERER=" + env : "") + (gl != null ? " | GL: " + gl : "");
    }

    public static String full() { init(); return full; }
    public static String shortName() { init(); return shortName; }
}

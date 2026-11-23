package com.zalithboost.util;

import com.zalithboost.config.ZbConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * Busca la entidad a la que apuntas. Seguro para servidores: respeta los bloques
 * (no ve a través de paredes) y no muestra entidades invisibles.
 */
public final class Targeting {
    /** Entidad que se muestra en la tarjeta (se mantiene 0.8 s tras dejar de apuntar). */
    public static LivingEntity shown;
    public static boolean aimed;      // ¿estás apuntando a algo ahora mismo?
    public static boolean inReach;    // ¿está dentro del alcance normal de golpe?
    public static double dist;
    public static float smoothHp;     // vida suavizada para animar la barra
    private static long lastSeen;

    private Targeting() {}

    public static void tick(MinecraftClient mc) {
        Entity cam = mc.getCameraEntity();
        if (cam == null || mc.world == null || mc.player == null) { shown = null; aimed = false; return; }

        double range = Math.max(4, ZbConfig.I.enemyRange);
        Vec3d start = cam.getCameraPosVec(1f);
        Vec3d look = cam.getRotationVec(1f);

        // Distancia al primer bloque: nada que esté detrás cuenta
        HitResult blockHit = cam.raycast(range, 1f, false);
        double blockDist = blockHit.getType() == HitResult.Type.MISS ? range : blockHit.getPos().distanceTo(start);
        Vec3d end = start.add(look.multiply(Math.min(range, blockDist)));
        Box box = cam.getBoundingBox().stretch(look.multiply(range)).expand(1.0);

        EntityHitResult r = ProjectileUtil.raycast(cam, start, end, box,
                e -> !e.isSpectator() && !e.isInvisible() && e.canHit() && e instanceof LivingEntity, range * range);

        long now = System.currentTimeMillis();
        aimed = false;
        inReach = false;
        if (r != null && r.getEntity() instanceof LivingEntity le) {
            if (le != shown) smoothHp = le.getHealth();
            shown = le;
            lastSeen = now;
            aimed = true;
            inReach = r.getPos().distanceTo(start) <= mc.player.getEntityInteractionRange();
        } else if (shown != null && (now - lastSeen > 800 || !shown.isAlive())) {
            shown = null;
        }
        if (shown != null) {
            dist = mc.player.distanceTo(shown);
            smoothHp += (shown.getHealth() - smoothHp) * 0.25f;
        }
    }
}

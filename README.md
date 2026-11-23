# ZalithBoost

Mod **100% cliente** (Fabric, Minecraft 1.21.4, Java 21) hecho para jugar en **celulares Android con Zalith Launcher**.
HUD de combate, tarjeta del enemigo con vida, menú táctil y optimización de FPS. Sin ventajas injustas
(sin X-ray, kill aura ni reach): la tarjeta y la mira respetan paredes y el alcance normal del juego.

## Qué incluye
- **Menú** (tecla `Z` configurable, botón flotante movible en pausa/inventario, y Mod Menu): temas, pestañas, buscador, perfiles.
- **Editar HUD**: arrastra y escala cada elemento con el dedo.
- **HUD**: FPS (prom/mín/máx + gráfica), CPS, coordenadas/bioma/hora, tarjeta del enemigo, armadura/durabilidad, efectos, monitor (RAM/batería/ping).
- **Combate**: mira personalizable que cambia de color según alcance, alerta de vida baja, aviso de durabilidad, sprint/sneak toggle, menos movimiento.
- **Rendimiento**: 4 perfiles de 1 toque, auto-ajuste dinámico, ajustes manuales, modo anti-calor, limpieza de memoria.
- **Diagnóstico**: renderer detectado, RAM, qué opción cuesta más FPS, argumentos JVM sugeridos, compatibilidad.

## Compilar
```bash
gradle build            # o ./gradlew build si generas el wrapper con: gradle wrapper --gradle-version 8.12
```
El `.jar` queda en `build/libs/zalithboost-1.0.0.jar` (no uses el `-sources`).
En GitHub: sube el proyecto, y el workflow `.github/workflows/build.yml` compila solo en cada push.
Para publicar un release crea un tag: `git tag v1.0.0 && git push --tags`.

## Instalar en Zalith Launcher
1. Instala una versión **Fabric 1.21.4** desde Zalith.
2. Copia a la carpeta `mods` de esa versión: `zalithboost-1.0.0.jar`, **Fabric API** y (opcional) **Mod Menu**.
3. Abre el juego. En los controles de Zalith asigna un botón a la tecla del mod para abrir el menú en pleno juego.

## Compatibilidad
Solo modifica opciones vanilla, por eso convive con Sodium, Lithium, VulkanMod, Entity Culling y Mod Menu.

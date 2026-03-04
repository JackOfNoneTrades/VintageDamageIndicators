package org.fentanylsolutions.vintagedamageindicators;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    private static Configuration config;

    // damage-particles
    public static boolean damageParticlesEnabled = true;
    public static float damageParticleGravity = 0.8f;
    public static float damageParticleScale = 3.0f;
    public static boolean damageParticleOutline = true;
    public static int damageParticleLifespan = 12;
    public static float damageParticleTransparency = 0.9947f;

    public static int hurtColor = 16755200;
    public static int healColor = 65280;

    public static boolean criticalParticlesEnabled = true;
    public static float criticalParticleGravity = -0.05f;
    public static float criticalParticleScale = 3.0f;
    public static int criticalParticleLifespan = 12;
    public static float criticalParticleTransparency = 0.9947f;
    public static int criticalParticleColor = 0x8C1E02;

    public static boolean dropShadow = true;
    public static float bounceStrength = 1.5f;
    public static boolean alwaysRender = false;
    public static boolean disableVanillaCriticalParticles = false;

    // hud-indicator
    public static boolean hudIndicatorEnabled = true;
    public static float maxDistance = 100;
    public static boolean colorblindHealthBar = false;
    public static boolean healthDecimals = true;
    public static boolean healthSeparator = true;
    public static int hudLingerTime = 30;
    public static float hudIndicatorSize = 0.75f;
    public static float hudIndicatorBackgroundOpacity = 0.75f;
    public static float hudEntitySize = 38.0f;
    public static float hudEntityYaw = 35.0f;
    public static float hudEntityPitch = -12.0f;
    public static boolean hudPreviewParticlesEnabled = true;
    public static boolean hudPotionEffectsEnabled = true;
    public static boolean hudPotionEffectTime = true;
    public static boolean hudIndicatorAlignLeft = true;
    public static boolean hudIndicatorAlignTop = true;
    public static int hudIndicatorPositionX = 10;
    public static int hudIndicatorPositionY = 10;
    public static boolean hudNameTextOutline = false;
    public static boolean hudHealthTextOutline = false;
    public static boolean disableBossBar = false;
    public static boolean hideOnDebug = false;
    public static String[] oldRenderEntities = {};

    // potion-sync
    public static boolean serverSendPotionTypesToOps = true;
    public static boolean serverSendPotionDurationsToOps = true;
    public static boolean serverSendPotionTypesToNonOps = true;
    public static boolean serverSendPotionDurationsToNonOps = true;

    // entity overrides
    public static String[] entityOverrides = {};

    // debug
    public static boolean debugMode = false;
    public static boolean printMobs = false;

    public static class Categories {

        public static final String damageParticles = "damage-particles";
        public static final String hudIndicator = "hud-indicator";
        public static final String potionSync = "potion-sync";
        public static final String entityOverrides = "type-overrides";
        public static final String debug = "debug";
    }

    public static void loadConfig(File configFile) {
        config = new Configuration(configFile);

        try {
            config.load();

            // damage-particles
            damageParticlesEnabled = config.getBoolean(
                "damageParticlesEnabled",
                Categories.damageParticles,
                damageParticlesEnabled,
                "Whether the pop-up particles when a mob is injured or healed are enabled.");
            damageParticleGravity = config.getFloat(
                "damageParticleGravity",
                Categories.damageParticles,
                damageParticleGravity,
                Float.MIN_VALUE,
                Float.MAX_VALUE,
                "Speed at which damage particles fall.");
            damageParticleScale = config.getFloat(
                "damageParticleScale",
                Categories.damageParticles,
                damageParticleScale,
                0.01F,
                10,
                "The scale of damage particles.");
            damageParticleOutline = config.getBoolean(
                "damageParticleOutline",
                Categories.damageParticles,
                damageParticleOutline,
                "Whether the numbers that appear as pop-up particles are outlined in a darker color.");
            damageParticleLifespan = config.getInt(
                "damageParticleLifespan",
                Categories.damageParticles,
                damageParticleLifespan,
                0,
                Integer.MAX_VALUE,
                "Damage particle lifespan.");
            damageParticleTransparency = config.getFloat(
                "damageParticleTransparency",
                Categories.damageParticles,
                damageParticleTransparency,
                0,
                1,
                "Damage particle transparency.");
            hurtColor = Integer.parseInt(
                config.getString(
                    "hurtColor",
                    Categories.damageParticles,
                    Integer.toHexString(hurtColor),
                    "Color of hurt particles, in hex format. (e.g. FFAA00)"),
                16);
            healColor = Integer.parseInt(
                config.getString(
                    "healColor",
                    Categories.damageParticles,
                    Integer.toHexString(healColor),
                    "Color of heal particles, in hex format. (e.g. 00FF00)"),
                16);

            criticalParticlesEnabled = config.getBoolean(
                "criticalParticlesEnabled",
                Categories.damageParticles,
                criticalParticlesEnabled,
                "Whether critical particles are enabled.");
            criticalParticleGravity = config.getFloat(
                "criticalParticleGravity",
                Categories.damageParticles,
                criticalParticleGravity,
                Float.MIN_VALUE,
                Float.MAX_VALUE,
                "Speed at which critical particles fall.");
            criticalParticleScale = config.getFloat(
                "criticalParticleScale",
                Categories.damageParticles,
                criticalParticleScale,
                0.01F,
                10,
                "The scale of critical particles.");
            criticalParticleLifespan = config.getInt(
                "criticalParticleLifespan",
                Categories.damageParticles,
                criticalParticleLifespan,
                0,
                Integer.MAX_VALUE,
                "Critical particle lifespan.");
            criticalParticleTransparency = config.getFloat(
                "criticalParticleTransparency",
                Categories.damageParticles,
                criticalParticleTransparency,
                0,
                1,
                "Critical particle transparency.");
            criticalParticleColor = Integer.parseInt(
                config.getString(
                    "criticalParticleColor",
                    Categories.damageParticles,
                    Integer.toHexString(criticalParticleColor),
                    "Color of critical particles, in hex format. (e.g. 8C1E02)"),
                16);

            dropShadow = config
                .getBoolean("dropShadow", Categories.damageParticles, dropShadow, "Whether particles drop shadows.");
            bounceStrength = config.getFloat(
                "bounceStrength",
                Categories.damageParticles,
                bounceStrength,
                0,
                Float.MAX_VALUE,
                "Strength at which particles bounce at spawn.");
            alwaysRender = config.getBoolean(
                "alwaysRender",
                Categories.damageParticles,
                alwaysRender,
                "Whether particles should always be visible, ignoring obstructing blocks.");
            disableVanillaCriticalParticles = config.getBoolean(
                "disableVanillaCriticalParticles",
                Categories.damageParticles,
                disableVanillaCriticalParticles,
                "Whether particles vanilla critical particles are disabled.");

            // hud-indicator
            hudIndicatorEnabled = config.getBoolean(
                "hudIndicatorEnabled",
                Categories.hudIndicator,
                hudIndicatorEnabled,
                "Whether the hud damage indicator is enabled.");
            maxDistance = config.getFloat(
                "maxDistance",
                Categories.hudIndicator,
                maxDistance,
                3,
                10000,
                "How far away (in blocks) entities can be to appear in the hud health indicator");
            colorblindHealthBar = config.getBoolean(
                "colorblindHealthBar",
                Categories.hudIndicator,
                colorblindHealthBar,
                "Whether health appears with a more visible yellow/black scheme.");
            healthDecimals = config.getBoolean(
                "healthDecimals",
                Categories.hudIndicator,
                healthDecimals,
                "Whether health appears with a decimal point.");
            healthSeparator = config.getBoolean(
                "healthSeparator",
                Categories.hudIndicator,
                healthSeparator,
                "Whether health appears appears as a | (true) or / (false).");
            hudLingerTime = config.getInt(
                "hudLingerTime",
                Categories.hudIndicator,
                hudLingerTime,
                0,
                1200,
                "How long after mousing over an entity the hud damage indicator remains on screen, in game ticks.");
            hudIndicatorSize = config.getFloat(
                "hudIndicatorSize",
                Categories.hudIndicator,
                hudIndicatorSize,
                0,
                10,
                "The relative size of hud indicator.");
            hudIndicatorBackgroundOpacity = config.getFloat(
                "hudIndicatorBackgroundOpacity",
                Categories.hudIndicator,
                hudIndicatorBackgroundOpacity,
                0,
                10,
                "How opaque the background of the hud indicator is.");
            hudEntitySize = config.getFloat(
                "hudEntitySize",
                Categories.hudIndicator,
                hudEntitySize,
                0,
                2000,
                "The size in pixels a usual entity should render as in the HUD indicator.");
            hudEntityYaw = config.getFloat(
                "hudEntityYaw",
                Categories.hudIndicator,
                hudEntityYaw,
                -180,
                180,
                "The global yaw angle used for entity previews.");
            hudEntityPitch = config.getFloat(
                "hudEntityPitch",
                Categories.hudIndicator,
                hudEntityPitch,
                -90,
                90,
                "The global pitch angle used for entity previews.");
            hudPreviewParticlesEnabled = config.getBoolean(
                "hudPreviewParticlesEnabled",
                Categories.hudIndicator,
                hudPreviewParticlesEnabled,
                "Whether damage popoffs are shown inside the HUD entity preview.");
            hudPotionEffectsEnabled = config.getBoolean(
                "hudPotionEffectsEnabled",
                Categories.hudIndicator,
                hudPotionEffectsEnabled,
                "Whether active potion effects are shown below the health bar.");
            hudPotionEffectTime = config.getBoolean(
                "hudPotionEffectTime",
                Categories.hudIndicator,
                hudPotionEffectTime,
                "Whether the remaining potion duration is shown next to each potion icon.");
            hudIndicatorAlignLeft = config.getBoolean(
                "hudIndicatorAlignLeft",
                Categories.hudIndicator,
                hudIndicatorAlignLeft,
                "True if the hud indicator appears on the left side of the screen, false for right.");
            hudIndicatorAlignTop = config.getBoolean(
                "hudIndicatorAlignTop",
                Categories.hudIndicator,
                hudIndicatorAlignTop,
                "True if the hud indicator appears on the top of the screen, false for bottom.");
            hudIndicatorPositionX = config.getInt(
                "hudIndicatorPositionX",
                Categories.hudIndicator,
                hudIndicatorPositionX,
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                "How many pixels from the left side of the screen the hud indicator is.");
            hudIndicatorPositionY = config.getInt(
                "hudIndicatorPositionY",
                Categories.hudIndicator,
                hudIndicatorPositionY,
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                "How many pixels from the top of the screen the hud indicator is.");
            hudNameTextOutline = config.getBoolean(
                "hudNameTextOutline",
                Categories.hudIndicator,
                hudNameTextOutline,
                "Whether the name of the entity in the hud indicator should be outlined.");
            hudHealthTextOutline = config.getBoolean(
                "hudHealthTextOutline",
                Categories.hudIndicator,
                hudHealthTextOutline,
                "Whether the health of the entity in the hud indicator should be outlined.");
            disableBossBar = config.getBoolean(
                "disableBossBar",
                Categories.hudIndicator,
                disableBossBar,
                "Whether to disable the boss bar.");
            hideOnDebug = config.getBoolean(
                "hideOnDebug",
                Categories.hudIndicator,
                hideOnDebug,
                "Whether to hide the HUD when the debug screen is enabled.");
            oldRenderEntities = config.getStringList(
                "oldRenderEntities",
                Categories.hudIndicator,
                oldRenderEntities,
                "List of entity class names to render with the model-only HUD fallback if the normal entity render behaves badly.");

            // potion-sync
            serverSendPotionTypesToOps = config.getBoolean(
                "serverSendPotionTypesToOps",
                Categories.potionSync,
                serverSendPotionTypesToOps,
                "Whether server-side VDI sends potion icons to op players.");
            serverSendPotionDurationsToOps = config.getBoolean(
                "serverSendPotionDurationsToOps",
                Categories.potionSync,
                serverSendPotionDurationsToOps,
                "Whether server-side VDI sends potion durations to op players.");
            serverSendPotionTypesToNonOps = config.getBoolean(
                "serverSendPotionTypesToNonOps",
                Categories.potionSync,
                serverSendPotionTypesToNonOps,
                "Whether server-side VDI sends potion icons to non-op players.");
            serverSendPotionDurationsToNonOps = config.getBoolean(
                "serverSendPotionDurationsToNonOps",
                Categories.potionSync,
                serverSendPotionDurationsToNonOps,
                "Whether server-side VDI sends potion durations to non-op players.");

            // entity overrides
            entityOverrides = config.getStringList(
                "entityOverrides",
                Categories.entityOverrides,
                entityOverrides,
                "Entity-specific render overrides.");

            // Debug
            debugMode = config.getBoolean("debugMode", Categories.debug, debugMode, "Enable debug logging");
            printMobs = config.getBoolean(
                "printMobs",
                Categories.debug,
                printMobs,
                "If set to true, print a list of mob names on game post init.");

        } catch (Exception e) {
            System.err.println("Error loading config: " + e.getMessage());
        } finally {
            config.save();
        }
    }

    public static Configuration getRawConfig() {
        return config;
    }

    public static void setEntityOverrides(String[] overrides) {
        entityOverrides = overrides == null ? new String[0] : overrides;
        if (config != null) {
            config.get(Categories.entityOverrides, "entityOverrides", new String[0])
                .set(entityOverrides);
        }
    }

    public static void setHudPreviewAngles(float yaw, float pitch) {
        hudEntityYaw = yaw;
        hudEntityPitch = pitch;
        if (config != null) {
            config.get(Categories.hudIndicator, "hudEntityYaw", hudEntityYaw)
                .set((double) hudEntityYaw);
            config.get(Categories.hudIndicator, "hudEntityPitch", hudEntityPitch)
                .set((double) hudEntityPitch);
        }
    }

    public static void save() {
        if (config != null) {
            config.save();
        }
    }

    public static boolean shouldSendPotionTypes(boolean operator) {
        return operator ? serverSendPotionTypesToOps : serverSendPotionTypesToNonOps;
    }

    public static boolean shouldSendPotionDurations(boolean operator) {
        return operator ? serverSendPotionDurationsToOps : serverSendPotionDurationsToNonOps;
    }
}

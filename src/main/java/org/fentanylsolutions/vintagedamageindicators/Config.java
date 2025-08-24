package org.fentanylsolutions.vintagedamageindicators;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    private static Configuration config;

    // damage-particles
    public static boolean damageParticlesEnabled = true;
    public static float damageParticleSize = 1;
    public static boolean damageParticleOutline = true;

    // hud-indicator
    public static boolean hudIndicatorEnabled = true;
    public static float maxDistance = 100;
    public static boolean colorblindHealthBar = false;
    public static boolean healthDecimals = true;
    public static boolean healthSeperator = true;
    public static int hudLingerTime = 30;
    public static float hudIndicatorSize = 0.75f;
    public static float hudIndicatorBackgroundOpacity = 0.75f;
    public static boolean hudIndicatorAlignLeft = true;
    public static boolean hudIndicatorAlignTop = true;
    public static int hudIndicatorPositionX = 10;
    public static int  hudIndicatorPositionY = 10;
    public static float hudEntitySize = 38;
    public static boolean hudNameTextOutline = false;
    public static boolean hudHealthTextOutline = false;
    public static String[] oldRenderEntities = {};

    // debug
    public static boolean debugMode = false;

    public static class Categories {

        public static final String damageParticles = "damage-particles";
        public static final String hudIndicator = "hud-indicator";
        public static final String debug = "debug";
    }

    public static void loadConfig(File configFile) {
        config = new Configuration(configFile);

        try {
            config.load();

            // damage-particles
            damageParticlesEnabled = config.getBoolean("damageParticlesEnabled", Categories.damageParticles, damageParticlesEnabled, "Whether the pop-up particles when a mob is injured or healed are enabled.");
            damageParticleSize = config.getFloat("damageParticleSize", Categories.damageParticles, damageParticleSize, 0.1F, 10, "The relative size of damage particles.");
            damageParticleOutline = config.getBoolean("damageParticleOutline", Categories.damageParticles, damageParticleOutline, "Whether the numbers that appear as pop-up particles are outlined in a darker color.");

            // hud-indicator
             hudIndicatorEnabled = config.getBoolean("hudIndicatorEnabled", Categories.hudIndicator, hudIndicatorEnabled, "Whether the hud damage indicator is enabled.");
             maxDistance = config.getFloat("maxDistance", Categories.hudIndicator, maxDistance, 3, 10000, "How far away (in blocks) entities can be to appear in the hud health indicator");
             colorblindHealthBar = config.getBoolean("colorblindHealthBar", Categories.hudIndicator, colorblindHealthBar, "Whether health appears with a more visible yellow/black scheme.");
             healthDecimals = config.getBoolean("healthDecimals", Categories.hudIndicator, healthDecimals, "Whether health appears with a decimal point.");
             healthSeperator = config.getBoolean("healthSeperator", Categories.hudIndicator, healthSeperator, "Whether health appears appears as a | (true) or / (false).");
             hudLingerTime = config.getInt("hudLingerTime", Categories.hudIndicator, hudLingerTime, 0, 1200, "How long after mousing over an entity the hud damage indicator remains on screen, in game ticks.");
             hudIndicatorSize = maxDistance = config.getFloat("hudIndicatorSize", Categories.hudIndicator, hudIndicatorSize, 0, 10, "The relative size of hud indicator.");
             hudIndicatorBackgroundOpacity = maxDistance = config.getFloat("hudIndicatorBackgroundOpacity", Categories.hudIndicator, hudIndicatorBackgroundOpacity, 0, 10, "How opaque the background of the hud indicator is.");
             hudIndicatorAlignLeft = config.getBoolean("hudIndicatorAlignLeft", Categories.hudIndicator, hudIndicatorAlignLeft, "True if the hud indicator appears on the left side of the screen, false for right.");
             hudIndicatorAlignTop = config.getBoolean("hudIndicatorAlignTop", Categories.hudIndicator, hudIndicatorAlignTop, "True if the hud indicator appears on the top of the screen, false for bottom.");
             hudIndicatorPositionX = config.getInt("hudIndicatorPositionX", Categories.hudIndicator, hudIndicatorPositionX, Integer.MIN_VALUE, Integer.MAX_VALUE, "How many pixels from the left side of the screen the hud indicator is.");
             hudIndicatorPositionY = config.getInt("hudIndicatorPositionY", Categories.hudIndicator, hudIndicatorPositionY, Integer.MIN_VALUE, Integer.MAX_VALUE, "How many pixels from the top of the screen the hud indicator is.");
             hudEntitySize = config.getFloat("hudEntitySize", Categories.hudIndicator, hudEntitySize, 0, 2000, "The size in pixels a usual entiud-ity should render as in the hud indicator.");
             hudNameTextOutline = config.getBoolean("hudNameTextOutline", Categories.hudIndicator, hudNameTextOutline, "Whether the name of the entity in the hud indicator should be outlined.");
             hudHealthTextOutline = config.getBoolean("hudHealthTextOutline", Categories.hudIndicator, hudHealthTextOutline, "Whether the health of the entity in the hud indicator should be outlined.");
             oldRenderEntities = config.getStringList("oldRenderEntities", Categories.hudIndicator, oldRenderEntities, "List of all entity_types to just render as a model instead of with entity context. add to this if an entity is rendering strangely.");

            // Debug
            debugMode = config.getBoolean("debugMode", Categories.debug, debugMode, "Enable debug logging");

        } catch (Exception e) {
            System.err.println("Error loading config: " + e.getMessage());
        } finally {
            // if (config.hasChanged()) {
            config.save();
            // }
        }
    }

    public static Configuration getRawConfig() {
        return config;
    }
}

package org.fentanylsolutions.vintagedamageindicators.client;

import java.lang.reflect.Field;

import net.minecraft.entity.EntityLivingBase;

import org.fentanylsolutions.vintagedamageindicators.VintageDamageIndicators;

import cpw.mods.fml.common.Loader;

/**
 * Entity-specific pre/post render patches for HUD preview rendering. Mod-specific patches are gated behind
 * {@link Loader#isModLoaded} checks and cache all reflection lookups at init time so the render path uses only cheap
 * {@code Class.isInstance()} checks.
 */
public final class PreviewRenderPatches {

    private static boolean initialized;

    private PreviewRenderPatches() {}

    // Thaumcraft
    private static boolean thaumcraftLoaded;
    private static Class<?> eldritchGuardianClass;
    private static Class<?> cultistPortalClass;
    private static Class<?> taintSporeClass;
    private static Field taintSporeDisplaySizeField;
    private static Class<?> mindSpiderClass;

    public static void init() {
        if (initialized) return;
        initialized = true;

        thaumcraftLoaded = Loader.isModLoaded("Thaumcraft");
        if (thaumcraftLoaded) {
            eldritchGuardianClass = tryLoadClass("thaumcraft.common.entities.monster.EntityEldritchGuardian");
            cultistPortalClass = tryLoadClass("thaumcraft.common.entities.monster.boss.EntityCultistPortal");
            taintSporeClass = tryLoadClass("thaumcraft.common.entities.monster.EntityTaintSpore");
            mindSpiderClass = tryLoadClass("thaumcraft.common.entities.monster.EntityMindSpider");
            if (taintSporeClass != null) {
                try {
                    taintSporeDisplaySizeField = taintSporeClass.getField("displaySize");
                } catch (NoSuchFieldException e) {
                    VintageDamageIndicators.LOG
                        .warn("EntityTaintSpore.displaySize field not found, skipping patch.", e);
                    taintSporeClass = null;
                }
            }
        }
    }

    private static Class<?> tryLoadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            VintageDamageIndicators.LOG.warn("Thaumcraft loaded but {} not found, skipping patch.", className);
            return null;
        }
    }

    /**
     * Apply entity-specific patches before preview rendering. Returns a {@link PatchState} that must be passed to
     * {@link #revertPatches} in a {@code finally} block.
     */
    public static PatchState applyPatches(EntityLivingBase entity, boolean noWorld) {
        if (!initialized) init();

        if (thaumcraftLoaded) {
            return ThaumcraftBridge.applyPatches(entity, noWorld);
        }

        return PatchState.NONE;
    }

    /**
     * Revert patches applied by {@link #applyPatches}.
     */
    public static void revertPatches(EntityLivingBase entity, PatchState state) {
        if (state == null || state == PatchState.NONE) return;

        if (thaumcraftLoaded) {
            ThaumcraftBridge.revertPatches(entity, state);
        }
    }

    public static final class PatchState {

        public static final PatchState NONE = new PatchState(-1, Float.NaN);

        private final int oldTicksExisted;
        private final float oldDisplaySize;

        private PatchState(int oldTicksExisted, float oldDisplaySize) {
            this.oldTicksExisted = oldTicksExisted;
            this.oldDisplaySize = oldDisplaySize;
        }
    }

    private static final class ThaumcraftBridge {

        /** Minimum ticksExisted for the CultistPortal spawn animation to complete. */
        private static final int CULTIST_PORTAL_SPAWN_TICKS = 50;

        /** Minimum ticksExisted for the MindSpider fade-in to reach full opacity (0.1 alpha). */
        private static final int MIND_SPIDER_FADE_TICKS = 10;

        private ThaumcraftBridge() {}

        static PatchState applyPatches(EntityLivingBase entity, boolean noWorld) {
            if (!noWorld) return PatchState.NONE;

            int oldTicks = -1;
            float oldDisplaySize = Float.NaN;

            if (cultistPortalClass != null && cultistPortalClass.isInstance(entity)
                && entity.ticksExisted < CULTIST_PORTAL_SPAWN_TICKS) {
                oldTicks = entity.ticksExisted;
                entity.ticksExisted = CULTIST_PORTAL_SPAWN_TICKS;
            }

            if (mindSpiderClass != null && mindSpiderClass.isInstance(entity)
                && entity.ticksExisted < MIND_SPIDER_FADE_TICKS) {
                oldTicks = entity.ticksExisted;
                entity.ticksExisted = MIND_SPIDER_FADE_TICKS;
            }

            if (taintSporeClass != null && taintSporeClass.isInstance(entity)) {
                try {
                    float displaySize = taintSporeDisplaySizeField.getFloat(entity);
                    if (displaySize < 1.0F) {
                        oldDisplaySize = displaySize;
                        // getSporeSize() returns DataWatcher byte 16, default 2 from constructor
                        taintSporeDisplaySizeField.setFloat(entity, 2.0F);
                    }
                } catch (IllegalAccessException ignored) {}
            }

            if (oldTicks >= 0 || !Float.isNaN(oldDisplaySize)) {
                return new PatchState(oldTicks, oldDisplaySize);
            }
            return PatchState.NONE;
        }

        static void revertPatches(EntityLivingBase entity, PatchState state) {
            if (state.oldTicksExisted >= 0) {
                entity.ticksExisted = state.oldTicksExisted;
            }
            if (!Float.isNaN(state.oldDisplaySize)) {
                try {
                    taintSporeDisplaySizeField.setFloat(entity, state.oldDisplaySize);
                } catch (IllegalAccessException ignored) {}
            }
        }
    }
}

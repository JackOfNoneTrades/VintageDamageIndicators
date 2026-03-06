package org.fentanylsolutions.vintagedamageindicators.client;

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

    public static void init() {
        if (initialized) return;
        initialized = true;

        thaumcraftLoaded = Loader.isModLoaded("Thaumcraft");
        if (thaumcraftLoaded) {
            try {
                eldritchGuardianClass = Class.forName("thaumcraft.common.entities.monster.EntityEldritchGuardian");
            } catch (ClassNotFoundException e) {
                VintageDamageIndicators.LOG
                    .warn("Thaumcraft loaded but EntityEldritchGuardian class not found, skipping patch.", e);
                eldritchGuardianClass = null;
            }
        }
    }

    /**
     * Apply entity-specific patches before preview rendering. Returns a {@link PatchState} that must be passed to
     * {@link #revertPatches} in a {@code finally} block.
     */
    public static PatchState applyPatches(EntityLivingBase entity) {
        if (!initialized) init();

        if (thaumcraftLoaded && eldritchGuardianClass != null) {
            return ThaumcraftBridge.applyPatches(entity);
        }

        return PatchState.NONE;
    }

    /**
     * Revert patches applied by {@link #applyPatches}.
     */
    public static void revertPatches(EntityLivingBase entity, PatchState state) {
        if (state == null || state == PatchState.NONE) return;

        if (thaumcraftLoaded && eldritchGuardianClass != null) {
            ThaumcraftBridge.revertPatches(entity, state);
        }
    }

    public static final class PatchState {

        public static final PatchState NONE = new PatchState();

        private PatchState() {}
    }

    private static final class ThaumcraftBridge {

        private ThaumcraftBridge() {}

        static PatchState applyPatches(EntityLivingBase entity) {
            if (eldritchGuardianClass.isInstance(entity)) {
                VintageDamageIndicators.debug(
                    "Applying Thaumcraft preview patch for " + entity.getClass()
                        .getSimpleName());
            }
            // The general renderViewEntity fix in HudEntityRenderer handles the NPE.
            // This bridge is a hook point for future Thaumcraft-specific workarounds.
            return PatchState.NONE;
        }

        static void revertPatches(EntityLivingBase entity, PatchState state) {
            // Nothing to revert currently
        }
    }
}

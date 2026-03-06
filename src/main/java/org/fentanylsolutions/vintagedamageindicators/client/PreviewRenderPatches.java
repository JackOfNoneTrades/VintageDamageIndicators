package org.fentanylsolutions.vintagedamageindicators.client;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import org.fentanylsolutions.vintagedamageindicators.VintageDamageIndicators;
import org.lwjgl.opengl.GL11;

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

    // Twilight Forest
    private static boolean twilightForestLoaded;

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

        twilightForestLoaded = Loader.isModLoaded("TwilightForest");
        if (twilightForestLoaded) {
            TwilightForestBridge.init();
        }
    }

    private static Class<?> tryLoadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            VintageDamageIndicators.LOG.warn("{} not found, skipping patch.", className);
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

    /**
     * Render additional model parts after the main entity render (e.g. Hydra heads). Called inside the render
     * try-block,
     * after {@code renderEntityWithPosYaw}.
     */
    public static void renderPostEffects(EntityLivingBase entity, boolean noWorld) {
        if (!initialized) init();

        if (twilightForestLoaded) {
            TwilightForestBridge.renderHydraHeads(entity, noWorld);
        }
    }

    /**
     * Redirect a HUD target entity to a more appropriate entity for display. For example, a Hydra neck entity redirects
     * to its parent Hydra body. Returns the original entity if no redirect applies.
     */
    public static EntityLivingBase resolveHudTarget(EntityLivingBase entity) {
        if (!initialized) init();

        if (twilightForestLoaded) {
            EntityLivingBase resolved = TwilightForestBridge.resolveHudTarget(entity);
            if (resolved != null) return resolved;
        }

        return entity;
    }

    /**
     * Adjust a freshly-created preview entity to match in-game dimensions. Some entities only set their size in
     * constructors that require a parent entity, so the World-only constructor leaves default dimensions.
     */
    public static void adjustPreviewEntity(EntityLivingBase entity) {
        if (!initialized) init();

        if (twilightForestLoaded) {
            TwilightForestBridge.adjustPreviewEntity(entity);
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

    private static final class TwilightForestBridge {

        private static Class<?> hydraClass;
        private static Class<?> hydraPartClass;
        private static Class<?> hydraNeckClass;
        private static Field hydraObjField;
        private static Field hydraHcField;
        private static Method shouldRenderHeadMethod;
        private static ModelRenderer[] headParts;
        private static ModelRenderer[][] neckParts;
        private static ResourceLocation hydraTexture;

        private TwilightForestBridge() {}

        static void init() {
            hydraClass = tryLoadClass("twilightforest.entity.boss.EntityTFHydra");
            hydraPartClass = tryLoadClass("twilightforest.entity.boss.EntityTFHydraPart");
            hydraNeckClass = tryLoadClass("twilightforest.entity.boss.EntityTFHydraNeck");
            if (hydraPartClass != null) {
                try {
                    hydraObjField = hydraPartClass.getField("hydraObj");
                } catch (NoSuchFieldException e) {
                    VintageDamageIndicators.LOG.warn("EntityTFHydraPart.hydraObj field not found.", e);
                }
            }
            if (hydraClass == null) return;

            try {
                hydraHcField = hydraClass.getField("hc");
                Class<?> headContainerClass = Class.forName("twilightforest.entity.boss.HydraHeadContainer");
                shouldRenderHeadMethod = headContainerClass.getMethod("shouldRenderHead");

                Class<?> modelClass = Class.forName("twilightforest.client.model.ModelTFHydra");
                Object model = modelClass.getConstructor()
                    .newInstance();

                headParts = new ModelRenderer[3];
                neckParts = new ModelRenderer[3][4];
                String[] suffixes = { "1", "2", "3" };
                String[] neckLetters = { "a", "b", "c", "d" };

                for (int i = 0; i < 3; i++) {
                    Field f = modelClass.getDeclaredField("head" + suffixes[i]);
                    f.setAccessible(true);
                    headParts[i] = (ModelRenderer) f.get(model);
                    for (int j = 0; j < 4; j++) {
                        f = modelClass.getDeclaredField("neck" + suffixes[i] + neckLetters[j]);
                        f.setAccessible(true);
                        neckParts[i][j] = (ModelRenderer) f.get(model);
                    }
                }

                hydraTexture = new ResourceLocation("twilightforest", "textures/model/hydra4.png");
            } catch (Exception e) {
                VintageDamageIndicators.LOG.warn("Failed to init Twilight Forest Hydra bridge.", e);
                hydraClass = null;
            }
        }

        static EntityLivingBase resolveHudTarget(EntityLivingBase entity) {
            if (hydraNeckClass != null && hydraNeckClass.isInstance(entity) && hydraObjField != null) {
                try {
                    Object hydra = hydraObjField.get(entity);
                    if (hydra instanceof EntityLivingBase) {
                        return (EntityLivingBase) hydra;
                    }
                } catch (IllegalAccessException ignored) {}
            }
            return null;
        }

        static void adjustPreviewEntity(EntityLivingBase entity) {
            // EntityTFHydraPart(World) does not call setSize; in-game constructor uses 3x3
            if (hydraPartClass != null && hydraPartClass.isInstance(entity)) {
                entity.width = 3.0F;
                entity.height = 3.0F;
            }
        }

        static void renderHydraHeads(EntityLivingBase entity, boolean noWorld) {
            if (hydraClass == null || !hydraClass.isInstance(entity)) return;
            if (headParts == null) return;

            // Determine head visibility: all visible for config preview, check containers in-game
            boolean[] visible = { true, true, true };
            if (!noWorld) {
                try {
                    Object[] hc = (Object[]) hydraHcField.get(entity);
                    if (hc != null) {
                        for (int i = 0; i < 3 && i < hc.length; i++) {
                            if (hc[i] != null) {
                                visible[i] = (boolean) shouldRenderHeadMethod.invoke(hc[i]);
                            } else {
                                visible[i] = false;
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }

            // Replicate RendererLivingEntity's transform stack to match the body render
            GL11.glPushMatrix();
            GL11.glTranslatef(0.0F, -entity.yOffset, 0.0F);
            GL11.glRotatef(180.0F - entity.renderYawOffset, 0.0F, 1.0F, 0.0F);
            GL11.glScalef(-1.0F, -1.0F, 1.0F);
            GL11.glTranslatef(0.0F, -24.0F * 0.0625F - 0.0078125F, 0.0F);

            Minecraft.getMinecraft()
                .getTextureManager()
                .bindTexture(hydraTexture);

            float f5 = 0.0625F;
            for (int i = 0; i < 3; i++) {
                if (visible[i]) {
                    for (int j = 0; j < 4; j++) {
                        neckParts[i][j].render(f5);
                    }
                    headParts[i].render(f5);
                }
            }

            GL11.glPopMatrix();
        }
    }
}

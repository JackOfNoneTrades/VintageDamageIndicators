package org.fentanylsolutions.vintagedamageindicators.client;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import org.fentanylsolutions.vintagedamageindicators.VintageDamageIndicators;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.common.Loader;

/**
 * Entity-specific pre/post render patches for HUD preview rendering. Mod-specific patches are gated behind
 * {@link Loader#isModLoaded} checks and cache all reflection lookups at init time so the render path uses only cheap
 * {@code Class.isInstance()} checks.
 */
public final class PreviewRenderPatches {

    private static boolean initialized;
    public static float hudPartialTicks = 1.0F;

    private PreviewRenderPatches() {}

    // Thaumcraft
    private static boolean thaumcraftLoaded;
    private static Class<?> eldritchGuardianClass;
    private static Class<?> cultistPortalClass;
    private static Class<?> taintSporeClass;
    private static Field taintSporeDisplaySizeField;
    private static Class<?> taintacleClass;
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
            taintacleClass = tryLoadClass("thaumcraft.common.entities.monster.EntityTaintacle");
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
    public static void renderPostEffects(EntityLivingBase entity, boolean noWorld, float originalPrevYawOffset,
        float originalYawOffset) {
        if (!initialized) init();

        if (twilightForestLoaded) {
            TwilightForestBridge.renderHydraHeads(entity, noWorld, originalPrevYawOffset, originalYawOffset);
        }
    }

    /**
     * Returns true if the HUD should not be shown for the given entity. For example, Hydra head entities are suppressed
     * because the parent Hydra body preview already renders heads.
     */
    public static boolean isHudSuppressed(EntityLivingBase entity) {
        if (!initialized) init();

        if (twilightForestLoaded) {
            return TwilightForestBridge.isHudSuppressed(entity);
        }

        return false;
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

            if (taintacleClass != null && taintacleClass.isInstance(entity)) {
                int emergeTicks = Math.max(1, Math.round(entity.height * 10.0F));
                if (entity.ticksExisted < emergeTicks) {
                    oldTicks = entity.ticksExisted;
                    entity.ticksExisted = emergeTicks;
                }
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
        private static Class<?> hydraHeadClass;
        private static Class<?> hydraNeckClass;
        private static Field hydraObjField;
        private static Field hydraHcField;
        private static Method shouldRenderHeadMethod;
        private static Method shouldRenderNeckMethod;
        private static Field headEntityField;
        private static Method getNeckArrayMethod;
        private static ModelRenderer[] headParts;
        private static ModelRenderer[][] neckParts;
        private static ModelBase hydraHeadModel;
        private static ModelBase hydraNeckModel;
        private static ResourceLocation hydraTexture;
        private static final WeakHashMap<EntityLivingBase, HydraPreviewState> LIVE_PREVIEW_STATES = new WeakHashMap<>();

        private TwilightForestBridge() {}

        static void init() {
            hydraClass = tryLoadClass("twilightforest.entity.boss.EntityTFHydra");
            hydraPartClass = tryLoadClass("twilightforest.entity.boss.EntityTFHydraPart");
            hydraHeadClass = tryLoadClass("twilightforest.entity.boss.EntityTFHydraHead");
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
                shouldRenderNeckMethod = headContainerClass.getMethod("shouldRenderNeck", int.class);
                headEntityField = headContainerClass.getField("headEntity");
                getNeckArrayMethod = headContainerClass.getMethod("getNeckArray");

                Class<?> modelClass = Class.forName("twilightforest.client.model.ModelTFHydra");
                Object model = modelClass.getConstructor()
                    .newInstance();
                hydraHeadModel = (ModelBase) Class.forName("twilightforest.client.model.ModelTFHydraHead")
                    .getConstructor()
                    .newInstance();
                hydraNeckModel = (ModelBase) Class.forName("twilightforest.client.model.ModelTFHydraNeck")
                    .getConstructor()
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

        static boolean isHudSuppressed(EntityLivingBase entity) {
            return hydraHeadClass != null && hydraHeadClass.isInstance(entity);
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

        static void renderHydraHeads(EntityLivingBase entity, boolean noWorld, float originalPrevYawOffset,
            float originalYawOffset) {
            if (hydraClass == null || !hydraClass.isInstance(entity) || headParts == null) return;
            if (noWorld || hydraHcField == null
                || shouldRenderHeadMethod == null
                || shouldRenderNeckMethod == null
                || headEntityField == null
                || getNeckArrayMethod == null
                || hydraHeadModel == null
                || hydraNeckModel == null) {
                renderStaticHeads(entity);
                return;
            }

            if (!renderLiveHeads(entity, originalPrevYawOffset, originalYawOffset)) {
                renderStaticHeads(entity);
            }
        }

        private static void renderStaticHeads(EntityLivingBase entity) {
            renderStaticHeadModel(entity);
        }

        private static boolean renderLiveHeads(EntityLivingBase hydra, float originalPrevYawOffset,
            float originalYawOffset) {
            try {
                Object[] headContainers = (Object[]) hydraHcField.get(hydra);
                if (headContainers == null) return false;

                float partialTicks = MathHelper.clamp_float(hudPartialTicks, 0.0F, 1.0F);
                float previewYawDelta = MathHelper.wrapAngleTo180_float(
                    hydra.renderYawOffset
                        - interpolateRotation(originalPrevYawOffset, originalYawOffset, partialTicks));
                HydraPreviewState previewState = LIVE_PREVIEW_STATES.get(hydra);
                if (previewState == null) {
                    previewState = new HydraPreviewState();
                    LIVE_PREVIEW_STATES.put(hydra, previewState);
                }
                previewState.beginFrame();

                Minecraft.getMinecraft()
                    .getTextureManager()
                    .bindTexture(hydraTexture);

                for (Object headContainer : headContainers) {
                    if (headContainer == null) continue;

                    Object headObj = headEntityField.get(headContainer);
                    if (!(headObj instanceof EntityLivingBase head)) continue;

                    Object neckArray = getNeckArrayMethod.invoke(headContainer);
                    Entity[] necks = neckArray instanceof Entity[] ? (Entity[]) neckArray : null;

                    if (necks != null) {
                        for (int i = 0; i < necks.length; i++) {
                            if (!(necks[i] instanceof EntityLivingBase neck)) continue;
                            if (!((Boolean) shouldRenderNeckMethod.invoke(headContainer, Integer.valueOf(i)))
                                .booleanValue()) {
                                continue;
                            }
                            PartPose rawPose = samplePartPose(neck, hydra, previewYawDelta, partialTicks);
                            PartPose smoothedPose = previewState.smooth(neck.getEntityId(), rawPose);
                            renderHydraNeckModel(hydra, neck, smoothedPose);
                        }
                    }

                    if (((Boolean) shouldRenderHeadMethod.invoke(headContainer)).booleanValue()) {
                        PartPose rawPose = samplePartPose(head, hydra, previewYawDelta, partialTicks);
                        PartPose smoothedPose = previewState.smooth(head.getEntityId(), rawPose);
                        renderHydraHeadModel(hydra, head, smoothedPose);
                    }
                }

                previewState.endFrame();
                return true;
            } catch (Exception e) {
                VintageDamageIndicators.LOG.debug("Hydra live HUD preview failed, falling back to static.", e);
                return false;
            }
        }

        private static void renderStaticHeadModel(EntityLivingBase entity) {
            GL11.glPushMatrix();
            GL11.glTranslatef(0.0F, -entity.yOffset, 0.0F);
            GL11.glRotatef(180.0F - entity.renderYawOffset, 0.0F, 1.0F, 0.0F);
            GL11.glScalef(-1.0F, -1.0F, 1.0F);
            GL11.glTranslatef(0.0F, -24.0F * 0.0625F - 0.0078125F, 0.0F);

            Minecraft.getMinecraft()
                .getTextureManager()
                .bindTexture(hydraTexture);

            float scale = 0.0625F;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 4; j++) {
                    neckParts[i][j].render(scale);
                }
                headParts[i].render(scale);
            }

            GL11.glPopMatrix();
        }

        private static PartPose samplePartPose(EntityLivingBase part, EntityLivingBase hydra, float previewYawDelta,
            float partialTicks) {
            double bodyX = hydra.lastTickPosX + (hydra.posX - hydra.lastTickPosX) * partialTicks;
            double bodyY = hydra.lastTickPosY + (hydra.posY - hydra.lastTickPosY) * partialTicks;
            double bodyZ = hydra.lastTickPosZ + (hydra.posZ - hydra.lastTickPosZ) * partialTicks;
            double partX = part.lastTickPosX + (part.posX - part.lastTickPosX) * partialTicks;
            double partY = part.lastTickPosY + (part.posY - part.lastTickPosY) * partialTicks;
            double partZ = part.lastTickPosZ + (part.posZ - part.lastTickPosZ) * partialTicks;
            double offsetX = partX - bodyX;
            double offsetY = partY - bodyY;
            double offsetZ = partZ - bodyZ;
            return new PartPose(
                rotateOffsetX(offsetX, offsetZ, previewYawDelta),
                offsetY,
                rotateOffsetZ(offsetX, offsetZ, previewYawDelta),
                shiftedYaw(part, partialTicks, previewYawDelta),
                interpolatedPitch(part, partialTicks));
        }

        private static void renderHydraHeadModel(EntityLivingBase hydra, EntityLivingBase entity, PartPose pose) {
            PartAngleState saved = new PartAngleState(
                entity.rotationYaw,
                entity.prevRotationYaw,
                entity.rotationYawHead,
                entity.prevRotationYawHead,
                entity.rotationPitch,
                entity.prevRotationPitch);
            entity.rotationYaw = pose.yaw;
            entity.prevRotationYaw = pose.yaw;
            entity.rotationYawHead = pose.yaw;
            entity.prevRotationYawHead = pose.yaw;
            entity.rotationPitch = pose.pitch;
            entity.prevRotationPitch = pose.pitch;
            renderHydraPartModel(hydraHeadModel, entity, hydra, pose.x, pose.y, pose.z, 0.0F, 0.0F, hudPartialTicks);
            entity.rotationYaw = saved.rotationYaw;
            entity.prevRotationYaw = saved.prevRotationYaw;
            entity.rotationYawHead = saved.rotationYawHead;
            entity.prevRotationYawHead = saved.prevRotationYawHead;
            entity.rotationPitch = saved.rotationPitch;
            entity.prevRotationPitch = saved.prevRotationPitch;
        }

        private static void renderHydraNeckModel(EntityLivingBase hydra, EntityLivingBase entity, PartPose pose) {
            renderHydraPartModel(
                hydraNeckModel,
                entity,
                hydra,
                pose.x,
                pose.y,
                pose.z,
                pose.yaw,
                pose.pitch,
                hudPartialTicks);
        }

        private static void renderHydraPartModel(ModelBase model, EntityLivingBase entity, EntityLivingBase hydra,
            double x, double y, double z, float yaw, float pitch, float partialTicks) {
            float scale = 0.0625F;

            GL11.glPushMatrix();
            try {
                GL11.glTranslated(x, y, z);
                GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                GL11.glScalef(-1.0F, -1.0F, 1.0F);
                GL11.glTranslatef(0.0F, -24.0F * scale - 0.0078125F, 0.0F);
                model.setLivingAnimations(entity, 0.0F, 0.0F, partialTicks);
                model.render(entity, 0.0F, 0.0F, 0.0F, yaw, pitch, scale);
                renderHydraHurtOverlay(model, entity, hydra, yaw, pitch, partialTicks, scale);
            } finally {
                GL11.glPopMatrix();
            }
        }

        private static void renderHydraHurtOverlay(ModelBase model, EntityLivingBase entity, EntityLivingBase hydra,
            float yaw, float pitch, float partialTicks, float scale) {
            if (hydra.hurtTime <= 0 && hydra.deathTime <= 0) {
                return;
            }

            float brightness = hydra.getBrightness(partialTicks);
            OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDepthFunc(GL11.GL_EQUAL);
            GL11.glColor4f(brightness, 0.0F, 0.0F, 0.4F);
            model.render(entity, 0.0F, 0.0F, 0.0F, yaw, pitch, scale);
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }

        private static float interpolateRotation(float previous, float current, float partialTicks) {
            float delta = current - previous;
            while (delta < -180.0F) {
                delta += 360.0F;
            }
            while (delta >= 180.0F) {
                delta -= 360.0F;
            }
            return previous + partialTicks * delta;
        }

        private static float shiftedYaw(EntityLivingBase entity, float partialTicks, float yawDelta) {
            return interpolateRotation(entity.prevRotationYaw, entity.rotationYaw, partialTicks) + yawDelta;
        }

        private static float interpolatedPitch(EntityLivingBase entity, float partialTicks) {
            return entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
        }

        private static double rotateOffsetX(double x, double z, float yawDegrees) {
            double radians = Math.toRadians(yawDegrees);
            return x * Math.cos(radians) - z * Math.sin(radians);
        }

        private static double rotateOffsetZ(double x, double z, float yawDegrees) {
            double radians = Math.toRadians(yawDegrees);
            return x * Math.sin(radians) + z * Math.cos(radians);
        }

        private static final class HydraPreviewState {

            private final Map<Integer, PartPose> poses = new HashMap<>();
            private final Map<Integer, Boolean> seenThisFrame = new HashMap<>();

            private void beginFrame() {
                seenThisFrame.clear();
            }

            private PartPose smooth(int entityId, PartPose rawPose) {
                seenThisFrame.put(Integer.valueOf(entityId), Boolean.TRUE);
                PartPose currentPose = poses.get(Integer.valueOf(entityId));
                if (currentPose == null || currentPose.distanceSq(rawPose) > 36.0D) {
                    PartPose seededPose = rawPose.copy();
                    poses.put(Integer.valueOf(entityId), seededPose);
                    return seededPose;
                }

                double distanceSq = currentPose.distanceSq(rawPose);
                float yawDelta = Math.abs(MathHelper.wrapAngleTo180_float(rawPose.yaw - currentPose.yaw));
                float pitchDelta = Math.abs(MathHelper.wrapAngleTo180_float(rawPose.pitch - currentPose.pitch));
                double positionAlpha = distanceSq < 0.5D ? 0.18D : 0.35D;
                float angleAlpha = yawDelta < 6.0F && pitchDelta < 6.0F ? 0.18F : 0.35F;

                currentPose.x += (rawPose.x - currentPose.x) * positionAlpha;
                currentPose.y += (rawPose.y - currentPose.y) * positionAlpha;
                currentPose.z += (rawPose.z - currentPose.z) * positionAlpha;
                currentPose.yaw = interpolateAngle(currentPose.yaw, rawPose.yaw, angleAlpha);
                currentPose.pitch = interpolateAngle(currentPose.pitch, rawPose.pitch, angleAlpha);
                return currentPose;
            }

            private void endFrame() {
                poses.keySet()
                    .removeIf(key -> !seenThisFrame.containsKey(key));
            }

            private static float interpolateAngle(float previous, float current, float alpha) {
                return previous + MathHelper.wrapAngleTo180_float(current - previous) * alpha;
            }
        }

        private static final class PartAngleState {

            private final float rotationYaw;
            private final float prevRotationYaw;
            private final float rotationYawHead;
            private final float prevRotationYawHead;
            private final float rotationPitch;
            private final float prevRotationPitch;

            private PartAngleState(float rotationYaw, float prevRotationYaw, float rotationYawHead,
                float prevRotationYawHead, float rotationPitch, float prevRotationPitch) {
                this.rotationYaw = rotationYaw;
                this.prevRotationYaw = prevRotationYaw;
                this.rotationYawHead = rotationYawHead;
                this.prevRotationYawHead = prevRotationYawHead;
                this.rotationPitch = rotationPitch;
                this.prevRotationPitch = prevRotationPitch;
            }
        }

        private static final class PartPose {

            private double x;
            private double y;
            private double z;
            private float yaw;
            private float pitch;

            private PartPose(double x, double y, double z, float yaw, float pitch) {
                this.x = x;
                this.y = y;
                this.z = z;
                this.yaw = yaw;
                this.pitch = pitch;
            }

            private double distanceSq(PartPose other) {
                double dx = other.x - this.x;
                double dy = other.y - this.y;
                double dz = other.z - this.z;
                return dx * dx + dy * dy + dz * dz;
            }

            private PartPose copy() {
                return new PartPose(this.x, this.y, this.z, this.yaw, this.pitch);
            }
        }
    }
}

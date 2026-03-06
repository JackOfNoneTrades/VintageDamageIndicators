package org.fentanylsolutions.vintagedamageindicators.client;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import org.fentanylsolutions.vintagedamageindicators.VintageDamageIndicators;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.mojang.authlib.GameProfile;

public final class HudEntityRenderer {

    private static final Set<Class<?>> RENDER_FAILED_CLASSES = new HashSet<>();
    private static final GameProfile DUMMY_PROFILE = new GameProfile(
        UUID.fromString("00000000-0000-0000-0000-000000000000"),
        "PreviewDummy");
    private static final java.nio.FloatBuffer MATRIX_BUF = BufferUtils.createFloatBuffer(16);
    private static EntityOtherPlayerMP dummyPlayer;

    private HudEntityRenderer() {}

    private static EntityLivingBase getOrCreateDummyPlayer(World world) {
        if (dummyPlayer == null || dummyPlayer.worldObj != world) {
            dummyPlayer = new EntityOtherPlayerMP(world, DUMMY_PROFILE);
        }
        return dummyPlayer;
    }

    public static void clearRenderFailedCache() {
        RENDER_FAILED_CLASSES.clear();
    }

    public static void drawEntity(int x, int y, float scale, float yawInput, float pitchInput, float rollInput,
        EntityLivingBase entity) {
        if (entity == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        RenderManager renderManager = RenderManager.instance;
        World oldWorld = renderManager.worldObj;
        TextureManager oldRenderEngine = renderManager.renderEngine;
        GameSettings oldOptions = renderManager.options;
        EntityLivingBase oldLivingPlayer = renderManager.livingPlayer;
        Entity oldViewEntity = renderManager.field_147941_i;
        double oldViewerPosX = renderManager.viewerPosX;
        double oldViewerPosY = renderManager.viewerPosY;
        double oldViewerPosZ = renderManager.viewerPosZ;
        float oldPlayerViewX = renderManager.playerViewX;
        float oldPlayerViewY = RenderManager.instance.playerViewY;
        float oldRenderYawOffset = entity.renderYawOffset;
        float oldRotationYaw = entity.rotationYaw;
        float oldRotationPitch = entity.rotationPitch;
        float oldPrevRotationYawHead = entity.prevRotationYawHead;
        float oldRotationYawHead = entity.rotationYawHead;
        int oldDragonRingBufferIndex = 0;
        double[][] oldDragonRingBuffer = null;
        double oldPosY = entity.posY;
        EntityLivingBase oldRenderViewEntity = minecraft.renderViewEntity;

        World renderWorld = minecraft.theWorld != null ? minecraft.theWorld : entity.worldObj;
        EntityLivingBase viewLiving = minecraft.thePlayer != null ? minecraft.thePlayer
            : getOrCreateDummyPlayer(renderWorld);
        Entity viewEntity = minecraft.renderViewEntity != null ? minecraft.renderViewEntity : viewLiving;
        minecraft.renderViewEntity = viewLiving;
        renderManager.cacheActiveRenderInfo(
            renderWorld,
            minecraft.getTextureManager(),
            minecraft.fontRenderer,
            viewLiving,
            viewEntity,
            minecraft.gameSettings,
            1.0F);

        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        // Read the current modelview to compute absolute screen position. The modelview may
        // contain GUI widget offsets, a HUD scale, and the -2000 Z translation. We transform
        // the point (x, y, 50) through the full matrix to get absolute coordinates, then
        // replace the modelview with glLoadIdentity so the projection scale only affects our
        // known coordinates, not the GUI's pre-existing transforms.
        MATRIX_BUF.clear();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, MATRIX_BUF);
        float absX = MATRIX_BUF.get(0) * x + MATRIX_BUF.get(4) * y + MATRIX_BUF.get(8) * 50.0F + MATRIX_BUF.get(12);
        float absY = MATRIX_BUF.get(1) * x + MATRIX_BUF.get(5) * y + MATRIX_BUF.get(9) * 50.0F + MATRIX_BUF.get(13);
        float absZ = MATRIX_BUF.get(2) * x + MATRIX_BUF.get(6) * y + MATRIX_BUF.get(10) * 50.0F + MATRIX_BUF.get(14);
        // Extract pre-existing XY scale from the modelview (e.g. HUD indicator size) so
        // the entity renders at the correct size within the GUI's coordinate space.
        float mvScale = (float) Math
            .sqrt(MATRIX_BUF.get(0) * MATRIX_BUF.get(0) + MATRIX_BUF.get(1) * MATRIX_BUF.get(1));
        float effectiveScale = scale * mvScale;
        // Apply scale via projection matrix so renderers that strip modelview rotations
        // (billboard-style rendering) still render at the correct size.
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glScalef(effectiveScale, effectiveScale, 1.0F);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glTranslatef(absX / effectiveScale, absY / effectiveScale, absZ);
        GL11.glScalef(-1.0F, 1.0F, 1.0F);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        GL11.glRotatef(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(rollInput, 0.0F, 0.0F, 1.0F);
        GL11.glRotatef(-(float) Math.atan(pitchInput / 40.0F) * 20.0F, 1.0F, 0.0F, 0.0F);
        float previewBodyYaw = (float) Math.atan(yawInput / 40.0F) * 20.0F;
        float previewRotationYaw = (float) Math.atan(yawInput / 40.0F) * 40.0F;
        float previewRotationPitch = -((float) Math.atan(pitchInput / 40.0F)) * 20.0F;

        if (entity instanceof EntityDragon) {
            previewBodyYaw += 180.0F;
            previewRotationYaw += 180.0F;
        }

        entity.renderYawOffset = previewBodyYaw;
        entity.rotationYaw = previewRotationYaw;
        entity.rotationPitch = previewRotationPitch;
        entity.rotationYawHead = previewRotationYaw;
        entity.prevRotationYawHead = previewRotationYaw;

        if (entity instanceof EntityDragon dragon) {
            oldDragonRingBufferIndex = dragon.ringBufferIndex;
            oldDragonRingBuffer = new double[dragon.ringBuffer.length][3];
            for (int i = 0; i < dragon.ringBuffer.length; i++) {
                System.arraycopy(dragon.ringBuffer[i], 0, oldDragonRingBuffer[i], 0, dragon.ringBuffer[i].length);
            }
            dragon.ringBufferIndex = 0;
            for (int i = 0; i < dragon.ringBuffer.length; i++) {
                dragon.ringBuffer[i][0] = previewRotationYaw;
                dragon.ringBuffer[i][1] = entity.posY;
                dragon.ringBuffer[i][2] = 0.0D;
            }
        }

        HudPreviewWorld previewWorld = entity.worldObj instanceof HudPreviewWorld ? (HudPreviewWorld) entity.worldObj
            : null;
        boolean oldForceDark = previewWorld != null && previewWorld.isForceDark();
        boolean isEyesEntity = EyesCompatHelper.isEyesEntity(entity);
        boolean shouldForceDark = previewWorld != null && isEyesEntity;
        if (previewWorld != null && oldForceDark != shouldForceDark) {
            previewWorld.setForceDark(shouldForceDark);
        }
        EyesCompatHelper.PreviewRenderState eyesPreviewState = EyesCompatHelper
            .beginPreviewRender(renderManager, entity);

        GL11.glTranslatef(0.0F, entity.yOffset, 0.0F);
        RenderManager.instance.playerViewY = 180.0F;

        float oldRotationX = ActiveRenderInfo.rotationX;
        float oldRotationZ = ActiveRenderInfo.rotationZ;
        float oldRotationYZ = ActiveRenderInfo.rotationYZ;
        float oldRotationXY = ActiveRenderInfo.rotationXY;
        float oldRotationXZ = ActiveRenderInfo.rotationXZ;
        float previewYaw = 180.0F;
        float previewPitch = 0.0F;
        ActiveRenderInfo.rotationX = MathHelper.cos(previewYaw * (float) Math.PI / 180.0F);
        ActiveRenderInfo.rotationZ = MathHelper.sin(previewYaw * (float) Math.PI / 180.0F);
        ActiveRenderInfo.rotationYZ = -ActiveRenderInfo.rotationZ
            * MathHelper.sin(previewPitch * (float) Math.PI / 180.0F);
        ActiveRenderInfo.rotationXY = ActiveRenderInfo.rotationX
            * MathHelper.sin(previewPitch * (float) Math.PI / 180.0F);
        ActiveRenderInfo.rotationXZ = MathHelper.cos(previewPitch * (float) Math.PI / 180.0F);

        boolean noWorld = minecraft.theWorld == null;
        PreviewRenderPatches.PatchState patchState = PreviewRenderPatches.applyPatches(entity, noWorld);
        GL11.glDisable(GL11.GL_CULL_FACE);
        try {
            if (!RENDER_FAILED_CLASSES.contains(entity.getClass())) {
                RenderManager.instance.renderEntityWithPosYaw(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
            }
            PreviewRenderPatches.renderPostEffects(entity, noWorld);
        } catch (Exception e) {
            RENDER_FAILED_CLASSES.add(entity.getClass());
            VintageDamageIndicators.LOG.warn(
                "Preview render failed for {}, skipping future attempts.",
                entity.getClass()
                    .getName(),
                e);
        } finally {
            GL11.glEnable(GL11.GL_CULL_FACE);
            PreviewRenderPatches.revertPatches(entity, patchState);
            EyesCompatHelper.endPreviewRender(renderManager, entity, eyesPreviewState);
            if (previewWorld != null && oldForceDark != shouldForceDark) {
                previewWorld.setForceDark(oldForceDark);
            }
        }
        entity.renderYawOffset = oldRenderYawOffset;
        entity.rotationYaw = oldRotationYaw;
        entity.rotationPitch = oldRotationPitch;
        entity.prevRotationYawHead = oldPrevRotationYawHead;
        entity.rotationYawHead = oldRotationYawHead;
        entity.posY = oldPosY;

        if (entity instanceof EntityDragon dragon && oldDragonRingBuffer != null) {
            dragon.ringBufferIndex = oldDragonRingBufferIndex;
            for (int i = 0; i < dragon.ringBuffer.length; i++) {
                System.arraycopy(oldDragonRingBuffer[i], 0, dragon.ringBuffer[i], 0, dragon.ringBuffer[i].length);
            }
        }

        ActiveRenderInfo.rotationX = oldRotationX;
        ActiveRenderInfo.rotationZ = oldRotationZ;
        ActiveRenderInfo.rotationYZ = oldRotationYZ;
        ActiveRenderInfo.rotationXY = oldRotationXY;
        ActiveRenderInfo.rotationXZ = oldRotationXZ;
        minecraft.renderViewEntity = oldRenderViewEntity;
        renderManager.worldObj = oldWorld;
        renderManager.renderEngine = oldRenderEngine;
        renderManager.options = oldOptions;
        renderManager.livingPlayer = oldLivingPlayer;
        renderManager.field_147941_i = oldViewEntity;
        renderManager.viewerPosX = oldViewerPosX;
        renderManager.viewerPosY = oldViewerPosY;
        renderManager.viewerPosZ = oldViewerPosZ;
        renderManager.playerViewX = oldPlayerViewX;
        renderManager.playerViewY = oldPlayerViewY;
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GL11.glDisable(GL11.GL_COLOR_MATERIAL);
    }
}

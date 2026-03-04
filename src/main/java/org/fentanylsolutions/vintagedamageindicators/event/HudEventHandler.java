package org.fentanylsolutions.vintagedamageindicators.event;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.fentanylsolutions.vintagedamageindicators.Config;
import org.fentanylsolutions.vintagedamageindicators.MobTypes;
import org.fentanylsolutions.vintagedamageindicators.VintageDamageIndicators;
import org.fentanylsolutions.vintagedamageindicators.client.HudIndicatorState;
import org.fentanylsolutions.vintagedamageindicators.varinstances.VarInstanceCommon;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class HudEventHandler extends Gui {

    private static final int PANEL_WIDTH = 208;
    private static final int PANEL_HEIGHT = 78;
    private static final int ENTITY_X = 45;
    private static final int ENTITY_Y = 58;
    private static final int HEALTH_BAR_X = 81;
    private static final int HEALTH_BAR_Y = 25;
    private static final int HEALTH_BAR_WIDTH = 124;
    private static final int HEALTH_BAR_HEIGHT = 18;
    private static final float ENTITY_RENDER_YAW_INPUT = 35.0F;
    private static final float ENTITY_RENDER_PITCH_INPUT = -12.0F;

    @SubscribeEvent
    public void onBossBarRender(RenderGameOverlayEvent.Pre event) {
        if (event.type == RenderGameOverlayEvent.ElementType.BOSSHEALTH && Config.disableBossBar
            && event.isCancelable()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.thePlayer == null || minecraft.gameSettings.hideGUI) {
            return;
        }
        if (!Config.hudIndicatorEnabled) {
            return;
        }
        if (minecraft.gameSettings.showDebugInfo && Config.hideOnDebug) {
            return;
        }
        if (minecraft.currentScreen != null && !(minecraft.currentScreen instanceof GuiChat)) {
            return;
        }
        if (VintageDamageIndicators.varInstanceClient == null) {
            return;
        }

        HudIndicatorState state = VintageDamageIndicators.varInstanceClient.hudIndicatorState;
        EntityLivingBase target = state.getTargetEntity();
        if (target == null || target.isDead) {
            return;
        }

        renderHud(event.resolution, target, state.getCurrentMobType());
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.thePlayer == null || VintageDamageIndicators.varInstanceClient == null) {
            return;
        }
        if (!Config.hudIndicatorEnabled) {
            clearTargetState();
            return;
        }
        if (minecraft.gameSettings.hideGUI) {
            clearTargetState();
            return;
        }
        if (minecraft.gameSettings.showDebugInfo && Config.hideOnDebug) {
            clearTargetState();
            return;
        }
        if (minecraft.currentScreen != null && !(minecraft.currentScreen instanceof GuiChat)) {
            clearTargetState();
            return;
        }

        updateTargetState(event.renderTickTime);
    }

    private void updateTargetState(float partialTicks) {
        HudIndicatorState state = VintageDamageIndicators.varInstanceClient.hudIndicatorState;
        EntityLivingBase target = findTarget(Config.maxDistance, partialTicks);
        if (target != null && !target.isDead && target.getHealth() > 0.0F) {
            state.setTarget(target, MobTypes.getTypeFor(target), Config.hudLingerTime, usesModelOnlyRender(target));
            return;
        }

        if (state.getTargetEntity() == null || state.getTargetEntity().isDead) {
            state.clearTarget();
            return;
        }

        if (state.getLingerTicks() > 0) {
            state.setLingerTicks(state.getLingerTicks() - 1);
        } else {
            state.clearTarget();
        }
    }

    private EntityLivingBase findTarget(double maxDistance, float partialTicks) {
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityLivingBase viewEntity = minecraft.renderViewEntity instanceof EntityLivingBase
            ? (EntityLivingBase) minecraft.renderViewEntity
            : minecraft.thePlayer;
        if (viewEntity == null || viewEntity.worldObj == null) {
            return null;
        }

        Vec3 eyePosition = viewEntity.getPosition(partialTicks);
        MovingObjectPosition blockHit = viewEntity.rayTrace(maxDistance, partialTicks);
        double closestDistance = blockHit == null ? maxDistance : eyePosition.distanceTo(blockHit.hitVec);
        Vec3 look = viewEntity.getLook(partialTicks);
        Vec3 lookEnd = eyePosition
            .addVector(look.xCoord * maxDistance, look.yCoord * maxDistance, look.zCoord * maxDistance);
        AxisAlignedBB searchBox = viewEntity.boundingBox
            .addCoord(look.xCoord * maxDistance, look.yCoord * maxDistance, look.zCoord * maxDistance)
            .expand(1.0D, 1.0D, 1.0D);
        List<Entity> candidates = viewEntity.worldObj.getEntitiesWithinAABBExcludingEntity(viewEntity, searchBox);
        EntityLivingBase closestTarget = null;

        for (Entity candidate : candidates) {
            if (!(candidate instanceof EntityLivingBase) || candidate.isInvisible() || !candidate.canBeCollidedWith()) {
                continue;
            }

            AxisAlignedBB candidateBox = candidate.boundingBox.expand(0.1D, 0.1D, 0.1D);
            MovingObjectPosition intercept = candidateBox.calculateIntercept(eyePosition, lookEnd);
            if (intercept == null) {
                continue;
            }

            double hitDistance = eyePosition.distanceTo(intercept.hitVec);
            if (hitDistance <= closestDistance) {
                closestDistance = hitDistance;
                closestTarget = (EntityLivingBase) candidate;
            }
        }

        return closestTarget;
    }

    private boolean usesModelOnlyRender(EntityLivingBase entity) {
        String className = entity.getClass()
            .getName();
        for (String oldRenderEntity : Config.oldRenderEntities) {
            if (oldRenderEntity != null && className.equals(oldRenderEntity.trim())) {
                return true;
            }
        }
        return false;
    }

    private void clearTargetState() {
        if (VintageDamageIndicators.varInstanceClient != null) {
            VintageDamageIndicators.varInstanceClient.hudIndicatorState.clearTarget();
        }
    }

    private void renderHud(ScaledResolution resolution, EntityLivingBase target, MobTypes mobType) {
        Minecraft minecraft = Minecraft.getMinecraft();
        float scale = Config.hudIndicatorSize;
        int x = Config.hudIndicatorAlignLeft ? Config.hudIndicatorPositionX
            : resolution.getScaledWidth() - Math.round(PANEL_WIDTH * scale) - Config.hudIndicatorPositionX;
        int y = Config.hudIndicatorAlignTop ? Config.hudIndicatorPositionY
            : resolution.getScaledHeight() - Math.round(PANEL_HEIGHT * scale) - Config.hudIndicatorPositionY;
        x = Math.max(0, x);
        y = Math.max(0, y);

        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0.0F);
        GL11.glScalef(scale, scale, 1.0F);

        drawBackground(minecraft);
        drawEntityPreview(target, mobType);
        drawFrame(minecraft);
        drawMobTypeIcon(minecraft, mobType);
        drawHealthBar(minecraft, target);
        drawName(minecraft.fontRenderer, target.getCommandSenderName());
        drawHealthText(minecraft.fontRenderer, target);

        GL11.glPopMatrix();
    }

    private void drawBackground(Minecraft minecraft) {
        minecraft.getTextureManager()
            .bindTexture(
                new ResourceLocation(VintageDamageIndicators.MODID, "textures/gui/damage_indicator_background.png"));
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, Config.hudIndicatorBackgroundOpacity);
        drawTexturedModalRect(0, 0, 0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private void drawFrame(Minecraft minecraft) {
        minecraft.getTextureManager()
            .bindTexture(new ResourceLocation(VintageDamageIndicators.MODID, "textures/gui/damage_indicator.png"));
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        drawTexturedModalRect(0, 0, 0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private void drawEntityPreview(EntityLivingBase target, MobTypes mobType) {
        PreviewSettings preview = getPreviewSettings(target, mobType);
        GuiInventory.func_147046_a(
            preview.x,
            preview.y,
            preview.scale,
            ENTITY_RENDER_YAW_INPUT,
            ENTITY_RENDER_PITCH_INPUT,
            target);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    private PreviewSettings getPreviewSettings(EntityLivingBase target, MobTypes mobType) {
        VarInstanceCommon.EntityOverride override = getEntityOverride(target);
        PreviewTuning builtIn = getBuiltInPreviewTuning(target);
        float autoScale = getAutoScale(target);
        float scale = autoScale * builtIn.scaleMultiplier;

        if (override != null) {
            if (override.scaleFactor > 0.0F) {
                scale = override.scaleFactor;
            }
            if (override.sizeModifier != 0.0F) {
                float sizeAdjustment = (3.0F - target.getEyeHeight()) * override.sizeModifier;
                scale += scale * sizeAdjustment;
            }
            if (target.isChild() && override.babyScaleModifier > 0.0F) {
                scale *= override.babyScaleModifier;
            }
        }

        int x = ENTITY_X + builtIn.xOffset;
        int y = ENTITY_Y + builtIn.yOffset;
        if (override != null) {
            x += Math.round(override.xOffset);
            y += Math.round(override.yOffset);
        }

        logPreviewSettings(target, mobType, override, builtIn, autoScale, scale, x, y);

        return new PreviewSettings(x, y, Math.max(1, Math.round(scale)));
    }

    private float getAutoScale(EntityLivingBase target) {
        float width = Math.max(target.width, 0.1F);
        float height = Math.max(target.height, 0.1F);
        float biggestDimension = Math.max(width * 1.2F + 0.3F, height * 0.9F) * 0.85F;
        float scale = Config.hudEntitySize;
        if (biggestDimension > 0.5F) {
            scale /= biggestDimension;
        }
        return scale;
    }

    private PreviewTuning getBuiltInPreviewTuning(EntityLivingBase target) {
        if (target instanceof EntityZombie) {
            return new PreviewTuning(0.82F, 0, 2);
        }
        if (target instanceof EntitySkeleton) {
            return new PreviewTuning(0.8F, 0, 3);
        }
        if (target instanceof EntityPlayer) {
            return new PreviewTuning(0.84F, 0, 4);
        }
        if (target instanceof EntityVillager) {
            return new PreviewTuning(0.88F, 0, 3);
        }
        if (target instanceof EntityWitch) {
            return new PreviewTuning(0.84F, 0, 4);
        }
        if (target instanceof EntityMooshroom) {
            return new PreviewTuning(0.9F, 0, 2);
        }
        if (target instanceof EntityChicken) {
            return new PreviewTuning(0.86F, 0, 1);
        }
        if (target instanceof EntityCreeper) {
            return new PreviewTuning(1.05F, 0, 2);
        }
        if (target instanceof EntityEnderman) {
            return new PreviewTuning(0.68F, 0, 8);
        }
        if (target instanceof EntityIronGolem) {
            return new PreviewTuning(0.78F, 0, 6);
        }
        if (target instanceof EntityWither) {
            return new PreviewTuning(0.72F, 0, 8);
        }
        if (target instanceof EntityGhast) {
            return new PreviewTuning(0.58F, 0, 2);
        }
        if (target instanceof EntitySquid) {
            return new PreviewTuning(0.9F, 0, -4);
        }
        if (target instanceof EntityOcelot) {
            return new PreviewTuning(1.1F, 0, 0);
        }
        if (target instanceof EntitySlime || target instanceof EntityMagmaCube) {
            return new PreviewTuning(0.85F, 0, 1);
        }
        return PreviewTuning.DEFAULT;
    }

    private VarInstanceCommon.EntityOverride getEntityOverride(EntityLivingBase target) {
        if (VintageDamageIndicators.varInstanceCommon == null
            || VintageDamageIndicators.varInstanceCommon.entityOverrides == null) {
            return null;
        }
        return VintageDamageIndicators.varInstanceCommon.entityOverrides.get(target.getClass());
    }

    private void drawMobTypeIcon(Minecraft minecraft, MobTypes mobType) {
        minecraft.getTextureManager()
            .bindTexture(mobType.getTexture());
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Gui.func_146110_a(5, 55, 0.0F, 0.0F, 18, 18, 18.0F, 18.0F);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private void drawHealthBar(Minecraft minecraft, EntityLivingBase target) {
        float health = Math.max(0.0F, Math.min(target.getHealth(), target.getMaxHealth()));
        float maxHealth = Math.max(target.getMaxHealth(), 0.0F);
        float ratio = maxHealth <= 0.0F ? 0.0F : health / maxHealth;
        int currentWidth = Math.round(HEALTH_BAR_WIDTH * ratio);
        int healthBarV = Config.colorblindHealthBar ? 36 : 0;

        minecraft.getTextureManager()
            .bindTexture(
                new ResourceLocation(VintageDamageIndicators.MODID, "textures/gui/damage_indicator_health.png"));
        drawTexturedModalRect(
            HEALTH_BAR_X,
            HEALTH_BAR_Y,
            0,
            healthBarV + HEALTH_BAR_HEIGHT,
            HEALTH_BAR_WIDTH,
            HEALTH_BAR_HEIGHT);
        if (currentWidth > 0) {
            drawTexturedModalRect(HEALTH_BAR_X, HEALTH_BAR_Y, 0, healthBarV, currentWidth, HEALTH_BAR_HEIGHT);
        }
    }

    private void drawName(FontRenderer fontRenderer, String name) {
        int width = fontRenderer.getStringWidth(name);
        float textScale = Math.min(1.0F, 113.0F / (float) Math.max(width, 1));
        GL11.glPushMatrix();
        GL11.glTranslatef(139.0F, 7.0F, 0.0F);
        GL11.glScalef(textScale, textScale, 1.0F);
        int scaledWidth = Math.round(width * textScale);
        int drawX = -Math.round(scaledWidth / (2.0F * textScale));
        if (Config.hudNameTextOutline) {
            fontRenderer.drawStringWithShadow(name, drawX, 0, 0xFFFFFF);
        } else {
            fontRenderer.drawString(name, drawX, 0, 0xFFFFFF);
        }
        GL11.glPopMatrix();
    }

    private void drawHealthText(FontRenderer fontRenderer, EntityLivingBase target) {
        float health = Math.max(0.0F, target.getHealth());
        float maxHealth = Math.max(0.0F, target.getMaxHealth());
        String separator = Config.healthSeparator ? " | " : "/";
        String healthText = formatHealth(health) + separator + formatHealth(maxHealth);
        if (!Config.healthDecimals) {
            healthText = (int) health + separator + (int) maxHealth;
        }

        int width = fontRenderer.getStringWidth(healthText);
        float textScale = Math.min(1.0F, 88.0F / (float) Math.max(width, 1));
        GL11.glPushMatrix();
        GL11.glTranslatef(136.0F, 30.0F, 0.0F);
        GL11.glScalef(textScale, textScale, 1.0F);
        int drawX = -Math.round(width / 2.0F);
        if (Config.hudHealthTextOutline) {
            fontRenderer.drawStringWithShadow(healthText, drawX, 0, 0xFFFFFF);
        } else {
            fontRenderer.drawString(healthText, drawX, 0, 0xFFFFFF);
        }
        GL11.glPopMatrix();
    }

    private String formatHealth(float value) {
        float rounded = Math.round(value * 5.0F) / 5.0F;
        String text = Float.toString(rounded);
        if (text.endsWith(".0")) {
            return text.substring(0, text.length() - 2);
        }
        return text;
    }

    private void logPreviewSettings(EntityLivingBase target, MobTypes mobType,
        VarInstanceCommon.EntityOverride override, PreviewTuning builtIn, float autoScale, float finalScale, int finalX,
        int finalY) {
        if (!VintageDamageIndicators.DEBUG_MODE && !Config.debugMode) {
            return;
        }
        if (target.ticksExisted % 20 != 0) {
            return;
        }

        String overrideSummary = "none";
        if (override != null) {
            overrideSummary = "scale=" + override.scaleFactor
                + ", size="
                + override.sizeModifier
                + ", baby="
                + override.babyScaleModifier
                + ", x="
                + override.xOffset
                + ", y="
                + override.yOffset;
        }

        VintageDamageIndicators.debug(
            "Preview " + target.getClass()
                .getName()
                + " type="
                + mobType
                + " bbox="
                + target.width
                + "x"
                + target.height
                + " eye="
                + target.getEyeHeight()
                + " autoScale="
                + autoScale
                + " builtIn="
                + builtIn.scaleMultiplier
                + "/"
                + builtIn.xOffset
                + "/"
                + builtIn.yOffset
                + " finalScale="
                + finalScale
                + " finalPos="
                + finalX
                + ","
                + finalY
                + " override="
                + overrideSummary);
    }

    private static class PreviewSettings {

        private final int x;
        private final int y;
        private final int scale;

        private PreviewSettings(int x, int y, int scale) {
            this.x = x;
            this.y = y;
            this.scale = scale;
        }
    }

    private static class PreviewTuning {

        private static final PreviewTuning DEFAULT = new PreviewTuning(1.0F, 0, 0);

        private final float scaleMultiplier;
        private final int xOffset;
        private final int yOffset;

        private PreviewTuning(float scaleMultiplier, int xOffset, int yOffset) {
            this.scaleMultiplier = scaleMultiplier;
            this.xOffset = xOffset;
            this.yOffset = yOffset;
        }
    }
}

package org.fentanylsolutions.vintagedamageindicators.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.WeakHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.fentanylsolutions.vintagedamageindicators.Config;
import org.fentanylsolutions.vintagedamageindicators.MobTypes;
import org.fentanylsolutions.vintagedamageindicators.VintageDamageIndicators;
import org.fentanylsolutions.vintagedamageindicators.client.HudEntityRenderer;
import org.fentanylsolutions.vintagedamageindicators.client.HudIndicatorState;
import org.fentanylsolutions.vintagedamageindicators.client.HudPreviewMath;
import org.fentanylsolutions.vintagedamageindicators.varinstances.VarInstanceCommon;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class HudEventHandler extends Gui {

    private static final int PANEL_WIDTH = HudPreviewMath.PANEL_WIDTH;
    private static final int PANEL_HEIGHT = HudPreviewMath.PANEL_HEIGHT;
    private static final int HEALTH_BAR_X = HudPreviewMath.HEALTH_BAR_X;
    private static final int HEALTH_BAR_Y = HudPreviewMath.HEALTH_BAR_Y;
    private static final int HEALTH_BAR_WIDTH = HudPreviewMath.HEALTH_BAR_WIDTH;
    private static final int HEALTH_BAR_HEIGHT = HudPreviewMath.HEALTH_BAR_HEIGHT;
    private static final int POTION_PANEL_X = HEALTH_BAR_X - 4;
    private static final int POTION_PANEL_Y = HEALTH_BAR_Y + HEALTH_BAR_HEIGHT + 2;
    private static final int POTION_PANEL_HEIGHT = 26;
    private static final int POTION_LEFT_WIDTH = 7;
    private static final int POTION_RIGHT_WIDTH = 6;
    private static final int POTION_CONTENT_PADDING_LEFT = 6;
    private static final int POTION_CONTENT_PADDING_RIGHT = 4;
    private static final int POTION_ICON_SIZE = 18;
    private static final int POTION_ICON_Y = 4;
    private static final int POTION_ENTRY_GAP = 4;
    private static final int POTION_TIME_GAP = 2;
    private static final int POTION_TIME_Y = 9;
    private static final int POTION_TIME_COLOR = 0xFFFFFF;
    private static final ResourceLocation POTION_LEFT_TEXTURE = new ResourceLocation(
        VintageDamageIndicators.MODID,
        "textures/gui/potion_left.png");
    private static final ResourceLocation POTION_LEFT_OVERLAY_TEXTURE = new ResourceLocation(
        VintageDamageIndicators.MODID,
        "textures/gui/potion_left_overlay.png");
    private static final ResourceLocation POTION_MIDDLE_TEXTURE = new ResourceLocation(
        VintageDamageIndicators.MODID,
        "textures/gui/potion_middle.png");
    private static final ResourceLocation POTION_MIDDLE_OVERLAY_TEXTURE = new ResourceLocation(
        VintageDamageIndicators.MODID,
        "textures/gui/potion_middle_overlay.png");
    private static final ResourceLocation POTION_RIGHT_TEXTURE = new ResourceLocation(
        VintageDamageIndicators.MODID,
        "textures/gui/potion_right.png");
    private static final ResourceLocation POTION_RIGHT_OVERLAY_TEXTURE = new ResourceLocation(
        VintageDamageIndicators.MODID,
        "textures/gui/potion_right_overlay.png");
    private static final ResourceLocation INVENTORY_TEXTURE = new ResourceLocation(
        "textures/gui/container/inventory.png");

    private final WeakHashMap<EntityLivingBase, PotionStripData> potionStripCache = new WeakHashMap<>();

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
        PotionStripData potionStrip = getPotionStripData(target, minecraft.fontRenderer);
        float scale = Config.hudIndicatorSize;
        int hudWidth = Math.max(PANEL_WIDTH, POTION_PANEL_X + potionStrip.stripWidth);
        int x = Config.hudIndicatorAlignLeft ? Config.hudIndicatorPositionX
            : resolution.getScaledWidth() - Math.round(hudWidth * scale) - Config.hudIndicatorPositionX;
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
        drawPotionStrip(minecraft, minecraft.fontRenderer, potionStrip);

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
        VarInstanceCommon.EntityOverride override = getEntityOverride(target);
        HudPreviewMath.PreviewSettings preview = HudPreviewMath.resolvePreviewSettings(target, override);
        logPreviewSettings(target, mobType, override, preview);
        HudEntityRenderer.drawEntity(preview.x, preview.y, preview.scale, preview.yaw, preview.pitch, target);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    private VarInstanceCommon.EntityOverride getEntityOverride(EntityLivingBase target) {
        if (VintageDamageIndicators.varInstanceCommon == null) {
            return null;
        }
        return VintageDamageIndicators.varInstanceCommon.getEntityOverride(target.getClass());
    }

    private void drawMobTypeIcon(Minecraft minecraft, MobTypes mobType) {
        minecraft.getTextureManager()
            .bindTexture(mobType.getTexture());
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Gui.func_146110_a(
            HudPreviewMath.ICON_X,
            HudPreviewMath.ICON_Y,
            0.0F,
            0.0F,
            HudPreviewMath.ICON_SIZE,
            HudPreviewMath.ICON_SIZE,
            (float) HudPreviewMath.ICON_SIZE,
            (float) HudPreviewMath.ICON_SIZE);
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
        float textScale = Math.min(1.0F, HudPreviewMath.NAME_MAX_WIDTH / (float) Math.max(width, 1));
        GL11.glPushMatrix();
        GL11.glTranslatef(HudPreviewMath.NAME_CENTER_X, HudPreviewMath.NAME_Y, 0.0F);
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
        float textScale = Math.min(1.0F, HudPreviewMath.HEALTH_TEXT_MAX_WIDTH / (float) Math.max(width, 1));
        GL11.glPushMatrix();
        GL11.glTranslatef(HudPreviewMath.HEALTH_TEXT_CENTER_X, HudPreviewMath.HEALTH_TEXT_Y, 0.0F);
        GL11.glScalef(textScale, textScale, 1.0F);
        int drawX = -Math.round(width / 2.0F);
        if (Config.hudHealthTextOutline) {
            fontRenderer.drawStringWithShadow(healthText, drawX, 0, 0xFFFFFF);
        } else {
            fontRenderer.drawString(healthText, drawX, 0, 0xFFFFFF);
        }
        GL11.glPopMatrix();
    }

    private void drawPotionStrip(Minecraft minecraft, FontRenderer fontRenderer, PotionStripData potionStrip) {
        if (potionStrip.entries.isEmpty()) {
            return;
        }

        int middleWidth = potionStrip.contentWidth;
        int rightX = POTION_PANEL_X + POTION_LEFT_WIDTH + middleWidth;

        minecraft.getTextureManager()
            .bindTexture(POTION_LEFT_TEXTURE);
        drawStandaloneTexture(POTION_PANEL_X, POTION_PANEL_Y, POTION_LEFT_WIDTH, POTION_PANEL_HEIGHT);

        if (middleWidth > 0) {
            minecraft.getTextureManager()
                .bindTexture(POTION_MIDDLE_TEXTURE);
            for (int offset = 0; offset < middleWidth; offset++) {
                drawStandaloneTexture(
                    POTION_PANEL_X + POTION_LEFT_WIDTH + offset,
                    POTION_PANEL_Y,
                    1,
                    POTION_PANEL_HEIGHT);
            }
        }

        minecraft.getTextureManager()
            .bindTexture(POTION_RIGHT_TEXTURE);
        drawStandaloneTexture(rightX, POTION_PANEL_Y, POTION_RIGHT_WIDTH, POTION_PANEL_HEIGHT);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, Config.hudIndicatorBackgroundOpacity);

        minecraft.getTextureManager()
            .bindTexture(POTION_LEFT_OVERLAY_TEXTURE);
        drawStandaloneTexture(POTION_PANEL_X, POTION_PANEL_Y, POTION_LEFT_WIDTH, POTION_PANEL_HEIGHT);

        if (middleWidth > 0) {
            minecraft.getTextureManager()
                .bindTexture(POTION_MIDDLE_OVERLAY_TEXTURE);
            for (int offset = 0; offset < middleWidth; offset++) {
                drawStandaloneTexture(
                    POTION_PANEL_X + POTION_LEFT_WIDTH + offset,
                    POTION_PANEL_Y,
                    1,
                    POTION_PANEL_HEIGHT);
            }
        }

        minecraft.getTextureManager()
            .bindTexture(POTION_RIGHT_OVERLAY_TEXTURE);
        drawStandaloneTexture(rightX, POTION_PANEL_Y, POTION_RIGHT_WIDTH, POTION_PANEL_HEIGHT);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_BLEND);

        int drawX = POTION_PANEL_X + POTION_CONTENT_PADDING_LEFT;

        for (int index = 0; index < potionStrip.entries.size(); index++) {
            PotionEntry entry = potionStrip.entries.get(index);
            int iconIndex = entry.potion.getStatusIconIndex();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            minecraft.getTextureManager()
                .bindTexture(INVENTORY_TEXTURE);
            drawTexturedModalRect(
                drawX,
                POTION_PANEL_Y + POTION_ICON_Y,
                iconIndex % 8 * 18,
                198 + iconIndex / 8 * 18,
                POTION_ICON_SIZE,
                POTION_ICON_SIZE);

            if (Config.hudPotionEffectTime) {
                fontRenderer.drawString(
                    entry.timeText,
                    drawX + POTION_ICON_SIZE + POTION_TIME_GAP,
                    POTION_PANEL_Y + POTION_TIME_Y,
                    POTION_TIME_COLOR);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
            }

            drawX += entry.width;
            if (index + 1 < potionStrip.entries.size()) {
                drawX += POTION_ENTRY_GAP;
            }
        }
    }

    private void drawStandaloneTexture(int x, int y, int width, int height) {
        Gui.func_146110_a(x, y, 0.0F, 0.0F, width, height, (float) width, (float) height);
    }

    private PotionStripData getPotionStripData(EntityLivingBase target, FontRenderer fontRenderer) {
        if (!Config.hudPotionEffectsEnabled) {
            return PotionStripData.EMPTY;
        }

        PotionStripData cached = this.potionStripCache.get(target);
        if (cached != null && cached.tick == target.ticksExisted && cached.showTime == Config.hudPotionEffectTime) {
            return cached;
        }

        Collection<?> activeEffects = target.getActivePotionEffects();
        if (activeEffects == null || activeEffects.isEmpty()) {
            PotionStripData empty = PotionStripData.empty(target.ticksExisted, Config.hudPotionEffectTime);
            this.potionStripCache.put(target, empty);
            return empty;
        }

        List<PotionEntry> entries = new ArrayList<>();
        for (Object effectObject : activeEffects) {
            if (!(effectObject instanceof PotionEffect)) {
                continue;
            }

            PotionEffect effect = (PotionEffect) effectObject;
            if (effect.getPotionID() < 0 || effect.getPotionID() >= Potion.potionTypes.length) {
                continue;
            }

            Potion potion = Potion.potionTypes[effect.getPotionID()];
            if (potion == null || !potion.hasStatusIcon()) {
                continue;
            }

            String timeText = Config.hudPotionEffectTime ? Potion.getDurationString(effect) : "";
            int entryWidth = POTION_ICON_SIZE;
            if (Config.hudPotionEffectTime) {
                entryWidth += POTION_TIME_GAP + fontRenderer.getStringWidth(timeText);
            }
            entries.add(new PotionEntry(potion, timeText, entryWidth));
        }

        if (entries.isEmpty()) {
            PotionStripData empty = PotionStripData.empty(target.ticksExisted, Config.hudPotionEffectTime);
            this.potionStripCache.put(target, empty);
            return empty;
        }

        Collections.sort(entries, Comparator.comparingInt(entry -> entry.potion.id));

        int contentWidth = POTION_CONTENT_PADDING_LEFT + POTION_CONTENT_PADDING_RIGHT;
        for (int index = 0; index < entries.size(); index++) {
            contentWidth += entries.get(index).width;
            if (index + 1 < entries.size()) {
                contentWidth += POTION_ENTRY_GAP;
            }
        }

        PotionStripData built = new PotionStripData(
            entries,
            POTION_LEFT_WIDTH + contentWidth + POTION_RIGHT_WIDTH,
            contentWidth,
            target.ticksExisted,
            Config.hudPotionEffectTime);
        this.potionStripCache.put(target, built);
        return built;
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
        VarInstanceCommon.EntityOverride override, HudPreviewMath.PreviewSettings preview) {
        if (!VintageDamageIndicators.DEBUG_MODE && !Config.debugMode) {
            return;
        }
        if (target.ticksExisted % 20 != 0) {
            return;
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
                + preview.autoScale
                + " builtIn="
                + preview.builtIn.scaleMultiplier
                + "/"
                + preview.builtIn.xOffset
                + "/"
                + preview.builtIn.yOffset
                + " finalScale="
                + preview.finalScale
                + " finalPos="
                + preview.x
                + ","
                + preview.y
                + " angle="
                + preview.yaw
                + "/"
                + preview.pitch
                + " override="
                + HudPreviewMath.describeOverride(override));
    }

    private static final class PotionStripData {

        private static final PotionStripData EMPTY = new PotionStripData(
            Collections.<PotionEntry>emptyList(),
            0,
            0,
            -1,
            false);

        private final List<PotionEntry> entries;
        private final int stripWidth;
        private final int contentWidth;
        private final int tick;
        private final boolean showTime;

        private PotionStripData(List<PotionEntry> entries, int stripWidth, int contentWidth, int tick,
            boolean showTime) {
            this.entries = entries;
            this.stripWidth = stripWidth;
            this.contentWidth = contentWidth;
            this.tick = tick;
            this.showTime = showTime;
        }

        private static PotionStripData empty(int tick, boolean showTime) {
            return new PotionStripData(Collections.<PotionEntry>emptyList(), 0, 0, tick, showTime);
        }
    }

    private static final class PotionEntry {

        private final Potion potion;
        private final String timeText;
        private final int width;

        private PotionEntry(Potion potion, String timeText, int width) {
            this.potion = potion;
            this.timeText = timeText;
            this.width = width;
        }
    }
}

package org.fentanylsolutions.vintagedamageindicators.mixins.early.minecraftforge;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiPlayerInfo;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraftforge.client.GuiIngameForge;

import org.fentanylsolutions.vintagedamageindicators.Config;
import org.fentanylsolutions.vintagedamageindicators.client.HudOverlayLayout;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiIngameForge.class, remap = false)
public abstract class MixinGuiIngameForge extends GuiIngame {

    @Unique
    private int vdi$bossBarShiftX;

    @Unique
    private int vdi$bossBarShiftY;

    @Unique
    private int vdi$playerListShiftX;

    @Unique
    private int vdi$playerListShiftY;

    protected MixinGuiIngameForge(Minecraft minecraft) {
        super(minecraft);
    }

    @Inject(method = "renderBossHealth", at = @At("HEAD"), remap = false)
    private void vdi$shiftBossBarHead(CallbackInfo ci) {
        this.vdi$bossBarShiftX = 0;
        this.vdi$bossBarShiftY = 0;
        if (BossStatus.bossName == null || BossStatus.statusBarTime <= 0 || Config.disableBossBar) {
            return;
        }

        ScaledResolution resolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
        HudOverlayLayout.OverlayRect hudRect = HudOverlayLayout.isHudVisible(this.mc)
            ? HudOverlayLayout.getConfiguredHudRect(resolution.getScaledWidth(), resolution.getScaledHeight())
            : null;
        int bossNameWidth = this.mc.fontRenderer.getStringWidth(BossStatus.bossName);
        HudOverlayLayout.BossBarLayout layout = HudOverlayLayout.resolveBossBarLayout(
            resolution.getScaledWidth(),
            resolution.getScaledHeight(),
            hudRect,
            true,
            bossNameWidth);
        int defaultLeft = resolution.getScaledWidth() / 2 - HudOverlayLayout.BOSS_BAR_WIDTH / 2;
        int defaultTop = HudOverlayLayout.BOSS_BAR_TEXT_Y;
        this.vdi$bossBarShiftX = layout.left - defaultLeft;
        this.vdi$bossBarShiftY = layout.top - defaultTop;
        if (this.vdi$bossBarShiftX != 0 || this.vdi$bossBarShiftY != 0) {
            GL11.glPushMatrix();
            GL11.glTranslatef(this.vdi$bossBarShiftX, this.vdi$bossBarShiftY, 0.0F);
        }
    }

    @Inject(method = "renderBossHealth", at = @At("RETURN"), remap = false)
    private void vdi$shiftBossBarReturn(CallbackInfo ci) {
        if (this.vdi$bossBarShiftX != 0 || this.vdi$bossBarShiftY != 0) {
            GL11.glPopMatrix();
            this.vdi$bossBarShiftX = 0;
            this.vdi$bossBarShiftY = 0;
        }
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "renderPlayerList", at = @At("HEAD"), remap = false)
    private void vdi$shiftPlayerListHead(int width, int height, CallbackInfo ci) {
        this.vdi$playerListShiftX = 0;
        this.vdi$playerListShiftY = 0;
        if (this.mc.theWorld == null || this.mc.thePlayer == null) {
            return;
        }

        NetHandlerPlayClient handler = this.mc.thePlayer.sendQueue;
        if (handler == null) {
            return;
        }

        boolean tabVisible = this.mc.gameSettings.keyBindPlayerList.getIsKeyPressed()
            && (!this.mc.isIntegratedServerRunning() || handler.playerInfoList.size() > 1
                || this.mc.theWorld.getScoreboard()
                    .func_96539_a(0) != null);
        if (!tabVisible) {
            return;
        }

        HudOverlayLayout.OverlayRect hudRect = HudOverlayLayout.isHudVisible(this.mc)
            ? HudOverlayLayout.getConfiguredHudRect(width, height)
            : null;
        int bossNameWidth = BossStatus.bossName == null ? 0 : this.mc.fontRenderer.getStringWidth(BossStatus.bossName);
        HudOverlayLayout.BossBarLayout bossLayout = HudOverlayLayout.resolveBossBarLayout(
            width,
            height,
            hudRect,
            !Config.disableBossBar && BossStatus.bossName != null && BossStatus.statusBarTime > 0,
            bossNameWidth);

        List<GuiPlayerInfo> players = (List<GuiPlayerInfo>) handler.playerInfoList;
        List<String> displayNames = new ArrayList<>(players.size());
        for (GuiPlayerInfo player : players) {
            ScorePlayerTeam team = this.mc.theWorld.getScoreboard()
                .getPlayersTeam(player.name);
            displayNames.add(ScorePlayerTeam.formatPlayerName(team, player.name));
        }

        HudOverlayLayout.PlayerListLayout layout = HudOverlayLayout.resolvePlayerListLayout(
            width,
            height,
            hudRect,
            bossLayout,
            this.mc.fontRenderer,
            displayNames,
            players.size(),
            handler.currentServerMaxPlayers);
        int defaultLeft = (width - layout.columns * layout.columnWidth) / 2;
        this.vdi$playerListShiftX = layout.left - defaultLeft;
        this.vdi$playerListShiftY = layout.top - HudOverlayLayout.PLAYER_LIST_TOP;
        if (this.vdi$playerListShiftX != 0 || this.vdi$playerListShiftY != 0) {
            GL11.glPushMatrix();
            GL11.glTranslatef(this.vdi$playerListShiftX, this.vdi$playerListShiftY, 0.0F);
        }
    }

    @Inject(method = "renderPlayerList", at = @At("RETURN"), remap = false)
    private void vdi$shiftPlayerListReturn(int width, int height, CallbackInfo ci) {
        if (this.vdi$playerListShiftX != 0 || this.vdi$playerListShiftY != 0) {
            GL11.glPopMatrix();
            this.vdi$playerListShiftX = 0;
            this.vdi$playerListShiftY = 0;
        }
    }
}

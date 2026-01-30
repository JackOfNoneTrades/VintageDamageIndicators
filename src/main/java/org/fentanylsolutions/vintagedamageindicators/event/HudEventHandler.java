package org.fentanylsolutions.vintagedamageindicators.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.fentanylsolutions.vintagedamageindicators.Config;

public class HudEventHandler {
    public static int lastMobId = 0;

    @SubscribeEvent
    public void onBossBarRender(RenderGameOverlayEvent.Pre event) {
        if (event.type == RenderGameOverlayEvent.ElementType.BOSSHEALTH && Config.disableBossBar && event.isCancelable()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (Minecraft.getMinecraft().thePlayer == null) {
            return;
        }
        if (Minecraft.getMinecraft().gameSettings.hideGUI) {
            lastMobId = 0;
            return;
        }
        if (Minecraft.getMinecraft().gameSettings.showDebugInfo && Config.hideOnDebug) {
            lastMobId = 0;
            return;
        }
        if (Minecraft.getMinecraft().currentScreen != null && !(Minecraft.getMinecraft().currentScreen instanceof GuiChat)) {
            lastMobId = 0;
            return;
        }
        if (Config.hudIndicatorEnabled) { // TODO: server permissions
            
        }
    }
}

package org.fentanylsolutions.vintagedamageindicators.gui;

import com.google.common.collect.ImmutableList;
import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import org.fentanylsolutions.vintagedamageindicators.Config;
import org.fentanylsolutions.vintagedamageindicators.VintageDamageIndicators;

import java.util.Set;

@SuppressWarnings("unused")
public class GuiFactory implements IModGuiFactory {

    @Override
    public void initialize(Minecraft minecraftInstance) {}

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return ConfigGui.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }

    public static class ConfigGui extends GuiConfig {

        private static IConfigElement ceDamageParticles = new ConfigElement(
            Config.getRawConfig()
                .getCategory(Config.Categories.damageParticles));
        private static IConfigElement ceHudIndicator = new ConfigElement(
            Config.getRawConfig()
                .getCategory(Config.Categories.hudIndicator));
        private static IConfigElement ceTypeOverrides = new ConfigElement(
            Config.getRawConfig()
                .getCategory(Config.Categories.typeOverrides));
        private static IConfigElement ceDebug = new ConfigElement(
            Config.getRawConfig()
                .getCategory(Config.Categories.debug));

        public ConfigGui(GuiScreen parentScreen) {
            super(
                parentScreen,
                ImmutableList.of(
                    // Construct directly here to prevent stale references
                    new ConfigElement(Config.getRawConfig().getCategory(Config.Categories.damageParticles)),
                    new ConfigElement(Config.getRawConfig().getCategory(Config.Categories.hudIndicator)),
                    new ConfigElement(Config.getRawConfig().getCategory(Config.Categories.typeOverrides)),
                    new ConfigElement(Config.getRawConfig().getCategory(Config.Categories.debug))
                ),
                VintageDamageIndicators.MODID,
                VintageDamageIndicators.MODID,
                false,
                false,
                I18n.format("vintagedamageindicators.configgui.title"));
        }

        @Override
        public void initGui() {
            // You can add buttons and initialize fields here
            super.initGui();
            VintageDamageIndicators.debug("Initializing config gui");
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            // You can do things like create animations, draw additional elements, etc. here
            super.drawScreen(mouseX, mouseY, partialTicks);
        }

        @Override
        protected void actionPerformed(GuiButton b) {
            VintageDamageIndicators.debug("Config button id " + b.id + " pressed");
            super.actionPerformed(b);
            /* "Done" button */
            if (b.id == 2000) {
                /* Syncing config */
                VintageDamageIndicators.debug("Saving config");
                Config.getRawConfig()
                    .save();
                Config.loadConfig(VintageDamageIndicators.confFile);
            }
        }
    }
}

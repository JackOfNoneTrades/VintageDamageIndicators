package org.fentanylsolutions.vintagedamageindicators.gui;

import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;

import org.fentanylsolutions.vintagedamageindicators.Config;
import org.fentanylsolutions.vintagedamageindicators.VintageDamageIndicators;

import com.google.common.collect.Lists;

import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.client.config.DummyConfigElement;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.config.IConfigElement;
import cpw.mods.fml.common.Loader;

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

        private static final int LOGO_PREVIEW_BUTTON_ID = 9107;
        private static final int HUD_POSITION_BUTTON_ID = 9108;
        private GuiButton logoPreviewButton;
        private GuiButton hudPositionButton;

        public ConfigGui(GuiScreen parentScreen) {
            super(
                parentScreen,
                createRootElements(),
                VintageDamageIndicators.MODID,
                VintageDamageIndicators.MODID,
                false,
                false,
                "Vintage Damage Indicators");
        }

        @Override
        public void initGui() {
            super.initGui();
            this.logoPreviewButton = new GuiButton(LOGO_PREVIEW_BUTTON_ID, 4, 4, 14, 14, "L");
            this.hudPositionButton = new GuiButton(HUD_POSITION_BUTTON_ID, 20, 4, 78, 14, "Position HUD");
            this.buttonList.add(this.logoPreviewButton);
            this.buttonList.add(this.hudPositionButton);
            VintageDamageIndicators.debug("Initializing config gui");
        }

        @Override
        protected void actionPerformed(GuiButton b) {
            if (b.id == LOGO_PREVIEW_BUTTON_ID) {
                this.mc.displayGuiScreen(new LogoPreviewScreen(this));
                return;
            }
            if (b.id == HUD_POSITION_BUTTON_ID) {
                this.mc.displayGuiScreen(new HudPositionScreen(this));
                return;
            }
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

        private static List<IConfigElement> createRootElements() {
            return Lists.newArrayList(
                new ConfigElement(
                    Config.getRawConfig()
                        .getCategory(Config.Categories.damageParticles)),
                new ConfigElement(
                    Config.getRawConfig()
                        .getCategory(Config.Categories.hudIndicator)),
                createEntityOverrideCategory(),
                new ConfigElement(
                    Config.getRawConfig()
                        .getCategory(Config.Categories.debug)));
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        private static IConfigElement createEntityOverrideCategory() {
            IConfigElement backingElement = new ConfigElement(
                Config.getRawConfig()
                    .getCategory(Config.Categories.entityOverrides));
            return new DummyConfigElement.DummyCategoryElement(
                "Entity Display Overrides",
                "vintagedamageindicators.configgui.entity-display-overrides",
                backingElement.getChildElements(),
                EntityOverrideCategoryEntry.class);
        }
    }

    public static class EntityOverrideCategoryEntry extends GuiConfigEntries.CategoryEntry {

        public EntityOverrideCategoryEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList,
            IConfigElement configElement) {
            super(owningScreen, owningEntryList, configElement);
        }

        @Override
        protected GuiScreen buildChildScreen() {
            GuiScreen customScreen = tryCreateEntityOverrideScreen(this.owningScreen);
            if (customScreen != null) {
                return customScreen;
            }
            return new GuiConfig(
                this.owningScreen,
                this.configElement.getChildElements(),
                this.owningScreen.modID,
                this.owningScreen.allRequireWorldRestart || this.configElement.requiresWorldRestart(),
                this.owningScreen.allRequireMcRestart || this.configElement.requiresMcRestart(),
                this.owningScreen.title,
                ((this.owningScreen.titleLine2 == null ? "" : this.owningScreen.titleLine2) + " > " + this.name));
        }
    }

    private static GuiScreen tryCreateEntityOverrideScreen(GuiScreen parentScreen) {
        if (!Loader.isModLoaded("modularui2")) {
            return null;
        }
        try {
            Class<?> factoryClass = Class
                .forName("org.fentanylsolutions.vintagedamageindicators.gui.mui.EntityOverrideEditorScreenFactory");
            return (GuiScreen) factoryClass.getMethod("create", GuiScreen.class)
                .invoke(null, parentScreen);
        } catch (ReflectiveOperationException | LinkageError e) {
            VintageDamageIndicators.LOG.warn(
                "Failed to open the ModularUI2 entity override editor. Falling back to the default config screen.",
                e);
            return null;
        }
    }
}

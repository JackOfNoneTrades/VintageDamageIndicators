package org.fentanylsolutions.vintagedamageindicators.client;

import java.util.List;

import net.minecraft.client.gui.FontRenderer;

import cpw.mods.fml.common.Loader;

public final class TabFacesCompatHelper {

    private static final boolean TAB_FACES_LOADED = Loader.isModLoaded("tabfaces");

    private TabFacesCompatHelper() {}

    public static boolean isLoaded() {
        return TAB_FACES_LOADED;
    }

    public static boolean shouldTrimTabMenu() {
        return TAB_FACES_LOADED && TabFacesBridge.shouldTrimTabMenu();
    }

    public static int adjustColumnWidth(int baseWidth, FontRenderer fontRenderer, List<String> displayNames) {
        if (!TAB_FACES_LOADED) {
            return baseWidth;
        }
        return TabFacesBridge.adjustColumnWidth(baseWidth, fontRenderer, displayNames);
    }

    private static final class TabFacesBridge {

        private TabFacesBridge() {}

        private static boolean shouldTrimTabMenu() {
            return org.fentanylsolutions.tabfaces.Config.trimTabMenu;
        }

        private static int adjustColumnWidth(int baseWidth, FontRenderer fontRenderer, List<String> displayNames) {
            if (!org.fentanylsolutions.tabfaces.Config.trimTabMenu || fontRenderer == null || displayNames == null) {
                return baseWidth;
            }

            int maxNameWidth = 0;
            for (String displayName : displayNames) {
                if (displayName != null) {
                    maxNameWidth = Math.max(maxNameWidth, fontRenderer.getStringWidth(displayName));
                }
            }

            int faceWidth = org.fentanylsolutions.tabfaces.Config.enableFacesInTabMenu
                ? org.fentanylsolutions.tabfaces.util.ClientUtil.faceWidth
                : 0;
            int measuredWidth = maxNameWidth + faceWidth
                + 12
                + 5
                + org.fentanylsolutions.tabfaces.Config.trimTabMenuExtraWidth;
            return Math.max(
                baseWidth,
                Math.min(measuredWidth, org.fentanylsolutions.tabfaces.Config.trimTabMenuMaxColumnWidth));
        }
    }
}

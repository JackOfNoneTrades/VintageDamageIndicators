package org.fentanylsolutions.vintagedamageindicators.gui.mui;

import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.fentanylsolutions.vintagedamageindicators.Config;
import org.fentanylsolutions.vintagedamageindicators.MobTypes;
import org.fentanylsolutions.vintagedamageindicators.VintageDamageIndicators;
import org.fentanylsolutions.vintagedamageindicators.client.HudEntityRenderer;
import org.fentanylsolutions.vintagedamageindicators.client.HudPreviewMath;
import org.fentanylsolutions.vintagedamageindicators.varinstances.VarInstanceCommon;
import org.lwjgl.opengl.GL11;

import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.IValue;
import com.cleanroommc.modularui.screen.GuiScreenWrapper;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.fakeworld.DummyWorld;
import com.cleanroommc.modularui.value.IntValue;
import com.cleanroommc.modularui.value.ObjectValue;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.menu.DropdownWidget;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class EntityOverrideEditorScreenFactory {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.###");
    private static final int PREFERRED_PANEL_WIDTH = 382;
    private static final int PREFERRED_PANEL_HEIGHT = 288;
    private static final int OUTER_PADDING = 6;
    private static final int CONTENT_TOP = 24;
    private static final int COLUMN_GAP = 8;
    private static final int SECTION_GAP = 4;
    private static final int MOB_LIST_WIDTH = 132;
    private static final int PREVIEW_COLUMN_WIDTH = 220;
    private static final int PREVIEW_SECTION_HEIGHT = 78;
    private static final int FIXED_EDITOR_HEIGHT = 20;
    private static final int LEFT_FOOTER_HEIGHT = 40;
    private static final int LABEL_WIDTH = 104;
    private static final int FIELD_WIDTH = 100;
    private static final int LEFT_LABEL_WIDTH = 40;
    private static final int LEFT_FIELD_WIDTH = 88;
    private static final int ROW_CHILD_PADDING = 4;
    private static final int PREVIEW_CHILD_WIDTH = 72;
    private static final int TYPE_DROPDOWN_WIDTH = PREVIEW_COLUMN_WIDTH - PREVIEW_CHILD_WIDTH - ROW_CHILD_PADDING;
    private static final int CONTROL_ROW_HEIGHT = 18;

    private EntityOverrideEditorScreenFactory() {}

    public static GuiScreen create(GuiScreen parentScreen) {
        EditorState state = new EditorState();
        int panelHeight = resolvePanelHeight(parentScreen);
        ModularScreen screen = new ModularScreen(
            VintageDamageIndicators.MODID,
            context -> buildMainPanel(state, panelHeight)).pausesGame(true);
        screen.getContext()
            .setSettings(new UISettings());
        screen.openParentOnClose(true);
        return new GuiScreenWrapper(screen) {

            @Override
            public void drawWorldBackground(int tint) {
                if (this.mc != null && this.mc.theWorld != null) {
                    this.drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680);
                } else {
                    this.drawBackground(tint);
                }
            }
        };
    }

    private static int resolvePanelHeight(GuiScreen parentScreen) {
        int availableHeight = 0;
        if (parentScreen != null && parentScreen.height > 0) {
            availableHeight = parentScreen.height;
        }
        if (availableHeight <= 0) {
            Minecraft minecraft = Minecraft.getMinecraft();
            ScaledResolution resolution = new ScaledResolution(
                minecraft,
                minecraft.displayWidth,
                minecraft.displayHeight);
            availableHeight = resolution.getScaledHeight();
        }
        return Math.max(220, Math.min(PREFERRED_PANEL_HEIGHT, availableHeight - 40));
    }

    private static ModularPanel buildMainPanel(EditorState state, int panelHeight) {
        int rightColumnLeft = OUTER_PADDING + MOB_LIST_WIDTH + COLUMN_GAP;

        ModularPanel panel = ModularPanel.defaultPanel("entity_override_editor", PREFERRED_PANEL_WIDTH, panelHeight)
            .padding(6)
            .onCloseAction(state::save);

        panel.child(ButtonWidget.panelCloseButton());
        panel.child(
            IKey.dynamic(state::getWindowTitle)
                .asWidget()
                .top(6)
                .left(18)
                .right(18)
                .height(12)
                .scale(0.75F)
                .textAlign(Alignment.Center));

        ListWidget mobList = buildMobList(state);
        mobList.left(OUTER_PADDING);
        mobList.top(CONTENT_TOP);
        mobList.width(MOB_LIST_WIDTH);
        mobList.bottom(LEFT_FOOTER_HEIGHT + SECTION_GAP + 6);
        panel.child(mobList);

        Flow leftFooter = buildLeftFooter(state);
        leftFooter.left(OUTER_PADDING);
        leftFooter.bottom(6);
        leftFooter.width(MOB_LIST_WIDTH);
        leftFooter.height(LEFT_FOOTER_HEIGHT);
        panel.child(leftFooter);

        ListWidget previewPane = buildPreviewPane(state);
        previewPane.left(rightColumnLeft);
        previewPane.top(CONTENT_TOP);
        previewPane.width(PREVIEW_COLUMN_WIDTH);
        previewPane.height(PREVIEW_SECTION_HEIGHT);
        panel.child(previewPane);

        Flow fixedPane = buildFixedEditorPane(state);
        fixedPane.left(rightColumnLeft);
        fixedPane.top(CONTENT_TOP + PREVIEW_SECTION_HEIGHT + SECTION_GAP);
        fixedPane.width(PREVIEW_COLUMN_WIDTH);
        fixedPane.height(FIXED_EDITOR_HEIGHT);
        panel.child(fixedPane);

        ListWidget formPane = buildEditorList(state);
        formPane.left(rightColumnLeft);
        formPane.top(CONTENT_TOP + PREVIEW_SECTION_HEIGHT + FIXED_EDITOR_HEIGHT + SECTION_GAP * 2);
        formPane.width(PREVIEW_COLUMN_WIDTH);
        formPane.bottom(6);
        panel.child(formPane);

        return panel;
    }

    private static Flow buildLeftFooter(EditorState state) {
        Flow column = Flow.column()
            .childPadding(2);

        column.child(buildCompactTextRow("Yaw", compactTextField(state.previewYawValue(), 1.0D)));
        column.child(buildCompactTextRow("Pitch", compactTextField(state.previewPitchValue(), 1.0D)));
        return column;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static ListWidget buildMobList(EditorState state) {
        ListWidget list = new ListWidget();
        list.child(
            IKey.str("Mobs")
                .asWidget()
                .marginBottom(4));

        if (state.options.isEmpty()) {
            list.child(
                IKey.str("No editable mobs found.")
                    .asWidget());
            return list;
        }

        list.children(state.options, option -> {
            ButtonWidget button = new ButtonWidget();
            button.widthRel(1.0F);
            button.height(18);
            button.marginBottom(1);
            button.overlay(
                IKey.dynamic(() -> state.getListLabel((EntityOption) option))
                    .alignment(Alignment.CenterLeft));
            button.onMousePressed(mouseButton -> {
                if (mouseButton == 0 || mouseButton == 1) {
                    state.select((EntityOption) option);
                    return true;
                }
                return false;
            });
            return button;
        });
        return list;
    }

    @SuppressWarnings("rawtypes")
    private static ListWidget buildPreviewPane(EditorState state) {
        ListWidget list = new ListWidget();

        if (state.options.isEmpty()) {
            list.child(
                IKey.str("No entities are available for editing.")
                    .asWidget());
            return list;
        }

        list.child(
            new PreviewDrawable(state).asWidget()
                .width(HudPreviewMath.PANEL_WIDTH)
                .height(HudPreviewMath.PANEL_HEIGHT));
        return list;
    }

    private static Flow buildFixedEditorPane(EditorState state) {
        Flow column = Flow.column()
            .childPadding(2);

        if (state.options.isEmpty()) {
            column.child(
                IKey.str("No entities are available for editing.")
                    .asWidget());
            return column;
        }

        column.child(buildPreviewTypeRow(state));
        return column;
    }

    @SuppressWarnings("rawtypes")
    private static ListWidget buildEditorList(EditorState state) {
        ListWidget list = new ListWidget();
        list.paddingRight(8);

        if (state.options.isEmpty()) {
            list.child(
                IKey.str("No entities are available for editing.")
                    .asWidget());
            return list;
        }

        list.child(buildToggleRow("Enabled", state.enabledValue(), state::getEnabledLabel));
        list.child(buildToggleRow("Append Baby Name", state.appendBabyNameValue(), state::getAppendBabyNameLabel));
        list.child(buildTextRow("Name", textField(state.selectedNameValue(), false, 0.0D)));

        list.child(buildTextRow("Adult Scale Mult", textField(state.scaleValue(), true, 0.05D)));
        list.child(buildTextRow("Adult X Offset", textField(state.xOffsetValue(), true, 0.25D)));
        list.child(buildTextRow("Adult Y Offset", textField(state.yOffsetValue(), true, 0.25D)));
        list.child(buildTextRow("Baby Scale Mult", textField(state.babyScaleValue(), true, 0.05D)));
        list.child(buildTextRow("Baby X Offset", textField(state.babyXOffsetValue(), true, 0.25D)));
        list.child(buildTextRow("Baby Y Offset", textField(state.babyYOffsetValue(), true, 0.25D)));
        list.child(buildTextRow("Baby Fallback Mult", textField(state.babyScaleModifierValue(), true, 0.05D)));
        list.child(buildTextRow("Yaw Offset", textField(state.yawOffsetValue(), true, 1.0D)));
        list.child(buildTextRow("Pitch Offset", textField(state.pitchOffsetValue(), true, 1.0D)));

        list.child(buildTextRow("Size Modifier", textField(state.sizeModifierValue(), true, 0.05D)));
        ButtonWidget resetButton = new ButtonWidget();
        resetButton.width(LABEL_WIDTH + FIELD_WIDTH + ROW_CHILD_PADDING);
        resetButton.height(18);
        resetButton.marginTop(4);
        resetButton.overlay(IKey.str("Reset Selected Override"));
        resetButton.onMousePressed(mouseButton -> {
            if (mouseButton == 0 || mouseButton == 1) {
                state.resetSelectedOverride();
                return true;
            }
            return false;
        });
        list.child(resetButton);
        return list;
    }

    private static Row buildPreviewTypeRow(EditorState state) {
        RefreshingDropdownWidget<MobTypes> dropdown = new RefreshingDropdownWidget<>(
            "mob_type_dropdown",
            MobTypes.class).width(TYPE_DROPDOWN_WIDTH)
                .height(18)
                .value(state.typeValue())
                .options(MobTypes.values())
                .maxVerticalMenuSize(160)
                .directionDown()
                .optionToWidget((type, forSelectedDisplay) -> buildDropdownLabel(formatMobType(type)));

        CycleButtonWidget previewToggle = new CycleButtonWidget().stateCount(2)
            .width(PREVIEW_CHILD_WIDTH)
            .height(18)
            .value(state.previewChildValue())
            .overlay(IKey.dynamic(state::getPreviewChildLabel));

        Row row = new Row();
        row.widthRel(1.0F);
        row.height(CONTROL_ROW_HEIGHT);
        row.childPadding(ROW_CHILD_PADDING);
        row.marginBottom(2);
        row.child(previewToggle);
        row.child(dropdown);
        return row;
    }

    private static Row buildTextRow(String label, TextFieldWidget field) {
        Row row = new Row();
        row.widthRel(1.0F);
        row.height(CONTROL_ROW_HEIGHT);
        row.childPadding(ROW_CHILD_PADDING);
        row.marginBottom(2);
        row.child(
            IKey.str(label)
                .asWidget()
                .width(LABEL_WIDTH)
                .heightRel(1.0F));
        row.child(field);
        return row;
    }

    private static Row buildCompactTextRow(String label, TextFieldWidget field) {
        Row row = new Row();
        row.widthRel(1.0F);
        row.height(CONTROL_ROW_HEIGHT);
        row.childPadding(ROW_CHILD_PADDING);
        row.marginBottom(2);
        row.child(
            IKey.str(label)
                .asWidget()
                .width(LEFT_LABEL_WIDTH)
                .heightRel(1.0F));
        row.child(field);
        return row;
    }

    private static Row buildToggleRow(String label, IntValue.Dynamic value, ToggleLabel labelSupplier) {
        CycleButtonWidget toggle = new CycleButtonWidget().stateCount(2)
            .width(FIELD_WIDTH)
            .height(18)
            .value(value)
            .overlay(IKey.dynamic(labelSupplier::get));

        Row row = new Row();
        row.widthRel(1.0F);
        row.height(CONTROL_ROW_HEIGHT);
        row.childPadding(ROW_CHILD_PADDING);
        row.marginBottom(2);
        row.child(
            IKey.str(label)
                .asWidget()
                .width(LABEL_WIDTH)
                .heightRel(1.0F));
        row.child(toggle);
        return row;
    }

    private static TextFieldWidget textField(StringValue.Dynamic value, boolean numeric, double baseScroll) {
        TextFieldWidget field = new TextFieldWidget().width(FIELD_WIDTH)
            .height(16)
            .value(value)
            .autoUpdateOnChange(true)
            .setTextAlignment(Alignment.CenterLeft);

        if (numeric) {
            field.setNumbersDouble(number -> number)
                .setDefaultNumber(0.0D)
                .setScrollValues(baseScroll, baseScroll * 0.2D, Math.max(baseScroll * 4.0D, 1.0D));
        }

        return field;
    }

    private static TextFieldWidget compactTextField(StringValue.Dynamic value, double baseScroll) {
        return new TextFieldWidget().width(LEFT_FIELD_WIDTH)
            .height(16)
            .value(value)
            .autoUpdateOnChange(true)
            .setTextAlignment(Alignment.CenterLeft)
            .setNumbersDouble(number -> number)
            .setDefaultNumber(0.0D)
            .setScrollValues(baseScroll, baseScroll * 0.2D, Math.max(baseScroll * 4.0D, 1.0D));
    }

    private static Flow buildDropdownLabel(String text) {
        TextWidget<?> label = IKey.str(text)
            .asWidget()
            .widgetTheme(IThemeApi.BUTTON)
            .textAlign(Alignment.CenterLeft);

        return Flow.row()
            .widthRel(1.0F)
            .height(18)
            .padding(4, 1)
            .child(label);
    }

    private static String formatMobType(MobTypes type) {
        String[] parts = type.name()
            .toLowerCase(Locale.ROOT)
            .split("_");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                builder.append(' ');
            }
            if (!parts[i].isEmpty()) {
                builder.append(Character.toUpperCase(parts[i].charAt(0)));
                if (parts[i].length() > 1) {
                    builder.append(parts[i].substring(1));
                }
            }
        }
        return builder.toString();
    }

    private static String formatFloat(float value) {
        synchronized (DECIMAL_FORMAT) {
            return DECIMAL_FORMAT.format(value);
        }
    }

    private static float parseFloat(String value) {
        if (value == null || value.trim()
            .isEmpty()) {
            return 0.0F;
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return 0.0F;
        }
    }

    private interface ToggleLabel {

        String get();
    }

    private static final class RefreshingDropdownWidget<T> extends DropdownWidget<T, RefreshingDropdownWidget<T>> {

        private IValue<T> trackedValue;
        private T lastSeenValue;

        private RefreshingDropdownWidget(String panelName, Class<T> valueType) {
            super(panelName, valueType);
        }

        @Override
        public RefreshingDropdownWidget<T> value(IValue<T> value) {
            this.trackedValue = value;
            this.lastSeenValue = value.getValue();
            return super.value(value);
        }

        @Override
        public void onUpdate() {
            super.onUpdate();
            if (this.trackedValue == null) {
                return;
            }
            T currentValue = this.trackedValue.getValue();
            if (!Objects.equals(this.lastSeenValue, currentValue)) {
                setValue(currentValue, false);
                this.lastSeenValue = currentValue;
            }
        }

        @Override
        public Result onMousePressed(int mouseButton) {
            deleteMenu();
            return super.onMousePressed(mouseButton);
        }
    }

    private static final class PreviewDrawable extends Gui implements IDrawable {

        private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(
            VintageDamageIndicators.MODID,
            "textures/gui/damage_indicator_background.png");
        private static final ResourceLocation FRAME_TEXTURE = new ResourceLocation(
            VintageDamageIndicators.MODID,
            "textures/gui/damage_indicator.png");
        private static final ResourceLocation HEALTH_TEXTURE = new ResourceLocation(
            VintageDamageIndicators.MODID,
            "textures/gui/damage_indicator_health.png");

        private final EditorState state;

        private PreviewDrawable(EditorState state) {
            this.state = state;
        }

        @Override
        public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
            int drawX = x + Math.max(0, (width - HudPreviewMath.PANEL_WIDTH) / 2);
            int drawY = y;

            GL11.glPushMatrix();
            GL11.glTranslatef(drawX, drawY, 0.0F);

            Minecraft minecraft = Minecraft.getMinecraft();
            EntityLivingBase entity = this.state.getPreviewEntity();
            VarInstanceCommon.EntityOverride override = this.state.getPreviewOverride();
            MobTypes mobType = this.state.getPreviewMobType(entity);

            drawBackground(minecraft);
            if (entity != null) {
                HudPreviewMath.PreviewSettings preview = HudPreviewMath
                    .resolvePreviewSettings(entity, override, this.state.previewYaw, this.state.previewPitch);
                drawEntityPreview(entity, preview);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
            }
            drawFrame(minecraft);
            drawMobTypeIcon(minecraft, mobType);
            drawHealthBar(minecraft, entity);
            drawName(minecraft.fontRenderer, this.state.getPreviewName());
            drawHealthText(minecraft.fontRenderer, entity);

            GL11.glPopMatrix();
        }

        private void drawEntityPreview(EntityLivingBase entity, HudPreviewMath.PreviewSettings preview) {
            HudEntityRenderer.drawEntity(preview.x, preview.y, preview.scale, preview.yaw, preview.pitch, entity);
        }

        private void drawBackground(Minecraft minecraft) {
            minecraft.getTextureManager()
                .bindTexture(BACKGROUND_TEXTURE);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, Config.hudIndicatorBackgroundOpacity);
            drawTexturedModalRect(0, 0, 0, 0, HudPreviewMath.PANEL_WIDTH, HudPreviewMath.PANEL_HEIGHT);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glDisable(GL11.GL_BLEND);
        }

        private void drawFrame(Minecraft minecraft) {
            minecraft.getTextureManager()
                .bindTexture(FRAME_TEXTURE);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            drawTexturedModalRect(0, 0, 0, 0, HudPreviewMath.PANEL_WIDTH, HudPreviewMath.PANEL_HEIGHT);
            GL11.glDisable(GL11.GL_BLEND);
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

        private void drawHealthBar(Minecraft minecraft, EntityLivingBase entity) {
            float health = entity == null ? 0.0F : Math.max(0.0F, Math.min(entity.getHealth(), entity.getMaxHealth()));
            float maxHealth = entity == null ? 0.0F : Math.max(entity.getMaxHealth(), 0.0F);
            float ratio = maxHealth <= 0.0F ? 0.0F : health / maxHealth;
            int currentWidth = Math.round(HudPreviewMath.HEALTH_BAR_WIDTH * ratio);
            int healthBarV = Config.colorblindHealthBar ? 36 : 0;

            minecraft.getTextureManager()
                .bindTexture(HEALTH_TEXTURE);
            drawTexturedModalRect(
                HudPreviewMath.HEALTH_BAR_X,
                HudPreviewMath.HEALTH_BAR_Y,
                0,
                healthBarV + HudPreviewMath.HEALTH_BAR_HEIGHT,
                HudPreviewMath.HEALTH_BAR_WIDTH,
                HudPreviewMath.HEALTH_BAR_HEIGHT);
            if (currentWidth > 0) {
                drawTexturedModalRect(
                    HudPreviewMath.HEALTH_BAR_X,
                    HudPreviewMath.HEALTH_BAR_Y,
                    0,
                    healthBarV,
                    currentWidth,
                    HudPreviewMath.HEALTH_BAR_HEIGHT);
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

        private void drawHealthText(FontRenderer fontRenderer, EntityLivingBase entity) {
            String healthText = "--";
            if (entity != null) {
                float health = Math.max(0.0F, entity.getHealth());
                float maxHealth = Math.max(0.0F, entity.getMaxHealth());
                String separator = Config.healthSeparator ? " | " : "/";
                healthText = formatHealth(health) + separator + formatHealth(maxHealth);
                if (!Config.healthDecimals) {
                    healthText = (int) health + separator + (int) maxHealth;
                }
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

        private String formatHealth(float value) {
            float rounded = Math.round(value * 5.0F) / 5.0F;
            String text = Float.toString(rounded);
            if (text.endsWith(".0")) {
                return text.substring(0, text.length() - 2);
            }
            return text;
        }
    }

    private static final class EditorState {

        private final List<EntityOption> options;
        private final Map<String, VarInstanceCommon.EntityOverride> overridesByClassName = new LinkedHashMap<>();
        private final List<String> preservedEntries = new ArrayList<>();
        private int selectedIndex;
        private boolean previewChild;
        private float previewYaw = Config.hudEntityYaw;
        private float previewPitch = Config.hudEntityPitch;
        private EntityLivingBase previewEntity;
        private String previewEntityClassName;

        private EditorState() {
            this.options = buildOptions();
            loadExistingOverrides();
        }

        private List<EntityOption> buildOptions() {
            List<String> names = new ArrayList<>();
            for (Object key : EntityList.stringToClassMapping.keySet()) {
                names.add(String.valueOf(key));
            }
            Collections.sort(names, String.CASE_INSENSITIVE_ORDER);

            Map<String, EntityOption> optionsByClassName = new LinkedHashMap<>();
            for (String registryName : names) {
                Object clsObject = EntityList.stringToClassMapping.get(registryName);
                if (!(clsObject instanceof Class)) {
                    continue;
                }
                Class<?> entityClass = (Class<?>) clsObject;
                if (!EntityLivingBase.class.isAssignableFrom(entityClass)
                    || Modifier.isAbstract(entityClass.getModifiers())) {
                    continue;
                }
                String className = entityClass.getName();
                if (!optionsByClassName.containsKey(className)) {
                    optionsByClassName.put(className, new EntityOption(registryName, className));
                }
            }
            return new ArrayList<>(optionsByClassName.values());
        }

        private void loadExistingOverrides() {
            Set<String> editableClassNames = new java.util.HashSet<>();
            for (EntityOption option : this.options) {
                editableClassNames.add(option.className);
            }

            if (VintageDamageIndicators.varInstanceCommon != null) {
                for (Map.Entry<String, VarInstanceCommon.EntityOverride> entry : VintageDamageIndicators.varInstanceCommon
                    .copyEntityOverridesByClassName()
                    .entrySet()) {
                    if (editableClassNames.contains(entry.getKey())) {
                        this.overridesByClassName.put(entry.getKey(), entry.getValue());
                    } else {
                        addPreservedEntry(
                            entry.getValue()
                                .serialize());
                    }
                }
                for (String preserved : VintageDamageIndicators.varInstanceCommon
                    .copyPreservedEntityOverrideEntries()) {
                    addPreservedEntry(preserved);
                }
                return;
            }

            for (String serialized : Config.entityOverrides) {
                if (serialized == null || serialized.trim()
                    .isEmpty()) {
                    continue;
                }
                try {
                    VarInstanceCommon.EntityOverride override = VarInstanceCommon.EntityOverride
                        .deserialize(serialized);
                    if (override.className == null || override.className.isEmpty()) {
                        addPreservedEntry(serialized);
                    } else if (editableClassNames.contains(override.className)) {
                        this.overridesByClassName.put(override.className, override.copy());
                    } else {
                        addPreservedEntry(serialized);
                    }
                } catch (RuntimeException e) {
                    addPreservedEntry(serialized);
                }
            }
        }

        private void save() {
            flushChanges();
            Config.save();
        }

        private void flushChanges() {
            Config.setHudPreviewAngles(this.previewYaw, this.previewPitch);

            if (VintageDamageIndicators.varInstanceCommon != null) {
                VintageDamageIndicators.varInstanceCommon
                    .replaceEntityOverrides(this.overridesByClassName, this.preservedEntries);
            } else {
                List<String> serialized = new ArrayList<>();
                for (EntityOption option : this.options) {
                    VarInstanceCommon.EntityOverride override = this.overridesByClassName.get(option.className);
                    if (override != null) {
                        serialized.add(override.serialize());
                    }
                }
                serialized.addAll(this.preservedEntries);
                Config.setEntityOverrides(serialized.toArray(new String[0]));
            }
        }

        private void addPreservedEntry(String serialized) {
            if (serialized != null && !this.preservedEntries.contains(serialized)) {
                this.preservedEntries.add(serialized);
            }
        }

        private String getListLabel(EntityOption option) {
            String label = getDisplayName(option);
            if (isSelected(option)) {
                return "> " + label;
            }
            return label;
        }

        private String getSelectedHeader() {
            return "Editing: " + getDisplayName(getSelectedOption());
        }

        private String getSelectedClassLabel() {
            return getSelectedOption().className;
        }

        private String getWindowTitle() {
            EntityOption option = getSelectedOption();
            return getDisplayName(option) + " | " + getSimpleClassName(option.className);
        }

        private String getSimpleClassName(String className) {
            int separator = className.lastIndexOf('.');
            return separator >= 0 ? className.substring(separator + 1) : className;
        }

        private String getDisplayName(EntityOption option) {
            VarInstanceCommon.EntityOverride override = this.overridesByClassName.get(option.className);
            if (override != null && override.displayName != null
                && !override.displayName.trim()
                    .isEmpty()) {
                return override.displayName;
            }
            return option.registryName;
        }

        private boolean isSelected(EntityOption option) {
            return this.options.indexOf(option) == this.selectedIndex;
        }

        private void select(EntityOption option) {
            int index = this.options.indexOf(option);
            if (index >= 0) {
                this.selectedIndex = index;
                this.previewEntity = null;
                this.previewEntityClassName = null;
            }
        }

        private EntityOption getSelectedOption() {
            if (this.options.isEmpty()) {
                return new EntityOption("Unavailable", "");
            }
            this.selectedIndex = Math.max(0, Math.min(this.selectedIndex, this.options.size() - 1));
            return this.options.get(this.selectedIndex);
        }

        private VarInstanceCommon.EntityOverride getExistingSelectedOverride() {
            return this.overridesByClassName.get(getSelectedOption().className);
        }

        private VarInstanceCommon.EntityOverride getPreviewOverride() {
            return getExistingSelectedOverride();
        }

        private VarInstanceCommon.EntityOverride getSelectedOverrideOrDefault() {
            VarInstanceCommon.EntityOverride override = getExistingSelectedOverride();
            if (override != null) {
                return override;
            }
            VarInstanceCommon.EntityOverride defaults = new VarInstanceCommon.EntityOverride();
            defaults.className = getSelectedOption().className;
            defaults.displayName = getSelectedOption().registryName;
            return defaults;
        }

        private VarInstanceCommon.EntityOverride getOrCreateSelectedOverride() {
            EntityOption option = getSelectedOption();
            VarInstanceCommon.EntityOverride override = this.overridesByClassName.get(option.className);
            if (override == null) {
                override = new VarInstanceCommon.EntityOverride();
                override.className = option.className;
                override.displayName = option.registryName;
                override.type = getDetectedTypeForSelectedOption();
                this.overridesByClassName.put(option.className, override);
            }
            return override;
        }

        private void resetSelectedOverride() {
            this.overridesByClassName.remove(getSelectedOption().className);
            flushChanges();
        }

        private String getPreviewName() {
            String name = getDisplayName(getSelectedOption());
            if (this.previewChild && getSelectedOverrideOrDefault().appendBabyName) {
                return "Baby " + name;
            }
            return name;
        }

        private MobTypes getPreviewMobType(EntityLivingBase entity) {
            VarInstanceCommon.EntityOverride override = getExistingSelectedOverride();
            if (override != null) {
                return override.type;
            }
            if (entity != null) {
                return MobTypes.getTypeFor(entity);
            }
            return MobTypes.UNKNOWN;
        }

        private EntityLivingBase getPreviewEntity() {
            EntityOption option = getSelectedOption();
            if (option.className.isEmpty()) {
                return null;
            }

            Minecraft minecraft = Minecraft.getMinecraft();
            if (this.previewEntity == null || !option.className.equals(this.previewEntityClassName)) {
                this.previewEntity = createPreviewEntity(option, minecraft);
                this.previewEntityClassName = this.previewEntity == null ? null : option.className;
            }

            applyPreviewChildState(this.previewEntity);
            return this.previewEntity;
        }

        private EntityLivingBase createPreviewEntity(EntityOption option, Minecraft minecraft) {
            World world = minecraft.theWorld;
            if (world == null && minecraft.thePlayer != null) {
                world = minecraft.thePlayer.worldObj;
            }
            if (world == null) {
                world = DummyWorld.INSTANCE;
            }

            Object created = EntityList.createEntityByName(option.registryName, world);
            if (created instanceof EntityLivingBase) {
                EntityLivingBase entity = (EntityLivingBase) created;
                entity.setPosition(0.0D, 0.0D, 0.0D);
                return entity;
            }
            return null;
        }

        private void applyPreviewChildState(EntityLivingBase entity) {
            if (entity == null) {
                return;
            }
            if (entity instanceof EntityAgeable) {
                ((EntityAgeable) entity).setGrowingAge(this.previewChild ? -24000 : 0);
            } else if (entity instanceof EntityZombie) {
                ((EntityZombie) entity).setChild(this.previewChild);
            }
        }

        private String getPreviewChildLabel() {
            return this.previewChild ? "Child" : "Adult";
        }

        private boolean isPreviewChildActive() {
            return this.previewChild;
        }

        private String getEnabledLabel() {
            return getSelectedOverrideOrDefault().enable ? "Enabled" : "Disabled";
        }

        private String getAppendBabyNameLabel() {
            return getSelectedOverrideOrDefault().appendBabyName ? "Yes" : "No";
        }

        private IntValue.Dynamic previewChildValue() {
            return new IntValue.Dynamic(() -> this.previewChild ? 1 : 0, value -> this.previewChild = value != 0);
        }

        private IntValue.Dynamic enabledValue() {
            return new IntValue.Dynamic(() -> getSelectedOverrideOrDefault().enable ? 1 : 0, value -> {
                getOrCreateSelectedOverride().enable = value != 0;
                flushChanges();
            });
        }

        private IntValue.Dynamic appendBabyNameValue() {
            return new IntValue.Dynamic(() -> getSelectedOverrideOrDefault().appendBabyName ? 1 : 0, value -> {
                getOrCreateSelectedOverride().appendBabyName = value != 0;
                flushChanges();
            });
        }

        private IValue<MobTypes> typeValue() {
            return new ObjectValue.Dynamic<>(MobTypes.class, () -> {
                VarInstanceCommon.EntityOverride override = getExistingSelectedOverride();
                if (override != null) {
                    return override.type;
                }
                return getDetectedTypeForSelectedOption();
            }, value -> {
                getOrCreateSelectedOverride().type = value == null ? MobTypes.UNKNOWN : value;
                flushChanges();
            });
        }

        private MobTypes getDetectedTypeForSelectedOption() {
            EntityLivingBase entity = getPreviewEntity();
            return entity == null ? MobTypes.UNKNOWN : MobTypes.getTypeFor(entity);
        }

        private StringValue.Dynamic selectedNameValue() {
            return new StringValue.Dynamic(
                () -> getSelectedOverrideOrDefault().displayName == null ? ""
                    : getSelectedOverrideOrDefault().displayName,
                value -> {
                    getOrCreateSelectedOverride().displayName = value;
                    flushChanges();
                });
        }

        private StringValue.Dynamic scaleValue() {
            return floatValue(
                () -> getSelectedOverrideOrDefault().scaleFactor,
                value -> getOrCreateSelectedOverride().scaleFactor = value);
        }

        private StringValue.Dynamic xOffsetValue() {
            return floatValue(
                () -> getSelectedOverrideOrDefault().xOffset,
                value -> getOrCreateSelectedOverride().xOffset = value);
        }

        private StringValue.Dynamic yOffsetValue() {
            return floatValue(
                () -> getSelectedOverrideOrDefault().yOffset,
                value -> getOrCreateSelectedOverride().yOffset = value);
        }

        private StringValue.Dynamic babyScaleValue() {
            return floatValue(
                () -> getSelectedOverrideOrDefault().babyScaleFactor,
                value -> getOrCreateSelectedOverride().babyScaleFactor = value);
        }

        private StringValue.Dynamic babyXOffsetValue() {
            return floatValue(
                () -> getSelectedOverrideOrDefault().babyXOffset,
                value -> getOrCreateSelectedOverride().babyXOffset = value);
        }

        private StringValue.Dynamic babyYOffsetValue() {
            return floatValue(
                () -> getSelectedOverrideOrDefault().babyYOffset,
                value -> getOrCreateSelectedOverride().babyYOffset = value);
        }

        private StringValue.Dynamic babyScaleModifierValue() {
            return floatValue(
                () -> getSelectedOverrideOrDefault().babyScaleModifier,
                value -> getOrCreateSelectedOverride().babyScaleModifier = value);
        }

        private StringValue.Dynamic sizeModifierValue() {
            return floatValue(
                () -> getSelectedOverrideOrDefault().sizeModifier,
                value -> getOrCreateSelectedOverride().sizeModifier = value);
        }

        private StringValue.Dynamic yawOffsetValue() {
            return floatValue(
                () -> getSelectedOverrideOrDefault().yawOffset,
                value -> getOrCreateSelectedOverride().yawOffset = value);
        }

        private StringValue.Dynamic pitchOffsetValue() {
            return floatValue(
                () -> getSelectedOverrideOrDefault().pitchOffset,
                value -> getOrCreateSelectedOverride().pitchOffset = value);
        }

        private StringValue.Dynamic previewYawValue() {
            return floatValue(() -> this.previewYaw, value -> this.previewYaw = value);
        }

        private StringValue.Dynamic previewPitchValue() {
            return floatValue(() -> this.previewPitch, value -> this.previewPitch = value);
        }

        private StringValue.Dynamic floatValue(FloatGetter getter, FloatSetter setter) {
            return new StringValue.Dynamic(() -> formatFloat(getter.get()), value -> {
                setter.set(parseFloat(value));
                flushChanges();
            });
        }
    }

    private interface FloatGetter {

        float get();
    }

    private interface FloatSetter {

        void set(float value);
    }

    private static final class EntityOption {

        private final String registryName;
        private final String className;

        private EntityOption(String registryName, String className) {
            this.registryName = registryName;
            this.className = className;
        }
    }
}

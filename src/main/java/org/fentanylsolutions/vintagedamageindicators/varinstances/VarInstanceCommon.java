package org.fentanylsolutions.vintagedamageindicators.varinstances;

import java.util.HashMap;

import org.fentanylsolutions.vintagedamageindicators.Config;
import org.fentanylsolutions.vintagedamageindicators.MobTypes;
import org.fentanylsolutions.vintagedamageindicators.VintageDamageIndicators;
import org.fentanylsolutions.vintagedamageindicators.util.XSTR;

public class VarInstanceCommon {

    public XSTR rand = new XSTR();

    public HashMap<Class, EntityOverride> entityOverrides;

    public void postInitHook() {
        buildOverrideList();
    }

    public void buildOverrideList() {
        if (entityOverrides == null) {
            entityOverrides = new HashMap<>();
        } else {
            entityOverrides.clear();
        }
        for (String s : Config.entityOverrides) {
            if (s == null || s.trim()
                .isEmpty()) {
                continue;
            }
            try {
                EntityOverride eo = EntityOverride.deserialize(s);
                if (!eo.enable || eo.className == null || eo.className.isEmpty()) {
                    continue;
                }
                Class cls = Class.forName(eo.className);
                entityOverrides.put(cls, eo);
            } catch (ClassNotFoundException e) {
                VintageDamageIndicators.debug("Skipping unknown override class: " + s);
            } catch (RuntimeException e) {
                VintageDamageIndicators.debug("Skipping malformed entity override: " + s);
            }
        }
    }

    public static class EntityOverride {

        public String className;
        public String displayName;
        public boolean appendBabyName;
        public float babyScaleModifier;
        public float scaleFactor;
        public float sizeModifier;
        public float xOffset;
        public float yOffset;
        public MobTypes type;
        public boolean enable;

        public EntityOverride() {
            this.type = MobTypes.UNKNOWN;
            this.enable = true;
        }

        public EntityOverride(String className, String displayName, boolean appendBabyName, float babyScaleModifier,
            float scaleFactor, float sizeModifier, float xOffset, float yOffset, MobTypes type, boolean enable) {
            this.className = className;
            this.displayName = displayName;
            this.appendBabyName = appendBabyName;
            this.babyScaleModifier = babyScaleModifier;
            this.scaleFactor = scaleFactor;
            this.sizeModifier = sizeModifier;
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.type = type;
            this.enable = enable;
        }

        public String serialize() {
            return String.format(
                "[%s]: <Name>: %s, <Append Baby Name>: %b, <Baby Scale Modifier>: %f, <Scale Factor>: %f, <Size Modifier>: %f, <X Offset>: %f, <Y Offset>: %f, <Type>: %s, <Enable>: %b",
                className,
                displayName,
                appendBabyName,
                babyScaleModifier,
                scaleFactor,
                sizeModifier,
                xOffset,
                yOffset,
                type.toString(),
                enable);
        }

        public static EntityOverride deserialize(String serialized) {
            EntityOverride entity = new EntityOverride();
            if (serialized == null) {
                return entity;
            }

            serialized = serialized.trim();
            if (serialized.isEmpty()) {
                return entity;
            }

            int classNameEndIndex = serialized.indexOf("]:");
            if (classNameEndIndex == -1) {
                return entity;
            }
            entity.className = serialized.substring(1, classNameEndIndex)
                .trim();

            String data = serialized.substring(classNameEndIndex + 2)
                .trim();

            String[] parts = data.split(",\\s*");

            for (String part : parts) {
                if (part.startsWith("<Name>:")) {
                    entity.displayName = part.substring("<Name>:".length())
                        .trim();
                } else if (part.startsWith("<Append Baby Name>:")) {
                    entity.appendBabyName = Boolean.parseBoolean(
                        part.substring("<Append Baby Name>:".length())
                            .trim());
                } else if (part.startsWith("<Baby Scale Modifier>:")) {
                    entity.babyScaleModifier = Float.parseFloat(
                        part.substring("<Baby Scale Modifier>:".length())
                            .trim());
                } else if (part.startsWith("<Scale Factor>:")) {
                    entity.scaleFactor = Float.parseFloat(
                        part.substring("<Scale Factor>:".length())
                            .trim());
                } else if (part.startsWith("<Size Modifier>:")) {
                    entity.sizeModifier = Float.parseFloat(
                        part.substring("<Size Modifier>:".length())
                            .trim());
                } else if (part.startsWith("<X Offset>:")) {
                    entity.xOffset = Float.parseFloat(
                        part.substring("<X Offset>:".length())
                            .trim());
                } else if (part.startsWith("<Y Offset>:")) {
                    entity.yOffset = Float.parseFloat(
                        part.substring("<Y Offset>:".length())
                            .trim());
                } else if (part.startsWith("<Type>:")) {
                    String mobType = part.substring("<Type>:".length())
                        .trim();
                    try {
                        entity.type = MobTypes.valueOf(mobType);
                    } catch (IllegalArgumentException ignored) {
                        //
                    }
                } else if (part.startsWith("<Enable>:")) {
                    entity.enable = Boolean.parseBoolean(
                        part.substring("<Enable>:".length())
                            .trim());
                }
            }

            return entity;
        }
    }
}

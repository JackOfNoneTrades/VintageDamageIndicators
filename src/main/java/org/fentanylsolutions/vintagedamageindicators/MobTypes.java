package org.fentanylsolutions.vintagedamageindicators;

import java.util.Locale;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.fentanylsolutions.vintagedamageindicators.varinstances.VarInstanceCommon;

public enum MobTypes {

    PLAYER,
    UNDEAD,
    UNDEAD_ANIMAL,
    ANIMAL,
    WATER_ANIMAL,
    AMBIENT,
    MONSTER,
    WATER_MONSTER,
    ARTHROPOD,
    ARTHROPOD_MONSTER,
    WATER_ARTHROPOD,
    GOLEM,
    VILLAGER,
    ILLAGER,
    UNKNOWN,
    BOSS;

    private final ResourceLocation texture;

    MobTypes() {
        texture = new ResourceLocation(
            "vintagedamageindicators:textures/gui/mob_types/" + name().toLowerCase(Locale.ROOT) + ".png");
    }

    public static MobTypes getTypeFor(EntityLivingBase entity) {
        if (VintageDamageIndicators.varInstanceCommon != null
            && VintageDamageIndicators.varInstanceCommon.entityOverrides != null) {
            VarInstanceCommon.EntityOverride eo = VintageDamageIndicators.varInstanceCommon.entityOverrides
                .get(entity.getClass());
            if (eo != null) {
                return eo.type;
            }
        }

        if (entity instanceof EntityPlayer) {
            return PLAYER;
        } else if (entity instanceof IBossDisplayData) {
            return BOSS;
        } else if (entity instanceof EntityWaterMob) {
            return WATER_ANIMAL;
        } else if (entity instanceof EntityVillager) {
            return VILLAGER;
        } else if (entity instanceof EntityGolem) {
            return GOLEM;
        } else if (entity instanceof EntitySpider) {
            return ARTHROPOD_MONSTER;
        } else if (entity instanceof EntityZombie || entity instanceof EntitySkeleton) {
            return UNDEAD;
        } else if (entity instanceof EntityWitch) {
            return VILLAGER; // TODO: maybe find a better type
        } else if (entity instanceof IMob) {
            return MONSTER;
        }

        return UNKNOWN;
    }

    public ResourceLocation getTexture() {
        return texture;
    }
}

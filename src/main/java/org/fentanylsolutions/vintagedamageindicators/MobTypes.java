package org.fentanylsolutions.vintagedamageindicators;

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


import java.util.Locale;

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
    MobTypes(){
        texture = new ResourceLocation("retrodamageindicators:textures/gui/mob_types/" + name().toLowerCase(Locale.ROOT) + ".png");
    }

    public static MobTypes getTypeFor(EntityLivingBase entity){
        if (VintageDamageIndicators.varInstanceCommon.playerTypeOverrides.contains(entity.getClass())) {
            return PLAYER;
        } else if (VintageDamageIndicators.varInstanceCommon.bossTypeOverrides.contains(entity.getClass())) {
            return BOSS;
        } else
        if (VintageDamageIndicators.varInstanceCommon.waterAnimalTypeOverrides.contains(entity.getClass())) {
            return WATER_ANIMAL;
        } else if (VintageDamageIndicators.varInstanceCommon.waterMonsterTypeOverrides.contains(entity.getClass())) {
            return WATER_MONSTER;
        } else
        if (VintageDamageIndicators.varInstanceCommon.monsterTypeOverrides.contains(entity.getClass())) {
            return MONSTER;
        } else
        if (VintageDamageIndicators.varInstanceCommon.undeadTypeOverrides.contains(entity.getClass())) {
            return UNDEAD;
        } else
        if (VintageDamageIndicators.varInstanceCommon.undeadAnimalTypeOverrides.contains(entity.getClass())) {
            return UNDEAD_ANIMAL;
        } else
        if (VintageDamageIndicators.varInstanceCommon.arthropodTypeOverrides.contains(entity.getClass())) {
            return ARTHROPOD;
        } else
        if (VintageDamageIndicators.varInstanceCommon.illagerTypeOverrides.contains(entity.getClass())) {
            return ILLAGER;
        } else
        if (VintageDamageIndicators.varInstanceCommon.villagerTypeOverrides.contains(entity.getClass())) {
            return VILLAGER;
        } else
        if (VintageDamageIndicators.varInstanceCommon.golemTypeOverrides.contains(entity.getClass())) {
            return GOLEM;
        } else
        if (VintageDamageIndicators.varInstanceCommon.ambientTypeOverrides.contains(entity.getClass())) {
            return AMBIENT;
        } else
        if (VintageDamageIndicators.varInstanceCommon.animalTypeOverrides.contains(entity.getClass())) {
            return ANIMAL;
        } else
        if (VintageDamageIndicators.varInstanceCommon.arthropodMonsterTypeOverrides.contains(entity.getClass())) {
            return ARTHROPOD_MONSTER;
        } else
        if (VintageDamageIndicators.varInstanceCommon.arthropodWaterMonsterTypeOverrides.contains(entity.getClass())) {
            return ARTHROPOD_MONSTER; // TODO: water arthropod monster
        } else
        if (VintageDamageIndicators.varInstanceCommon.arthropodWaterTypeOverrides.contains(entity.getClass())) {
            return WATER_ARTHROPOD;
        } else

        if(entity instanceof EntityPlayer){
            return PLAYER;
        } else
        if(entity instanceof IBossDisplayData){
            return BOSS;
        } else
        if (entity instanceof EntityWaterMob) {
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
        } else
        if (entity instanceof IMob) {
            return MONSTER;
        }

        return UNKNOWN;
    }

    public ResourceLocation getTexture() {
        return texture;
    }
}

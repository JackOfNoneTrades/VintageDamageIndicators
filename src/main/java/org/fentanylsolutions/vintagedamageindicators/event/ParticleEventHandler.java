package org.fentanylsolutions.vintagedamageindicators.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraftforge.event.entity.living.LivingEvent;

import org.fentanylsolutions.vintagedamageindicators.Config;
import org.fentanylsolutions.vintagedamageindicators.client.HudPreviewParticles;
import org.fentanylsolutions.vintagedamageindicators.rendering.DamageIndicatorParticle;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ParticleEventHandler {

    public static HashMap<Integer, Integer> healths = new HashMap<>();
    public static Map<Integer, Collection<PotionEffect>> potionEffects = new HashMap();
    public static List<Integer> enemies = new ArrayList();

    private void updateHealth(EntityLivingBase el) {
        int lastHealth;
        if (el != null) {
            int currentHealth = MathHelper.ceiling_float_int(el.getHealth());
            if (healths.containsKey(Integer.valueOf(el.getEntityId()))
                && (lastHealth = healths.get(Integer.valueOf(el.getEntityId()))
                    .intValue()) != 0
                && lastHealth != currentHealth) {
                int damage = lastHealth - currentHealth;
                if (Config.hudPreviewParticlesEnabled) {
                    HudPreviewParticles.spawnDamage(el, damage);
                }
                if (Config.damageParticlesEnabled) {
                    DamageIndicatorParticle customParticle = new DamageIndicatorParticle(
                        Minecraft.getMinecraft().theWorld,
                        el.posX,
                        el.posY + el.height,
                        el.posZ,
                        0.001d,
                        0.05f * Config.bounceStrength,
                        0.001d,
                        damage);
                    if (Minecraft.getMinecraft().thePlayer.canEntityBeSeen(el)) {
                        customParticle.renderOnTop = true;
                    } else if (Minecraft.getMinecraft()
                        .isSingleplayer()) {
                            customParticle.renderOnTop = Config.alwaysRender;
                        }
                    if (el != Minecraft.getMinecraft().thePlayer
                        || Minecraft.getMinecraft().gameSettings.thirdPersonView != 0) {
                        Minecraft.getMinecraft().effectRenderer.addEffect(customParticle);
                    }
                }
            }
            healths.put(Integer.valueOf(el.getEntityId()), Integer.valueOf(currentHealth));
        }
    }

    public static void doCritical(Entity target) {
        if (Config.hudPreviewParticlesEnabled && Config.criticalParticlesEnabled) {
            HudPreviewParticles.spawnCritical(target);
        }
        if (!Config.criticalParticlesEnabled) {
            return;
        }

        DamageIndicatorParticle customParticle = new DamageIndicatorParticle(
            Minecraft.getMinecraft().theWorld,
            target.posX,
            target.posY + target.height,
            target.posZ,
            0.001d,
            0.05f * Config.bounceStrength,
            0.001d);
        if (Minecraft.getMinecraft().thePlayer.canEntityBeSeen(target)) {
            customParticle.renderOnTop = true;
        } else if (Minecraft.getMinecraft()
            .isSingleplayer()) {
                customParticle.renderOnTop = Config.alwaysRender;
            }
        if (target != Minecraft.getMinecraft().thePlayer
            || Minecraft.getMinecraft().gameSettings.thirdPersonView != 0) {
            Minecraft.getMinecraft().effectRenderer.addEffect(customParticle);
        }
    }

    @SubscribeEvent
    public void livingUpdateEvent(LivingEvent.LivingUpdateEvent event) {
        if (!FMLCommonHandler.instance()
            .getEffectiveSide()
            .isClient()) {
            return;
        }

        EntityLivingBase entityLiving = event.entityLiving;
        if (FMLCommonHandler.instance()
            .getEffectiveSide()
            .isClient()) {
            EntityPlayer entityLivingBase = Minecraft.getMinecraft().thePlayer;
            if (entityLiving.getCommandSenderName()
                .equals(Minecraft.getMinecraft().thePlayer.getCommandSenderName())) {
                entityLiving = Minecraft.getMinecraft().thePlayer;
            }
            if (entityLivingBase != null && entityLivingBase.worldObj != null) {
                if (Config.damageParticlesEnabled || Config.hudPreviewParticlesEnabled) {
                    updateHealth(entityLiving);
                }
                if (event.entityLiving.isDead) {
                    HudPreviewParticles.clear(event.entity.getEntityId());
                    potionEffects.remove(Integer.valueOf(event.entity.getEntityId()));
                    healths.remove(Integer.valueOf(event.entity.getEntityId()));
                    enemies.remove(Integer.valueOf(event.entity.getEntityId()));
                    return;
                }
                return;
            }
            if (FMLCommonHandler.instance()
                .getSide()
                .isClient()) {
                if (event.entityLiving.isDead) {
                    HudPreviewParticles.clear(event.entity.getEntityId());
                    potionEffects.remove(Integer.valueOf(event.entity.getEntityId()));
                    healths.remove(Integer.valueOf(event.entity.getEntityId()));
                    enemies.remove(Integer.valueOf(event.entity.getEntityId()));
                    return;
                }
                potionEffects.put(
                    Integer.valueOf(event.entityLiving.getEntityId()),
                    event.entityLiving.getActivePotionEffects());
            }
        }
    }
}

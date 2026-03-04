package org.fentanylsolutions.vintagedamageindicators.network;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class EntityPotionEffectsMessage implements IMessage {

    private int entityId;
    private List<PotionEntry> effects = new ArrayList<>();

    public EntityPotionEffectsMessage() {}

    public EntityPotionEffectsMessage(int entityId, List<PotionEntry> effects) {
        this.entityId = entityId;
        if (effects != null) {
            this.effects = effects;
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        int size = buf.readUnsignedByte();
        this.effects = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            this.effects
                .add(new PotionEntry(buf.readUnsignedByte(), buf.readInt(), buf.readUnsignedByte(), buf.readBoolean()));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeByte(this.effects.size());
        for (PotionEntry entry : this.effects) {
            buf.writeByte(entry.potionId);
            buf.writeInt(entry.duration);
            buf.writeByte(entry.amplifier);
            buf.writeBoolean(entry.durationMax);
        }
    }

    public static final class Handler implements IMessageHandler<EntityPotionEffectsMessage, IMessage> {

        @Override
        public IMessage onMessage(final EntityPotionEffectsMessage message, MessageContext ctx) {
            Minecraft.getMinecraft()
                .func_152344_a(() -> applyMessage(message));
            return null;
        }

        private static void applyMessage(EntityPotionEffectsMessage message) {
            Minecraft minecraft = Minecraft.getMinecraft();
            if (minecraft == null || minecraft.theWorld == null) {
                return;
            }

            Entity entity = minecraft.theWorld.getEntityByID(message.entityId);
            if (!(entity instanceof EntityLivingBase)) {
                return;
            }

            EntityLivingBase living = (EntityLivingBase) entity;
            Set<Integer> incomingPotionIds = new HashSet<>();
            for (PotionEntry entry : message.effects) {
                incomingPotionIds.add(entry.potionId);
            }

            List<PotionEffect> existingEffects = new ArrayList<>(living.getActivePotionEffects());
            for (PotionEffect existing : existingEffects) {
                if (!incomingPotionIds.contains(existing.getPotionID())) {
                    living.removePotionEffectClient(existing.getPotionID());
                }
            }

            for (PotionEntry entry : message.effects) {
                PotionEffect effect = new PotionEffect(entry.potionId, entry.duration, entry.amplifier);
                effect.setPotionDurationMax(entry.durationMax);
                living.addPotionEffect(effect);
            }
        }
    }

    public static final class PotionEntry {

        public final int potionId;
        public final int duration;
        public final int amplifier;
        public final boolean durationMax;

        public PotionEntry(int potionId, int duration, int amplifier, boolean durationMax) {
            this.potionId = potionId;
            this.duration = duration;
            this.amplifier = amplifier;
            this.durationMax = durationMax;
        }

        public static PotionEntry fromServerEffect(PotionEffect effect) {
            return new PotionEntry(effect.getPotionID(), effect.getDuration(), effect.getAmplifier(), false);
        }
    }
}

package org.fentanylsolutions.vintagedamageindicators.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import org.fentanylsolutions.vintagedamageindicators.network.EntityPotionEffectsMessage;
import org.fentanylsolutions.vintagedamageindicators.network.EntityPotionEffectsMessage.PotionEntry;
import org.fentanylsolutions.vintagedamageindicators.network.VDINetwork;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.Side;

public class PotionSyncEventHandler {

    private final WeakHashMap<EntityLivingBase, Integer> potionStateHashes = new WeakHashMap<>();
    private final Map<NetworkManager, Boolean> clientsWithChannel = Collections.synchronizedMap(new WeakHashMap<>());

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.entityLiving;
        if (entity == null || entity.worldObj == null
            || entity.worldObj.isRemote
            || !(entity.worldObj instanceof WorldServer)) {
            return;
        }

        int stateHash = computeStateHash(entity.getActivePotionEffects());
        Integer previousHash = this.potionStateHashes.get(entity);
        if (previousHash != null && previousHash.intValue() == stateHash) {
            return;
        }

        this.potionStateHashes.put(entity, stateHash);
        syncToTrackingPlayers(entity, (WorldServer) entity.worldObj);
    }

    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.target instanceof EntityLivingBase) || !(event.entityPlayer instanceof EntityPlayerMP)) {
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP) event.entityPlayer;
        if (!isChannelAvailable(player.playerNetServerHandler.netManager)) {
            return;
        }

        sendSnapshot((EntityLivingBase) event.target, player);
    }

    @SubscribeEvent
    public void onChannelRegistration(FMLNetworkEvent.CustomPacketRegistrationEvent<?> event) {
        if (event.side != Side.SERVER) {
            return;
        }

        if ("REGISTER".equals(event.operation)) {
            this.clientsWithChannel.put(event.manager, event.registrations.contains(VDINetwork.CHANNEL_NAME));
        } else if ("UNREGISTER".equals(event.operation)) {
            this.clientsWithChannel.remove(event.manager);
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ServerDisconnectionFromClientEvent event) {
        this.clientsWithChannel.remove(event.manager);
    }

    private void syncToTrackingPlayers(EntityLivingBase entity, WorldServer world) {
        Set<net.minecraft.entity.player.EntityPlayer> trackingPlayers = world.getEntityTracker()
            .getTrackingPlayers(entity);
        if (trackingPlayers == null || trackingPlayers.isEmpty()) {
            return;
        }

        EntityPotionEffectsMessage message = buildSnapshot(entity);
        for (net.minecraft.entity.player.EntityPlayer trackingPlayer : trackingPlayers) {
            if (trackingPlayer instanceof EntityPlayerMP) {
                EntityPlayerMP player = (EntityPlayerMP) trackingPlayer;
                if (isChannelAvailable(player.playerNetServerHandler.netManager)) {
                    VDINetwork.getChannel()
                        .sendTo(message, player);
                }
            }
        }
    }

    private void sendSnapshot(EntityLivingBase entity, EntityPlayerMP player) {
        VDINetwork.getChannel()
            .sendTo(buildSnapshot(entity), player);
    }

    private boolean isChannelAvailable(NetworkManager manager) {
        Boolean known = this.clientsWithChannel.get(manager);
        return Boolean.TRUE.equals(known) || manager.isLocalChannel();
    }

    private EntityPotionEffectsMessage buildSnapshot(EntityLivingBase entity) {
        List<PotionEntry> entries = new ArrayList<>();
        for (Object effectObject : entity.getActivePotionEffects()) {
            if (effectObject instanceof PotionEffect) {
                entries.add(PotionEntry.fromServerEffect((PotionEffect) effectObject));
            }
        }
        return new EntityPotionEffectsMessage(entity.getEntityId(), entries);
    }

    private int computeStateHash(Collection<PotionEffect> effects) {
        int hash = 1;
        for (PotionEffect effect : effects) {
            hash = 31 * hash + effect.getPotionID();
            hash = 31 * hash + effect.getAmplifier();
            hash = 31 * hash + effect.getDuration();
        }
        return hash;
    }
}

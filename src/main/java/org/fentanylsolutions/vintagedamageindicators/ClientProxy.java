package org.fentanylsolutions.vintagedamageindicators;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.common.MinecraftForge;
import org.fentanylsolutions.vintagedamageindicators.event.HudEventHandler;
import org.fentanylsolutions.vintagedamageindicators.event.ParticleEventHandler;

public class ClientProxy extends CommonProxy {

    // Override CommonProxy methods here, if you want a different behaviour on the client (e.g. registering renders).
    // Don't forget to call the super methods as well.

    @Override
    public void init(FMLInitializationEvent event) {
        ParticleEventHandler particleEventHandler = new ParticleEventHandler();
        MinecraftForge.EVENT_BUS.register(particleEventHandler);

        HudEventHandler hudEventHandler = new HudEventHandler();
        MinecraftForge.EVENT_BUS.register(hudEventHandler);
        FMLCommonHandler.instance().bus().register(hudEventHandler);
    }
}

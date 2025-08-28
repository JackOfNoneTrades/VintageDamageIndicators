package org.fentanylsolutions.vintagedamageindicators;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {

    // Override CommonProxy methods here, if you want a different behaviour on the client (e.g. registering renders).
    // Don't forget to call the super methods as well.

    @Override
    public void init(FMLInitializationEvent event) {
        EventHandler eventHandler = new EventHandler();
        MinecraftForge.EVENT_BUS.register(eventHandler);
    }
}

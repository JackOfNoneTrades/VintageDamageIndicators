package org.fentanylsolutions.vintagedamageindicators;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;

import org.fentanylsolutions.vintagedamageindicators.command.OpenOverrideEditorCommand;
import org.fentanylsolutions.vintagedamageindicators.event.HudEventHandler;
import org.fentanylsolutions.vintagedamageindicators.event.ParticleEventHandler;
import org.fentanylsolutions.vintagedamageindicators.network.VDINetwork;
import org.fentanylsolutions.vintagedamageindicators.varinstances.VarInstanceClient;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    // Override CommonProxy methods here, if you want a different behaviour on the client (e.g. registering renders).
    // Don't forget to call the super methods as well.

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        VintageDamageIndicators.varInstanceClient = new VarInstanceClient();
        VDINetwork.initClient();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        ParticleEventHandler particleEventHandler = new ParticleEventHandler();
        MinecraftForge.EVENT_BUS.register(particleEventHandler);

        HudEventHandler hudEventHandler = new HudEventHandler();
        MinecraftForge.EVENT_BUS.register(hudEventHandler);
        FMLCommonHandler.instance()
            .bus()
            .register(hudEventHandler);

        OpenOverrideEditorCommand openOverrideEditorCommand = new OpenOverrideEditorCommand();
        ClientCommandHandler.instance.registerCommand(openOverrideEditorCommand);
        FMLCommonHandler.instance()
            .bus()
            .register(openOverrideEditorCommand);
    }
}

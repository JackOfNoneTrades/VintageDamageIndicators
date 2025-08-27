package org.fentanylsolutions.vintagedamageindicators;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.common.MinecraftForge;
import org.fentanylsolutions.vintagedamageindicators.util.MobUtil;
import org.fentanylsolutions.vintagedamageindicators.varinstances.VarInstanceCommon;

public class CommonProxy {

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        VintageDamageIndicators.varInstanceCommon = new VarInstanceCommon();
        VintageDamageIndicators.LOG.info("I am " + VintageDamageIndicators.MODID + " at version " + Tags.VERSION);
        Config.loadConfig(VintageDamageIndicators.confFile);
    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        EventHandler eventHandler = new EventHandler();
        MinecraftForge.EVENT_BUS.register(eventHandler);
    }

    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        if (Config.printMobs) {
            MobUtil.printMobNames();
        }

        VintageDamageIndicators.varInstanceCommon.postInitHook();
    }

    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {}
}

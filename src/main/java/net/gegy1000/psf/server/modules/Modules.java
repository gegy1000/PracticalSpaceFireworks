package net.gegy1000.psf.server.modules;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.module.IModuleFactory;
import net.gegy1000.psf.server.modules.ModuleBattery.BatteryTier;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nonnull;

@EventBusSubscriber(modid = PracticalSpaceFireworks.MODID)
public class Modules {
    
    private static IForgeRegistry<IModuleFactory> registry; 
    
    @SubscribeEvent
    public static void createRegistry(RegistryEvent.NewRegistry event) {
        registry = new RegistryBuilder<IModuleFactory>()
                .setName(new ResourceLocation(PracticalSpaceFireworks.MODID, "modules"))
                .setType(IModuleFactory.class)
                .create();
    }
    
    @SubscribeEvent
    public static void registerModules(RegistryEvent.Register<IModuleFactory> event) {
        registry.register(new SimpleModuleFactory(ModuleController::new).setRegistryName("controller_simple"));
        
        registry.register(new SimpleModuleFactory(() -> new EmptyModule("strut_cube")).setRegistryName("strut_cube"));
        registry.register(new SimpleModuleFactory(() -> new EmptyModule("strut_slope")).setRegistryName("strut_slope"));
        
        registry.register(new SimpleModuleFactory(() -> new ModuleBattery(BatteryTier.SIMPLE, 100000)).setRegistryName("battery_simple"));
        registry.register(new SimpleModuleFactory(() -> new ModuleThruster(ModuleThruster.ThrusterTier.SIMPLE)).setRegistryName("thruster_simple"));
//        registry.register(new SimpleModuleFactory(() -> new EmptyModule("antenna")).setRegistryName("antenna"));
        registry.register(new SimpleModuleFactory(() -> new ModuleEntityDetector(ModuleEntityDetector.EntityDetectorTier.SIMPLE)).setRegistryName("entity_detector_simple"));
        registry.register(new SimpleModuleFactory(ModuleEntityMarker::new).setRegistryName("entity_marker"));
        registry.register(new SimpleModuleFactory(ModuleFuelTank::new).setRegistryName("fuel_tank"));
        registry.register(new SimpleModuleFactory(() -> new EmptyModule("payload_separator")).setRegistryName("payload_separator"));
        registry.register(new SimpleModuleFactory(ModuleTerrainScanner::new).setRegistryName("terrain_scanner"));
//        registry.register(new SimpleModuleFactory(ModuleWeatherScanner::new).setRegistryName("weather_scanner"));
        registry.register(new SimpleModuleFactory(() -> new ModuleSolarPanel("small", 8)).setRegistryName("solar_panel_small"));
        registry.register(new SimpleModuleFactory(() -> new ModuleSolarPanel("large", 20)).setRegistryName("solar_panel_large"));
        registry.register(new SimpleModuleFactory(ModuleSpaceLaser::new).setRegistryName("laser"));
        registry.register(new SimpleModuleFactory(ModuleFuelValve::new).setRegistryName("fuel_valve"));
    }

    public static @Nonnull IForgeRegistry<IModuleFactory> get() {
        final IForgeRegistry<IModuleFactory> r = registry;
        if (r != null) {
            return r;
        }
        throw new IllegalStateException("Accessing module registry too early!");
    }
}

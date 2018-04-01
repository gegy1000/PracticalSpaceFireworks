package net.gegy1000.psf.server;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.capability.world.CapabilityWorldData;
import net.gegy1000.psf.server.capability.world.SatelliteWorldData;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = PracticalSpaceFireworks.MODID)
public class ServerEventHandler {
    @SubscribeEvent
    public static void onAttachWorldCapabilities(AttachCapabilitiesEvent<World> event) {
        event.addCapability(CapabilityWorldData.SATELLITE_ID, new SatelliteWorldData.Impl(event.getObject()));
    }

    @SubscribeEvent
    public static void onWorldUpdate(TickEvent.WorldTickEvent event) {
        if (event.world.hasCapability(CapabilityWorldData.SATELLITE_INSTANCE, null)) {
           event.world.getCapability(CapabilityWorldData.SATELLITE_INSTANCE, null).getSatellites().forEach(satellite -> satellite.tickSatellite());
        }
    }
}

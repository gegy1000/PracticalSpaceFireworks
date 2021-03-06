package net.gegy1000.psf.server;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.block.controller.CraftGraph;
import net.gegy1000.psf.server.capability.world.CapabilityWorldData;
import net.gegy1000.psf.server.capability.world.SatelliteWorldData;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

import java.util.List;

@Mod.EventBusSubscriber(modid = PracticalSpaceFireworks.MODID)
public class ServerEventHandler {
    @SubscribeEvent
    public static void onAttachWorldCapabilities(AttachCapabilitiesEvent<World> event) {
        event.addCapability(CapabilityWorldData.SATELLITE_ID, new SatelliteWorldData.Impl(event.getObject()));
    }

    @SubscribeEvent
    public static void onWorldUpdate(TickEvent.WorldTickEvent event) {
        if (event.phase == Phase.END && !event.world.isRemote) {
            SatelliteWorldData satellites = event.world.getCapability(CapabilityWorldData.SATELLITE_INSTANCE, null);
            if (satellites != null) satellites.tick(event.world.getTotalWorldTime());
        }
    }

    @SubscribeEvent
    public static void onCollide(GetCollisionBoxesEvent event) {
        Entity entity = event.getEntity();
        if (entity != null) {
            AxisAlignedBB entityBounds = event.getAabb();
            for (EntitySpacecraft sc : entity.getEntityWorld().getEntitiesWithinAABB(
                    EntitySpacecraft.class, entityBounds.grow(CraftGraph.RANGE)
            )) {
                if (sc != entity && entityBounds.intersects(sc.getEntityBoundingBox())) {
                    List<AxisAlignedBB> collisionBoxes = event.getCollisionBoxesList();
                    AxisAlignedBB entityCollision = entityBounds.offset(-sc.posX, -sc.posY, -sc.posZ);
                    for (AxisAlignedBB bound : sc.getBody().collectTransformedBlockBounds()) {
                        if (entityCollision.intersects(bound)) {
                            collisionBoxes.add(bound.offset(sc.posX, sc.posY, sc.posZ));
                        }
                    }
                }
            }
        }
    }
}

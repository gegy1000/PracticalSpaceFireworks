package net.gegy1000.psf.server.block.remote.tile;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import java.util.UUID;

import net.gegy1000.psf.api.spacecraft.IListedSpacecraft;
import net.gegy1000.psf.api.spacecraft.ISatellite;
import net.gegy1000.psf.server.block.remote.packet.PacketSetName;
import net.gegy1000.psf.server.entity.spacecraft.PacketLaunchTile;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.minecraft.util.math.BlockPos;

@ParametersAreNonnullByDefault
public class TileListedSpacecraft implements IListedSpacecraft {
    private final ISatellite satellite;

    public TileListedSpacecraft(ISatellite satellite) {
        this.satellite = satellite;
    }
    
    @Nonnull
    @Override
    public UUID getId() {
        return this.satellite.getId();
    }

    @Nonnull
    @Override
    public String getName() {
        return this.satellite.getName();
    }

    @Override
    public void setName(@Nonnull String name) {
        PSFNetworkHandler.network.sendToServer(new PacketSetName(satellite.getId(), name));
        satellite.setName(name);
    }

    @Nonnull
    @Override
    public BlockPos getPosition() {
        return satellite.getPosition();
    }

    @Override
    public void launch() {
        PSFNetworkHandler.network.sendToServer(new PacketLaunchTile(satellite.getPosition()));
    }

    @Override
    public boolean canLaunch() {
        return true;
    }
    
    @Override
    public boolean isDestroyed() {
        return this.satellite.isDestroyed();
    }
}

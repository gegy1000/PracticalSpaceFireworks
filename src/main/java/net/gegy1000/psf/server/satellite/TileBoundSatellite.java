package net.gegy1000.psf.server.satellite;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import net.gegy1000.psf.api.IController;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.server.block.controller.TileController;
import net.gegy1000.psf.server.block.controller.TileController.ScanValue;
import net.gegy1000.psf.server.block.remote.IListedSpacecraft;
import net.gegy1000.psf.server.block.remote.tile.TileListedSpacecraft;
import net.gegy1000.psf.server.capability.CapabilityController;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftBlockAccess;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftBuilder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class TileBoundSatellite implements ISatellite {
    
    private final TileController controller;
    
    @Getter
    private UUID id = UUID.randomUUID();
    
    @Setter
    @Nonnull
    private String name = "";

    @Override
    public IController getController() {
        return controller.getCapability(CapabilityController.INSTANCE, null);
    }

    @Override
    public Collection<IModule> getModules() {
        return controller.getModules().values().stream().map(ScanValue::getModule).collect(Collectors.toList());
    }

    @Override
    public BlockPos getPosition() {
        return controller.getPos();
    }

    @Override
    public SpacecraftBlockAccess buildBlockAccess(World world) {
        BlockPos origin = controller.getPos();
        SpacecraftBuilder builder = new SpacecraftBuilder();
        for (val e : controller.getModules().entrySet()) {
            builder.setBlockState(e.getKey().subtract(origin), e.getValue().getState());
        }
        return builder.buildBlockAccess(origin, world);
    }

    @Override
    public IListedSpacecraft toListedCraft() {
        return new TileListedSpacecraft(this);
    }

    @Override
    public World getWorld() {
        return controller.getWorld();
    }

    @Override
    public String getName() {
        return name.isEmpty() ? ISatellite.super.getName() : name;
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = ISatellite.super.serializeNBT();
        tag.setLong("uuid_msb", getId().getMostSignificantBits());
        tag.setLong("uuid_lsb", getId().getLeastSignificantBits());
        tag.setString("name", name);
        return tag;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        ISatellite.super.deserializeNBT(tag);
        this.id = new UUID(tag.getLong("uuid_msb"), tag.getLong("uuid_lsb"));
        this.name = tag.getString("name");
    }
}
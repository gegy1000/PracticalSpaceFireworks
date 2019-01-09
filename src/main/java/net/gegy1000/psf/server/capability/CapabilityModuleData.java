package net.gegy1000.psf.server.capability;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.api.IAdditionalMass;
import net.gegy1000.psf.api.ILaser;
import net.gegy1000.psf.api.data.IEntityList;
import net.gegy1000.psf.api.data.ITerrainScan;
import net.gegy1000.psf.api.data.IWeatherData;
import net.gegy1000.psf.server.modules.cap.EnergyStats;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nonnull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CapabilityModuleData {
    @SuppressWarnings("null")
    @CapabilityInject(IAdditionalMass.class)
    @Nonnull
    public static final Capability<IAdditionalMass> ADDITIONAL_MASS = null;

    @SuppressWarnings("null")
    @CapabilityInject(IEntityList.class)
    @Nonnull
    public static final Capability<IEntityList> ENTITY_LIST = null;

    @SuppressWarnings("null")
    @CapabilityInject(ITerrainScan.class)
    @Nonnull
    public static final Capability<ITerrainScan> TERRAIN_SCAN = null;
    
    @SuppressWarnings("null")
    @CapabilityInject(ILaser.class)
    @Nonnull
    public static final Capability<ILaser> SPACE_LASER = null;

    @SuppressWarnings("null")
    @CapabilityInject(EnergyStats.class)
    @Nonnull
    public static final Capability<EnergyStats> ENERGY_STATS = null;

    @SuppressWarnings("null")
    @CapabilityInject(IWeatherData.class)
    @Nonnull
    public static final Capability<IWeatherData> WEATHER_DATA = null;

    public static void register() {
        // TODO default IStorage ?
        CapabilityManager.INSTANCE.register(IAdditionalMass.class, new BlankStorage<>(), () -> null); // FIXME
        CapabilityManager.INSTANCE.register(IEntityList.class, new BlankStorage<>(), () -> null); // FIXME
        CapabilityManager.INSTANCE.register(ITerrainScan.class, new BlankStorage<>(), () -> null); // FIXME
        CapabilityManager.INSTANCE.register(ILaser.class, new BlankStorage<>(), () -> null); // FIXME
        CapabilityManager.INSTANCE.register(EnergyStats.class, new BlankStorage<>(), () -> null); // FIXME
        CapabilityManager.INSTANCE.register(IWeatherData.class, new BlankStorage<>(), () -> null); // FIXME
    }
}

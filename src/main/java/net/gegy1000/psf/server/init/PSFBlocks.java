package net.gegy1000.psf.server.init;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.api.RegisterTileEntity;
import net.gegy1000.psf.server.block.BlockPSFFluid;
import net.gegy1000.psf.server.block.controller.BlockController;
import net.gegy1000.psf.server.block.controller.ControllerType;
import net.gegy1000.psf.server.block.fueler.BlockFuelLoader;
import net.gegy1000.psf.server.block.module.*;
import net.gegy1000.psf.server.block.production.BlockAirCompressor;
import net.gegy1000.psf.server.block.production.BlockAirIntake;
import net.gegy1000.psf.server.block.production.BlockAirSeparator;
import net.gegy1000.psf.server.block.production.BlockKeroseneExtractor;
import net.gegy1000.psf.server.block.remote.BlockRemoteControlSystem;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static net.gegy1000.psf.PracticalSpaceFireworks.namespace;
import static net.minecraftforge.fml.common.registry.GameRegistry.registerTileEntity;

@Accessors(fluent = true)
@ObjectHolder(PracticalSpaceFireworks.MODID)
@EventBusSubscriber(modid = PracticalSpaceFireworks.MODID)
public final class PSFBlocks {
    private static final String KEROSENE = "kerosene";
    private static final String LIQUID_OXYGEN = "liquid_oxygen";
    private static final String LIQUID_NITROGEN = "liquid_nitrogen";
    private static final String FILTERED_AIR = "filtered_air";
    private static final String COMPRESSED_AIR = "compressed_air";

    private static final String STRUT_CUBE = "strut_cube";
    private static final String STRUT_SLOPE = "strut_slope";

    private static final String CONTROLLER_SIMPLE = "controller_simple";
    private static final String REMOTE_CONTROL_SYSTEM = "remote_control_system";
    private static final String PAYLOAD_SEPARATOR = "payload_separator";

    private static final String FUEL_LOADER = "fuel_loader";
    private static final String FUEL_VALVE = "fuel_valve";
    private static final String FUEL_TANK = "fuel_tank";
    private static final String THRUSTER_SIMPLE = "thruster_simple";

    private static final String KEROSENE_EXTRACTOR = "kerosene_extractor";
    private static final String AIR_INTAKE = "air_intake";
    private static final String AIR_COMPRESSOR = "air_compressor";
    private static final String AIR_SEPARATOR = "air_separator";

    private static final String SOLAR_PANEL_SMALL = "solar_panel_small";
    private static final String SOLAR_PANEL_LARGE = "solar_panel_large";
    private static final String BATTERY_SIMPLE = "battery_simple";

    private static final String ENTITY_DETECTOR_SIMPLE = "entity_detector_simple";
    private static final String ENTITY_MARKER = "entity_marker";

    private static final String TERRAIN_SCANNER = "terrain_scanner";
    private static final String WEATHER_SCANNER = "weather_scanner";

    private static final String LASER = "laser";

    private static final Set<Block> ALL = new LinkedHashSet<>();

    @Getter
    @ObjectHolder(KEROSENE)
    private static BlockFluidFinite kerosene;

    @Getter
    @ObjectHolder(LIQUID_OXYGEN)
    private static BlockFluidFinite liquidOxygen;

    @Getter
    @ObjectHolder(LIQUID_NITROGEN)
    private static BlockFluidFinite liquidNitrogen;

    @Getter
    @ObjectHolder(FILTERED_AIR)
    private static BlockFluidFinite filteredAir;

    @Getter
    @ObjectHolder(COMPRESSED_AIR)
    private static BlockFluidFinite compressedAir;

    @Getter
    @ObjectHolder(STRUT_CUBE)
    private static BlockStrutFixed strutCube;

    @Getter
    @ObjectHolder(STRUT_SLOPE)
    private static BlockStrutOrientable strutSlope;

    @Getter
    @ObjectHolder(CONTROLLER_SIMPLE)
    private static BlockController controllerSimple;

    @Getter
    @ObjectHolder(REMOTE_CONTROL_SYSTEM)
    private static BlockRemoteControlSystem remoteControlSystem;

    @Getter
    @ObjectHolder(PAYLOAD_SEPARATOR)
    private static BlockPayloadSeparator payloadSeparator;

    @Getter
    @ObjectHolder(FUEL_LOADER)
    private static BlockFuelLoader fuelLoader;

    @Getter
    @ObjectHolder(FUEL_VALVE)
    private static BlockFuelValve fuelValve;

    @Getter
    @ObjectHolder(FUEL_TANK)
    private static BlockFuelTank fuelTank;

    @Getter
    @ObjectHolder(THRUSTER_SIMPLE)
    private static BlockModule thrusterSimple;

    @Getter
    @ObjectHolder(KEROSENE_EXTRACTOR)
    private static BlockKeroseneExtractor keroseneExtractor;

    @Getter
    @ObjectHolder(AIR_INTAKE)
    private static BlockAirIntake airIntake;

    @Getter
    @ObjectHolder(AIR_COMPRESSOR)
    private static BlockAirCompressor airCompressor;

    @Getter
    @ObjectHolder(AIR_SEPARATOR)
    private static BlockAirSeparator airSeparator;

    @Getter
    @ObjectHolder(SOLAR_PANEL_SMALL)
    private static BlockModule solarPanelSmall;

    @Getter
    @ObjectHolder(SOLAR_PANEL_LARGE)
    private static BlockMultiblockModule solarPanelLarge;

    @Getter
    @ObjectHolder(BATTERY_SIMPLE)
    private static BlockBattery batterySimple;

    @Getter
    @ObjectHolder(ENTITY_DETECTOR_SIMPLE)
    private static BlockModule entityDetectorSimple;

    @Getter
    @ObjectHolder(ENTITY_MARKER)
    private static BlockModule entityMarker;

    @Getter
    @ObjectHolder(TERRAIN_SCANNER)
    private static BlockModule terrainScanner;

    @Getter
    @ObjectHolder(WEATHER_SCANNER)
    private static BlockModule weatherScanner;

    @Getter
    @ObjectHolder(LASER)
    private static BlockMultiblockModule laser;

    private PSFBlocks() {
        throw new UnsupportedOperationException();
    }

    public static Set<Block> allBlocks() {
        return Collections.unmodifiableSet(ALL);
    }

    @SubscribeEvent
    static void registerBlocks(final RegistryEvent.Register<Block> event) {
        @Nonnull val registry = event.getRegistry();

        register(registry, KEROSENE, new BlockPSFFluid(PSFFluids.kerosene(), Material.WATER));
        register(registry, LIQUID_OXYGEN, new BlockPSFFluid(PSFFluids.liquidOxygen(), Material.WATER));
        register(registry, LIQUID_NITROGEN, new BlockPSFFluid(PSFFluids.liquidNitrogen(), Material.WATER));
        register(registry, FILTERED_AIR, new BlockPSFFluid(PSFFluids.filteredAir(), Material.WATER));
        register(registry, COMPRESSED_AIR, new BlockPSFFluid(PSFFluids.compressedAir(), Material.WATER));

        register(registry, STRUT_CUBE, new BlockStrutFixed(STRUT_CUBE));
        register(registry, STRUT_SLOPE, new BlockStrutOrientable.Slope(STRUT_SLOPE));

        register(registry, CONTROLLER_SIMPLE, new BlockController(ControllerType.BASIC));
        register(registry, REMOTE_CONTROL_SYSTEM, new BlockRemoteControlSystem());
        register(registry, PAYLOAD_SEPARATOR, new BlockPayloadSeparator());

        register(registry, FUEL_LOADER, new BlockFuelLoader());
        register(registry, FUEL_VALVE, new BlockFuelValve());
        register(registry, FUEL_TANK, new BlockFuelTank());
        register(registry, THRUSTER_SIMPLE, new BlockThruster(THRUSTER_SIMPLE));

        register(registry, KEROSENE_EXTRACTOR, new BlockKeroseneExtractor());
        register(registry, AIR_INTAKE, new BlockAirIntake());
        register(registry, AIR_COMPRESSOR, new BlockAirCompressor());
        register(registry, AIR_SEPARATOR, new BlockAirSeparator());

        register(registry, SOLAR_PANEL_SMALL, new BlockSmallSolarPanel());
        register(registry, SOLAR_PANEL_LARGE, new BlockLargeSolarPanel(SOLAR_PANEL_LARGE));
        register(registry, BATTERY_SIMPLE, new BlockBattery(BATTERY_SIMPLE));

        registerModule(registry, ENTITY_DETECTOR_SIMPLE);
        registerModule(registry, ENTITY_MARKER);
        registerModule(registry, TERRAIN_SCANNER);
        registerModule(registry, WEATHER_SCANNER);

        register(registry, LASER, new BlockLaser(LASER));

        // Register module TE only once
        registerTileEntity(TileModule.class, namespace("module"));
        registerTileEntity(TileDummyModule.class, namespace("dummy_module"));
    }

    private static void registerModule(final IForgeRegistry<Block> registry, final String name) {
        registerModule(registry, Material.IRON, name);
    }

    private static void registerModule(final IForgeRegistry<Block> registry, final Material material, final String name) {
        register(registry, name, new BlockModule(material, name));
    }

    private static <T extends Block> void register(final IForgeRegistry<Block> registry, final String name, final T block) {
        val id = namespace(name);
        block.setRegistryName(id).setTranslationKey(namespace(name, '.'));
        checkState(ALL.add(block), "Already registered [%s: %s]", id, block);
        registry.register(block);
        if (block instanceof RegisterTileEntity) {
            registerTileEntity(((RegisterTileEntity) block).getEntityClass(), id);
        }
    }
}

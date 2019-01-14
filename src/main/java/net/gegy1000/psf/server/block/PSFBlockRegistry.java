package net.gegy1000.psf.server.block;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.api.RegisterItemBlock;
import net.gegy1000.psf.server.api.RegisterTileEntity;
import net.gegy1000.psf.server.block.controller.BlockController;
import net.gegy1000.psf.server.block.controller.ControllerType;
import net.gegy1000.psf.server.block.data.BlockDataViewer;
import net.gegy1000.psf.server.block.fueler.BlockFuelLoader;
import net.gegy1000.psf.server.block.module.*;
import net.gegy1000.psf.server.block.production.BlockAirCompressor;
import net.gegy1000.psf.server.block.production.BlockAirIntake;
import net.gegy1000.psf.server.block.production.BlockAirSeparator;
import net.gegy1000.psf.server.block.production.BlockKeroseneExtractor;
import net.gegy1000.psf.server.block.remote.BlockRemoteControlSystem;
import net.gegy1000.psf.server.fluid.PSFFluidRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = PracticalSpaceFireworks.MODID)
public class PSFBlockRegistry {
    private static final Set<Block> REGISTERED_BLOCKS = new LinkedHashSet<>();
    private static final Set<ItemBlock> REGISTERED_ITEM_BLOCKS = new LinkedHashSet<>();

    public static BlockFluidFinite kerosene;
    public static BlockFluidFinite liquidOxygen;
    public static BlockFluidFinite liquidNitrogen;
    public static BlockFluidFinite filteredAir;
    public static BlockFluidFinite compressedAir;

    public static BlockStrutFixed strut;
    public static BlockStrutOrientable strutSlope;

    public static BlockController basicController;
    public static BlockModule thruster;

    public static BlockRemoteControlSystem remoteControlSystem;
    public static BlockDataViewer dataViewer;
    public static BlockFuelLoader fuelLoader;
    public static BlockAirIntake airIntake;
    public static BlockAirCompressor airCompressor;
    public static BlockAirSeparator airSeparator;

    public static BlockFuelTank fuelTank;
    public static BlockPayloadSeparator payloadSeparator;
    public static BlockModule solarPanelSmall;
    public static BlockMultiblockModule solarPanelLarge;
    public static BlockMultiblockModule laser;

    @SubscribeEvent
    public static void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        register(event, "controller.simple", basicController = new BlockController(ControllerType.BASIC));

        // Modules
        strut = register(event, "strut_cube", new BlockStrutFixed("strut_cube"));
        strutSlope = register(event, "strut_slope", new BlockStrutOrientable.Slope("strut_slope"));
        register(event, "battery.simple", new BlockBattery("battery_simple"));
        thruster = register(event, "thruster.simple", new BlockThruster("thruster_simple"));
        registerModuleBlock(event, "entity_detector.simple");
        registerModuleBlock(event, "entity_marker");
        fuelTank = register(event, "fuel_tank", new BlockFuelTank());
        payloadSeparator = register(event, "payload_separator", new BlockPayloadSeparator());
        registerModuleBlock(event, "terrain_scanner");
        registerModuleBlock(event, "weather_scanner");
        solarPanelSmall = register(event, "solar_panel_small", new BlockSmallSolarPanel());
        solarPanelLarge = register(event, "solar_panel_large", new BlockMultiblockModule(Material.IRON, "solar_panel_large"));
        laser = register(event, "laser", new BlockMultiblockModule(Material.IRON, "laser") {

            @Override
            protected int getHeight() {
                return 2;
            }
        });

        kerosene = register(event, "kerosene", new BlockPSFFluid(PSFFluidRegistry.KEROSENE, Material.WATER));
        liquidOxygen = register(event, "liquid_oxygen", new BlockPSFFluid(PSFFluidRegistry.LIQUID_OXYGEN, Material.WATER));
        liquidNitrogen = register(event, "liquid_nitrogen", new BlockPSFFluid(PSFFluidRegistry.LIQUID_NITROGEN, Material.WATER));
        filteredAir = register(event, "filtered_air", new BlockPSFFluid(PSFFluidRegistry.FILTERED_AIR, Material.WATER));
        compressedAir = register(event, "compressed_air", new BlockPSFFluid(PSFFluidRegistry.COMPRESSED_AIR, Material.WATER));

        remoteControlSystem = register(event, "remote_control_system", new BlockRemoteControlSystem());
        dataViewer = register(event, "data_viewer", new BlockDataViewer());
        fuelLoader = register(event, "fuel_loader", new BlockFuelLoader());
        airIntake = register(event, "air_intake", new BlockAirIntake());
        airCompressor = register(event, "air_compressor", new BlockAirCompressor());
        airSeparator = register(event, "air_separator", new BlockAirSeparator());
        register(event, "kerosene_extractor", new BlockKeroseneExtractor());

        register(event, "fuel_valve", new BlockFuelValve());

        // Register module TE only once
        GameRegistry.registerTileEntity(TileModule.class, new ResourceLocation(PracticalSpaceFireworks.MODID, "module"));
        GameRegistry.registerTileEntity(TileDummyModule.class, new ResourceLocation(PracticalSpaceFireworks.MODID, "dummy_module"));
    }

    @SubscribeEvent
    public static void onRegisterItems(RegistryEvent.Register<Item> event) {
        for (Block block : REGISTERED_BLOCKS) {
            if (block instanceof RegisterItemBlock) {
                if (block.getRegistryName() == null) {
                    PracticalSpaceFireworks.LOGGER.warn("Tried to register ItemBlock for block without registry name!");
                    continue;
                }
                ItemBlock itemBlock = ((RegisterItemBlock) block).createItemBlock(block);
                event.getRegistry().register(itemBlock.setRegistryName(block.getRegistryName()));
                REGISTERED_ITEM_BLOCKS.add(itemBlock);
            }
        }
    }

    @SubscribeEvent
    public static void onMissingBlockMappings(RegistryEvent.MissingMappings<Block> event) {
        for (RegistryEvent.MissingMappings.Mapping<Block> mapping : event.getMappings()) {
            if (mapping.key.getPath().equals("strut")) {
                ResourceLocation key = new ResourceLocation(PracticalSpaceFireworks.MODID, "strut_cube");
                Block block = ForgeRegistries.BLOCKS.getValue(key);
                if (block != null) {
                    mapping.remap(block);
                    mapping.ignore();
                }
            }
        }
    }

    private static BlockModule registerModuleBlock(RegistryEvent.Register<Block> event, @Nonnull String identifier) {
        return registerModuleBlock(event, Material.IRON, identifier);
    }

    private static BlockModule registerModuleBlock(RegistryEvent.Register<Block> event, Material material, @Nonnull String identifier) {
        return register(event, identifier, new BlockModule(material, identifier.replace('.', '_')));
    }

    private static <T extends Block> T register(RegistryEvent.Register<Block> event, @Nonnull String identifier, T block) {
        event.getRegistry().register(block.setRegistryName(new ResourceLocation(PracticalSpaceFireworks.MODID, identifier.replace('.', '_'))));
        block.setTranslationKey(PracticalSpaceFireworks.MODID + "." + identifier);
        REGISTERED_BLOCKS.add(block);

        if (block instanceof RegisterTileEntity) {
            ResourceLocation blockEntityKey = new ResourceLocation(PracticalSpaceFireworks.MODID, identifier);
            GameRegistry.registerTileEntity(((RegisterTileEntity) block).getEntityClass(), blockEntityKey);
        }

        return block;
    }

    public static Set<Block> getRegisteredBlocks() {
        return Collections.unmodifiableSet(REGISTERED_BLOCKS);
    }

    public static Set<ItemBlock> getRegisteredItemBlocks() {
        return Collections.unmodifiableSet(REGISTERED_ITEM_BLOCKS);
    }
}

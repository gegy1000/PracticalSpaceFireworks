package net.gegy1000.psf.api.module;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.gegy1000.psf.api.PSFAPIProps;
import net.gegy1000.psf.api.spacecraft.ISatellite;
import net.gegy1000.psf.api.util.IUnique;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ParametersAreNonnullByDefault
public interface IModule extends IUnique, INBTSerializable<NBTTagCompound>, ICapabilityProvider {

    void setOwner(@Nullable ISatellite satellite);

    @Nullable
    ISatellite getOwner();

    default void handleModuleChange(Collection<IModule> modules) {
    }

    default void onSatelliteTick(ISatellite satellite) {
    }

    default int getTickInterval() {
        return 20;
    }

    String getName();
    
    default String getUnlocalizedName() {
        return String.format("tile.%s.module.%s", PSFAPIProps.MODID, getName());
    }

    @SideOnly(Side.CLIENT)
    default String getLocalizedName() {
        return I18n.format(getUnlocalizedName() + ".name");
    }

    @Nullable
    ResourceLocation getRegistryName();

    IModule setRegistryName(@Nullable ResourceLocation registryName);

    default <T extends IModuleData> Collection<T> getConnectedCaps(ISatellite satellite, Capability<T> capability) {
        return satellite.getModuleCaps(capability);
    }

    @Override
    default boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == PSFAPIProps.CAPABILITY_MODULE;
    }

    @Nullable
    @Override
    default <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (hasCapability(capability, facing)) {
            if (capability == PSFAPIProps.CAPABILITY_MODULE) {
                return PSFAPIProps.CAPABILITY_MODULE.cast(this);
            }
        }
        return null;
    }
    
    /* Module Settings */
    
    /**
     * Treat this similarly to equals(), except for fuzzy grouping of modules that can be listed as one.
     * <p>
     * Therefore, this must always be symmetric, so <code>a.groupWith(b) == b.groupWitih(a)</code> 
     */
    default boolean groupWith(IModule other) {
        return other.getClass() == getClass();
    }
    
    default Collection<IModuleConfig> getConfigs() {
        return Collections.emptyList();
    }
            
    @Nullable
    default IModuleConfig getConfig(String key) {
        return null;
    }
    
    /**
     * Updates will be reflected instantly, but the size of the list should be consistent for this instance.
     */
    default List<String> getSummary() {
        return Collections.emptyList();
    }

    /* Client Syncing */
    
    default boolean isDirty() {
        return false;
    }
    
    default void dirty(boolean dirty) {
        if (dirty) {
            ISatellite owner = getOwner();
            if (owner != null && !owner.isInvalid()) {
                owner.markDirty();
            }
        }
    }
    
    // TODO override this in most modules!!
    default NBTTagCompound getUpdateTag() {
        return serializeNBT();
    }
    
    default void readUpdateTag(NBTTagCompound tag) {
        deserializeNBT(tag);
    }

}

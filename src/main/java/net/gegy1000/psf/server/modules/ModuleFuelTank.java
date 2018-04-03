package net.gegy1000.psf.server.modules;

import com.google.common.collect.Lists;
import net.gegy1000.psf.api.IAdditionalMass;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.server.capability.CapabilityModuleData;
import net.gegy1000.psf.server.fluid.PSFFluidRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.FluidHandlerFluidMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class ModuleFuelTank extends EmptyModule implements IModule {
    private static final int CAPACITY = 250;

    private final FluidTank keroseneTank;
    private final FluidTank liquidOxygenTank;
    private final FuelFluidHandler storage;

    public ModuleFuelTank() {
        super("fuel_tank");

        this.keroseneTank = new FluidTank(PSFFluidRegistry.KEROSENE, CAPACITY, CAPACITY);
        this.liquidOxygenTank = new FluidTank(PSFFluidRegistry.LIQUID_OXYGEN, CAPACITY, CAPACITY);
        this.storage = new FuelFluidHandler();
        this.storage.addHandler(PSFFluidRegistry.KEROSENE, this.keroseneTank);
        this.storage.addHandler(PSFFluidRegistry.LIQUID_OXYGEN, this.liquidOxygenTank);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = super.serializeNBT();

        Capability<IFluidHandler> cap = CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
        tag.setTag("kerosene_tank", cap.getStorage().writeNBT(cap, this.keroseneTank, null));
        tag.setTag("liquid_oxygen_tank", cap.getStorage().writeNBT(cap, this.liquidOxygenTank, null));

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);

        Capability<IFluidHandler> cap = CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
        cap.getStorage().readNBT(cap, this.keroseneTank, null, nbt.getTag("kerosene_tank"));
        cap.getStorage().readNBT(cap, this.liquidOxygenTank, null, nbt.getTag("liquid_oxygen_tank"));
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return super.hasCapability(capability, facing) || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(storage);
        } else if (capability == CapabilityModuleData.ADDITIONAL_MASS) {
            return CapabilityModuleData.ADDITIONAL_MASS.cast(this.storage);
        }
        return super.getCapability(capability, facing);
    }

    private class FuelFluidHandler extends FluidHandlerFluidMap implements IAdditionalMass {
        @Override
        public IFluidTankProperties[] getTankProperties() {
            List<IFluidTankProperties> tanks = Lists.newArrayList();
            for (Map.Entry<Fluid, IFluidHandler> entry : handlers.entrySet()) {
                IFluidTankProperties[] tankProperties = entry.getValue().getTankProperties();
                for (IFluidTankProperties properties : tankProperties) {
                    if (properties.getContents() != null) {
                        tanks.add(properties);
                    } else {
                        tanks.add(new IFluidTankProperties() {
                            @Override
                            public FluidStack getContents() {
                                return new FluidStack(entry.getKey(), 0);
                            }

                            @Override
                            public int getCapacity() {
                                return properties.getCapacity();
                            }

                            @Override
                            public boolean canFill() {
                                return true;
                            }

                            @Override
                            public boolean canDrain() {
                                return true;
                            }

                            @Override
                            public boolean canFillFluidType(FluidStack fluidStack) {
                                return fluidStack.getFluid() == entry.getKey();
                            }

                            @Override
                            public boolean canDrainFluidType(FluidStack fluidStack) {
                                return fluidStack.getFluid() == entry.getKey();
                            }
                        });
                    }
                }
            }
            return tanks.toArray(new IFluidTankProperties[0]);
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            int fill = super.fill(resource, doFill);
            if (fill > 0) {
                dirty(true);
            }
            return fill;
        }

        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            FluidStack drain = super.drain(resource, doDrain);
            if (drain != null && drain.amount > 0) {
                dirty(true);
            }
            return drain;
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            FluidStack drain = super.drain(maxDrain, doDrain);
            if (drain != null && drain.amount > 0) {
                dirty(true);
            }
            return drain;
        }

        @Override
        public double getAdditionalMass() {
            double additonalMass = 0.0;
            for (Map.Entry<Fluid, IFluidHandler> handler : handlers.entrySet()) {
                int density = handler.getKey().getDensity() * 1000;

                IFluidTankProperties properties = handler.getValue().getTankProperties()[0];
                FluidStack contents = properties.getContents();
                if (contents != null) {
                    additonalMass += (double) contents.amount / properties.getCapacity() * density;
                }
            }

            return additonalMass;
        }
    }
}

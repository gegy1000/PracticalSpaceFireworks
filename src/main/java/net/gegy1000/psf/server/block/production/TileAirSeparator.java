package net.gegy1000.psf.server.block.production;

import lombok.AllArgsConstructor;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.block.production.state.StateMachine;
import net.gegy1000.psf.server.block.production.state.StateMachineBuilder;
import net.gegy1000.psf.server.block.production.state.StateType;
import net.gegy1000.psf.server.capability.MultiTankFluidHandler;
import net.gegy1000.psf.server.capability.TypedFluidTank;
import net.gegy1000.psf.server.init.PSFFluids;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@ParametersAreNonnullByDefault
public class TileAirSeparator extends TileEntity implements ITickable {
    private static final double OXYGEN_AMOUNT = 0.21;
    private static final double NITROGEN_AMOUNT = 0.78;

    private static final int TANK_SIZE = 1000;
    private static final int DISTILL_PER_TICK = 2;

    public static final StateType FILLING_STATE = new StateType("filling");
    public static final StateType DISTILLING_STATE = new StateType("distilling");
    public static final StateType DRAINING_STATE = new StateType("draining");

    private static final StateMachineBuilder<StepCtx> STATE_MACHINE_BUILDER = new StateMachineBuilder<StepCtx>()
            .withInitState(DRAINING_STATE)
            .withStep(FILLING_STATE, ctx -> {
                if (ctx.inputContents != null && ctx.inputContents.amount >= ctx.inputProperties.getCapacity()) {
                    return DISTILLING_STATE;
                }
                return FILLING_STATE;
            })
            .withStep(DISTILLING_STATE, ctx -> {
                MasterInfo master = ctx.master;
                if (ctx.inputContents == null || ctx.inputContents.amount <= 0) {
                    master.combinedOxygen.fill(new FluidStack(PSFFluids.liquidOxygen(), (int) Math.round(master.oxygenRemainder)), true);
                    master.combinedNitrogen.fill(new FluidStack(PSFFluids.liquidNitrogen(), (int) Math.round(master.nitrogenRemainder)), true);
                    master.oxygenRemainder = 0.0;
                    master.nitrogenRemainder = 0.0;
                    return DRAINING_STATE;
                }

                FluidStack drainedInput = master.combinedInput.drain(DISTILL_PER_TICK, true);
                if (drainedInput != null && drainedInput.amount > 0) {
                    double oxygenAmount = drainedInput.amount * OXYGEN_AMOUNT + master.oxygenRemainder;
                    double nitrogenAmount = drainedInput.amount * NITROGEN_AMOUNT + master.nitrogenRemainder;
                    int oxygenFillAmount = MathHelper.floor(oxygenAmount);
                    int nitrogenFillAmount = MathHelper.floor(nitrogenAmount);
                    master.oxygenRemainder = oxygenAmount - oxygenFillAmount;
                    master.nitrogenRemainder = nitrogenAmount - nitrogenFillAmount;

                    // TODO: What if these outputs are full?
                    master.combinedOxygen.fill(new FluidStack(PSFFluids.liquidOxygen(), oxygenFillAmount), true);
                    master.combinedNitrogen.fill(new FluidStack(PSFFluids.liquidNitrogen(), nitrogenFillAmount), true);
                }

                return DISTILLING_STATE;
            })
            .withStep(DRAINING_STATE, ctx -> {
                FluidStack nitrogenContents = ctx.master.combinedNitrogen.getTankProperties()[0].getContents();
                FluidStack oxygenContents = ctx.master.combinedOxygen.getTankProperties()[0].getContents();
                if ((nitrogenContents == null || nitrogenContents.amount <= 0) && (oxygenContents == null || oxygenContents.amount <= 0)) {
                    return FILLING_STATE;
                }

                return DRAINING_STATE;
            });

    private final IFluidHandler localInput = new TypedFluidTank(TANK_SIZE, PSFFluids.compressedAir());
    private final IFluidHandler localOxygen = new TypedFluidTank(MathHelper.floor(TANK_SIZE * OXYGEN_AMOUNT), PSFFluids.liquidOxygen());
    private final IFluidHandler localNitrogen = new TypedFluidTank(MathHelper.floor(TANK_SIZE * NITROGEN_AMOUNT), PSFFluids.liquidNitrogen());

    private MasterInfo masterInfo = null;
    private final List<TileAirSeparator> connected = new ArrayList<>();

    private boolean connectedDirty = true;

    @Override
    public void update() {
        if (connectedDirty || connected.stream().anyMatch(TileEntity::isInvalid)) {
            scanConnected();
        }

        if (!world.isRemote && masterInfo != null) {
            masterInfo.update(world);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setTag("input_fluid", CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.writeNBT(localInput, null));
        compound.setTag("output_oxygen", CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.writeNBT(localOxygen, null));
        compound.setTag("output_nitrogen", CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.writeNBT(localNitrogen, null));
        if (masterInfo != null && masterInfo.isMaster(this)) {
            compound.setTag("master", masterInfo.serialize(new NBTTagCompound()));
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.readNBT(localInput, null, compound.getTag("input_fluid"));
        CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.readNBT(localOxygen, null, compound.getTag("output_oxygen"));
        CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.readNBT(localNitrogen, null, compound.getTag("output_nitrogen"));
        if (compound.hasKey("master")) {
            masterInfo = MasterInfo.deserialize(compound.getCompoundTag("master"));
        }
        connectedDirty = true;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.readFromNBT(tag);
    }

    @Nonnull
    private EnumFacing getFacing() {
        return world.getBlockState(getPos()).getValue(BlockHorizontal.FACING);
    }

    private boolean canDoFluid(@Nullable EnumFacing facing) {
        return facing == null || facing.getAxis().isVertical() || facing.getAxis() == getFacing().rotateY().getAxis();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return masterInfo != null && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && canDoFluid(facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (masterInfo != null && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && canDoFluid(facing)) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(getFluidHandler(masterInfo, facing));
        }
        return super.getCapability(capability, facing);
    }

    private IFluidHandler getFluidHandler(MasterInfo masterInfo, @Nullable EnumFacing facing) {
        if (facing == null) {
            return masterInfo.combinedStorage;
        }

        if (facing.getAxis() == EnumFacing.Axis.Y && masterInfo.stateMachine.getState() == FILLING_STATE) {
            return masterInfo.combinedInput;
        } else if (masterInfo.stateMachine.getState() == DRAINING_STATE) {
            EnumFacing output = getFacing().rotateY();
            if (facing == output) {
                return masterInfo.combinedNitrogen;
            } else if (facing == output.getOpposite()) {
                return masterInfo.combinedOxygen;
            }
        }

        return EmptyFluidHandler.INSTANCE;
    }

    public List<TileAirSeparator> getConnectedSeparators() {
        if (connected.isEmpty() || connectedDirty) {
            scanConnected();
        }
        return Collections.unmodifiableList(connected);
    }

    private void scanConnected() {
        connected.clear();
        connectedDirty = false;

        BlockPos pos = getPos();

        TileEntity entity;
        while ((entity = world.getTileEntity(pos)) instanceof TileAirSeparator) {
            connected.add((TileAirSeparator) entity);
            pos = pos.offset(EnumFacing.UP);
        }

        pos = getPos().down();
        while ((entity = world.getTileEntity(pos)) instanceof TileAirSeparator) {
            connected.add((TileAirSeparator) entity);
            pos = pos.offset(EnumFacing.DOWN);
        }

        if (connected.isEmpty()) {
            PracticalSpaceFireworks.LOGGER.warn("Found no air separator connections! This should never happen!");
            return;
        }

        // Choose the separator with the lowest Y to be the master as that is consistent across all calls
        TileAirSeparator master = connected.stream().min(Comparator.comparingInt(te -> te.pos.getY())).get();

        if (getPos().equals(master.getPos())) {
            if (masterInfo == null) {
                masterInfo = new MasterInfo(getPos());
            }
            masterInfo.transferTo(this);
            masterInfo.buildTotalStorage(connected);
        } else {
            if (master.connectedDirty) {
                master.scanConnected();
            }
            masterInfo = master.masterInfo;
        }
    }

    public void markConnectedDirty() {
        connectedDirty = true;
    }

    @AllArgsConstructor
    static class StepCtx {
        final MasterInfo master;
        final IFluidTankProperties inputProperties;
        @Nullable
        final FluidStack inputContents;
    }

    private static class MasterInfo {
        private BlockPos masterPos;

        private IFluidHandler combinedInput = EmptyFluidHandler.INSTANCE;
        private IFluidHandler combinedNitrogen = EmptyFluidHandler.INSTANCE;
        private IFluidHandler combinedOxygen = EmptyFluidHandler.INSTANCE;
        private IFluidHandler combinedStorage = EmptyFluidHandler.INSTANCE;

        private final StateMachine<StepCtx> stateMachine = STATE_MACHINE_BUILDER.build();

        private double oxygenRemainder;
        private double nitrogenRemainder;

        private MasterInfo(BlockPos masterPos) {
            this.masterPos = masterPos;
        }

        void transferTo(TileAirSeparator tile) {
            this.masterPos = tile.pos;
        }

        void update(World world) {
            IFluidTankProperties inputProperties = combinedInput.getTankProperties()[0];
            FluidStack inputContents = inputProperties.getContents();

            StepCtx ctx = new StepCtx(this, inputProperties, inputContents);
            stateMachine.update(ctx, world.getTotalWorldTime());
        }

        void buildTotalStorage(List<TileAirSeparator> connected) {
            List<IFluidHandler> inputHandlers = new ArrayList<>();
            List<IFluidHandler> nitrogenHandlers = new ArrayList<>();
            List<IFluidHandler> oxygenHandlers = new ArrayList<>();
            for (TileAirSeparator separator : connected) {
                inputHandlers.add(separator.localInput);
                nitrogenHandlers.add(separator.localNitrogen);
                oxygenHandlers.add(separator.localOxygen);
            }

            List<IFluidHandler> allHandlers = new ArrayList<>(inputHandlers.size() + nitrogenHandlers.size() + oxygenHandlers.size());
            allHandlers.addAll(inputHandlers);
            allHandlers.addAll(nitrogenHandlers);
            allHandlers.addAll(oxygenHandlers);

            combinedStorage = new MultiTankFluidHandler(allHandlers);
            combinedInput = new MultiTankFluidHandler(inputHandlers);
            combinedNitrogen = new MultiTankFluidHandler(nitrogenHandlers);
            combinedOxygen = new MultiTankFluidHandler(oxygenHandlers);
        }

        boolean isMaster(TileAirSeparator separator) {
            return separator.pos.equals(this.masterPos);
        }

        NBTTagCompound serialize(NBTTagCompound compound) {
            compound.setInteger("x", masterPos.getX());
            compound.setInteger("y", masterPos.getY());
            compound.setInteger("z", masterPos.getZ());
            compound.setDouble("oxygen_remainder", oxygenRemainder);
            compound.setDouble("nitrogen_remainder", nitrogenRemainder);
            compound.setTag("state", stateMachine.serialize(new NBTTagCompound()));
            return compound;
        }

        static MasterInfo deserialize(NBTTagCompound compound) {
            BlockPos masterPos = new BlockPos(compound.getInteger("x"), compound.getInteger("y"), compound.getInteger("z"));
            MasterInfo info = new MasterInfo(masterPos);
            info.oxygenRemainder = compound.getDouble("oxygen_remainder");
            info.nitrogenRemainder = compound.getDouble("nitrogen_remainder");
            info.stateMachine.deserialize(compound.getCompoundTag("state"));
            return info;
        }
    }
}

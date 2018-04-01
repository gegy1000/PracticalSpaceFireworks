package net.gegy1000.psf.server.block.remote;

import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftBlockAccess;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;

@ParametersAreNonnullByDefault
public interface IListedSpacecraft {
    @Nonnull
    String getName();

    void setName(@Nonnull String name);

    @Nonnull
    BlockPos getPosition();

    void requestVisualData();

    class Visual {
        private final SpacecraftBlockAccess blockAccess;
        private final Collection<IModule> modules;

        public Visual(SpacecraftBlockAccess blockAccess, Collection<IModule> modules) {
            this.blockAccess = blockAccess;
            this.modules = modules;
        }

        @Nonnull
        public SpacecraftBlockAccess getBlockAccess() {
            return this.blockAccess;
        }

        @Nonnull
        public Collection<IModule> getModules() {
            return this.modules;
        }
    }
}

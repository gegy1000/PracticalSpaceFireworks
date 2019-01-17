package net.gegy1000.psf.server.block;

import net.gegy1000.psf.server.sound.PSFSounds;
import net.minecraft.block.SoundType;
import net.minecraft.init.SoundEvents;

public final class PSFSoundType {
    public static final SoundType SOLAR_PANEL = new SoundType(
            1.0F, 1.8F,
            SoundEvents.BLOCK_METAL_BREAK,
            SoundEvents.BLOCK_METAL_STEP,
            SoundEvents.BLOCK_METAL_PLACE,
            SoundEvents.BLOCK_METAL_HIT,
            SoundEvents.BLOCK_METAL_FALL
    );

    public static final SoundType METAL = new SoundType(
            1.0F, 1.75F,
            PSFSounds.METAL_BREAK,
            PSFSounds.METAL_STEP,
            PSFSounds.METAL_PLACE,
            PSFSounds.METAL_HIT,
            PSFSounds.METAL_FALL
    );

    private PSFSoundType() {
    }
}
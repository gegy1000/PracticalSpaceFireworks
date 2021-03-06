package net.gegy1000.psf.server.modules.configs;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.base.Strings;

import lombok.AccessLevel;
import lombok.Getter;
import net.minecraft.nbt.NBTTagCompound;

@ParametersAreNonnullByDefault
public class ConfigBasicToggle extends AbstractConfig {

    private final String[] states;
    
    @Getter(AccessLevel.PROTECTED)
    private int state;
    
    public ConfigBasicToggle(String key, int defaultState, String... states) {
        super(key);
        this.state = defaultState;
        this.states = states;
    }
    
    @Override
    public String getValue() {
        return Strings.nullToEmpty(states[state]);
    }
    
    @Override
    public void modified(@Nullable Object newValue) {
        state = (state + 1) % states.length;
    }

    @Override
    public ConfigType getType() {
        return ConfigType.TOGGLE;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("state", state);
        return tag;
    }

    @Override
    public void deserializeNBT(@Nullable NBTTagCompound tag) {
        if (tag != null) {
            this.state = tag.getInteger("state");
        }
    }
}

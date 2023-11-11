package net.ellivers.pettable.api;

import net.minecraft.sound.SoundEvent;

public class SoundEventArgs {
    public SoundEvent soundEvent;
    public float volume;
    public float pitch;

    public SoundEventArgs(SoundEvent soundEvent, float volume, float pitch) {
        this.soundEvent = soundEvent;
        this.volume = volume;
        this.pitch = pitch;
    }
}

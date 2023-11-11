package net.ellivers.pettable.api;

import net.ellivers.pettable.Pettable;
import net.minecraft.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;

public interface PettableAPI {
    static HashMap<EntityType<?>, SoundEventArgs> getExistingSoundExceptions() {
        return Pettable.getSoundExceptions();
    }

    default HashMap<EntityType<?>, SoundEventArgs> getSoundExceptions() {
        return new HashMap<>();
    };
}

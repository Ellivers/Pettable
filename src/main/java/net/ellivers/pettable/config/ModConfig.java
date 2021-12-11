package net.ellivers.pettable.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

import static net.ellivers.pettable.Pettable.MOD_ID;

@Config(name = MOD_ID)
public class ModConfig implements ConfigData {

    public boolean heal_owner = true;

}

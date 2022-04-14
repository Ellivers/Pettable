package net.ellivers.pettable;

import net.ellivers.pettable.config.MidnightConfig;
import net.ellivers.pettable.config.ModConfig;
import net.fabricmc.api.ModInitializer;

public class Pettable implements ModInitializer {

	public static final String MOD_ID = "pettable";

	@Override
	public void onInitialize() {
		MidnightConfig.init(MOD_ID, ModConfig.class);
	}
}

package net.ellivers.pettable;

import net.ellivers.pettable.config.ModConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

import java.io.IOException;

public class Pettable implements ModInitializer {

	public static final String MOD_ID = "pettable";

	// exceptions
	public static Tag<EntityType<?>> NOT_PETTABLE;
	// can only be pet if baby
	public static Tag<EntityType<?>> NOT_PETTABLE_ADULT;

	@Override
	public void onInitialize() {
		NOT_PETTABLE = TagRegistry.entityType(new Identifier("pettable", "not_pettable"));
		NOT_PETTABLE_ADULT = TagRegistry.entityType(new Identifier("pettable", "not_pettable_adult"));

		try {
			ModConfig.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ModConfig.save();
	}
}

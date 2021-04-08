package net.ellivers.pettable;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public class Pettable implements ModInitializer {

	// exceptions
	public static Tag<EntityType<?>> NOT_PETTABLE;
	// can only be pet if baby
	public static Tag<EntityType<?>> NOT_PETTABLE_ADULT;

	@Override
	public void onInitialize() {
		NOT_PETTABLE = TagRegistry.entityType(new Identifier("pettable", "not_pettable"));
		NOT_PETTABLE_ADULT = TagRegistry.entityType(new Identifier("pettable", "not_pettable_adult"));
	}
}

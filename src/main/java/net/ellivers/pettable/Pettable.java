package net.ellivers.pettable;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.ellivers.pettable.config.ModConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.entity.EntityType;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public class Pettable implements ModInitializer {

	public static final String MOD_ID = "pettable";

	// exceptions
	public static Tag<EntityType<?>> NOT_PETTABLE;
	// can only be pet if baby
	public static Tag<EntityType<?>> NOT_PETTABLE_ADULT;

	@Override
	public void onInitialize() {
		NOT_PETTABLE = TagFactory.ENTITY_TYPE.create(new Identifier("pettable", "not_pettable"));
		NOT_PETTABLE_ADULT = TagFactory.ENTITY_TYPE.create(new Identifier("pettable", "not_pettable_adult"));

		AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);

	}
}

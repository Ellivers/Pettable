package net.ellivers.pettable;

import net.minecraft.entity.EntityType;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static net.ellivers.pettable.Pettable.MOD_ID;

public class EntityTags {
    // not pettable
    public static final TagKey<EntityType<?>> NOT_PETTABLE = TagKey.of(Registry.ENTITY_TYPE_KEY, new Identifier(MOD_ID, "not_pettable"));
    // only pettable as babies
    public static final TagKey<EntityType<?>> NOT_PETTABLE_ADULT = TagKey.of(Registry.ENTITY_TYPE_KEY, new Identifier(MOD_ID, "not_pettable_adult"));
}

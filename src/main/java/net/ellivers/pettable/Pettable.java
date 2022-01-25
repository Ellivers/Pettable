package net.ellivers.pettable;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.ellivers.pettable.config.ModConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.network.ServerPlayerEntity;
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

		ServerPlayNetworking.registerGlobalReceiver(new Identifier(MOD_ID,"client_pet"), (server, player, handler, buf, sender) -> {
			Entity target = player.getEntityWorld().getEntityById(buf.readInt());
			server.execute(() -> {
				System.out.println("pettable: if this shows up in the client log something went wrong");
				for (ServerPlayerEntity entity: PlayerLookup.tracking(target))
					ServerPlayNetworking.send(entity,new Identifier(MOD_ID,"server_pet"),buf);

			});

		});
	}
}

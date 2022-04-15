package net.ellivers.pettable;

import net.ellivers.pettable.config.MidnightConfig;
import net.ellivers.pettable.config.ModConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PufferfishEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.UUID;

public class Pettable implements ModInitializer {

	public static final String MOD_ID = "pettable";

	// not pettable
	public static final TagKey<EntityType<?>> NOT_PETTABLE = TagKey.of(Registry.ENTITY_TYPE_KEY, new Identifier(MOD_ID, "not_pettable"));
	// only pettable as babies
	public static final TagKey<EntityType<?>> NOT_PETTABLE_ADULT = TagKey.of(Registry.ENTITY_TYPE_KEY, new Identifier(MOD_ID, "not_pettable_adult"));

	@Override
	public void onInitialize() {

		MidnightConfig.init(MOD_ID, ModConfig.class);

		ServerPlayNetworking.registerGlobalReceiver(new Identifier(MOD_ID, "client_pet"), (server, player, handler, buf, responseSender) -> {
			boolean petPlayer = buf.readBoolean();

			PacketByteBuf buf1 = PacketByteBufs.create();
			buf1.writeBoolean(petPlayer);

			Entity entity;
			if (petPlayer) {
				UUID uuid = buf.readUuid();
				entity = player.getWorld().getPlayerByUuid(uuid);
				buf1.writeUuid(uuid);
			}
			else {
				int bruh = buf.readInt();
				entity = player.getWorld().getEntityById(bruh);
				buf1.writeInt(bruh);
			}

			if (entity != null) {
				if (ModConfig.heal_owner && entity instanceof TameableEntity && ((TameableEntity) entity).isOwner(player)) {
					((TameableEntity) entity).heal(2);
					player.heal(2);

					PacketByteBuf buf2 = PacketByteBufs.create();
					buf2.writeUuid(PlayerEntity.getUuidFromProfile(player.getGameProfile()));
					for (ServerPlayerEntity player1 : PlayerLookup.tracking(player.getWorld(), player.getBlockPos())) {
						ServerPlayNetworking.send(player1, new Identifier(MOD_ID, "player_heal"), buf2);
					}
				}
				// Funni puffer sting
				if (entity instanceof PufferfishEntity && ((PufferfishEntity) entity).getPuffState() > 0) {
					entity.onPlayerCollision(player);
				}
				else {
					for (ServerPlayerEntity player1 : PlayerLookup.tracking((ServerWorld) entity.getEntityWorld(), entity.getBlockPos())) {
						ServerPlayNetworking.send(player1, new Identifier(MOD_ID, "server_pet"), buf1);
					}
				}
				player.networkHandler.sendPacket(new EntityAnimationS2CPacket(player, 0));
			}
		});

	}
}

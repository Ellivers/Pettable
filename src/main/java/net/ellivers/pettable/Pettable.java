package net.ellivers.pettable;

import net.ellivers.pettable.api.PettableAPI;
import net.ellivers.pettable.api.SoundEventArgs;
import net.ellivers.pettable.config.MidnightConfig;
import net.ellivers.pettable.config.ModConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.PufferfishEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class Pettable implements ModInitializer {

	public static final String MOD_ID = "pettable";
	public static final Logger LOGGGER = LoggerFactory.getLogger("Pettable");

	// pettable even if the entity type otherwise wouldn't be allowed
	public static final TagKey<EntityType<?>> PETTABLE_ANYWAY = TagKey.of(RegistryKeys.ENTITY_TYPE, new Identifier(MOD_ID, "pettable_anyway"));
	// not pettable
	public static final TagKey<EntityType<?>> NOT_PETTABLE = TagKey.of(RegistryKeys.ENTITY_TYPE, new Identifier(MOD_ID, "not_pettable"));
	// only pettable as babies
	public static final TagKey<EntityType<?>> NOT_PETTABLE_ADULT = TagKey.of(RegistryKeys.ENTITY_TYPE, new Identifier(MOD_ID, "not_pettable_adult"));


	private static final HashMap<EntityType<?>, SoundEventArgs> SoundExceptions = new HashMap<>();

	public static HashMap<EntityType<?>, SoundEventArgs> getSoundExceptions() {
		return SoundExceptions;
	}

	static {
		SoundExceptions.put(EntityType.SNIFFER, new SoundEventArgs(SoundEvents.ENTITY_SNIFFER_HAPPY, 1.0F, 1.0F));
	}

	private boolean areNotSamePlayer(PlayerEntity player1, PlayerEntity player2) {
		return !player1.getGameProfile().getId().equals(player2.getGameProfile().getId());
	}

	private final Random petRandom = new Random();
	@Override
	public void onInitialize() {
		FabricLoader.getInstance().getEntrypointContainers("pettable", PettableAPI.class).forEach(entrypoint -> {
			ModMetadata metadata = entrypoint.getProvider().getMetadata();
			String modId = metadata.getId();
			try {
				PettableAPI entry = entrypoint.getEntrypoint();
				SoundExceptions.putAll(entry.getSoundExceptions());
			}
			catch (Throwable e) {
				LOGGGER.error("Mod \"" + modId + "\" has errors in its API usage!", e);
			}
		});

		MidnightConfig.init(MOD_ID, ModConfig.class);

		ServerPlayNetworking.registerGlobalReceiver(new Identifier(MOD_ID, "client_pet"), (server, player, handler, buf, responseSender) -> {
			World world = player.getEntityWorld();

			boolean petPlayer = buf.readBoolean();

			PacketByteBuf buf1 = PacketByteBufs.create();
			buf1.writeUuid(player.getGameProfile().getId());
			buf1.writeBoolean(petPlayer);

			Entity entity;
			if (petPlayer) {
				UUID uuid = buf.readUuid();
				entity = world.getPlayerByUuid(uuid);
				buf1.writeUuid(uuid);
			}
			else {
				int bruh = buf.readInt();
				entity = world.getEntityById(bruh);
				buf1.writeInt(bruh);
			}

			if (entity != null) {
				if (entity instanceof SlimeEntity) {
					int i = ((SlimeEntity) entity).getSize();
					if (!entity.isSilent()) entity.playSound(SoundEvents.ENTITY_SLIME_SQUISH_SMALL, 0.4F * (float)i, ((petRandom.nextFloat() - petRandom.nextFloat()) * 0.2F + 1.0F) / 0.8F);
				}

				if (entity instanceof MobEntity && !entity.isSilent()) {
					((MobEntity) entity).ambientSoundChance = -((MobEntity) entity).getMinAmbientSoundDelay();
					SoundEventArgs exception = getSoundExceptions().get(entity.getType());
					if (exception == null) {
						((MobEntity) entity).playAmbientSound();
					}
					else entity.playSound(exception.soundEvent, exception.volume, exception.pitch);
				}

				if (ModConfig.heal_owner && entity instanceof TameableEntity && ((TameableEntity) entity).isOwner(player)) {
					((TameableEntity) entity).heal(2);
					player.heal(2);

					PacketByteBuf buf2 = PacketByteBufs.create();
					buf2.writeUuid(player.getGameProfile().getId());
					for (ServerPlayerEntity player1 : PlayerLookup.tracking((ServerWorld) world, player.getBlockPos())) {
						if (areNotSamePlayer(player, player1))
							ServerPlayNetworking.send(player1, new Identifier(MOD_ID, "player_heal"), buf2);
					}
				}
				// Funni puffer sting
				if (entity instanceof PufferfishEntity && ((PufferfishEntity) entity).getPuffState() > 0) {
					entity.onPlayerCollision(player);
				}
				for (ServerPlayerEntity player1 : PlayerLookup.tracking((ServerWorld) entity.getEntityWorld(), entity.getBlockPos())) {
					if (areNotSamePlayer(player, player1))
						ServerPlayNetworking.send(player1, new Identifier(MOD_ID, "server_pet"), buf1);
				}
			}
		});

	}
}

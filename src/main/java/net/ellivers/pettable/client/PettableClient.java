package net.ellivers.pettable.client;

import net.ellivers.pettable.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.PufferfishEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

import static net.ellivers.pettable.Pettable.*;


public class PettableClient implements ClientModInitializer {
    private static KeyBinding keyBinding;
    private int petCooldown;

    @Override
    public void onInitializeClient() {
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key." + MOD_ID + ".pet",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,
                "key.categories." + MOD_ID
        ));

        ClientPlayNetworking.registerGlobalReceiver(new Identifier(MOD_ID,"server_pet"), (client, handler, buf, responseSender) -> {
            PlayerEntity petter = handler.getWorld().getPlayerByUuid(buf.readUuid());
            boolean petPlayer = buf.readBoolean();

            Entity target = petPlayer ? handler.getWorld().getPlayerByUuid(buf.readUuid()) : handler.getWorld().getEntityById(buf.readInt());

            client.execute(() -> {
                petter.swingHand(petter.getActiveHand());
                if (shouldShowEffects(target)) pettingEffects(client.world, target);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(new Identifier(MOD_ID, "player_heal"), (client, handler, buf, responseSender) -> {
            Entity target = handler.getWorld().getPlayerByUuid(buf.readUuid());
            client.execute(() -> pettingEffects(client.world, target));
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (petCooldown > 0) petCooldown--;
            else if (keyBinding.isPressed() && client.player != null) {
                client.execute(this::attemptPet);
            }
        });
    }

    private boolean shouldShowEffects(Entity entity) {
        return !(entity instanceof PufferfishEntity && ((PufferfishEntity) entity).getPuffState() > 0);
    }

    private void attemptPet() {
        MinecraftClient client = MinecraftClient.getInstance();
        HitResult hit = client.crosshairTarget;

        if (hit != null && hit.getType().equals(HitResult.Type.ENTITY)) {
            Entity entity = ((EntityHitResult) hit).getEntity();
            PlayerEntity playerEntity = client.player;
            assert playerEntity != null;
            Hand hand = playerEntity.getActiveHand();

            EntityType<?> type = entity.getType();

            /* Whoa, that's a huge if-check
               Here's how it works.
               It checks if the entity:
               * Is an instance of PassiveEntity, AmbientEntity, PlayerEntity, or WaterCreatureEntity
               OR
               * Is a small SlimeEntity and not an instance of MagmaCubeEntity
               Then, if the former check was true, it does the following:
               * Makes sure the entity isn't a player while the config doesn't allow player petting
               * Makes sure the entity isn't in the not_pettable_adult tag and is an adult
               ALL of the checks above don't matter if the entity is in the pettable_anyway tag
               Finally, it does the following:
               * Makes sure the entity isn't in the not_pettable tag
               * Checks if the player is allowed to pet (correct gamemode and isn't holding anything)

               Thanks for coming to my TED-talk.
             */
            if (((( entity instanceof PassiveEntity || entity instanceof AmbientEntity || entity instanceof PlayerEntity || entity instanceof WaterCreatureEntity
            || (entity instanceof SlimeEntity && !(entity instanceof MagmaCubeEntity) && ((SlimeEntity) entity).isSmall()) ) && !(entity instanceof PlayerEntity && !ModConfig.pet_players)
            && !(type.isIn(NOT_PETTABLE_ADULT) && entity instanceof MobEntity && !((MobEntity) entity).isBaby())) || type.isIn(PETTABLE_ANYWAY)) && !type.isIn(NOT_PETTABLE) && playerCanPet(playerEntity, hand)) {

                // Special case for angery entities
                if (!(entity instanceof PlayerEntity) && entity instanceof Angerable) {
                    if (!((Angerable) entity).hasAngerTime()) {
                        successfullyPet(entity);
                    }
                } else {
                    successfullyPet(entity);
                }

            }
        }
    }

    private boolean playerCanPet(PlayerEntity player, Hand hand) {
        return !player.isSpectator() && player.getStackInHand(hand).isEmpty() && player.getMainHandStack().isEmpty();
    }

    private void successfullyPet(Entity entity) {
        this.petCooldown = ModConfig.petting_cooldown;

        PlayerEntity playerEntity = MinecraftClient.getInstance().player;
        if (ModConfig.heal_owner && entity instanceof TameableEntity && ((TameableEntity) entity).isOwner(playerEntity)) {
            pettingEffects(playerEntity.getEntityWorld(), playerEntity);
        }
        if (shouldShowEffects(entity)) pettingEffects(entity.getEntityWorld(), entity);
        playerEntity.swingHand(playerEntity.getActiveHand());

        networkPet(entity);
    }

    private void networkPet(Entity entity) {
        PacketByteBuf buf = PacketByteBufs.create();
        if (entity instanceof PlayerEntity){
            buf.writeBoolean(true);
            buf.writeUuid(((PlayerEntity)entity).getGameProfile().getId());
        } else {
            buf.writeBoolean(false);
            buf.writeInt(entity.getId());
        }
        ClientPlayNetworking.send(new Identifier(MOD_ID, "client_pet"), buf);
    }

    private final Random petRandom = new Random();
    private void pettingEffects(World world, Entity entity) {
        if (entity instanceof SlimeEntity) {
            int i = ((SlimeEntity) entity).getSize();

            if (!entity.isInvisible()) for(int j = 0; j < i * 8; ++j) {
                float f = petRandom.nextFloat() * 6.2831855F;
                float g = petRandom.nextFloat() * 0.5F + 0.5F;
                float h = MathHelper.sin(f) * (float)i * 0.5F * g;
                float k = MathHelper.cos(f) * (float)i * 0.5F * g;
                world.addParticle(ParticleTypes.ITEM_SLIME, entity.getX() + (double)h, entity.getY(), entity.getZ() + (double)k, 0.0D, 0.0D, 0.0D);
            }

            ((SlimeEntity) entity).targetStretch = -0.5F;
        }
        double d = petRandom.nextGaussian() * 0.02D;
        double e = petRandom.nextGaussian() * 0.02D;
        double f = petRandom.nextGaussian() * 0.02D;
        for (int k = 0; k < 3; ++k)
            world.addParticle(ParticleTypes.HEART, entity.getParticleX(1.0D), entity instanceof PlayerEntity ? entity.getY() + 0.5D : entity.getEyeY(), entity.getParticleZ(1.0D), d, e, f);
    }
}

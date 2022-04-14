package net.ellivers.pettable.mixin;

import net.ellivers.pettable.config.ModConfig;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.PufferfishEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

import static net.ellivers.pettable.EntityTags.NOT_PETTABLE;
import static net.ellivers.pettable.EntityTags.NOT_PETTABLE_ADULT;
import static net.ellivers.pettable.Pettable.*;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements Angerable {

    private final Random petRandom = new Random();
    private int petCooldown;

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        if (this.petCooldown > 0) --petCooldown;
    }

    @Inject(method = "interact",at = @At("HEAD"), cancellable = true)
    public void interact(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if ((entity instanceof PassiveEntity
                || entity instanceof AmbientEntity
                || entity instanceof PlayerEntity
                || entity instanceof WaterCreatureEntity
                || (entity instanceof SlimeEntity && !(entity instanceof MagmaCubeEntity) && ((SlimeEntity) entity).isSmall()))
                && playerCanPet(((PlayerEntity) (Object) this), hand) && !(entity instanceof PlayerEntity && !ModConfig.pet_players)) {
            if (this.petCooldown <= 0) {

                EntityType<?> type = entity.getType();

                // Special case for angery entities
                if (!(entity instanceof PlayerEntity) && entity instanceof Angerable) {
                    if (!((Angerable) entity).hasAngerTime()) {
                        successfullyPet(entity.getEntityWorld(), entity);
                        cir.setReturnValue(ActionResult.SUCCESS);
                    }
                }
                // Funni puffer sting
                else if (entity instanceof PufferfishEntity && ((PufferfishEntity) entity).getPuffState() > 0) {
                    entity.onPlayerCollision((PlayerEntity) (Object) this);
                    cir.setReturnValue(ActionResult.SUCCESS);
                } else if (!type.isIn(NOT_PETTABLE) && !(type.isIn(NOT_PETTABLE_ADULT) && entity instanceof MobEntity && !((MobEntity) entity).isBaby())) {
                    successfullyPet(entity.getEntityWorld(), entity);
                    cir.setReturnValue(ActionResult.SUCCESS);
                }
            } else cir.setReturnValue(ActionResult.PASS);
        }
    }

    private boolean playerCanPet(PlayerEntity player, Hand hand) {
        return !player.isSpectator() && player.shouldCancelInteraction() && player.getStackInHand(hand).isEmpty() && player.getMainHandStack().isEmpty();
    }

    private void successfullyPet(World world, Entity entity) {

        this.petCooldown = ModConfig.petting_cooldown;

        if (entity instanceof SlimeEntity) {
            int i = ((SlimeEntity) entity).getSize();
            if (!entity.isSilent()) entity.playSound(SoundEvents.ENTITY_SLIME_SQUISH_SMALL, 0.4F * (float)i, ((petRandom.nextFloat() - petRandom.nextFloat()) * 0.2F + 1.0F) / 0.8F);
        }

        if (!(entity instanceof PlayerEntity)) {
            if (!entity.isSilent()) {
                ((MobEntity) entity).ambientSoundChance = -((MobEntity) entity).getMinAmbientSoundDelay();
                ((MobEntity) entity).playAmbientSound();
            }
            if (ModConfig.heal_owner && entity instanceof TameableEntity && ((TameableEntity) entity).isOwner((LivingEntity) (Object) this)) {
                ((TameableEntity) entity).heal(2);
                ((LivingEntity) (Object) this).heal(2);
                networkPet(world, (PlayerEntity) (Object) this);
            }
        }
        networkPet(world, entity);
    }

    private void networkPet(World world, Entity entity) {
        if (!world.isClient() ){
            PacketByteBuf buf = PacketByteBufs.create();
            if (entity instanceof PlayerEntity){
                buf.writeBoolean(true);
                buf.writeUuid(PlayerEntity.getUuidFromProfile(((PlayerEntity)entity).getGameProfile()));
            } {
                buf.writeBoolean(false);
                buf.writeInt(entity.getId());
            }
            for (ServerPlayerEntity player : PlayerLookup.tracking((ServerWorld) entity.getEntityWorld(),entity.getBlockPos())){
                ServerPlayNetworking.send(player, new Identifier(MOD_ID, "server_pet"), buf);
            }
        }
    }

}

package net.ellivers.pettable.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.PufferfishEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

import static net.ellivers.pettable.Pettable.NOT_PETTABLE;
import static net.ellivers.pettable.Pettable.NOT_PETTABLE_ADULT;
import static net.ellivers.pettable.config.ModConfig.heal_owner;

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
                || entity instanceof WaterCreatureEntity
                || (entity instanceof SlimeEntity && !(entity instanceof MagmaCubeEntity) && ((SlimeEntity) entity).isSmall()))
                && checkPlayer(((PlayerEntity) (Object) this), hand)) {
            if (this.petCooldown <= 0) {

                EntityType<?> type = entity.getType();

                // Special case for angery entities
                if (entity instanceof Angerable) {
                    if (!((Angerable) entity).hasAngerTime()) {
                        successfullyPet(entity.getEntityWorld(), entity);
                        cir.setReturnValue(ActionResult.SUCCESS);
                    }
                }
                // Funni puffer sting
                else if (entity instanceof PufferfishEntity && ((PufferfishEntity) entity).getPuffState() > 0) {
                    entity.onPlayerCollision((PlayerEntity) (Object) this);
                    cir.setReturnValue(ActionResult.SUCCESS);
                } else if (!type.isIn(NOT_PETTABLE) && !(type.isIn(NOT_PETTABLE_ADULT) && !((net.minecraft.entity.mob.MobEntity) entity).isBaby())) {
                    successfullyPet(entity.getEntityWorld(), entity);
                    cir.setReturnValue(ActionResult.SUCCESS);
                }
            } else cir.setReturnValue(ActionResult.PASS);
        }
    }

    private boolean checkPlayer(PlayerEntity player, Hand hand) {
        return !player.isSpectator() && player.shouldCancelInteraction() && player.getStackInHand(hand).isEmpty() && player.getMainHandStack().isEmpty();
    }

    private void successfullyPet(World world, Entity entity) {
        System.out.println(heal_owner);

        this.petCooldown = 30;

        if (entity instanceof SlimeEntity) {
            int i = ((SlimeEntity) entity).getSize();

            if (!entity.isInvisible()) for(int j = 0; j < i * 8; ++j) {
                float f = petRandom.nextFloat() * 6.2831855F;
                float g = petRandom.nextFloat() * 0.5F + 0.5F;
                float h = MathHelper.sin(f) * (float)i * 0.5F * g;
                float k = MathHelper.cos(f) * (float)i * 0.5F * g;
                entity.world.addParticle(ParticleTypes.ITEM_SLIME, entity.getX() + (double)h, entity.getY(), entity.getZ() + (double)k, 0.0D, 0.0D, 0.0D);
            }

            if (!entity.isSilent()) entity.playSound(SoundEvents.ENTITY_SLIME_SQUISH_SMALL, 0.4F * (float)i, ((petRandom.nextFloat() - petRandom.nextFloat()) * 0.2F + 1.0F) / 0.8F);
            ((SlimeEntity) entity).targetStretch = -0.5F;
        }

        if (!entity.isSilent()) {
            ((MobEntity) entity).ambientSoundChance = -((MobEntity) entity).getMinAmbientSoundDelay();
            ((MobEntity) entity).playAmbientSound();
        }
        if (entity instanceof TameableEntity && ((TameableEntity) entity).getOwnerUuid() == ((LivingEntity) (Object) this).getUuid()) {
            ((TameableEntity) entity).heal(2);
            ((LivingEntity) (Object) this).heal(2);
            spawnHearts(world, (LivingEntity) (Object) this);
        }
        spawnHearts(world, entity);
    }

    private void spawnHearts(World world, Entity entity) {
        double d = petRandom.nextGaussian() * 0.02D;
        double e = petRandom.nextGaussian() * 0.02D;
        double f = petRandom.nextGaussian() * 0.02D;
        for (int k = 0; k < 3; ++k)
            world.addParticle(ParticleTypes.HEART, entity.getParticleX(1.0D), entity instanceof PlayerEntity ? entity.getY() + 0.5D : entity.getEyeY(), entity.getParticleZ(1.0D), d, e, f);
    }

}

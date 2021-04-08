package net.ellivers.pettable.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.PufferfishEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

import static net.ellivers.pettable.Pettable.NOT_PETTABLE;
import static net.ellivers.pettable.Pettable.NOT_PETTABLE_ADULT;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements Angerable {

    @Inject(method = "interact",at = @At("HEAD"), cancellable = true)
    public void interact(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if ((entity instanceof PassiveEntity
                || entity instanceof AmbientEntity
                || entity instanceof WaterCreatureEntity
                || (entity instanceof SlimeEntity && !(entity instanceof MagmaCubeEntity) && ((SlimeEntity) entity).isSmall()))
                && checkPlayer(((PlayerEntity) (Object) this), hand)) {

            EntityType<?> type = entity.getType();

            // Special case for wolves
            if (entity instanceof WolfEntity) {
                if (!((WolfEntity) entity).hasAngerTime()) {
                    spawnHearts(entity.getEntityWorld(), entity);
                    cir.setReturnValue(ActionResult.SUCCESS);
                }
            }
            // Funni puffer sting
            else if (entity instanceof PufferfishEntity && ((PufferfishEntity) entity).getPuffState() > 0) {
                entity.onPlayerCollision((PlayerEntity) (Object) this);
                cir.setReturnValue(ActionResult.SUCCESS);
            }

            else if (!type.isIn(NOT_PETTABLE) && !(type.isIn(NOT_PETTABLE_ADULT) && !((net.minecraft.entity.mob.MobEntity) entity).isBaby())) {
                spawnHearts(entity.getEntityWorld(), entity);
                cir.setReturnValue(ActionResult.SUCCESS);
            }
        }
    }

    private boolean checkPlayer(PlayerEntity player, Hand hand) {
        return player.shouldCancelInteraction() && player.getStackInHand(hand).isEmpty() && player.getMainHandStack().isEmpty();
    }

    private void spawnHearts(World world, Entity entity) {
        double d = new Random().nextGaussian() * 0.02D;
        double e = new Random().nextGaussian() * 0.02D;
        double f = new Random().nextGaussian() * 0.02D;
        for (int k = 0; k < 3; ++k)
            world.addParticle(ParticleTypes.HEART, entity.getParticleX(1.0D), entity.getEyeY(), entity.getParticleZ(1.0D), d, e, f);
    }

}

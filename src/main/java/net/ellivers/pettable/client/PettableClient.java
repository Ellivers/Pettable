package net.ellivers.pettable.client;

import net.ellivers.pettable.Pettable;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.Random;


public class PettableClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(new Identifier(Pettable.MOD_ID,"server_pet"), (client, handler, buf, responseSender) -> {
            boolean player = buf.readBoolean();

            Entity target = player ? handler.getWorld().getPlayerByUuid(buf.readUuid()) : handler.getWorld().getEntityById(buf.readInt());

            client.execute(() -> {
                spawnHearts(client.world, target);
            });
        });
    }

    private final Random petRandom = new Random();
    private void spawnHearts(World world, Entity entity) {
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

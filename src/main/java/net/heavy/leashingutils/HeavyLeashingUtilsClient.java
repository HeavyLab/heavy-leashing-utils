package net.heavy.leashingutils;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.heavy.leashingutils.config.HeavyLeashingUtilsConfig;
import net.heavy.leashingutils.events.AttachEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public class HeavyLeashingUtilsClient implements ClientModInitializer {
    public ConfigHolder<HeavyLeashingUtilsConfig> configHolder;
    private HeavyLeashingUtilsNumberOfAttachedEntities heavyLeashingUtilsNumberOfAttachedEntities = HeavyLeashingUtilsNumberOfAttachedEntities.getInstance();

    @Override
    public void onInitializeClient() {
        this.configHolder = AutoConfig.register(HeavyLeashingUtilsConfig.class, Toml4jConfigSerializer::new);

        AttachEvents.ATTACH_LEASH.register(() -> {
            heavyLeashingUtilsNumberOfAttachedEntities.increment();
        });

        AttachEvents.DETACH_LEASH.register(() -> {
            if (heavyLeashingUtilsNumberOfAttachedEntities.getNumberOfAttachedEntities() > 0) {
                heavyLeashingUtilsNumberOfAttachedEntities.decrement();

                if (this.configHolder.getConfig().playDetachSound) {
                    BlockPos playerPosition = MinecraftClient.getInstance().player.getBlockPos();
                    SoundInstance sound = new PositionedSoundInstance(SoundEvents.ENTITY_LEASH_KNOT_PLACE, SoundCategory.MASTER, 1f, 1f, Random.create(), playerPosition);
                    MinecraftClient.getInstance().getSoundManager().play(sound);
                }
            }
        });
    }

    @Nullable
    public static PlayerEntity getCameraPlayer() {
        return MinecraftClient.getInstance().getCameraEntity() instanceof PlayerEntity player ? player : null;
    }
}

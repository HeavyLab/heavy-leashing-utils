package net.heavy.leashingutils.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.autoconfig.AutoConfig;
import net.heavy.leashingutils.HeavyLeashingUtilsClient;
import net.heavy.leashingutils.HeavyLeashingUtilsNumberOfAttachedEntities;
import net.heavy.leashingutils.config.HeavyLeashingUtilsConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow
    @Final
    private static Identifier HOTBAR_TEXTURE;
    @Unique
    private static final int WIDTH = 22;
    @Unique
    private static final int HEIGHT = 22;
    @Unique
    private HeavyLeashingUtilsConfig config;
    @Shadow
    protected abstract void renderHotbarItem(DrawContext context, int x, int y, float tickDelta, PlayerEntity player, ItemStack stack, int seed);

    @Shadow @Final private MinecraftClient client;
    @Unique
    private HeavyLeashingUtilsNumberOfAttachedEntities heavyLeashingUtilsNumberOfAttachedEntities = HeavyLeashingUtilsNumberOfAttachedEntities.getInstance();

    @Inject(method = "<init>(Lnet/minecraft/client/MinecraftClient;)V", at = @At(value = "RETURN"))
    private void onInit(MinecraftClient client, CallbackInfo ci) {
        // Get Config
        this.config = AutoConfig.getConfigHolder(HeavyLeashingUtilsConfig.class).getConfig();
        // Register Save Listener
        AutoConfig.getConfigHolder(HeavyLeashingUtilsConfig.class).registerSaveListener((manager, data) -> {
            // Update local config when new settings are saved
            this.config = data;
            return ActionResult.SUCCESS;
        });
    }

    @Inject(method = "renderHotbar", at = @At("TAIL"))
    public void renderLeashHud(DrawContext context, float tickDelta, CallbackInfo ci) {
        if (!config.displayAttachedStatusIcon) return;

        if (this.heavyLeashingUtilsNumberOfAttachedEntities.getNumberOfAttachedEntities() <= 0) return;

        PlayerEntity player = HeavyLeashingUtilsClient.getCameraPlayer();
        if (player == null) return;

        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 200);

        int iconPositionX = switch (config.iconHudPosition) {
            case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> 6 + config.iconHudXOffset;
            case TOP_CENTER -> (context.getScaledWindowWidth() / 2 - 22) + config.iconHudXOffset;
            case TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT -> (context.getScaledWindowWidth() - 22) + config.iconHudXOffset;
        };
        int iconPositionY = switch (config.iconHudPosition) {
            case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> 3 + config.iconHudYOffset;
            case CENTER_LEFT, CENTER_RIGHT -> (context.getScaledWindowHeight() / 2 - 22) + config.iconHudYOffset;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> (context.getScaledWindowHeight() - 25) + config.iconHudYOffset;
        };

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        context.getMatrices().push();
        context.getMatrices().translate(0, 0, -91);
        context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, 0, 0, iconPositionX - 3, iconPositionY, WIDTH - 3, HEIGHT);
        context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, 182 - 3, 0, iconPositionX + WIDTH - 6, iconPositionY, 3, HEIGHT);
        context.getMatrices().pop();

        this.renderHotbarItem(context, iconPositionX, iconPositionY + 3, tickDelta, player, new ItemStack(Items.LEAD), 1);
        if (config.displayAttachedStatusCount) {
            context.drawCenteredTextWithShadow(
                this.client.textRenderer,
                Integer.toString(this.heavyLeashingUtilsNumberOfAttachedEntities.getNumberOfAttachedEntities()),
                iconPositionX + 16,
                iconPositionY + 16,
                0xffffff
            );
        }

        context.getMatrices().pop();
    }
}

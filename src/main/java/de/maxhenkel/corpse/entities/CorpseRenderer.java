package de.maxhenkel.corpse.entities;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import de.maxhenkel.corelib.CachedMap;
import de.maxhenkel.corelib.client.PlayerSkins;
import de.maxhenkel.corpse.Main;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class CorpseRenderer extends EntityRenderer<CorpseEntity> {

    private final CachedMap<CorpseEntity, DummyPlayer> players;
    private final CachedMap<CorpseEntity, DummySkeleton> skeletons;
    private final DummyPlayerRenderer playerRenderer;
    private final DummyPlayerRenderer playerRendererSmallArms;
    private final SkeletonRenderer skeletonRenderer;

    public CorpseRenderer(EntityRendererProvider.Context renderer) {
        super(renderer);
        players = new CachedMap<>(10_000L);
        skeletons = new CachedMap<>(10_000L);
        playerRenderer = new DummyPlayerRenderer(renderer, false);
        playerRendererSmallArms = new DummyPlayerRenderer(renderer, true);
        skeletonRenderer = new SkeletonRenderer(renderer);
    }

    @Override
    public ResourceLocation getTextureLocation(CorpseEntity entity) {
        return null;
    }

    @Override
    public void render(CorpseEntity entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLightIn) {
        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLightIn);
        matrixStack.pushPose();

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(-entity.getYRot()));

        if (Main.SERVER_CONFIG.spawnCorpseOnFace.get()) {
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(90F));
            matrixStack.translate(0D, -1D, -2.01D / 16D);
        } else {
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90F));
            matrixStack.translate(0D, -1D, 2.01D / 16D);
        }

        if (entity.isSkeleton()) {
            DummySkeleton skeleton = skeletons.get(entity, () -> new DummySkeleton(entity.level, entity.getEquipment()));
            skeletonRenderer.render(skeleton, entityYaw, 1F, matrixStack, buffer, packedLightIn);
        } else {
            AbstractClientPlayer abstractClientPlayerEntity = players.get(entity, () -> new DummyPlayer((ClientLevel) entity.level, new GameProfile(entity.getCorpseUUID().orElse(new UUID(0L, 0L)), entity.getCorpseName()), entity.getEquipment(), entity.getCorpseModel()));
            if (PlayerSkins.isSlim(entity.getCorpseUUID().orElse(new UUID(0L, 0L)))) {
                playerRendererSmallArms.render(abstractClientPlayerEntity, 0F, 1F, matrixStack, buffer, packedLightIn);
            } else {
                playerRenderer.render(abstractClientPlayerEntity, 0F, 1F, matrixStack, buffer, packedLightIn);
            }
        }
        matrixStack.popPose();
    }

}

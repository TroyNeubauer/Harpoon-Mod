package com.troy.tco.entity;

import com.troy.tco.Constants;
import com.troy.tco.TCO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import static com.troy.tco.TCO.logger;

@SideOnly(Side.CLIENT)
public class EntiryHarpoonRenderer implements IRenderFactory<EntityHarpoon>
{
	private static final ResourceLocation texture = new ResourceLocation(Constants.MODID, "entity/harpoon.png");

	public class HarpoonModel extends ModelBase {
		protected ModelRenderer testCube;
		public HarpoonModel()
		{
			testCube = new ModelRenderer(this);
			testCube.addBox(-0.5f, -0.5f, -0.5f, 1, 1, 1);
			testCube.setRotationPoint(0, 0, 0);
		}

		@Override
		public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
			testCube.render(scale);
		}
	}
	private ModelBase modelEmpty = new HarpoonModel();

	@Override
	public Render<EntityHarpoon> createRenderFor(RenderManager manager) {
		return new Render<EntityHarpoon>(manager) {
			@Override
			protected ResourceLocation getEntityTexture(EntityHarpoon entity) {
				return texture;
			}

			@Override
			public void doRender(EntityHarpoon entity, double x, double y, double z, float entityYaw, float partialTicks) {
				//Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale
				GlStateManager.pushMatrix();
				GlStateManager.disableTexture2D();
				GlStateManager.disableCull();
				GlStateManager.translate(x, y, z);
				modelEmpty.render(entity, 0.0f, 0.0f, 0.0f, entityYaw, 0.0f, 0.6125f);
				GlStateManager.popMatrix();
				GlStateManager.enableTexture2D();
				GlStateManager.enableCull();


				AxisAlignedBB boundingBox = entity.getEntityBoundingBox();
				double nx = x - entity.lastTickPosX;
				double ny = y - entity.lastTickPosY;
				double nz = z - entity.lastTickPosZ;
/*				GlStateManager.pushMatrix();

				GlStateManager.disableTexture2D();
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder bufferbuilder = tessellator.getBuffer();
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				bufferbuilder.setTranslation(nx, ny, nz);
				bufferbuilder.begin(7, DefaultVertexFormats.POSITION_NORMAL);

				bufferbuilder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
				bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
				bufferbuilder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
				bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
				bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
				bufferbuilder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
				bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
				bufferbuilder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
				bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).normal(0.0F, -1.0F, 0.0F).endVertex();
				bufferbuilder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).normal(0.0F, -1.0F, 0.0F).endVertex();
				bufferbuilder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, -1.0F, 0.0F).endVertex();
				bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, -1.0F, 0.0F).endVertex();
				bufferbuilder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 1.0F, 0.0F).endVertex();
				bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 1.0F, 0.0F).endVertex();
				bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 1.0F, 0.0F).endVertex();
				bufferbuilder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 1.0F, 0.0F).endVertex();
				bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
				bufferbuilder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
				bufferbuilder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
				bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
				bufferbuilder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).normal(1.0F, 0.0F, 0.0F).endVertex();
				bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).normal(1.0F, 0.0F, 0.0F).endVertex();
				bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).normal(1.0F, 0.0F, 0.0F).endVertex();
				bufferbuilder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).normal(1.0F, 0.0F, 0.0F).endVertex();

				bufferbuilder.pos(boundingBox.minX * 4, boundingBox.maxY + 20.0f, boundingBox.maxZ * 4).normal(0.0F, 1.0F, 0.0F).endVertex();
				bufferbuilder.pos(boundingBox.maxX * 4, boundingBox.maxY + 20.0f, boundingBox.maxZ * 4).normal(0.0F, 1.0F, 0.0F).endVertex();
				bufferbuilder.pos(boundingBox.maxX * 4, boundingBox.maxY + 20.0f, boundingBox.minZ * 4).normal(0.0F, 1.0F, 0.0F).endVertex();
				bufferbuilder.pos(boundingBox.minX * 4, boundingBox.maxY + 20.0f, boundingBox.minZ * 4).normal(0.0F, 1.0F, 0.0F).endVertex();


				tessellator.draw();
				bufferbuilder.setTranslation(0.0D, 0.0D, 0.0D);
				GlStateManager.enableTexture2D();

				GlStateManager.popMatrix();
*/

				//TCO.logger.info("Rendering... Anchored to: " + entity.getAnchorPos());
				//modelEmpty.render(entity, 0.0f, 0.0f, 0.0f, entityYaw, 0.0f, 1.0f);


				GlStateManager.pushMatrix();

				GlStateManager.disableTexture2D();
				GlStateManager.disableCull();
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder bufferbuilder = tessellator.getBuffer();
				GlStateManager.color(0.0F, 0.0F, 0.0F, 1.0F);
				bufferbuilder.setTranslation(nx, ny, nz);
				bufferbuilder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_NORMAL);

				bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 0.0F, -1.0F).endVertex();
				bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
				bufferbuilder.pos(entity.getAnchorPos().x, entity.getAnchorPos().y, entity.getAnchorPos().z).normal(0.0F, 0.0F, -1.0F).endVertex();
				//logger.info("Anchor pos is " + entity.getAnchorPos());
				tessellator.draw();
				bufferbuilder.setTranslation(0.0D, 0.0D, 0.0D);
				GlStateManager.enableCull();
				GlStateManager.enableTexture2D();

				GlStateManager.popMatrix();

			}

			int frames = 0;
			@Override
			public boolean shouldRender(EntityHarpoon harpoon, ICamera camera, double camX, double camY, double camZ) {
				return harpoon.isInRangeToRender3d(camX, camY, camZ) && camera.isBoundingBoxInFrustum(harpoon.getTotalBB().grow(20));
			}
		};
	}
}


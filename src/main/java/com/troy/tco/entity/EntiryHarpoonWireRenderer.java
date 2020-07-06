package com.troy.tco.entity;

import com.troy.tco.Constants;
import com.troy.tco.api.IJoinable;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import static com.troy.tco.TCO.logger;

@SideOnly(Side.CLIENT)
public class EntiryHarpoonWireRenderer implements IRenderFactory<EntityHarpoonWire>
{
	private static final ResourceLocation texture = new ResourceLocation(Constants.MODID, "entity/harpoon.png");

	public class HarpoonModel extends ModelBase {
		protected ModelRenderer testCube;
		public HarpoonModel()
		{
			testCube = new ModelRenderer(this);
			testCube.addBox(-1.5f, -1.5f, -1.5f, 3, 3, 3);
			testCube.setRotationPoint(0, 0, 0);
		}

		@Override
		public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
			testCube.render(scale);
		}
	}
	private ModelBase modelEmpty = new HarpoonModel();

	@Override
	public Render<EntityHarpoonWire> createRenderFor(RenderManager manager) {
		return new Render<EntityHarpoonWire>(manager) {
			@Override
			protected ResourceLocation getEntityTexture(EntityHarpoonWire entity) {
				return texture;
			}

			@Override
			public void doRender(EntityHarpoonWire entity, double x, double y, double z, float entityYaw, float partialTicks) {
				if (entity.getStart() != null && entity.getEnd() != null)
				{
					//logger.info("rendering wire");
					//GlStateManager.pushMatrix();
					Tessellator tessellator = Tessellator.getInstance();
					BufferBuilder bufferbuilder = tessellator.getBuffer();
					//GlStateManager.color(0.0F, 0.0F, 0.0F, 1.0F);
					bufferbuilder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_NORMAL);

					IJoinable start = entity.getStart(), end = entity.getEnd();
					bufferbuilder.pos(start.getPos().x, start.getPos().y, start.getPos().z).normal(0.0F, 0.0F, -1.0F).endVertex();
					bufferbuilder.pos(start.getPos().x, start.getPos().y, start.getPos().z).normal(0.0F, 0.0F, -1.0F).endVertex();
					bufferbuilder.pos(end.getPos().x, end.getPos().y, end.getPos().z).normal(0.0F, 0.0F, -1.0F).endVertex();
					//logger.info("Anchor pos is " + entity.getAnchorPos());
					tessellator.draw();

					//GlStateManager.popMatrix();
				}

				GlStateManager.pushMatrix();
				GlStateManager.disableTexture2D();
				GlStateManager.enableBlend();
				GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GlStateManager.translate(x, y, z);
				GlStateManager.color(0.0f, 0.0f, 0.0f, 0.3f);
				modelEmpty.render(entity, 0.0f, 0.0f, 0.0f, entityYaw, 0.0f, 0.6125f);
				GlStateManager.enableTexture2D();
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();

			}

		};
	}
}


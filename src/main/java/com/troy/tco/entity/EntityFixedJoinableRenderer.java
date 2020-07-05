package com.troy.tco.entity;

import com.troy.tco.Constants;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityFixedJoinableRenderer implements IRenderFactory<EntityFixedJoinable>
{
	private static final ResourceLocation texture = new ResourceLocation(Constants.MODID, "entity/harpoon.png");

	public class HarpoonModel extends ModelBase {
		protected ModelRenderer testCube;
		public HarpoonModel()
		{
			testCube = new ModelRenderer(this);
			testCube.addBox(-0.25f, -0.25f, -0.25f, 1, 1, 1);
			testCube.setRotationPoint(0, 0, 0);
		}

		@Override
		public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
			testCube.render(scale);
		}
	}
	private ModelBase modelEmpty = new HarpoonModel();

	@Override
	public Render<EntityFixedJoinable> createRenderFor(RenderManager manager) {
		return new Render<EntityFixedJoinable>(manager) {
			@Override
			protected ResourceLocation getEntityTexture(EntityFixedJoinable entity) {
				return texture;
			}

			@Override
			public void doRender(EntityFixedJoinable entity, double x, double y, double z, float entityYaw, float partialTicks) {
				GlStateManager.pushMatrix();
				GlStateManager.disableTexture2D();
				GlStateManager.disableCull();
				GlStateManager.translate(x, y, z);
				GlStateManager.color(0.0f, 0.0f, 1.0f);
				modelEmpty.render(entity, 0.0f, 0.0f, 0.0f, entityYaw, 0.0f, 0.6125f);
				GlStateManager.popMatrix();
				GlStateManager.enableTexture2D();

			}

		};
	}
}


package com.troy.tco.entity;

import com.troy.tco.Constants;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntiryHarpoonRenderer implements IRenderFactory<EntityFlyingHarpoon>
{
	private static final ResourceLocation texture = new ResourceLocation(Constants.MODID, "entity/harpoon.png");

	public class Model extends ModelBase {
		protected ModelRenderer testCube;
		public Model()
		{
			testCube = new ModelRenderer(this);
			testCube.addBox(-4.0f, -4.0f, -4.0f, 8, 8, 8);
			testCube.setRotationPoint(0, 0, 0);
		}

		@Override
		public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
			testCube.render(scale);
		}
	}
	private ModelBase modelEmpty =new Model();

	@Override
	public Render<EntityFlyingHarpoon> createRenderFor(RenderManager manager) {
		return new Render<EntityFlyingHarpoon>(manager) {
			@Override
			protected ResourceLocation getEntityTexture(EntityFlyingHarpoon entity) {
				return texture;
			}

			@Override
			public void doRender(EntityFlyingHarpoon entity, double x, double y, double z, float entityYaw, float partialTicks) {
				bindTexture(texture);
				modelEmpty.render(entity, 0.0f, 0.0f, 0.0f, entityYaw, 0.0f, 1.0f);
			}
		};
	}
}


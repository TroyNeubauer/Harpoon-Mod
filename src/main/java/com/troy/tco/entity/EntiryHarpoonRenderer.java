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
				GlStateManager.pushMatrix();
				GlStateManager.disableTexture2D();
				GlStateManager.disableCull();
				GlStateManager.translate(x, y, z);
				GlStateManager.color(0.1f, 0.1f, 0.1f);
				modelEmpty.render(entity, 0.0f, 0.0f, 0.0f, entityYaw, 0.0f, 0.6125f);
				GlStateManager.popMatrix();
				GlStateManager.enableTexture2D();

			}

		};
	}
}


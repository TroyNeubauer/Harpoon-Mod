package com.troy.tco.entity;

import com.troy.tco.Constants;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntiryHarpoonRenderer implements IRenderFactory<EntityHarpoon>
{
	private static final ResourceLocation texture = new ResourceLocation(Constants.MODID, "entity/harpoon.png");

	@Override
	public Render<EntityHarpoon> createRenderFor(RenderManager manager) {
		return new Render<EntityHarpoon>(manager) {
			@Override
			protected ResourceLocation getEntityTexture(EntityHarpoon entity) {
				return texture;
			}

			@Override
			public void doRender(EntityHarpoon entity, double x, double y, double z, float entityYaw, float partialTicks) {
				bindTexture(texture);
			}
		};
	}
}


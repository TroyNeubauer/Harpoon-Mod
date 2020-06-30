package com.troy.tco.proxy;

import com.troy.tco.entity.EntiryHarpoonRenderer;
import com.troy.tco.entity.EntityFlyingHarpoon;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy
{
	@Override
	public void registerRenderers() {
		RenderingRegistry.registerEntityRenderingHandler(EntityFlyingHarpoon.class, new EntiryHarpoonRenderer());
	}

	@Override
	public void registerItemRenderer(Item item, int meta, String id) {
		ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), id));
	}
}

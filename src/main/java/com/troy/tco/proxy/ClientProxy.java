package com.troy.tco.proxy;

import com.troy.tco.TCO;
import com.troy.tco.entity.EntiryHarpoonRenderer;
import com.troy.tco.entity.EntityHarpoon;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy
{
	@Override
	public void registerRenderers()
	{
		TCO.logger.info("Registered entity harpoon renderer");
		RenderingRegistry.registerEntityRenderingHandler(EntityHarpoon.class, new EntiryHarpoonRenderer());
	}

	@Override
	public void registerItemRenderer(Item item, int meta, String id)
	{
		TCO.logger.info("Registered item renderer for item " + item.getUnlocalizedName());
		ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), id));
	}
}

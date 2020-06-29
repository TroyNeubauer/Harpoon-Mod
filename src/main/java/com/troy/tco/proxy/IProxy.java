package com.troy.tco.proxy;

import net.minecraft.item.Item;

public interface IProxy
{
	public void registerRenderers();
	public void registerItemRenderer(Item item, int meta, String id);
}

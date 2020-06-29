package com.troy.tco.item;

import net.minecraft.creativetab.CreativeTabs;

public class ItemHarpoon extends ItemBase {
	public ItemHarpoon()
	{
		super("harpoon");
		setMaxStackSize(8);
		setCreativeTab(CreativeTabs.COMBAT);
	}
}

package com.troy.tco.item;

import com.troy.tco.Constants;
import net.minecraft.item.Item;

public class ItemBase extends Item {

	public ItemBase(String name)
	{
		super();
		this.setRegistryName(Constants.MODID, name);
		this.setUnlocalizedName(name);
	}

}

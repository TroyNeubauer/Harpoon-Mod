package com.troy.tco.item;

import com.troy.tco.Constants;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBase extends Item {

	public ItemBase(String name)
	{
		super();
		this.setRegistryName(Constants.MODID, name);
		this.setUnlocalizedName(name);
	}

}

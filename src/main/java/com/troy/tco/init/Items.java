package com.troy.tco.init;

import com.troy.tco.item.ItemHarpoon;
import com.troy.tco.item.ItemHarpoonGun;
import net.minecraft.item.Item;

public class Items {

	public static final ItemHarpoonGun HARPOON_GUN = new ItemHarpoonGun();
	public static final ItemHarpoon HARPOON = new ItemHarpoon();

	public static final Item[] ALL = { HARPOON_GUN, HARPOON };
}

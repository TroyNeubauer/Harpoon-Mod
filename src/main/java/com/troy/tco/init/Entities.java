package com.troy.tco.init;

import com.troy.tco.Constants;
import com.troy.tco.entity.EntityHarpoon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;

public class Entities {

	private static int ID = 0;

	public static final EntityEntry HARPOON = EntityEntryBuilder.create()
			.entity(EntityHarpoon.class)
			.id(new ResourceLocation(Constants.MODID, "harpoon"), ID++)
			.name("Harpoon")
			.tracker(256, 50, true)
			.build();

	public static final EntityEntry[] ALL = { HARPOON };
}

package com.troy.tco.entity;

import com.troy.tco.Constants;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;

public class Entities {

	private static int ID = 0;

	public static final EntityEntry FLYING_HARPOON = EntityEntryBuilder.create()
			.entity(EntityFlyingHarpoon.class)
			.id(new ResourceLocation(Constants.MODID, "flying_harpoon"), ID++)
			.name("Flying Harpoon")
			.tracker(256, 1, true)
			.build();

	public static final EntityEntry FIXED_HARPOON = EntityEntryBuilder.create()
			.entity(EntityFixedHarpoon.class)
			.id(new ResourceLocation(Constants.MODID, "fixed_harpoon"), ID++)
			.name("Fixed Harpoon")
			.tracker(256, 1, true)
			.build();

	public static final EntityEntry[] ALL = { FLYING_HARPOON, FIXED_HARPOON };
}

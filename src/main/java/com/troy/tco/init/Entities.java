package com.troy.tco.init;

import com.troy.tco.Constants;
import com.troy.tco.entity.EntityFixedJoinable;
import com.troy.tco.entity.EntityHarpoon;
import com.troy.tco.entity.EntityHarpoonWire;
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

	public static final EntityEntry HARPOON_WIRE = EntityEntryBuilder.create()
			.entity(EntityHarpoonWire.class)
			.id(new ResourceLocation(Constants.MODID, "harpoon_wire"), ID++)
			.name("Harpoon Wire")
			.tracker(256, 50, true)
			.build();

	public static final EntityEntry FIXED_HARPOON_BASE = EntityEntryBuilder.create()
			.entity(EntityFixedJoinable.class)
			.id(new ResourceLocation(Constants.MODID, "fixed_harpoon_base"), ID++)
			.name("Fixed Harpoon Base")
			.tracker(50, 1, false)
			.build();


	public static final EntityEntry[] ALL = { HARPOON, HARPOON_WIRE, FIXED_HARPOON_BASE };
}

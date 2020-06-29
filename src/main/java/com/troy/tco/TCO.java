package com.troy.tco;

import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.Logger;

@Mod(modid = Constants.MODID, name = Constants.NAME, version = Constants.VERSION)
public class TCO
{
	public static Logger logger;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		logger = event.getModLog();
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event)
	{
		logger.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
	}
}

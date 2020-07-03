package com.troy.tco;

import com.troy.tco.proxy.IProxy;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.Logger;

@Mod(modid = Constants.MODID, name = Constants.NAME, version = Constants.VERSION)
public class TCO
{
	public static Logger logger;
	public static TCONetworkHandler networkHandler;

	@SidedProxy(clientSide = "com.troy.tco.proxy.ClientProxy", serverSide = "com.troy.tco.proxy.ServerProxy")
	public static IProxy proxy;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		logger = event.getModLog();
		networkHandler = new TCONetworkHandler();
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event)
	{

	}
}

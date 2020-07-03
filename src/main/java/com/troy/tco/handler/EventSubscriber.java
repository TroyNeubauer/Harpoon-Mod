package com.troy.tco.handler;

import com.troy.tco.Constants;
import com.troy.tco.TCO;
import com.troy.tco.entity.Entities;
import com.troy.tco.entity.EntityHarpoon;
import com.troy.tco.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;

import static com.troy.tco.TCO.logger;
import static com.troy.tco.TCO.proxy;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public class EventSubscriber
{

	@SubscribeEvent
	public static void registerEntities(final RegistryEvent.Register<EntityEntry> event)
	{
		event.getRegistry().registerAll(Entities.ALL);
		TCO.logger.info("Registered Entities");
	}


	@SubscribeEvent
	public static void registerItems(final RegistryEvent.Register<Item> event)
	{
		event.getRegistry().registerAll(com.troy.tco.init.Items.ALL);
		TCO.logger.info("Registered Items");
	}


	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent event) {
		for (Item item : Items.ALL)
		{
			proxy.registerItemRenderer(item, 0, "inventory");
		}
		TCO.logger.info("Registered item models");
		proxy.registerRenderers();
	}

}

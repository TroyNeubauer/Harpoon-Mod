package com.troy.tco.handler;

import com.troy.tco.Constants;
import com.troy.tco.TCO;
import com.troy.tco.entity.Entities;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public class EventSubscriber {

	@SubscribeEvent
	public static void registerEntities(final RegistryEvent.Register<EntityEntry> event) {
		event.getRegistry().registerAll(Entities.ALL);
		TCO.logger.info("Registered Entities");
	}


	@SubscribeEvent
	public static void registerItems(final RegistryEvent.Register<Item> event) {
		event.getRegistry().registerAll(com.troy.tco.init.Items.ALL);
		TCO.logger.info("Registered Items");


	}
}

package com.troy.tco.handler;

import com.troy.tco.Constants;
import com.troy.tco.TCO;
import com.troy.tco.TCONetworkHandler;
import com.troy.tco.entity.EntityHarpoonWire;
import com.troy.tco.init.Entities;
import com.troy.tco.entity.EntityHarpoon;
import com.troy.tco.init.Items;
import com.troy.tco.util.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void onMouseInput(InputEvent.MouseInputEvent event)
	{
		if (Minecraft.getMinecraft().gameSettings.keyBindUseItem.isPressed())
		{
			double regularClickDistance;
			RayTraceResult mouseOver = Minecraft.getMinecraft().objectMouseOver;
			if (mouseOver.typeOfHit == RayTraceResult.Type.MISS)
			{
				//The entity is infinitely far away. Always let the player onto the harpoon line
				regularClickDistance = Double.MAX_VALUE;
			}
			else
			{
				regularClickDistance = Math.sqrt(Minecraft.getMinecraft().objectMouseOver.hitVec.subtract(Minecraft.getMinecraft().player.getPositionVector()).lengthSquared());
			}

			EntityPlayerSP player = Minecraft.getMinecraft().player;
			float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
			Vec3d playerPos = player.getPositionEyes(partialTicks);
			Vec3d playerLook = player.getLook(partialTicks);
			final float harpoonReachDistance = 5.0f;
			Vec3d end = playerPos.addVector(playerLook.x * harpoonReachDistance, playerLook.y * harpoonReachDistance, playerLook.z * harpoonReachDistance);
			for (Entity entity : Minecraft.getMinecraft().world.getLoadedEntityList())
			{
				if (entity instanceof EntityHarpoonWire)
				{
					EntityHarpoonWire wire = (EntityHarpoonWire) entity;
					if (wire.getStart() == null || wire.getEnd() == null)
					{
						//The wire's entities are still loading
						continue;
					}
					double distanceToWire = MathUtils.distBetweenLines(playerPos, end, wire.getStart().getPos(), wire.getEnd().getPos());
					if (distanceToWire < regularClickDistance && distanceToWire < 1.0)
					{
						logger.info("Clicked line: " + distanceToWire);
						TCO.networkHandler.sendToServer(new TCONetworkHandler.WireInteractMessage(wire));
					}
				}
			}
		}

	}

}

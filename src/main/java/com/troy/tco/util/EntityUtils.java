package com.troy.tco.util;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class EntityUtils
{
	public static Entity getEntityByID(int entityID, World world)
	{

		for(Entity entity: world.getLoadedEntityList())
		{
			if(entity.getEntityId() == entityID)
			{
				return entity;
			}
		}
		return null;

	}

}

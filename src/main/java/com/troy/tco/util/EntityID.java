package com.troy.tco.util;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import java.util.UUID;

public abstract class EntityID
{
	public abstract Entity load(World world);
	public abstract String toString();



	public static EntityID from(int entityID)
	{
		return new EntityIntID(entityID);
	}

	public static EntityID from(UUID uuid)
	{
		return new EntityUUIDID(uuid);
	}

	private static class EntityIntID extends EntityID
	{
		private final int entityID;

		public EntityIntID(int entityID)
		{
			this.entityID = entityID;
		}

		public Entity load(World world)
		{
			return world.getEntityByID(entityID);
		}

		@Override
		public String toString() {
			return "EntityIntID{" +
					"entityID=" + entityID +
					'}';
		}
	}
	private static class EntityUUIDID extends EntityID
	{
		private final UUID uuid;

		public EntityUUIDID(UUID uuid)
		{
			this.uuid = uuid;
		}

		public Entity load(World world)
		{
			for (Entity e : world.getLoadedEntityList())
			{
				if (e.getUniqueID().equals(uuid))
				{
					return e;
				}
			}
			return null;
		}

		@Override
		public String toString() {
			return "EntityUUIDID{" +
					"uuid=" + uuid +
					'}';
		}
	}


}

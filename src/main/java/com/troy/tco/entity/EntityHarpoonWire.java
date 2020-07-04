package com.troy.tco.entity;

import com.troy.tco.api.IJoinable;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemLead;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import java.util.List;

import static com.troy.tco.TCO.logger;

public class EntityHarpoonWire extends Entity implements IEntityAdditionalSpawnData
{
	private IJoinable start, end;

	public EntityHarpoonWire(World world, IJoinable start, IJoinable end)
	{
		super(world);
		this.start = start;
		this.end = end;
	}

	@Override
	protected void entityInit()
	{
	}

	@Override
	public boolean canRiderInteract()
	{
		return true;
	}

	int ticks = 0;
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		if (ticks++ % 40 == 0)
		{
			logger.info("wire!");
		}
		if (!this.world.isRemote)
		{
			if (this.start.isBroken() || this.end.isBroken())
			{
				logger.info("Line broke. Killing all entities");
				this.start.getEntity().setDead();
				this.end.getEntity().setDead();
				this.setDead();
				return;
			}
			double newX = (start.getPos().x + end.getPos().x) / 2.0;
			double newY = (start.getPos().y + end.getPos().y) / 2.0;
			double newZ = (start.getPos().z + end.getPos().z) / 2.0;
			if (newX != this.posX || newY != this.posY || newZ != this.posZ)
			{
				this.setPosition(newX, newY, newZ);
				this.setEntityBoundingBox(new AxisAlignedBB(start.getPos(), end.getPos()));
			}
		}
	}

	public boolean interact(EntityPlayer entityplayer, EnumHand hand) //interact : change back when Forge updates
	{
		if(isDead)
			return false;
		if(world.isRemote)
			return false;
		ItemStack currentItem = entityplayer.getHeldItemMainhand();
		if(currentItem.getItem() instanceof ItemLead)
		{
			if(getControllingPassenger() instanceof EntityAnimal)
			{
				// Minecraft will handle dismounting the mob
				return true;
			}

			double checkRange = 10;
			List<EntityAnimal> nearbyAnimals = world.getEntitiesWithinAABB(EntityAnimal.class,
					new AxisAlignedBB(posX - checkRange, posY - checkRange, posZ - checkRange, posX + checkRange,
							posY + checkRange, posZ + checkRange));
			for(EntityAnimal animal : nearbyAnimals)
			{
				if(animal.getLeashed() && animal.getLeashHolder() == entityplayer)
				{
					if(animal.startRiding(this))
					{
						animal.clearLeashed(true, !entityplayer.capabilities.isCreativeMode);
					}
					else
					{
						logger.warn("Failed to put pet in seat");
					}
				}
			}
			return true;
		}
		// Put them in the seat
		if(!entityplayer.startRiding(this))
		{
			logger.warn("Failed to mount seat");
		}
		return true;
	}


	protected boolean canFitPassenger(Entity passenger)
	{
		return true;
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound)
	{
		int startID = compound.getInteger("StartID");
		int endID = compound.getInteger("EndID");
		setLinks(startID, endID);
	}

	private void setLinks(int startID, int endID)
	{
		Entity start = world.getEntityByID(startID);
		Entity end = world.getEntityByID(endID);
		if (start == null || end == null)
		{
			logger.warn("Failed to read EntityHarpoonWire! Start or end entity ID's are invalid! Start: " + startID + " end: " + endID);
			this.setDead();
		}
		if (!(start instanceof IJoinable))
		{
			logger.warn("Failed to read EntityHarpoonWire! Start entity class is not an IJoinable! Got: " + start.getClass());
			this.setDead();
			return;
		}
		if (!(end instanceof IJoinable))
		{
			((IJoinable) start).getEntity().setDead();
			logger.warn("Failed to read EntityHarpoonWire! End entity class is not an IJoinable! Got: " + end.getClass());
			this.setDead();
			return;
		}
		this.start = (IJoinable) start;
		this.end = (IJoinable) end;
		logger.info("Read wire links successfully!");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound)
	{
		compound.setInteger("StartID", start.getEntity().getEntityId());
		compound.setInteger("EndID", end.getEntity().getEntityId());
	}

	@Override
	public boolean canBeCollidedWith()
	{
		//This thing has a massive hitbox so no please
		return false;
	}

	@Override
	public boolean canBePushed()
	{
		return false;
	}

	@Override
	protected boolean canBeRidden(Entity entityIn)
	{
		//Wires cannot ride other entities
		return false;
	}

	@Override
	public void writeSpawnData(ByteBuf buffer)
	{
		buffer.writeInt(start.getEntity().getEntityId());
		buffer.writeInt(end.getEntity().getEntityId());
	}

	@Override
	public void readSpawnData(ByteBuf additionalData)
	{
		logger.info("Reading harpoon wire spawn data");
		setLinks(additionalData.readInt(), additionalData.readInt());
	}
}

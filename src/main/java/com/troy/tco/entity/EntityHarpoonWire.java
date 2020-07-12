package com.troy.tco.entity;

import com.troy.tco.api.IJoinable;
import com.troy.tco.util.EntityID;
import com.troy.tco.util.MathUtils;
import com.troy.tco.util.Vector3f;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemLead;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.troy.tco.TCO.logger;

public class EntityHarpoonWire extends Entity implements IEntityAdditionalSpawnData
{
	private IJoinable start, end;
	private HashMap<Integer, Vector3f> velocities = new HashMap<>();

	public EntityHarpoonWire(World world)
	{
		super(world);
		this.forceSpawn = true;
		this.ignoreFrustumCheck = true;
		setSize(0.5f, 0.5f);
	}

	public EntityHarpoonWire(World world, IJoinable start, IJoinable end)
	{
		this(world);
		this.start = start;
		this.end = end;
		onUpdate();
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

		ensureLinks();
		if (start == null || end == null) return;
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
		}
		float distance = (float) Vector3f.subtract(start.getPos(), end.getPos()).length();
		double newX = (start.getPos().x + end.getPos().x) / 2.0;
		double newY = (start.getPos().y + end.getPos().y) / 2.0;
		double newZ = (start.getPos().z + end.getPos().z) / 2.0;
		if (newX != this.posX || newY != this.posY || newZ != this.posZ)
		{
			this.setPosition(newX, newY, newZ);
			//this.setSize(distance, distance);
			//this.setEntityBoundingBox(new AxisAlignedBB(start.getPos(), end.getPos()));
		}
	}

	public boolean interact(EntityPlayer entityplayer, Vector3f clickPosclickPos)
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
						animal.setPosition(clickPosclickPos.x, clickPosclickPos.y, clickPosclickPos.z);
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
		if(entityplayer.startRiding(this))
		{
			entityplayer.setPosition(clickPosclickPos.x, clickPosclickPos.y, clickPosclickPos.z);
		}
		else
		{
			logger.warn("Failed to mount seat");
		}
		return true;
	}

	//Returns the component of vector onto direction, normalizing the vectors in the process
	private static float getComponentInAxis(Vector3f vector, Vector3f direction)
	{
		return Vector3f.dot(vector, direction) / vector.length();
	}

	private static final Vector3f GRAVITY_AXIS = new Vector3f(0, 1, 0);

	@Override
	public void updatePassenger(Entity passenger)
	{
		if (isPassenger(passenger))
		{
			if (!isReady()) return;

			logger.info("Updating " + passenger);
			Vector3f velocity = velocities.get(passenger.getEntityId());

			Vector3f start = getStart().getPos();
			Vector3f end = getEnd().getPos();

			Vector3f wireDelta = new Vector3f((float) (end.x - start.x), (float) (end.y - start.y), (float) (end.z - start.z));
			Vector3f wireDirection = Vector3f.normalise(wireDelta);
			logger.info("Wire direction is " + wireDirection);

			Vector3f wireRight = Vector3f.cross(wireDirection, GRAVITY_AXIS);
			logger.info("Wire right is " + wireRight);

			Vector3f wireNormal = Vector3f.cross(wireDirection, wireRight.negate());
			logger.info("Wire normal is " + wireNormal);

			float gravity = 9.81f / 20.0f / 10.0f;
			Vector3f normalForce = Vector3f.scale(wireNormal, gravity / getComponentInAxis(wireNormal, GRAVITY_AXIS));
			logger.info("normal FORCE: " + normalForce + " gravity is " + gravity);

			//Sum all forces
			Vector3f force = new Vector3f();
			force.add(normalForce);
			force.subtract(Vector3f.scale(GRAVITY_AXIS, gravity));

			velocity.add(force);
			float length = velocity.length();
			velocity.set(wireDelta);
			velocity.setLength(length);

			Vector3f pos = new Vector3f(passenger.posX, passenger.posY, passenger.posZ);
			pos.add(velocity);

			float distanceToStart = Vector3f.distsnace(start, pos);
			float wireLength = Vector3f.distsnace(start, pos);
			float doneRatio = Math.min(distanceToStart / wireLength, 1.0f);


			MathUtils.clampPointToLine(pos, start, end);
			passenger.setPosition(pos.x, pos.y, pos.z);
			velocities.put(passenger.getEntityId(), velocity);

		}
	}

	@Override
	protected void addPassenger(Entity passenger)
	{
		super.addPassenger(passenger);
		velocities.put(passenger.getEntityId(), new Vector3f(0.0f, 0.0f, 0.0f));
	}

	@Override
	protected void removePassenger(Entity passenger)
	{
		super.removePassenger(passenger);
		velocities.remove(passenger.getEntityId());
	}

	protected boolean canFitPassenger(Entity passenger)
	{
		return true;
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound)
	{
		UUID startID = compound.getUniqueId("TCOStartID");
		UUID endID = compound.getUniqueId("TCOEndID");
		setLinks(EntityID.from(startID), EntityID.from(endID), false);
	}

	private EntityID tempStartID, tempEndID;
	private void setLinks(EntityID startID, EntityID endID, boolean force)
	{
		tempStartID = startID;
		tempEndID = endID;
		Entity start = startID.load(world);
		Entity end = endID.load(world);
		if (start == null && force)
		{
			logger.warn("Failed to read EntityHarpoonWire! Start's entity ID is invalid! Start: " + startID);
			setDead();
			return;
		}
		if (end == null && force)
		{
			logger.warn("Failed to read EntityHarpoonWire! Start's entity ID is invalid! Start: " + endID);
			setDead();
			return;
		}
		if (start == null || end == null)
		{
			logger.info("Failed to find linked entities on the first attempt");
			return;
		}
		if (!(start instanceof IJoinable))
		{
			logger.error("Failed to read EntityHarpoonWire! Start entity class is not an IJoinable! Got: " + start.getClass());
			this.setDead();
			return;
		}
		if (!(end instanceof IJoinable))
		{
			((IJoinable) start).getEntity().setDead();
			logger.error("Failed to read EntityHarpoonWire! End entity class is not an IJoinable! Got: " + end.getClass());
			this.setDead();
			return;
		}
		this.start = (IJoinable) start;
		this.end = (IJoinable) end;
		logger.info("Read wire links successfully!");
	}

	private int tryCount = 0;
	private void ensureLinks()
	{
		if (start == null && end == null)
		{
			setLinks(tempStartID, tempEndID, tryCount >= 5);
		}
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound)
	{
		compound.setUniqueId("TCOStartID", start.getEntity().getUniqueID());
		compound.setUniqueId("TCOEndID", end.getEntity().getUniqueID());
		logger.info("Saving wire entity!");
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

	private boolean isReady()
	{
		return start != null && end != null;
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
		setLinks(EntityID.from(additionalData.readInt()), EntityID.from(additionalData.readInt()), false);
	}

	public IJoinable getEnd() {
		return end;
	}

	public IJoinable getStart() {
		return start;
	}
}

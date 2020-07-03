package com.troy.tco.entity;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemLead;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.troy.tco.TCO.logger;

public class EntityHarpoon extends Entity implements IProjectile, IEntityAdditionalSpawnData
{

	private static final Predicate<Entity> ARROW_TARGETS = Predicates.and(EntitySelectors.NOT_SPECTATING, EntitySelectors.IS_ALIVE, new Predicate<Entity>()
	{
		public boolean apply(@Nullable Entity entity)
		{
			if (entity == null) return false;
			else return entity.canBeCollidedWith();
		}
	});

	private static final DataParameter<Byte> CRITICAL = EntityDataManager.<Byte>createKey(EntityArrow.class, DataSerializers.BYTE);
	private float health;
	private int xTile;
	private int yTile;
	private int zTile;
	private Block inTile;
	protected boolean inGround;
	protected int timeInGround;
	public EntityLivingBase shootingEntity;
	private int ticksInGround;
	private int ticksInAir;
	private Vec3d anchorPos;

	public EntityHarpoon(World worldIn)
	{
		super(worldIn);
		this.xTile = -1;
		this.yTile = -1;
		this.zTile = -1;
		this.health = 1.5f;
		this.setSize(0.5F, 0.5F);
	}

	public EntityHarpoon(World worldIn, double x, double y, double z)
	{
		this(worldIn);
		this.setPosition(x, y, z);
	}

	public EntityHarpoon(World worldIn, EntityLivingBase shooter, Vec3d anchorPos)
	{
		this(worldIn, shooter.posX, shooter.posY + (double) shooter.getEyeHeight() - 0.1, shooter.posZ);
		this.anchorPos = anchorPos;
		this.shootingEntity = shooter;
	}

	@Override
	protected void entityInit()
	{

	}

	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance)
	{
		double d0 = this.getEntityBoundingBox().getAverageEdgeLength() * 10.0D;

		if (Double.isNaN(d0))
		{
			d0 = 1.0D;
		}

		d0 = d0 * 64.0D * getRenderDistanceWeight();
		return distance < d0 * d0;
	}

	public void shoot(Entity shooter, float pitch, float yaw, float p_184547_4_, float velocity, float inaccuracy)
	{
		float f = -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
		float f1 = -MathHelper.sin(pitch * 0.017453292F);
		float f2 = MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
		this.shoot((double)f, (double)f1, (double)f2, velocity, inaccuracy);
		this.motionX += shooter.motionX;
		this.motionZ += shooter.motionZ;

		if (!shooter.onGround)
		{
			this.motionY += shooter.motionY;
		}
	}

	public void shoot(double x, double y, double z, float velocity, float inaccuracy)
	{
		float f = MathHelper.sqrt(x * x + y * y + z * z);
		x = x / (double)f;
		y = y / (double)f;
		z = z / (double)f;
		x = x + this.rand.nextGaussian() * 0.007499999832361937D * (double)inaccuracy;
		y = y + this.rand.nextGaussian() * 0.007499999832361937D * (double)inaccuracy;
		z = z + this.rand.nextGaussian() * 0.007499999832361937D * (double)inaccuracy;
		x = x * (double)velocity;
		y = y * (double)velocity;
		z = z * (double)velocity;
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;
		float f1 = MathHelper.sqrt(x * x + z * z);
		this.rotationYaw = (float)(MathHelper.atan2(x, z) * (180D / Math.PI));
		this.rotationPitch = (float)(MathHelper.atan2(y, (double)f1) * (180D / Math.PI));
		this.prevRotationYaw = this.rotationYaw;
		this.prevRotationPitch = this.rotationPitch;
		this.ticksInGround = 0;
	}

	@SideOnly(Side.CLIENT)
	public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport)
	{

	}

	@SideOnly(Side.CLIENT)
	public void setVelocity(double x, double y, double z)
	{
		//Nop because it gets set improperly by mc network code
	}

	@Override
	public boolean canRiderInteract() {
		return true;
	}


	@Override
	public boolean processInitialInteract(EntityPlayer entityplayer,
										  EnumHand hand) //interact : change back when Forge updates
	{
		if(isDead)
			return false;
		if(world.isRemote)
			return false;
		// If they are using a repair tool, don't put them in
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
	public boolean canBeCollidedWith()
	{
		return !isDead;
	}


	public void onUpdate()
	{
		super.onUpdate();
		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
		{
			float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI));
			this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)f) * (180D / Math.PI));
			this.prevRotationYaw = this.rotationYaw;
			this.prevRotationPitch = this.rotationPitch;
		}

		BlockPos blockpos = new BlockPos(this.xTile, this.yTile, this.zTile);
		IBlockState iblockstate = this.world.getBlockState(blockpos);
		Block block = iblockstate.getBlock();

		if (iblockstate.getMaterial() != Material.AIR)
		{
			AxisAlignedBB axisalignedbb = iblockstate.getCollisionBoundingBox(this.world, blockpos);

			if (axisalignedbb != Block.NULL_AABB && axisalignedbb.offset(blockpos).contains(new Vec3d(this.posX, this.posY, this.posZ)))
			{
				this.inGround = true;
			}
		}

		if (this.inGround)
		{

			if (block != this.inTile && !this.world.collidesWithAnyBlock(this.getEntityBoundingBox().grow(0.05D)))
			{
				this.inGround = false;
				this.motionX = 0.0;
				this.motionY = 0.0;
				this.motionZ = 0.0;
				this.ticksInGround = 0;
				this.ticksInAir = 0;
			}
			else
			{
				++this.ticksInGround;
			}

			++this.timeInGround;
		}
		else
		{
			this.timeInGround = 0;
			++this.ticksInAir;
			raytrace();

			this.posX += this.motionX;
			this.posY += this.motionY;
			this.posZ += this.motionZ;
			float f4 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI));

			for (this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)f4) * (180D / Math.PI)); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
			{
				;
			}

			while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
			{
				this.prevRotationPitch += 360.0F;
			}

			while (this.rotationYaw - this.prevRotationYaw < -180.0F)
			{
				this.prevRotationYaw -= 360.0F;
			}

			while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
			{
				this.prevRotationYaw += 360.0F;
			}

			this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
			this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
			float drag = 0.99F;

			if (this.isInWater() && world.isRemote)
			{
				for (int i = 0; i < 4; ++i)
				{
					float f3 = 0.25F;
					this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * 0.25D, this.posY - this.motionY * 0.25D, this.posZ - this.motionZ * 0.25D, this.motionX, this.motionY, this.motionZ);
				}

				drag = 0.9F;
			}

			if (this.isWet())
			{
				this.extinguish();
			}

			this.motionX *= (double) drag;
			this.motionY *= (double) drag;
			this.motionZ *= (double) drag;

			if (!this.hasNoGravity())
			{
				this.motionY -= 0.05;
			}

			this.setPosition(this.posX, this.posY, this.posZ);
			this.doBlockCollisions();
		}
	}

	private void raytrace()
	{
		final Vec3d start = new Vec3d(this.posX, this.posY, this.posZ);
		Vec3d tempStart = start;
		Vec3d end = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

		ArrayList<Pair<BlockPos, IBlockState>> tempBrokenBlocks = null;
		if (world.isRemote)
		{
			tempBrokenBlocks = new ArrayList<>();
		}
		while(true)
		{
			RayTraceResult raytraceresult = this.world.rayTraceBlocks(tempStart, end, false, true, false);

			if (raytraceresult == null)
			{
				break;
			}
			BlockPos pos = raytraceresult.getBlockPos();
			IBlockState state = this.world.getBlockState(pos);
			float strength = state.getBlock().getExplosionResistance(world, pos, this, new Explosion(world, this, pos.getX(), pos.getY(), pos.getZ(), 1.0f, true, true));

			//Only subtract blocks with a resistance > 0.3 (allows for an infinite number of flowers, leaves, grass, etc to be broken)
			if (strength > 0.3f)
			{
				this.health -= strength;
			}
			if (this.health <= 0.0f)
			{
				//This harpoon has broken all the blocks that is can
				//Stop it here
				this.health = 0.0f;

				this.xTile = pos.getX();
				this.yTile = pos.getY();
				this.zTile = pos.getZ();
				this.inTile = state.getBlock();

				//Project where the harpoon will land so it touches the block that stopped it
				this.motionX = (double) ((float) (raytraceresult.hitVec.x - this.posX));
				this.motionY = (double) ((float) (raytraceresult.hitVec.y - this.posY));
				this.motionZ = (double) ((float) (raytraceresult.hitVec.z - this.posZ));
				float f2 = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
				this.posX -= this.motionX / (double) f2 * 0.05;
				this.posY -= this.motionY / (double) f2 * 0.05;
				this.posZ -= this.motionZ / (double) f2 * 0.05;

				//Set the end to here to stop the entity ray-tracer from hitting mobs through walls
				end = new Vec3d(raytraceresult.hitVec.x, raytraceresult.hitVec.y, raytraceresult.hitVec.z);
				this.inGround = true;

				if (state.getMaterial() != Material.AIR)
				{
					this.inTile.onEntityCollidedWithBlock(this.world, pos, state, this);
				}

				break;
			}
			else
			{
				//Break the weak block and re-trace starting from where the block that we broke is
				if (tempBrokenBlocks != null)
				{
					//"simulate" breaking blocks by restoring them after so the server can have the final say
					tempBrokenBlocks.add(Pair.of(pos, state));
				}
				world.setBlockToAir(pos);


				tempStart = new Vec3d(raytraceresult.hitVec.x, raytraceresult.hitVec.y, raytraceresult.hitVec.z);
			}
		}
		if (tempBrokenBlocks != null)
		{
			for (Pair<BlockPos, IBlockState> pair : tempBrokenBlocks)
			{
				world.setBlockState(pair.getLeft(), pair.getRight());
			}
		}


		List<Entity> list = this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0D), ARROW_TARGETS);
		double d0 = 0.0D;

		for (int i = 0; i < list.size(); ++i)
		{
			Entity victim = list.get(i);

			if (victim != this.shootingEntity || this.ticksInAir >= 5)
			{
				AxisAlignedBB axisalignedbb = victim.getEntityBoundingBox()/*.grow(0.3)*/;
				RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(start, end);

				if (raytraceresult != null)
				{
					if (this.shootingEntity instanceof EntityPlayer && raytraceresult.entityHit instanceof EntityPlayer && !((EntityPlayer)this.shootingEntity).canAttackPlayer((EntityPlayer) raytraceresult.entityHit))
					{
						//The shooter cant attack this player
						continue;
					}
					float speed = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
					float damage = MathHelper.ceil((double) speed * 5.0f);

					DamageSource damagesource;
					if (this.shootingEntity == null)
					{
						damagesource = new DamageSource("tco_harpoon");
					}
					else
					{
						damagesource = new EntityDamageSource("tco_harpoon", shootingEntity);
					}

					if (!world.isRemote)
					{
						if (this.isBurning() && !(victim instanceof EntityEnderman))
						{
							victim.setFire(5);
						}
						if (victim.attackEntityFrom(damagesource, damage))
						{
							//this.playSound(SoundEvents.ENTITY_ARROW_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
						}
					}

				}
			}
		}
	}


	public void move(MoverType type, double x, double y, double z)
	{
		super.move(type, x, y, z);

		if (this.inGround)
		{
			this.xTile = MathHelper.floor(this.posX);
			this.yTile = MathHelper.floor(this.posY);
			this.zTile = MathHelper.floor(this.posZ);
		}
	}

	public void writeEntityToNBT(NBTTagCompound compound)
	{
		compound.setInteger("xTile", this.xTile);
		compound.setInteger("yTile", this.yTile);
		compound.setInteger("zTile", this.zTile);
		compound.setByte("inGround", (byte)(this.inGround ? 1 : 0));
		compound.setFloat("Health", health);
		compound.setDouble("AnchorX", anchorPos.x);
		compound.setDouble("AnchorY", anchorPos.y);
		compound.setDouble("AnchorZ", anchorPos.z);
	}

	public void readEntityFromNBT(NBTTagCompound compound)
	{
		this.xTile = compound.getInteger("xTile");
		this.yTile = compound.getInteger("yTile");
		this.zTile = compound.getInteger("zTile");
		this.inGround = compound.getByte("inGround") == 1;
		this.health = compound.getFloat("Health");
		this.anchorPos = new Vec3d(compound.getDouble("AnchorX"), compound.getDouble("AnchorY"), compound.getDouble("AnchorZ"));
	}


	@Override
	public void writeSpawnData(ByteBuf buffer) {
		buffer.writeDouble(motionX);
		buffer.writeDouble(motionY);
		buffer.writeDouble(motionZ);
		buffer.writeDouble(anchorPos.x);
		buffer.writeDouble(anchorPos.y);
		buffer.writeDouble(anchorPos.z);
	}

	@Override
	public void readSpawnData(ByteBuf additionalData) {
		motionX = additionalData.readDouble();
		motionY = additionalData.readDouble();
		motionZ = additionalData.readDouble();
		anchorPos = new Vec3d(additionalData.readDouble(), additionalData.readDouble(), additionalData.readDouble());
	}

	public Vec3d getAnchorPos() {
		return anchorPos;
	}
}

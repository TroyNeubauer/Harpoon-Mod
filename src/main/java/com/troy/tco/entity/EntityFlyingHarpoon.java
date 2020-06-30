package com.troy.tco.entity;

import com.troy.tco.TCO;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class EntityFlyingHarpoon extends EntityThrowable implements IEntityAdditionalSpawnData {

	private EntityPlayer shooter;

	public EntityFlyingHarpoon(World worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
	}

	public EntityFlyingHarpoon(World worldIn, EntityPlayer shooter) {
		this(worldIn, shooter.posX, shooter.posY + shooter.getEyeHeight() - 0.2, shooter.posZ);
		this.shooter = shooter;
	}


	public EntityFlyingHarpoon(World worldIn) {
		super(worldIn);
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		throw new RuntimeException("Nop. Equalivant logic must be done through on update");
	}

	@Override
	public void onUpdate() {
		//Copied from EntityThrowable::onUpdate
		this.lastTickPosX = this.posX;
		this.lastTickPosY = this.posY;
		this.lastTickPosZ = this.posZ;
		this.onEntityUpdate();

		if (this.throwableShake > 0)
		{
			--this.throwableShake;
		}

		if (this.inGround)
		{
/*			if (this.world.getBlockState(new BlockPos(this.posX, this.posY, this.posZ)).getBlock() != this.inTile)
			{
				TCO.logger.info("Killing harpoon ID " + getEntityId() + " because its anchor block broke!");
				setDead();
				return;
			}*/
		}
		else
		{
			Vec3d initalPos = new Vec3d(this.posX, this.posY, this.posZ);
			Vec3d nextPos = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
			RayTraceResult raytraceresult = null;
			while(true)
			{
				raytraceresult = this.world.rayTraceBlocks(initalPos, nextPos);
				nextPos = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
				if (raytraceresult == null || raytraceresult.typeOfHit == RayTraceResult.Type.MISS)
				{
					raytraceresult = null;
					break;
				}
				BlockPos pos = raytraceresult.getBlockPos();
				IBlockState state = world.getBlockState(pos);
				//World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion
				//    public Explosion(World worldIn, Entity entityIn, double x, double y, double z, float size, boolean flaming, boolean damagesTerrain)
				float resistance = state.getBlock().getExplosionResistance(world, pos, this, new Explosion(world, this, pos.getX(), pos.getY(), pos.getZ(), 1.0f, true, true));
				if (resistance <= 0.3f)
				{
					TCO.logger.info("Going through block " + state.getBlock().getLocalizedName() + " resistance: " + resistance);
					world.setBlockToAir(pos);
				}
			}

			if (raytraceresult != null)
			{
				nextPos = new Vec3d(raytraceresult.hitVec.x, raytraceresult.hitVec.y, raytraceresult.hitVec.z);
			}

			Entity entity = null;
			List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0D));
			double d0 = 0.0D;
			boolean flag = false;

			for (int i = 0; i < list.size(); ++i)
			{
				Entity entity1 = list.get(i);

				if (entity1.canBeCollidedWith())
				{
					if (entity1 == this.ignoreEntity)
					{
						flag = true;
					}
					else if (this.thrower != null && this.ticksExisted < 2 && this.ignoreEntity == null)
					{
						this.ignoreEntity = entity1;
						flag = true;
					}
					else
					{
						flag = false;
						AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow(0.30000001192092896D);
						RayTraceResult raytraceresult1 = axisalignedbb.calculateIntercept(initalPos, nextPos);

						if (raytraceresult1 != null)
						{
							double d1 = initalPos.squareDistanceTo(raytraceresult1.hitVec);

							if (d1 < d0 || d0 == 0.0D)
							{
								entity = entity1;
								d0 = d1;
							}
						}
					}
				}
			}


			if (entity != null && entity != shooter)
			{
				entity.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), 50.0f);
			}

			if (raytraceresult != null)
			{
				if (raytraceresult.typeOfHit == RayTraceResult.Type.BLOCK && this.world.getBlockState(raytraceresult.getBlockPos()).getBlock() == Blocks.PORTAL)
				{
					this.setPortal(raytraceresult.getBlockPos());
				}
				else if (raytraceresult.typeOfHit == RayTraceResult.Type.BLOCK)
				{
					if (getEntityWorld().isRemote)
					{
						float pitch = ThreadLocalRandom.current().nextInt(100, 300) / 10000.0f;
						world.playSound((EntityPlayer) null, posX, posY, posZ, SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 1.0f, pitch);
						if (shooter != null)
						{
							world.playSound((EntityPlayer) shooter, shooter.posX, shooter.posY, shooter.posZ, SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 1.0f, pitch);
						}
					}
					this.motionX = 0;
					this.motionY = 0;
					this.motionZ = 0;
					if (!world.isRemote)
					{
						this.setPositionAndUpdate(raytraceresult.hitVec.x, raytraceresult.hitVec.y, raytraceresult.hitVec.z);
						markVelocityChanged();
						setNoGravity(true);
						TCO.logger.info("IMPACT stropping entity: " + getEntityId() + " pos set to: " + posX + ", " + posY + ", " + posZ + ", ");
					}
				}
			}

			this.posX += this.motionX;
			this.posY += this.motionY;
			this.posZ += this.motionZ;
			float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI));

			for (this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)f) * (180D / Math.PI)); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
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
			float speed = 1.0f;
			float acceleration = this.getGravityVelocity();

			if (this.isInWater())
			{
				for (int j = 0; j < 4; ++j)
				{
					this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * 0.25D, this.posY - this.motionY * 0.25D, this.posZ - this.motionZ * 0.25D, this.motionX, this.motionY, this.motionZ);
				}

				speed = 0.8f;
			}

			this.motionX *= (double) speed;
			this.motionZ *= (double) speed;
			this.motionY *= (double) speed;

			if (!this.hasNoGravity())
			{
				this.motionY -= (double)acceleration;
			}

			this.setPosition(this.posX, this.posY, this.posZ);
		}


	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		if (shooter != null)
		{
			UUID uuid = shooter.getUniqueID();
			TCO.logger.info("Writing uuid: " + uuid);
			buffer.writeLong(uuid.getMostSignificantBits());
			buffer.writeLong(uuid.getLeastSignificantBits());
		}
		else
		{
			buffer.writeLong(0);
			buffer.writeLong(0);
		}
	}

	@Override
	public void readSpawnData(ByteBuf additionalData) {
		long upper = additionalData.readLong();
		long lower = additionalData.readLong();
		UUID uuid = new UUID(upper, lower);
		TCO.logger.info("Read uuid: " + uuid);
		this.shooter = getEntityWorld().getPlayerEntityByUUID(uuid);
	}


	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		UUID uuid = compound.getUniqueId("Shooter");
		TCO.logger.info("Read shooter UUID: " + uuid);
		shooter = getEntityWorld().getPlayerEntityByUUID(uuid);
		if (shooter == null)
		{
			TCO.logger.info("Failed to load harpoon shooter from UUID");
			String username = compound.getString("ShooterName");
			shooter = getEntityWorld().getPlayerEntityByName(username);
			TCO.logger.info("Read shooter from username: " + username);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		if (shooter != null)
		{
			TCO.logger.info("saving harpoon cap! Shooter: " + shooter.toString());
			compound.setUniqueId("Shooter", shooter.getUniqueID());
			compound.setString("ShooterName", shooter.getName());
		}
		else
		{
			TCO.logger.info("refusing to save null shooter");
		}
		return compound;
	}
}

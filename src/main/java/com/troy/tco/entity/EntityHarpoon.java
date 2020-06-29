package com.troy.tco.entity;

import com.google.common.base.Predicates;
import com.troy.tco.TCO;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class EntityHarpoon extends EntityArrow {

/*		private static final Predicate<Entity> ARROW_TARGETS = Predicates.and(EntitySelectors.NOT_SPECTATING, EntitySelectors.IS_ALIVE, new Predicate<Entity>() {
			@Override
			public boolean apply(@Nullable Entity targetEntity) {
				if (targetEntity.hurtResistantTime > 0) {
					targetEntity.hurtResistantTime = 0;
					return false;
				}
				if (targetEntity instanceof EntityEnderman) {
					return true;
				}
				return targetEntity.canBeCollidedWith();
			}
		});*/

		//private static final DataParameter<Integer> TYPE = EntityDataManager.<Integer>createKey(EntityHarpoon.class, DataSerializers.VARINT);

		public static final AxisAlignedBB ON_BLOCK_AABB = new AxisAlignedBB(-0.05D, -0.05D, -0.05D, 0.05D, 0.05D, 0.05D);

		public EntityHarpoon(World worldIn, double x, double y, double z) {
			super(worldIn, x, y, z);
			this.onUpdate();
		}

		public EntityHarpoon(World worldIn, EntityLivingBase shooter) {
			this(worldIn, shooter.posX, shooter.posY + shooter.getEyeHeight() - 0.2, shooter.posZ);
			this.shootingEntity = shooter;

			this.pickupStatus = PickupStatus.DISALLOWED;
			this.onUpdate();
		}

		public EntityHarpoon(World worldIn) {
			super(worldIn);
			this.onUpdate();
		}

		@Override
		public void shoot(Entity shooter, float pitch, float yaw, float unused, float velocity, float inaccuracy) {
			TCO.logger.info("Shooting with pitch: " + pitch + " yaw " + yaw);
			float f = -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
			float f1 = -MathHelper.sin(pitch * 0.017453292F);
			float f2 = MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
			this.shoot(f, f1, f2, velocity, inaccuracy);
			this.motionX += shooter.motionX;
			this.motionZ += shooter.motionZ;
		}

		@Override
		public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
			super.shoot(x, y, z, velocity, inaccuracy);
		}

		@Override
		public void onUpdate() {
			onEntityUpdate();

			if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
				float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
				this.rotationYaw = (float) (MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI));
				this.rotationPitch = (float) (MathHelper.atan2(this.motionY, f) * (180D / Math.PI));
				this.prevRotationYaw = this.rotationYaw;
				this.prevRotationPitch = this.rotationPitch;
			}

			BlockPos blockpos = new BlockPos(this.posX, this.posY, this.posZ);
			IBlockState state = this.world.getBlockState(blockpos);
			Block block = this.world.getBlockState(blockpos).getBlock();

			if (state.getMaterial() != Material.AIR) {
				AxisAlignedBB aabb = state.getCollisionBoundingBox(this.world, blockpos);
				if (aabb != Block.NULL_AABB && aabb.grow(0.1F).offset(blockpos).contains(new Vec3d(this.posX, this.posY, this.posZ))) {
					this.inGround = true;
				}
			}

			if (this.arrowShake > 0)
				this.arrowShake--;

			if (this.inGround) {
				updateInGround(state);
			} else {
				updateInAir();
			}
			TCO.logger.info("Y: " + this.posY);
		}

		private void updateInAir() {
			this.timeInGround = 0;

			Vec3d vec3d1 = new Vec3d(this.posX, this.posY, this.posZ);
			Vec3d vec3d = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
			RayTraceResult raytraceresult = this.world.rayTraceBlocks(vec3d1, vec3d, false, true, false);

			if (raytraceresult != null) {
				vec3d = new Vec3d(raytraceresult.hitVec.x, raytraceresult.hitVec.y, raytraceresult.hitVec.z);
			}

			Entity entity = this.findEntityOnPath(vec3d1, vec3d);

			if (entity != null) {
				raytraceresult = new RayTraceResult(entity);
			}

			if (raytraceresult != null && raytraceresult.entityHit instanceof EntityPlayer) {
				EntityPlayer entityplayer = (EntityPlayer) raytraceresult.entityHit;

				if (this.shootingEntity instanceof EntityPlayer && !((EntityPlayer) this.shootingEntity).canAttackPlayer(entityplayer)) {
					raytraceresult = null;
				}
			}

			if (raytraceresult != null && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
				this.onHit(raytraceresult);
			}

			BlockPos blockpos = this.getPosition();
			IBlockState iblockstate = this.world.getBlockState(blockpos);

			this.posX += this.motionX;
			this.posY += this.motionY;
			this.posZ += this.motionZ;
			float f4 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.rotationYaw = (float) (MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI));

			for (this.rotationPitch = (float) (MathHelper.atan2(this.motionY, f4) * (180D / Math.PI)); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
				;
			}

			while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
				this.prevRotationPitch += 360.0F;
			}

			while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
				this.prevRotationYaw -= 360.0F;
			}

			while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
				this.prevRotationYaw += 360.0F;
			}

			this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
			this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
			float f1 = 0.99F;
			float f2 = 0.05F;

			if (this.isInWater()) {
				for (int i = 0; i < 4; ++i) {
					float f3 = 0.25F;
					this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * 0.25D, this.posY - this.motionY * 0.25D, this.posZ - this.motionZ * 0.25D, this.motionX, this.motionY, this.motionZ);
				}

				f1 = 0.6F;
			}

			if (this.isWet()) {
				this.extinguish();
			}

			this.motionX *= f1;
			this.motionY *= f1;
			this.motionZ *= f1;

			if (!this.hasNoGravity())
				this.motionY -= 0.05;
			this.setPosition(this.posX, this.posY, this.posZ);
			this.doBlockCollisions();
		}

		public void updateInGround(IBlockState state) {
			Block block = state.getBlock();
			int meta = block.getMetaFromState(state);

			++this.timeInGround;
		}

		private void onHitBlock(RayTraceResult raytraceResultIn) {

			BlockPos blockpos = raytraceResultIn.getBlockPos();
			IBlockState iblockstate = this.world.getBlockState(blockpos);

			if (!this.world.isRemote) {
				world.createExplosion(this, blockpos.getX(), blockpos.getY(), blockpos.getZ(), 8, true);
				TCO.logger.info("BOOM!");
				this.actuallySetDead();
			}
		}

		@Override
		protected ItemStack getArrowStack() {
			return ItemStack.EMPTY;
		}

		public void actuallySetDead()
		{
			super.setDead();
		}

		@Override
		public void setDead()
		{

		}

		@Override
		public void onHit(RayTraceResult raytraceResultIn) {
			Entity entity = raytraceResultIn.entityHit;

			if (entity == null) {
				onHitBlock(raytraceResultIn);
			}
		}

		@Override
		public boolean getIsCritical() {
			return false;
		}

	}
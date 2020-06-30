package com.troy.tco.entity;

import com.troy.tco.TCO;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class EntityFlyingHarpoon extends EntityThrowable implements IEntityAdditionalSpawnData {

	private EntityPlayer shooter;

	public EntityFlyingHarpoon(World worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
		this.onUpdate();
	}

	public EntityFlyingHarpoon(World worldIn, EntityPlayer shooter) {
		this(worldIn, shooter.posX, shooter.posY + shooter.getEyeHeight() - 0.2, shooter.posZ);
		this.shooter = shooter;
	}


	public EntityFlyingHarpoon(World worldIn) {
		super(worldIn);
		this.onUpdate();
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if (result.typeOfHit != RayTraceResult.Type.BLOCK)
		{
			throw new RuntimeException("Expected only block impacts for EntityHarpoon. Event bus handlers not registered properly?");
		}
		if (getEntityWorld().isRemote)
		{
			float pitch = ThreadLocalRandom.current().nextInt(100, 300) / 10000.0f;
			world.playSound((EntityPlayer) null, posX, posY, posZ, SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 1.0f, pitch);
			TCO.logger.info("Using pitch " + pitch);
			if (shooter != null)
			{
				world.playSound((EntityPlayer) shooter, shooter.posX, shooter.posY, shooter.posZ, SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 1.0f, pitch);
			}
		}
		setDead();
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

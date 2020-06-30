package com.troy.tco.entity;

import com.troy.tco.TCO;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class EntityFixedHarpoon extends Entity /*implements IEntityAdditionalSpawnData*/ {

	private final static BlockPos NO_START_POS = new BlockPos(-1, -1, -1);

	private EntityPlayer shooter;
	private BlockPos startPos = NO_START_POS;

	public EntityFixedHarpoon(World worldIn, double x, double y, double z) {
		super(worldIn);
		setPosition(x, y, z);
	}

	public EntityFixedHarpoon(World worldIn, EntityPlayer shooter) {
		this(worldIn, shooter.posX, shooter.posY + shooter.getEyeHeight() - 0.2, shooter.posZ);
		this.shooter = shooter;
	}


	public EntityFixedHarpoon(World worldIn) {
		super(worldIn);
		this.onUpdate();
	}

	//Returns true if this is anchored from both ends (the player has right clicked a block after the harpoon was shot)
	public boolean isFixed()
	{
		return !startPos.equals(NO_START_POS);
	}

	@Override
	protected void entityInit() {

	}
/*
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
		if (shooter == null)
		{
			TCO.logger.info("Fixed harpoon failed to find shooter! failing!");
			setDead();
		}
	}
*/

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		UUID uuid = compound.getUniqueId("Shooter");
		TCO.logger.info("Fixed e: Read shooter UUID: " + uuid);
		shooter = getEntityWorld().getPlayerEntityByUUID(uuid);
		if (shooter == null)
		{
			TCO.logger.info("Failed to load fixed harpoon shooter from UUID");
			String username = compound.getString("ShooterName");
			shooter = getEntityWorld().getPlayerEntityByName(username);
		}
		int x = compound.getInteger("StartX");
		int y = compound.getInteger("StartY");
		int z = compound.getInteger("StartZ");
		startPos = new BlockPos(x, y, z);
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		if (shooter != null)
		{
			TCO.logger.info("saving fixed harpoon cap! Shooter: " + shooter.toString());
			compound.setUniqueId("Shooter", shooter.getUniqueID());
			compound.setString("ShooterName", shooter.getName());
		}
		else
		{
			TCO.logger.info("refusing to save null shooter");
		}
		compound.setInteger("StartX", startPos.getX());
		compound.setInteger("StartY", startPos.getY());
		compound.setInteger("StartZ", startPos.getZ());
	}

}

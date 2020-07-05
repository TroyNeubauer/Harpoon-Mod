package com.troy.tco.entity;

import com.troy.tco.api.IJoinable;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import static com.troy.tco.TCO.logger;

public class EntityFixedJoinable extends Entity implements IJoinable, IEntityAdditionalSpawnData
{
	private BlockPos tile;

	public EntityFixedJoinable(World world)
	{
		super(world);
		setSize(0.5f, 0.5f);
		this.forceSpawn = true;
	}


	public EntityFixedJoinable(World world, BlockPos tile, Vec3d pos)
	{
		this(world);
		this.tile = tile;
		setPosition(pos.x, pos.y, pos.z);
	}

	@Override
	public Vec3d getPos()
	{
		return getPositionVector();
	}

	int ticks = 0;
	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if (world.getBlockState(tile).getBlock() == Blocks.AIR)
		{
			this.setDead();
		}
	}

	@Override
	public boolean isBroken()
	{
		return isDead;
	}

	@Override
	public Entity getEntity()
	{
		return this;
	}

	@Override
	protected void entityInit()
	{
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound)
	{
		tile = new BlockPos(compound.getInteger("TCOTileX"), compound.getInteger("TCOTileY"), compound.getInteger("TCOTileZ"));
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound)
	{
		compound.setInteger("TCOTileX", tile.getX());
		compound.setInteger("TCOTileY", tile.getY());
		compound.setInteger("TCOTileZ", tile.getZ());
	}

	@Override
	public void writeSpawnData(ByteBuf buffer)
	{
		buffer.writeInt(tile.getX());
		buffer.writeInt(tile.getY());
		buffer.writeInt(tile.getZ());
	}

	@Override
	public void readSpawnData(ByteBuf additionalData)
	{
		this.tile = new BlockPos(additionalData.readInt(), additionalData.readInt(), additionalData.readInt());
	}
}

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
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class EntityHarpoon extends EntityThrowable {


	public EntityHarpoon(World worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
		this.onUpdate();
	}

	public EntityHarpoon(World worldIn, EntityLivingBase shooter) {
		this(worldIn, shooter.posX, shooter.posY + shooter.getEyeHeight() - 0.2, shooter.posZ);
	}

	public EntityHarpoon(World worldIn) {
		super(worldIn);
		this.onUpdate();
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if (result.typeOfHit != RayTraceResult.Type.BLOCK)
		{
			throw new RuntimeException("Expected only block impacts for EntityHarpoon. Event bus handlers not registered properly?");
		}
		getEntityWorld().createExplosion(this, result.getBlockPos().getX(), result.getBlockPos().getY(), result.getBlockPos().getZ(), 5, false);
	}
}

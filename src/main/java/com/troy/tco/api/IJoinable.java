package com.troy.tco.api;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

//Represents something that can be joined to a second joinable using a wire
public interface IJoinable
{
	public Vec3d getPos();
	public boolean isBroken();
	public Entity getEntity();
}

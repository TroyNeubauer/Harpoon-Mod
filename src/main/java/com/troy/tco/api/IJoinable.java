package com.troy.tco.api;

import com.troy.tco.util.Vector3f;
import net.minecraft.entity.Entity;

//Represents something that can be joined to a second joinable using a wire
public interface IJoinable
{
	public Vector3f getPos();
	public boolean isBroken();
	public Entity getEntity();
}

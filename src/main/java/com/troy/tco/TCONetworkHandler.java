package com.troy.tco;

import com.troy.tco.entity.EntityHarpoonWire;
import com.troy.tco.util.Vector3f;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import static com.troy.tco.TCO.logger;

public class TCONetworkHandler extends SimpleNetworkWrapper
{
	public TCONetworkHandler()
	{
		super(Constants.MODID);
		int id = 0;
		registerMessage(MyMessageHandler.class, WireInteractMessage.class, id++, Side.SERVER);
	}


	public static class WireInteractMessage implements IMessage
	{
		public WireInteractMessage(){}

		private int wireID;
		private float clickX;
		private float clickY;
		private float clickZ;

		public WireInteractMessage(EntityHarpoonWire wire, Vector3f clickPos)
		{
			this.wireID = wire.getEntityId();
			this.clickX = clickPos.x;
			this.clickY = clickPos.y;
			this.clickZ = clickPos.z;
		}

		@Override
		public void toBytes(ByteBuf buf)
		{
			buf.writeInt(wireID);
			buf.writeFloat(clickX);
			buf.writeFloat(clickY);
			buf.writeFloat(clickZ);
		}

		@Override
		public void fromBytes(ByteBuf buf)
		{
			this.wireID = buf.readInt();
			this.clickX = buf.readFloat();
			this.clickY = buf.readFloat();
			this.clickZ = buf.readFloat();
		}
	}

	public static class MyMessageHandler implements IMessageHandler<WireInteractMessage, IMessage>
	{
		@Override
		public IMessage onMessage(WireInteractMessage message, MessageContext ctx) {
			EntityPlayerMP sendingPlayer = ctx.getServerHandler().player;
			sendingPlayer.getServerWorld().addScheduledTask(() -> {
				Entity wireEntity = sendingPlayer.getServerWorld().getEntityByID(message.wireID);
				if (wireEntity == null)
				{
					logger.warn("Unable to find wire entity: " + message.wireID + " while trying to ride the harpoon wire");
				}
				else if (!(wireEntity instanceof EntityHarpoonWire))
				{
					logger.warn("Wire entity: " + wireEntity.getEntityId() + " is not a harpoon wire! Is a " + wireEntity.getClass() + " = " + wireEntity.toString());
				}
				else
				{
					EntityHarpoonWire wire = ((EntityHarpoonWire) wireEntity);
					wire.interact(sendingPlayer, new Vector3f(message.clickX, message.clickY, message.clickZ));
				}
			});
			// No response packet
			return null;
		}
	}

}

package com.fluidtranslator.network;

import com.fluidtranslator.tileentity.TileEntityUniversalTank;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;

/**
 * Network message sent from the client to the server to request a change
 * in the operation mode of a {@link com.fluidtranslator.tileentity.TileEntityUniversalTank}.
 * <p>
 * This message is typically triggered when the player clicks the mode button
 * inside the tank's GUI. It carries the tank's coordinates in the world
 * (x, y, z) and the new mode to be applied.
 * <p>
 * The server receives this message and updates the tank's state accordingly.
 * The {@link Handler} inner class handles this logic on the server side.
 *
 * <p>
 */
public class MessageSetOperationMode implements IMessage {
    private int x, y, z;
    private short mode;

    public MessageSetOperationMode() {}

    public MessageSetOperationMode(int x, int y, int z, short mode) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.mode = mode;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        mode = buf.readShort();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeShort(mode);
    }

    public static class Handler implements IMessageHandler<MessageSetOperationMode, IMessage> {
        @Override
        public IMessage onMessage(MessageSetOperationMode message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            TileEntity te = player.worldObj.getTileEntity(message.x, message.y, message.z);
            if (te instanceof TileEntityUniversalTank) {
                ((TileEntityUniversalTank) te).setTankMode(message.mode);
            }
            return null;
        }
    }
}


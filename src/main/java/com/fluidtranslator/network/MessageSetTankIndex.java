package com.fluidtranslator.network;

import com.fluidtranslator.tileentity.TileEntityHBMAdapter;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;

/**
 * Network message sent from the client to the server to select a specific tank
 * inside a machine connected to a {@link TileEntityHBMAdapter}.
 * <p>
 * The {@code index} field identifies which tank in the machine's internal array
 * of tanks should be targeted. This message is typically triggered when the player
 * interacts with the adapter's GUI to change the currently selected tank.
 * <p>
 * The server receives this message and updates the adapter's state accordingly.
 * The {@link Handler} inner class handles the server-side logic.
 */
public class MessageSetTankIndex implements IMessage {
    private int x, y, z;
    private int index;

    public MessageSetTankIndex() { }

    public MessageSetTankIndex(int x, int y, int z, int index) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.index = index;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        index = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(index);
    }

    public static class Handler implements IMessageHandler<MessageSetTankIndex, IMessage> {
        @Override
        public IMessage onMessage(MessageSetTankIndex message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            TileEntity te = player.worldObj.getTileEntity(message.x, message.y, message.z);
            if (te instanceof TileEntityHBMAdapter) {
                ((TileEntityHBMAdapter) te).setTankIndex(message.index);
            }
            return null;
        }
    }
}

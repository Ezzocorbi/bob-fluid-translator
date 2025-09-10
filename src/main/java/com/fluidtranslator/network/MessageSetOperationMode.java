package com.fluidtranslator.network;

import com.fluidtranslator.tileentity.TileEntityUniversalTank;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;

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
                System.out.println("set tank mode " + message.mode);
                ((TileEntityUniversalTank) te).setTankMode(message.mode);
            }
            return null;
        }
    }
}



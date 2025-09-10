package com.fluidtranslator.network;

import com.fluidtranslator.tileentity.TileEntityHBMAdapter;
import com.fluidtranslator.tileentity.TileEntityUniversalTank;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;

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

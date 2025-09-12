package com.ezzo.fluidtranslator.network;

import com.ezzo.fluidtranslator.tileentity.TileEntityHBMAdapter;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;

/**
 * Network message sent from the client to the server to toggle the "reset tank" feature
 * of a {@link TileEntityHBMAdapter}.
 * <p>
 * Normally, when a tank from HBM is drained to 0 mB, it still retains its last
 * {@code FluidType}. This can cause issues when the adapter is used with Forge fluids,
 * since other systems may consider the tank to still hold a non-empty fluid type.
 * <p>
 * To solve this, the adapter provides an option to automatically reset the tank's
 * fluid type back to {@code NONE} when it becomes completely empty. This message
 * communicates the player's choice (via the adapter's GUI button) to the server,
 * ensuring that the adapter's behavior stays synchronized with the player's intent.
 * <p>
 * The {@code reset} flag determines whether the reset behavior is enabled or disabled.
 * The {@link Handler} inner class applies the change on the server side.
 */
public class MessageResetTank implements IMessage {
    private int x, y, z;
    private boolean reset;

    public MessageResetTank() { }

    public MessageResetTank(int x, int y, int z, boolean reset) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.reset = reset;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        reset = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeBoolean(reset);
    }

    public static class Handler implements IMessageHandler<MessageResetTank, IMessage> {
        @Override
        public IMessage onMessage(MessageResetTank message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            TileEntity te = player.worldObj.getTileEntity(message.x, message.y, message.z);
            if (te instanceof TileEntityHBMAdapter) {
                ((TileEntityHBMAdapter) te).shouldResetFluidType(message.reset);
            }
            return null;
        }
    }
}

package com.ezzo.fluidtranslator;

import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardSenderMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.util.fauxpointtwelve.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility class that encapsulates both {@link IFluidStandardSenderMK2} and
 * {@link IFluidStandardReceiverMK2} interfaces into a single handler.
 * <p>
 * While {@link IFluidStandardTransceiverMK2} implements both
 * interfaces, not all fluid-capable machines in the game are transceivers.
 * Some machines act purely as senders, others purely as receivers. This class
 * provides a unified abstraction that can wrap either role (or both) and
 * expose a common API for interacting with them.
 * <p>
 * Additionally, helper methods are provided to locate sender/receiver tiles
 * within multiblock structures by resolving from any of their dummy blocks.
 */

public class FluidHandler {
    private IFluidStandardSenderMK2 sender;
    private IFluidStandardReceiverMK2 receiver;

    public FluidHandler() {
    }

    public FluidHandler(IFluidStandardSenderMK2 sender) {
        this.sender = sender;
    }

    public FluidHandler(IFluidStandardReceiverMK2 receiver) {
        this.receiver = receiver;
    }

    public FluidHandler(IFluidStandardSenderMK2 sender, IFluidStandardReceiverMK2 receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }


    public long getFluidAvailable(FluidType type, int pressure) {
        if (sender != null) return sender.getFluidAvailable(type, pressure);
        else return 0;
    }

    public void useUpFluid(FluidType type, int pressure, long amount) {
        if (sender != null) sender.useUpFluid(type, pressure, amount);
    }

    public long getDemand(FluidType type, int pressure) {
        if (receiver != null) return receiver.getDemand(type, pressure);
        else return 0;
    }

    public long transferFluid(FluidType type, int pressure, long amount) {
        if (receiver != null) return receiver.transferFluid(type, pressure, amount);
        else return 0;
    }

    public void setReceiver(IFluidStandardReceiverMK2 receiver) {
        this.receiver = receiver;
    }

    public void setSender(IFluidStandardSenderMK2 sender) {
        this.sender = sender;
    }

    public IFluidStandardReceiverMK2 getReceiver() {
        return receiver;
    }

    public IFluidStandardSenderMK2 getSender() {
        return sender;
    }

    public FluidTank[] getReceivingTanks() {
        if (receiver != null) return receiver.getReceivingTanks();
        else return null;
    }

    public FluidTank[] getSendingTanks() {
        if (sender != null) return sender.getSendingTanks();
        else return null;
    }

    public FluidTank[] getAllTanks() {
        List<FluidTank> tanks = new LinkedList<FluidTank>();
        if (receiver != null) tanks.addAll(Arrays.asList(receiver.getAllTanks()));
        else if (sender != null) tanks.addAll(Arrays.asList(sender.getAllTanks()));;
        return tanks.toArray(new FluidTank[0]);
    }

    /**
     * Attempts to locate the {@link IFluidStandardReceiverMK2} of a multiblock machine from any
     * of its tiles
     * @param world World where the machine is located
     * @param tile Position of a tile that makes up the multiblock
     * @return The {@link TileEntity} which implements {@link IFluidStandardReceiverMK2} if found, {@code null} if not found
     */
    public TileEntity findReceiver(World world, BlockPos tile) {
        TileEntity neighborTile = world.getTileEntity(tile.getX(), tile.getY(), tile.getZ());

        // Case 1: Neighbor is directly a fluid transceiver
        if (neighborTile instanceof IFluidStandardReceiverMK2) {
            return neighborTile;
        }

        // Case 2: Neighbor is part of a multiblock machine (BlockDummyable)
        Block neighborBlock = world.getBlock(tile.getX(), tile.getY(), tile.getZ());
        if (neighborBlock instanceof BlockDummyable) {
            BlockDummyable dummy = (BlockDummyable) neighborBlock;

            // Get the coordinates of the multiblock's core
            int[] corePos = dummy.findCore(world, tile.getX(), tile.getY(), tile.getZ());
            if (corePos == null || corePos.length < 3) return null;
            TileEntity coreTile = world.getTileEntity(corePos[0], corePos[1], corePos[2]);

            if (coreTile instanceof IFluidStandardReceiverMK2) {
                return coreTile;
            }
        }
        return null;
    }

    /**
     * Attempts to locate the {@link IFluidStandardSenderMK2} of a multiblock machine from any
     * of its tiles
     * @param world World where the machine is located
     * @param tile Position of a tile that makes up the multiblock
     * @return The {@link TileEntity} which implements {@link IFluidStandardSenderMK2} if found, {@code null} if not found
     */
    public TileEntity findSender(World world, BlockPos tile) {
        TileEntity neighborTile = world.getTileEntity(tile.getX(), tile.getY(), tile.getZ());

        // Case 1: Neighbor is directly a fluid transceiver
        if (neighborTile instanceof IFluidStandardSenderMK2) {
            return neighborTile;
        }

        // Case 2: Neighbor is part of a multiblock machine (BlockDummyable)
        Block neighborBlock = world.getBlock(tile.getX(), tile.getY(), tile.getZ());
        if (neighborBlock instanceof BlockDummyable) {
            BlockDummyable dummy = (BlockDummyable) neighborBlock;

            // Get the coordinates of the multiblock's core
            int[] corePos = dummy.findCore(world, tile.getX(), tile.getY(), tile.getZ());
            if (corePos == null || corePos.length < 3) return null;
            TileEntity coreTile = world.getTileEntity(corePos[0], corePos[1], corePos[2]);

            if (coreTile instanceof IFluidStandardSenderMK2) {
                return coreTile;
            }
        }
        return null;
    }
}

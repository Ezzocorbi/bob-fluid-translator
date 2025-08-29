package com.fluidtranslator.tileentity;

import api.hbm.fluidmk2.FluidNode;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.fluidtranslator.TankModes;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.lib.Library;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.uninos.UniNodespace;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.HashSet;

public class TileEntityHBMFluidTank extends TileEntityMachineBase implements IFluidStandardTransceiverMK2 {

    protected FluidNode node;
    protected FluidType lastType = Fluids.NONE;
    public FluidTank tank;
    public short mode = 0;

    public TileEntityHBMFluidTank() {
        super(1);
        tank = new FluidTank(Fluids.NONE, 2000);
    }

    /**
     *
     * @return Restituisce la tank interna se la tile entity è disposta ad
     * inviare i fluidi, altrimenti non reistuisce alcuna tank
     */
    @Override
    public FluidTank[] getSendingTanks() {
        if (mode == TankModes.BUFFER.ordinal || mode == TankModes.SENDER.ordinal)
            return new FluidTank[] {tank};
        else
            return new FluidTank[0]; // Non restituisce alcuna tank
    }

    /**
     *
     * @return Restituisce la tank interna se la tile entity è disposta ad
     * accettare i fluidi, altrimenti non reistuisce alcuna tank
     */
    @Override
    public FluidTank[] getReceivingTanks() {
        if (mode == TankModes.BUFFER.ordinal || mode == TankModes.RECEIVER.ordinal)
            return new FluidTank[] {tank};
        else
            return new FluidTank[0]; // Non restituisce alcuna tank
    }

    @Override
    public FluidTank[] getAllTanks() {
        return new FluidTank[] { tank };
    }

    /**
     * @return Volume ancora disponibile nella tank per questo fluido alla
     *         pressione richiesta. Ritorna 0 se la modalità non accetta input
     *         oppure se la pressione non coincide.
     */
    @Override
    public long getDemand(FluidType type, int pressure) {
        if(this.mode == TankModes.SENDER.ordinal || this.mode == TankModes.DISABLED.ordinal) return 0;
        if(tank.getPressure() != pressure) return 0;
        return type == tank.getTankType() ? tank.getMaxFill() - tank.getFill() : 0;
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public String getName() {
        return "hbmFluidTankTest";
    }

    /**
     * Riempie la tank interna con il fluido
     * @param type Fluido che entra nella tank
     * @param pressure Pressione del fluido
     * @param amount Volume del fluido da immettere nella tank
     * @return Volume di fluido rimasto che non può entrare
     */
    public long transferFluid(FluidType type, int pressure, long amount) {
        long toTransfer = Math.min(getDemand(type, pressure), amount);
        tank.setFill(tank.getFill() + (int) toTransfer);
        return amount - toTransfer;
    }

    /**
     * Calcola il volume di un fluido presente nella tank
     * @param type Fluido richiesto
     * @param pressure Pressione del fluido
     * @return Volume del fluido presente nella tank
     */
    public long getFluidAvailable(FluidType type, int pressure) {
        long amount = 0;
        for(FluidTank tank : getSendingTanks()) {
            if(tank.getTankType() == type && tank.getPressure() == pressure) amount += tank.getFill();
        }
        return amount;
    }

    /**
     * Rimuove dalla tank un certo volume di fluido
     * @param type Fluido da rimuovere
     * @param pressure Pressione del fluido
     * @param amount Volume da rimuovere
     */
    public void useUpFluid(FluidType type, int pressure, long amount) {
        int tanks = 0;
        for(FluidTank tank : getSendingTanks()) {
            if(tank.getTankType() == type && tank.getPressure() == pressure) tanks++;
        }
        if(tanks > 1) {
            int firstRound = (int) Math.floor((double) amount / (double) tanks);
            for(FluidTank tank : getSendingTanks()) {
                if(tank.getTankType() == type && tank.getPressure() == pressure) {
                    int toRem = Math.min(firstRound, tank.getFill());
                    tank.setFill(tank.getFill() - toRem);
                    amount -= toRem;
                }
            }
        }
        if(amount > 0) for(FluidTank tank : getSendingTanks()) {
            if(tank.getTankType() == type && tank.getPressure() == pressure) {
                int toRem = (int) Math.min(amount, tank.getFill());
                tank.setFill(tank.getFill() - toRem);
                amount -= toRem;
            }
        }
    }

    /**
     * Aggiornamento della tile entity lato server:
     * - In modalità BUFFER crea/aggiorna un nodo proprio e si registra sia come provider che receiver,
     *   agendo come ponte all'interno del network dei fluidi.
     * - In altre modalità interagisce coi nodi vicini:
     *   - SENDER invia fluido ai nodi connessi
     *   - RECEIVER si registra come ricevitore dai nodi connessi
     *   - altre modalità rimuovono i collegamenti esistenti
     */
    @Override
    public void updateEntity() {
        if(mode == TankModes.DISABLED.ordinal && this.node != null) {
            UniNodespace.destroyNode(worldObj, xCoord, yCoord, zCoord, tank.getTankType().getNetworkProvider());
            this.node = null;
            return;
        }

        if(!worldObj.isRemote) {
            // In buffer mode, acts like a pipe block, providing fluid to its own node
            // otherwise, it is a regular providing/receiving machine, blocking further propagation
            if(mode == TankModes.BUFFER.ordinal) {
                if(this.node == null || this.node.expired || tank.getTankType() != lastType) { // Se il nodo non è disponbile, va ricavato dal network

                    this.node = (FluidNode) UniNodespace.getNode(worldObj, xCoord, yCoord, zCoord, tank.getTankType().getNetworkProvider());

                    if(this.node == null || this.node.expired || tank.getTankType() != lastType) { // Se il nodo ancora non è disponibile, lo rigeneriamo
                        this.node = this.createNode(tank.getTankType());
                        UniNodespace.createNode(worldObj, this.node);
                        lastType = tank.getTankType();
                    }
                }

                if(node != null && node.hasValidNet()) { // Se il nodo è finalmente disponibile, lo marchiamo come provider e receiver
                    node.net.addProvider(this);
                    node.net.addReceiver(this);
                }
            } else { // Se non siamo in modalità buffer, eliminiamo il nodo corrente
                if(this.node != null) {
                    UniNodespace.destroyNode(worldObj, xCoord, yCoord, zCoord, tank.getTankType().getNetworkProvider());
                    this.node = null;
                }

                for(DirPos pos : getConPos()) { // Per ogni connessione, generiamo un nodo appropriato
                    FluidNode dirNode = (FluidNode) UniNodespace.getNode(worldObj, pos.getX(), pos.getY(), pos.getZ(), tank.getTankType().getNetworkProvider());

                    if(mode == TankModes.SENDER.ordinal) { // Se siamo in modalità sender, inviamo del fluido al nodo target
                        tryProvide(tank, worldObj, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
                    } else { // Altrimenti, eliminiamo un eventuale nodo a cui invieremmo il fluido
                        if(dirNode != null && dirNode.hasValidNet()) dirNode.net.removeProvider(this);
                    }

                    if(mode == TankModes.RECEIVER.ordinal) { // Se siamo in modalità receiver, marchiamo il nodo target come punto da cui ricevere del fluido
                        if(dirNode != null && dirNode.hasValidNet()) dirNode.net.addReceiver(this);
                    } else { // Altrimenti, eliminiamo un eventuale nodo da cui riceveremmo del fluido
                        if(dirNode != null && dirNode.hasValidNet()) dirNode.net.removeReceiver(this);
                    }
                }
            }
            this.networkPackNT(50);
        }
    }

    /**
     * Restituisce una collezione di posizioni in cui la tank può essere connessa al network dei fluidi
     * Questo è il caso di un blocco a sei facce
     * @return Array di posizione, dove ogni posizione è una possibile connessione al network di fluidi
     */
    protected DirPos[] getConPos() {
        return new DirPos[] {
                new DirPos(xCoord + 1, yCoord, zCoord, Library.POS_X),
                new DirPos(xCoord - 1, yCoord, zCoord, Library.NEG_X),
                new DirPos(xCoord, yCoord + 1, zCoord, Library.POS_Y),
                new DirPos(xCoord, yCoord - 1, zCoord, Library.NEG_Y),
                new DirPos(xCoord, yCoord, zCoord + 1, Library.POS_Z),
                new DirPos(xCoord, yCoord, zCoord - 1, Library.NEG_Z)
        };
    }

    /**
     * Genera un nodo del network di fluidi che descrive la posizione di ogni connettore di tile entity
     * @param type Fluido del nodo
     * @return Un nodo nuovo per il network di fluidi
     */
    protected FluidNode createNode(FluidType type) {
        DirPos[] conPos = getConPos();

        HashSet<BlockPos> posSet = new HashSet<BlockPos>();
        posSet.add(new BlockPos(this));
        for(DirPos pos : conPos) {
            ForgeDirection dir = pos.getDir();
            posSet.add(new BlockPos(pos.getX() - dir.offsetX, pos.getY() - dir.offsetY, pos.getZ() - dir.offsetZ));
        }

        return new FluidNode(type.getNetworkProvider(), posSet.toArray(new BlockPos[posSet.size()])).setConnections(conPos);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        NBTTagCompound tankTag = new NBTTagCompound();
        tank.writeToNBT(tankTag, "tank");
        tag.setTag("Tank", tankTag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("Tank")) {
            tank.readFromNBT(tag.getCompoundTag("Tank"), "tank");
        }
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        this.writeToNBT(nbtTag);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.func_148857_g());
    }
}

package com.fluidtranslator.blocks;

import com.fluidtranslator.FluidTranslator;
import com.fluidtranslator.TankModes;
import com.fluidtranslator.container.GuiIds;
import com.fluidtranslator.tileentity.TileEntityUniversalTank;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.items.machine.IItemFluidIdentifier;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class BlockUniversalTank extends BlockContainer {

    public BlockUniversalTank() {
        super(Material.rock);
        setBlockName("universalFluidTank");
        setBlockTextureName("minecraft:glass");
        setHardness(4.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int capacity) {
        return new TileEntityUniversalTank();
    }

    @Override
    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
        super.onBlockClicked(world, x, y, z, player);
        if(world.isRemote) { // Don't execute this logic on the server, the client is enough
            return;
        }
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityUniversalTank) {
            TileEntityUniversalTank teTank = (TileEntityUniversalTank) te;
            teTank.setTankMode((short)((teTank.getTankMode() + 1) % 4));
            String modeS = TankModes.byOrdinal(teTank.getTankMode()).toString().toLowerCase();
            player.addChatMessage(new ChatComponentText("Set tank mode to " + modeS));
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
                                    EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if(!world.isRemote) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileEntityUniversalTank) {
                TileEntityUniversalTank teTank = (TileEntityUniversalTank) te;
                FluidTank tank = teTank.getAllTanks()[0];
                ItemStack heldItem = player.getHeldItem();
                if (heldItem != null && heldItem.getItem() instanceof IItemFluidIdentifier) { // Set fluid ID if players right clicks with fluid ID
                    FluidType type = ((IItemFluidIdentifier) player.getHeldItem().getItem()).getType(world, x, y, z, player.getHeldItem());
                    tank.setTankType(type);
                    player.addChatMessage(new ChatComponentText("Set fluid type to " + type.getName()));
                } else {
                    player.openGui(FluidTranslator.instance, GuiIds.UNIVERSAL_TANK.ordinal, world, x, y, z);
                }
//                player.addChatMessage(new ChatComponentText("Fill: " + tank.getFill() + ", fluid: " + tank.getTankType().getName())); // Debug message
            }
        }
        return true;
    }
}

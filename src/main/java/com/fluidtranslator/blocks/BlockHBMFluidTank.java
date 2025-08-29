package com.fluidtranslator.blocks;

import com.fluidtranslator.TankModes;
import com.fluidtranslator.tileentity.TileEntityHBMFluidTank;
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

public class BlockHBMFluidTank extends BlockContainer {

    public BlockHBMFluidTank() {
        super(Material.rock);
        setBlockName("simpleHBMTank");
        setBlockTextureName("minecraft:glass");
        setHardness(4.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int capacity) {
        return new TileEntityHBMFluidTank();
    }

    @Override
    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
        super.onBlockClicked(world, x, y, z, player);
        if(world.isRemote) { // Don't execute this logic on the server, the client is enough
            return;
        }
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityHBMFluidTank) {
            TileEntityHBMFluidTank teTank = (TileEntityHBMFluidTank) te;
            teTank.mode = (short)((teTank.mode + 1) % 4);
            String modeS = TankModes.byOrdinal(teTank.mode).toString().toLowerCase();
            player.addChatMessage(new ChatComponentText("Set tank mode to " + modeS));
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
                                    EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if(world.isRemote) { // Don't execute this logic on the server, the client is enough
            return false;
        }
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityHBMFluidTank) {
            TileEntityHBMFluidTank teTank = (TileEntityHBMFluidTank) te;
            FluidTank tank = teTank.getAllTanks()[0];
            ItemStack heldItem = player.getHeldItem();
            if (heldItem != null && heldItem.getItem() instanceof IItemFluidIdentifier) { // Set fluid ID if players right clicks with fluid ID
                FluidType type = ((IItemFluidIdentifier) player.getHeldItem().getItem()).getType(world, x, y, z, player.getHeldItem());
                tank.setTankType(type);
                player.addChatMessage(new ChatComponentText("Set fluid type to " + type.getName()));
            } else {
                FluidType fluidType = tank.getTankType();
                int fill = tank.getFill();
                player.addChatMessage(new ChatComponentText(fluidType.getName() + " | " + fill));
            }
        }
        return true;
    }
}

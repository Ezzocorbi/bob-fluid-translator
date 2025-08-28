package com.fluidtranslator.blocks;

import com.fluidtranslator.tileentity.TileEntityHBMFluidTank;
import com.fluidtranslator.tileentity.TileEntityHBMWrapper;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.items.ModItems;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.ItemFluidIDMulti;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

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
        TileEntityHBMFluidTank teTank = (TileEntityHBMFluidTank) te;
        String modeS = "invalid";
        switch(teTank.mode) {
            case 0:
                modeS = "receiver";
                break;
            case 1:
                modeS = "buffer";
                break;
            case 2:
                modeS = "sender";
                break;
            case 3:
                modeS = "disabled";
                break;
        }
        teTank.mode = (short)((teTank.mode + 1) % 4);
//        teTank.markDirtyAndUpdate();
        player.addChatMessage(new ChatComponentText("Set tank mode to " + modeS));
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

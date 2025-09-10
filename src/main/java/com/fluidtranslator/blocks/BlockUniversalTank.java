package com.fluidtranslator.blocks;

import com.fluidtranslator.FluidTranslator;
import com.fluidtranslator.container.GuiIds;
import com.fluidtranslator.tileentity.TileEntityUniversalTank;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
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

//    @Override
//    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
//        super.onBlockClicked(world, x, y, z, player);
//        if(world.isRemote) { // Don't execute this logic on the server, the client is enough
//            return;
//        }
//        TileEntity te = world.getTileEntity(x, y, z);
//        if (te instanceof TileEntityUniversalTank) {
//            TileEntityUniversalTank teTank = (TileEntityUniversalTank) te;
//            teTank.setTankMode((short)((teTank.getTankMode() + 1) % 4));
//            String modeS = TankModes.byOrdinal(teTank.getTankMode()).toString().toLowerCase();
//            player.addChatMessage(new ChatComponentText("Set tank mode to " + modeS));
//        }
//    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
                                    EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if(!world.isRemote) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileEntityUniversalTank) {
                player.openGui(FluidTranslator.instance, GuiIds.UNIVERSAL_TANK.ordinal, world, x, y, z);
            }
        }
        return true;
    }
}

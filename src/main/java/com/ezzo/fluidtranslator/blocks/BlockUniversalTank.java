package com.ezzo.fluidtranslator.blocks;

import com.ezzo.fluidtranslator.FluidTranslator;
import com.ezzo.fluidtranslator.container.GuiIds;
import com.ezzo.fluidtranslator.tileentity.TileEntityUniversalTank;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockUniversalTank extends BlockContainer {

    @SideOnly(Side.CLIENT)
    private IIcon topIcon;
    @SideOnly(Side.CLIENT)
    private IIcon sideIcon;
    @SideOnly(Side.CLIENT)
    private IIcon bottomIcon;

    private final int capacity;

    public BlockUniversalTank(int capacity) {
        super(Material.rock);
        setBlockName("universalFluidTank");
        setBlockTextureName("minecraft:glass");
        setHardness(4.0F);
        this.capacity = capacity;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityUniversalTank(this.capacity);
    }

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

    public int getCapacity() {
        return capacity;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister reg) {
        topIcon = reg.registerIcon(FluidTranslator.MODID + ":fluid_tank_top");
        sideIcon = reg.registerIcon(FluidTranslator.MODID + ":fluid_tank_side");
        bottomIcon = reg.registerIcon(FluidTranslator.MODID + ":fluid_tank_bottom");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta) {
        if (side == 1) return topIcon;
        if (side == 0) return bottomIcon;
        return sideIcon;
    }
}

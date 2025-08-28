package com.fluidtranslator.blocks;

import com.fluidtranslator.tileentity.TileEntityHBMFluidTank;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.IPersistentInfoProvider;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.List;

public class BlockHBMFluidTank extends BlockDummyable implements IPersistentInfoProvider {

    public BlockHBMFluidTank() {
        super(Material.iron);
    }

    @Override
    public int[] getDimensions() {
        return new int[] {1, 0, 0, 0, 0, 0};
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
        return new TileEntityHBMFluidTank();
    }

    @Override
    public void addInformation(ItemStack stack, NBTTagCompound persistentTag, EntityPlayer player, List list, boolean ext) {
        FluidTank tank = new FluidTank(Fluids.NONE, 0);
        tank.readFromNBT(persistentTag, "tank");
        list.add(EnumChatFormatting.YELLOW + "" + tank.getFill() + "/" + tank.getMaxFill() + "mB " + tank.getTankType().getLocalizedName());
    }
}

package com.ezzo.fluidtranslator.item;

import com.ezzo.fluidtranslator.blocks.CustomFluidBlock;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

/**
 * This class is used to match the localization of HBM's fluids
 */
public class CustomFluidItemBlock extends ItemBlock {

    public CustomFluidItemBlock(Block block) {
        super(block);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        if (this.field_150939_a instanceof CustomFluidBlock) {
            return ((CustomFluidBlock) field_150939_a).getLocalizedName();
        } else {
            return super.getItemStackDisplayName(stack);
        }
    }
}

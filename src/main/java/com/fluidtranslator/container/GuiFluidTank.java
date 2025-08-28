package com.fluidtranslator.container;

import com.fluidtranslator.tileentity.TileEntitySimpleFluidTank;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

public class GuiFluidTank extends GuiContainer {
    private TileEntitySimpleFluidTank tank;

    public GuiFluidTank(InventoryPlayer playerInv, TileEntitySimpleFluidTank tank) {
        super(new ContainerFluidTank(playerInv, tank));
        this.tank = tank;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRendererObj.drawString("Fluid Tank", 8, 6, 4210752);
        FluidStack fluidStack = tank.getTankInfo(ForgeDirection.UP)[0].fluid;
        if (fluidStack != null) {
            fontRendererObj.drawString(
                    fluidStack.getFluid().getLocalizedName() + ": " +
                            fluidStack.amount + " mB",
                    8, 20, 4210752
            );
        } else {
            fontRendererObj.drawString("Empty", 8, 20, 4210752);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        mc.getTextureManager().bindTexture(new ResourceLocation("bobfluidtranslator:textures/gui/fluid_tank.png"));
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}

package com.fluidtranslator.container.universaltank;

import com.fluidtranslator.CustomFluidRegistry;
import com.fluidtranslator.FluidTranslator;
import com.fluidtranslator.tileentity.TileEntityUniversalTank;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.gui.GuiInfoContainer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

public class GuiFluidTank extends GuiInfoContainer {
    private static final ResourceLocation texture = new ResourceLocation(FluidTranslator.MODID + ":textures/gui/fluid_tank.png");
    private final TileEntityUniversalTank tank;
    private final int xLeftPixel = 74; // left pixel where the tank starts
    private final int yTopPixel = 8; // top pixel where the tank starts
    private final int tankWidth = 28;
    private final int tankHeight = 70;

    public GuiFluidTank(InventoryPlayer playerInv, TileEntityUniversalTank tank) {
        super(new ContainerFluidTank(playerInv, tank));
        this.tank = tank;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        int color = 4210752;
        int x = 8;
        int y = 6;
        this.fontRendererObj.drawString("wait for: " + tank.operationDelay, x, y, color);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(texture); // bind GUI texture
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        FluidTankInfo tankInfo = tank.getTankInfo(ForgeDirection.UP)[0];
        FluidStack fluid = tankInfo.fluid;
        if (fluid != null && fluid.amount > 0) {
            // Setup and draw fluid in tank
            int xStart = xLeftPixel;
            int yStart = yTopPixel + tankHeight;
            int capacity = tankInfo.capacity;
            drawFluid(fluid,
                    guiLeft + xStart, guiTop + yStart,
                    tankWidth, tankHeight);

            // Setup and draw tank info
            String[] info = new String[] {
                    fluid.getLocalizedName(),
                    fluid.amount + "/" + capacity + "mB"
            };
            drawTankInfo(info, mouseX, mouseY);
        } else {
            drawTankInfo(new String[] {"Empty"}, mouseX, mouseY);
        }
    }

    @SideOnly(Side.CLIENT)
    private void drawTankInfo(String[] stringsToDraw, int mouseX, int mouseY) {
        int leftBound = guiLeft + xLeftPixel;
        int rightBound = guiLeft + xLeftPixel + tankWidth;
        int bottomBound = guiTop + yTopPixel;
        int topBound = guiTop + 8 + tankHeight;
        if (mouseX >= leftBound && mouseX <= rightBound &&
                mouseY >= bottomBound && mouseY <= topBound) {
            this.drawInfo(stringsToDraw, mouseX + 5, mouseY);
        }
    }

    @SideOnly(Side.CLIENT)
    private void drawFluid(FluidStack fluid, int x, int y, int width, int height) {
        FluidType fluidType = CustomFluidRegistry.getHBMFluid(fluid.getFluid());
        mc.getTextureManager().bindTexture(fluidType.getTexture());

        int capacity = tank.getTankInfo(ForgeDirection.UP)[0].capacity;
        int fill = (fluid.amount * height) / capacity;

        double minX = x;
        double maxX = x + width;
        double minY = y - fill; // y = minY corresponds to top, y = maxY corresponds to bottom
        double maxY = y;

        double minU = 0;
        double maxU = width / 16D;
        double minV = 1D - fill / 16D; // by incrementing minV, we draw the fluid starting from the bottom
        double maxV = 1;

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(minX, maxY, this.zLevel, minU, maxV);
        tessellator.addVertexWithUV(maxX, maxY, this.zLevel, maxU, maxV);
        tessellator.addVertexWithUV(maxX, minY, this.zLevel, maxU, minV);
        tessellator.addVertexWithUV(minX, minY, this.zLevel, minU, minV);
        tessellator.draw();
    }
}

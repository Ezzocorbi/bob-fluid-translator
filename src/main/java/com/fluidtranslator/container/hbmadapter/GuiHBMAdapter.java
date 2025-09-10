package com.fluidtranslator.container.hbmadapter;

import com.fluidtranslator.FluidTranslator;
import com.fluidtranslator.ModFluidRegistry;
import com.fluidtranslator.network.MessageSetTankIndex;
import com.fluidtranslator.network.ModNetwork;
import com.fluidtranslator.tileentity.TileEntityHBMAdapter;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.gui.GuiInfoContainer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

public class GuiHBMAdapter extends GuiInfoContainer {
    private static final ResourceLocation texture = new ResourceLocation(FluidTranslator.MODID + ":textures/gui/fluid_adapter.png");
    private static final ResourceLocation buttons = new ResourceLocation("minecraft:textures/gui/container/villager.png");
    private final TileEntityHBMAdapter tank;
    private final int xLeftPixel = 74; // left pixel where the tank starts
    private final int yTopPixel = 8; // top pixel where the tank starts
    private final int tankWidth = 28;
    private final int tankHeight = 70;

    public GuiHBMAdapter(InventoryPlayer playerInv, TileEntityHBMAdapter tank) {
        super(new ContainerHBMAdapter(playerInv, tank));
        this.tank = tank;
        this.xSize = 176;
        this.ySize = 166;
    }

    protected void mouseClicked(int x, int y, int i) {
        super.mouseClicked(x, y, i);

        // Left button click action
        if(x < guiLeft + 62 + 8 && x >= guiLeft + 62 && y <= guiTop + 37 + 13 && y > guiTop + 37) {
            mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
            int len = tank.getAllTanks().length;
            int newIndex = (tank.getTankIndex() - 1 + len) % len;
            tank.setTankIndex(newIndex);
            ModNetwork.INSTANCE.sendToServer(new MessageSetTankIndex(tank.xCoord, tank.yCoord, tank.zCoord, newIndex));
            return;
        }

        // Right button click action
        if(x < guiLeft + 105 + 8 && x >= guiLeft + 105 && y <= guiTop + 37 + 13 && y > guiTop + 37) {
            mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
            int len = tank.getAllTanks().length;
            int newIndex = (tank.getTankIndex() + 1) % len;
            tank.setTankIndex(newIndex);
            ModNetwork.INSTANCE.sendToServer(new MessageSetTankIndex(tank.xCoord, tank.yCoord, tank.zCoord, newIndex));
            return;
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {

        // Left button tooltip
        if(x < guiLeft + 62 + 8 && x >= guiLeft + 62 && y <= guiTop + 37 + 13 && y > guiTop + 37) {
            mc.getTextureManager().bindTexture(buttons);
            drawTexturedModalRect(62, 37, 190, 22, 8, 13);
            this.drawInfo(new String[] {"Cycle tanks"}, x + 2 - guiLeft, y + 1 - guiTop);
            return;
        }

        // Right button tooltip
        if(x < guiLeft + 105 + 8 && x >= guiLeft + 105 && y <= guiTop + 37 + 13 && y > guiTop + 37) {
            mc.getTextureManager().bindTexture(buttons);
            drawTexturedModalRect(105, 37, 190, 3, 8, 13);
            this.drawInfo(new String[] {"Cycle tanks"}, x + 2 - guiLeft, y + 1 - guiTop);
            return;
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        // Draw buttons to change selected tank
        mc.getTextureManager().bindTexture(buttons);
        drawTexturedModalRect(guiLeft + 62, guiTop + 37, 178, 22, 8, 13);
        drawTexturedModalRect(guiLeft + 105, guiTop + 37, 178, 3, 8, 13);

        try {
            FluidTankInfo[] tankInfoArr = tank.getTankInfo(ForgeDirection.UP);
            if (tankInfoArr == null) {
                drawTankInfo(new String[] {"No tank attached"}, mouseX, mouseY);
                return;
            }

            FluidTankInfo tankInfo = tankInfoArr[0];
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
        } catch (IllegalArgumentException e) {
            drawTankInfo(new String[] {"Error: unable to render fluid:"}, mouseX, mouseY);
            System.err.println(String.format("An error occurred while trying to render the fluid contained in the tank at %d %d %d", tank.xCoord, tank.yCoord, tank.zCoord));
            e.printStackTrace();
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
        FluidType fluidType = ModFluidRegistry.getHBMFluid(fluid.getFluid());
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

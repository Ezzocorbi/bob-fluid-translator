package com.ezzo.fluidtranslator.container.hbmadapter;

import com.ezzo.fluidtranslator.FluidTranslator;
import com.ezzo.fluidtranslator.ModFluidRegistry;
import com.ezzo.fluidtranslator.network.MessageResetTank;
import com.ezzo.fluidtranslator.network.MessageSetTankIndex;
import com.ezzo.fluidtranslator.network.ModNetwork;
import com.ezzo.fluidtranslator.tileentity.TileEntityHBMAdapter;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.gui.GuiInfoContainer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.List;

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
        if (x < guiLeft + 62 + 8 && x >= guiLeft + 62 && y <= guiTop + 37 + 13 && y > guiTop + 37) {
            if (!tank.isConnected()) return;
            mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
            int len = tank.getAllTanks().length;
            int newIndex = (tank.getTankIndex() - 1 + len) % len;
            tank.setTankIndex(newIndex);
            ModNetwork.INSTANCE.sendToServer(new MessageSetTankIndex(tank.xCoord, tank.yCoord, tank.zCoord, newIndex));
            return;
        }

        // Right button click action
        if (x < guiLeft + 105 + 8 && x >= guiLeft + 105 && y <= guiTop + 37 + 13 && y > guiTop + 37) {
            if (!tank.isConnected()) return;
            mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
            int len = tank.getAllTanks().length;
            int newIndex = (tank.getTankIndex() + 1) % len;
            tank.setTankIndex(newIndex);
            ModNetwork.INSTANCE.sendToServer(new MessageSetTankIndex(tank.xCoord, tank.yCoord, tank.zCoord, newIndex));
            return;
        }

        if (x < guiLeft + 154 + 16 && x >= guiLeft + 154 && y <= guiTop + 35 + 16 && y > guiTop + 35) {
            mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
            boolean resetTank = !tank.doesResetFluidType();
            ModNetwork.INSTANCE.sendToServer(new MessageResetTank(tank.xCoord, tank.yCoord, tank.zCoord, resetTank));
            return;
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {

    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        // Draw buttons to change selected tank
        mc.getTextureManager().bindTexture(buttons);
        drawTexturedModalRect(guiLeft + 62, guiTop + 37, 178, 22, 8, 13);
        drawTexturedModalRect(guiLeft + 105, guiTop + 37, 178, 3, 8, 13);

        drawBucketButton(mouseX, mouseY);

        try {
            if (!tank.isConnected()) {
                drawTankInfo(new String[] {"No tank attached"}, mouseX, mouseY);
                return;
            }

            FluidTankInfo tankInfo = tank.getTankInfo(ForgeDirection.UP)[0];
            FluidStack fluidStack = tankInfo.fluid;
            if (fluidStack != null && fluidStack.amount > 0) {
                // Setup and draw fluid in tank
                int xStart = xLeftPixel;
                int yStart = yTopPixel + tankHeight;
                drawFluid(fluidStack,
                        guiLeft + xStart, guiTop + yStart,
                        tankWidth, tankHeight);
            }

            String[] info;
            FluidTank hbmTank = tank.getInternalTank();
            if (hbmTank == null) {
                info = new String[] {"No tank attached"};
            } else {
                info = new String[] {
                        hbmTank.getTankType().getLocalizedName(),
                        hbmTank.getFill() + "/" + hbmTank.getMaxFill() + "mB"
                };
            }
            drawTankInfo(info, mouseX, mouseY);
        } catch (IllegalArgumentException e) {
            drawTankInfo(new String[] {"Error: unable to display fluid"}, mouseX, mouseY);
            System.err.println(String.format("An error occurred while trying to render the fluid in the adapter at %d %d %d", tank.xCoord, tank.yCoord, tank.zCoord));
            e.printStackTrace();
        }

        drawArrowButtonHighlights(mouseX, mouseY);
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


    /**
     * Renders the blue highlights on the left/right arrow buttons when hovered with the mouse pointer
     * @param x mouseX
     * @param y mouseY
     */
    @SideOnly(Side.CLIENT)
    private void drawArrowButtonHighlights(int x, int y) {
        // Left button tooltip
        if (x < guiLeft + 62 + 8 && x >= guiLeft + 62 && y <= guiTop + 37 + 13 && y > guiTop + 37) {
            mc.getTextureManager().bindTexture(buttons);
            drawTexturedModalRect(guiLeft + 62, guiTop + 37, 190, 22, 8, 13);
            this.drawInfo(new String[] {"Cycle tanks"}, x + 2, y + 1);
            return;
        }

        // Right button tooltip
        if (x < guiLeft + 105 + 8 && x >= guiLeft + 105 && y <= guiTop + 37 + 13 && y > guiTop + 37) {
            mc.getTextureManager().bindTexture(buttons);
            drawTexturedModalRect(guiLeft + 105, guiTop + 37, 190, 3, 8, 13);
            this.drawInfo(new String[] {"Cycle tanks"}, x + 2, y + 1);
            return;
        }
    }

    /**
     * Renders the bucket button on the right of the GUI
     * @param x mouseX
     * @param y mouseY
     */
    @SideOnly(Side.CLIENT)
    private void drawBucketButton(int x, int y) {
        // Reset fluid-type button tooltip
        if (x < guiLeft + 150 + 16 && x >= guiLeft + 150 && y <= guiTop + 35 + 16 && y > guiTop + 35) {
            String[] info;
            String formatting = EnumChatFormatting.GRAY + "" + EnumChatFormatting.ITALIC;
            if (tank.doesResetFluidType()) {
                info = new String[] {"Reset tank's fluid ID on empty",
                        formatting + "Tank will forget its fluid ID when emptied",
                        formatting + "Useful for automation through this adapter"};
            } else {
                info = new String[] {"Keep tank's fluid ID on empty",
                        formatting + "Tank will keep its fluid ID when emptied",
                        formatting + "This adapter won't insert a different fluid"};
            }

            float scale = 0.75f;
            int xPos = (int)((guiLeft + 110) / scale);
            int yPos = (int)((guiTop + 56) / scale);
            GL11.glPushMatrix();
            GL11.glScalef(scale, scale, 1.0f);
            this.drawFixedTooltip(Arrays.asList(info), xPos, yPos);
            GL11.glPopMatrix();
        }

        // Reset fluid-type button icon
        if (tank.doesResetFluidType()) {
            drawItem(Items.bucket, guiLeft + 150, guiTop + 35);
        } else {
            drawItem(Items.water_bucket, guiLeft + 150, guiTop + 35);
        }
    }

    @SideOnly(Side.CLIENT)
    private void drawItem(Item item, int x, int y) {
        mc.getTextureManager().bindTexture(TextureMap.locationItemsTexture);
        IIcon icon = item.getIconFromDamage(0);

        float minU = icon.getMinU();
        float maxU = icon.getMaxU();
        float minV = icon.getMinV();
        float maxV = icon.getMaxV();

        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.addVertexWithUV(x, y + 16, zLevel, minU, maxV);
        t.addVertexWithUV(x + 16, y + 16, zLevel, maxU, maxV);
        t.addVertexWithUV(x + 16,y, zLevel, maxU, minV);
        t.addVertexWithUV(x, y, zLevel, minU, minV);
        t.draw();
    }

    @SideOnly(Side.CLIENT)
    private void drawFixedTooltip(List<String> lines, int x, int y) {
        if (lines == null || lines.isEmpty()) return;

        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        int maxWidth = 0;
        for (String line : lines) {
            int width = this.fontRendererObj.getStringWidth(line);
            if (width > maxWidth) maxWidth = width;
        }

        int tooltipX = x;
        int tooltipY = y;
        int tooltipHeight = 8;

        if (lines.size() > 1) {
            tooltipHeight += 2 + (lines.size() - 1) * 10;
        }

        int backgroundColor = 0xF0100010; // black
        int borderColor1   = 0x505000FF; // purple border
        int borderColor2   = 0x5028007F;

        this.zLevel = 300.0F;
        itemRender.zLevel = 300.0F;

        // Background rectangle
        drawGradientRect(tooltipX - 3, tooltipY - 4,
                tooltipX + maxWidth + 3, tooltipY - 3,
                backgroundColor, backgroundColor);
        drawGradientRect(tooltipX - 3, tooltipY + tooltipHeight + 3,
                tooltipX + maxWidth + 3, tooltipY + tooltipHeight + 4,
                backgroundColor, backgroundColor);
        drawGradientRect(tooltipX - 3, tooltipY - 3,
                tooltipX + maxWidth + 3, tooltipY + tooltipHeight + 3,
                backgroundColor, backgroundColor);
        drawGradientRect(tooltipX - 4, tooltipY - 3,
                tooltipX - 3, tooltipY + tooltipHeight + 3,
                backgroundColor, backgroundColor);
        drawGradientRect(tooltipX + maxWidth + 3, tooltipY - 3,
                tooltipX + maxWidth + 4, tooltipY + tooltipHeight + 3,
                backgroundColor, backgroundColor);

        // Smooth border
        drawGradientRect(tooltipX - 3, tooltipY - 2,
                tooltipX - 2, tooltipY + tooltipHeight + 2,
                borderColor1, borderColor2);
        drawGradientRect(tooltipX + maxWidth + 2, tooltipY - 2,
                tooltipX + maxWidth + 3, tooltipY + tooltipHeight + 2,
                borderColor1, borderColor2);
        drawGradientRect(tooltipX - 3, tooltipY - 3,
                tooltipX + maxWidth + 3, tooltipY - 2,
                borderColor1, borderColor1);
        drawGradientRect(tooltipX - 3, tooltipY + tooltipHeight + 2,
                tooltipX + maxWidth + 3, tooltipY + tooltipHeight + 3,
                borderColor2, borderColor2);

        // Text
        int textY = tooltipY;
        for (int i = 0; i < lines.size(); ++i) {
            String line = lines.get(i);
            this.fontRendererObj.drawStringWithShadow(line, tooltipX, textY, 0xFFFFFF);
            if (i == 0) {
                textY += 12;
            } else {
                textY += 10;
            }
        }

        this.zLevel = 0.0F;
        itemRender.zLevel = 0.0F;

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        RenderHelper.enableStandardItemLighting();
    }
}

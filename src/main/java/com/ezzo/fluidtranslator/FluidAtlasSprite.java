package com.ezzo.fluidtranslator;

import com.hbm.inventory.fluid.FluidType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

public class FluidAtlasSprite extends TextureAtlasSprite {

    private final FluidType fluid;

    protected FluidAtlasSprite(String spriteName, FluidType fluid) {
        super(spriteName);
        this.fluid = fluid;
    }

    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        return true;
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation location) {
        try {
            framesTextureData.clear();
            this.frameCounter = 0;
            this.tickCounter = 0;

            ResourceLocation loc = new ResourceLocation(getTextureForFluid(this.fluid)); // correct
            BufferedImage texImg = ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(loc).getInputStream());
            int[] buffer = new int[texImg.getHeight() * texImg.getWidth()];

            int size = (int)(1 + Math.log10(texImg.getWidth()) / Math.log10(2)); // this equals to 1 + log base 2 of texImg.getWidth()
            int[][] mipmaps = new int[size][];
            texImg.getRGB(0, 0, texImg.getWidth(), texImg.getHeight(), buffer, 0, texImg.getWidth());
            Arrays.fill(mipmaps, buffer);

            this.setIconHeight(texImg.getHeight());
            this.setIconWidth(texImg.getWidth());
            this.framesTextureData.add(mipmaps);
            return false;
        } catch (IOException e) {
            String errorMsg = "Fatal error: Unable to load texture " + location.getResourceDomain() + location.getResourcePath();
            System.err.println(errorMsg);
            Minecraft.getMinecraft().crashed(new CrashReport(errorMsg, e));
            return false;
        }
    }

    private String getTextureForFluid(FluidType fluid) {
        return "hbm:textures/gui/fluids/" + fluid.getName().toLowerCase() + ".png";
    }
}

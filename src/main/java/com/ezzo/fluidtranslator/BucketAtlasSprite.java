package com.ezzo.fluidtranslator;

import com.hbm.inventory.fluid.FluidType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class BucketAtlasSprite extends TextureAtlasSprite {

    private final FluidType fluid;

    protected BucketAtlasSprite(String spriteName, FluidType fluid) {
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

            ResourceLocation bucketLoc = new ResourceLocation(FluidTranslator.MODID + ":textures/items/generic_bucket.png");
            ResourceLocation fluidTexture = fluid.getTexture();

            BufferedImage bucketBuf = ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(bucketLoc).getInputStream());
            BufferedImage fluidBuf = ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(fluidTexture).getInputStream());

            int[] rawBucket = new int[bucketBuf.getWidth()*bucketBuf.getHeight()];
            int[] rawFluid = new int[fluidBuf.getWidth()*fluidBuf.getHeight()];

            bucketBuf.getRGB(0, 0, bucketBuf.getWidth(), bucketBuf.getHeight(), rawBucket, 0, bucketBuf.getWidth());
            fluidBuf.getRGB(0, 0, fluidBuf.getWidth(), fluidBuf.getHeight(), rawFluid, 0, fluidBuf.getWidth());

            applyColorToBucket(rawBucket, fluid.getColor());

            int size = (int)(1 + Math.log10(bucketBuf.getWidth()) / Math.log10(2));
            int[][] mipmaps = new int[size][];
            for (int i = 0; i < size; i++) {
                mipmaps[i] = rawBucket;
            }

            this.setIconHeight(bucketBuf.getHeight());
            this.setIconWidth(bucketBuf.getWidth());
            this.framesTextureData.add(mipmaps);
            return false;
        } catch (IOException e) {
            String errorMsg = "Error: Unable to load texture " + location.getResourceDomain() + ":" + location.getResourcePath();
            FluidTranslator.logger.error(errorMsg);
            return false;
        }
    }

    /**
     * Apply the supplied {@code color} to the pixels that represent the fluid.
     * The magic numbers represent the x,y coords of those pixels.
     *  @param bucketImg
     * @param color
     */
    private void applyColorToBucket(int[] bucketImg, int color) {

        for (int i = 53; i < 58 + 1; i++) {
            bucketImg[i] = multiplyRGB(bucketImg[i], color);
        }

        for (int i = 67; i < 76 + 1; i++) {
            bucketImg[i] = multiplyRGB(bucketImg[i], color);
        }

        for (int i = 84; i < 91 + 1; i++) {
            bucketImg[i] = multiplyRGB(bucketImg[i], color);
        }

        for (int i = 102; i < 105 + 1; i++) {
            bucketImg[i] = multiplyRGB(bucketImg[i], color);
        }

        bucketImg[122] = multiplyRGB(bucketImg[122], color);
        bucketImg[123] = multiplyRGB(bucketImg[123], color);
        bucketImg[134] = multiplyRGB(bucketImg[134], color);
        bucketImg[138] = multiplyRGB(bucketImg[138], color);
        bucketImg[154] = multiplyRGB(bucketImg[154], color);
        bucketImg[186] = multiplyRGB(bucketImg[186], color);
    }

    private int multiplyRGB(int rgb, int tint) {
        int tr = (tint >> 16) & 0xFF;
        int tg = (tint >> 8) & 0xFF;
        int tb = tint & 0xFF;

        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        int a = (rgb >> 24) & 0xFF;

        r = r * tr / 255;
        g = g * tg / 255;
        b = b * tb / 255;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}

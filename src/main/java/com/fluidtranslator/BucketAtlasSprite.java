package com.fluidtranslator;

import com.hbm.inventory.fluid.FluidType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

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
            ResourceLocation fluidTexture = new ResourceLocation(getTextureForFluid(this.fluid));

            BufferedImage bucketBuf = ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(bucketLoc).getInputStream());
            BufferedImage fluidBuf = ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(fluidTexture).getInputStream());

//            File imgfile = new File("~/Desktop/image.png");
//            ImageIO.write(bucketBuf, "png", imgfile);
//            assert imgfile.length() > 0;

            int[] rawBucket = new int[bucketBuf.getWidth()*bucketBuf.getHeight()];
            int[] rawFluid = new int[fluidBuf.getWidth()*fluidBuf.getHeight()];

            bucketBuf.getRGB(0, 0, bucketBuf.getWidth(), bucketBuf.getHeight(), rawBucket, 0, bucketBuf.getWidth());
            fluidBuf.getRGB(0, 0, fluidBuf.getWidth(), fluidBuf.getHeight(), rawFluid, 0, fluidBuf.getWidth());

            int fluidTint = getAverageRGBColor(rawFluid);

            // Take specific pixels in the bucket's texture and tint them to resemble the fluid
            for (int i = 53; i < 58 + 1; i++) {
                rawBucket[i] = multiplyRGB(rawBucket[i], fluidTint);
            }

            for (int i = 67; i < 76 + 1; i++) {
                rawBucket[i] = multiplyRGB(rawBucket[i], fluidTint);
            }

            for (int i = 84; i < 91 + 1; i++) {
                rawBucket[i] = multiplyRGB(rawBucket[i], fluidTint);
            }

            for (int i = 102; i < 105 + 1; i++) {
                rawBucket[i] = multiplyRGB(rawBucket[i], fluidTint);
            }

            rawBucket[122] = multiplyRGB(rawBucket[122], fluidTint);
            rawBucket[123] = multiplyRGB(rawBucket[123], fluidTint);
            rawBucket[134] = multiplyRGB(rawBucket[134], fluidTint);
            rawBucket[138] = multiplyRGB(rawBucket[138], fluidTint);
            rawBucket[154] = multiplyRGB(rawBucket[154], fluidTint);
            rawBucket[186] = multiplyRGB(rawBucket[186], fluidTint);

            int size = (int)(1 + Math.log10(bucketBuf.getWidth()) / Math.log10(2));
            int[][] mipmaps = new int[size][];
            Arrays.fill(mipmaps, rawBucket);

            this.setIconHeight(bucketBuf.getHeight());
            this.setIconWidth(bucketBuf.getWidth());
            this.framesTextureData.add(mipmaps);
            return false;
        } catch (IOException e) {
            String errorMsg = "Fatal error: Unable to load texture " + location.getResourceDomain() + ":" + location.getResourcePath();
            System.err.println(errorMsg);
            Minecraft.getMinecraft().crashed(new CrashReport(errorMsg, e));
            return false;
        }
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

    private int getAverageRGBColor(int[] image) {
        long sumR = 0, sumG = 0, sumB = 0;
        int count = image.length;

        for (int rgb : image) {
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            sumR += r;
            sumG += g;
            sumB += b;
        }

        int avgR = (int)(sumR / count);
        int avgG = (int)(sumG / count);
        int avgB = (int)(sumB / count);

        return (avgR << 16) | (avgG << 8) | avgB;
    }

    private String getTextureForFluid(FluidType fluid) {
        return "hbm:textures/gui/fluids/" + fluid.getName().toLowerCase() + ".png";
    }
}

package com.ezzo.fluidtranslator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class MissingnoBucketAtlasSprite extends TextureAtlasSprite {
    protected MissingnoBucketAtlasSprite(String name) {
        super(name);
    }

    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        return true;
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation location) {
        try {
            ResourceLocation loc = new ResourceLocation(FluidTranslator.MODID + ":textures/items/missingno_bucket.png");
            BufferedImage buffer = ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(loc).getInputStream());

            int[] rawTexture = new int[buffer.getWidth()*buffer.getHeight()];
            buffer.getRGB(0, 0, buffer.getWidth(), buffer.getHeight(), rawTexture, 0, buffer.getWidth());

            int size = (int)(1 + Math.log10(buffer.getWidth()) / Math.log10(2));
            int[][] mipmaps = new int[size][];
            for (int i = 0; i < size; i++) {
                mipmaps[i] = rawTexture;
            }

            this.setIconHeight(buffer.getHeight());
            this.setIconWidth(buffer.getWidth());
            this.framesTextureData.add(mipmaps);
            return false;
        } catch (IOException e) {
            String errorMsg = "Error: Unable to load texture " + location.getResourceDomain() + ":" + location.getResourcePath();
            FluidTranslator.logger.error(errorMsg);
            return false;
        }
    }
}

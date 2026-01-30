package com.ezzo.fluidtranslator;

import com.ezzo.fluidtranslator.blocks.CustomFluidBlock;
import com.ezzo.fluidtranslator.item.GenericBucket;
import com.typesafe.config.ConfigException;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fluids.Fluid;


public class ModEventHandler {

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        switch (event.map.getTextureType()) {
            case 0: // blocks
                registerFluidBlockSprites(event);
                break;

            case 1: //items
                registerItemSprites(event);
                break;
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onTextureStitchPost(TextureStitchEvent.Post event) {

        switch (event.map.getTextureType()) {
            case 0: // blocks
                applyFluidBlockSprites(event);
                break;

            case 1: // items
                applyItemSprites(event);
                break;
        }
    }

    private void registerFluidBlockSprites(TextureStitchEvent.Pre event) {
        ModFluidRegistry.validFluids().forEach(f -> {
            if (!ModFluidRegistry.textureExists(f)) return;

            String spriteName = FluidTranslator.MODID + ":" + f.getName().toLowerCase() + "_texture";
            FluidAtlasSprite sprite = new FluidAtlasSprite(spriteName, f);
            event.map.setTextureEntry(sprite.getIconName(), sprite);
        });
    }

    private void registerItemSprites(TextureStitchEvent.Pre event) {
        ModFluidRegistry.validFluids().forEach(f -> {
            if (ModFluidRegistry.textureExists(f)) {
                String spriteName = FluidTranslator.MODID + ":" + f.getName().toLowerCase() + "_bucket";
                BucketAtlasSprite sprite = new BucketAtlasSprite(spriteName, f);
                event.map.setTextureEntry(sprite.getIconName(), sprite);
            } else {
                MissingnoBucketAtlasSprite sprite = new MissingnoBucketAtlasSprite(FluidTranslator.MODID + ":missingno_bucket");
                event.map.setTextureEntry(sprite.getIconName(), sprite);
            }
        });
    }

    private void applyFluidBlockSprites(TextureStitchEvent.Post event) {
        ModFluidRegistry.validFluids().forEach(f -> {
            String spriteName = FluidTranslator.MODID + ":" + f.getName().toLowerCase() + "_texture";
            TextureAtlasSprite sprite = event.map.getAtlasSprite(spriteName);

            Fluid forgeFluid = ModFluidRegistry.getForgeFluid(f);
            if (forgeFluid == null) return;

            Block fluidBlock = forgeFluid.getBlock();
            if (!(fluidBlock instanceof CustomFluidBlock)) return;

            ((CustomFluidBlock)fluidBlock).setIcons(sprite); // Apply the texture to the fluid's block
        });
    }

    private void applyItemSprites(TextureStitchEvent.Post event) {
        ModFluidRegistry.validFluids().forEach(f -> {
            String spriteName =
                ModFluidRegistry.textureExists(f)
                    ? FluidTranslator.MODID + ":" + f.getName().toLowerCase() + "_bucket"
                    : FluidTranslator.MODID + ":missingno_bucket";

            TextureAtlasSprite sprite = event.map.getAtlasSprite(spriteName);

            Fluid forgeFluid = ModFluidRegistry.getForgeFluid(f);
            if (forgeFluid == null) return;

            GenericBucket bucket = GenericBucket.getBuckerForFluid(forgeFluid);
            if (bucket == null) return;

            bucket.setIcon(sprite); // Apply the texture to the item
        });
    }
}

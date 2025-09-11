package com.fluidtranslator;

import com.fluidtranslator.blocks.CustomFluidBlock;
import com.fluidtranslator.item.GenericBucket;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fluids.Fluid;

public class ModEventHandler {

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        if (event.map.getTextureType() == 0) { // 0 = blocks
            FluidType[] fluids = Fluids.getAll();
            for (FluidType fluid : fluids) { // For each fluid from HBM's add its texture to the atlas
                if (ModFluidRegistry.isBlackListed(fluid)) continue;
                String spriteName = FluidTranslator.MODID + ":" + fluid.getName().toLowerCase() + "_texture";
                FluidAtlasSprite sprite = new FluidAtlasSprite(spriteName, fluid);
                event.map.setTextureEntry(sprite.getIconName(), sprite);
            }
        } else if (event.map.getTextureType() == 1) { // 1 = items
            FluidType[] fluids = Fluids.getAll();
            for (FluidType fluid : fluids) {
                if (ModFluidRegistry.isBlackListed(fluid)) continue;
                String spriteName = FluidTranslator.MODID + ":" + fluid.getName().toLowerCase() + "_bucket";
                BucketAtlasSprite sprite = new BucketAtlasSprite(spriteName, fluid);
                event.map.setTextureEntry(sprite.getIconName(), sprite);
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onTextureStitchPost(TextureStitchEvent.Post event) {
        if (event.map.getTextureType() == 0) {
            FluidType[] fluids = Fluids.getAll();
            for (FluidType fluidType : fluids) { // For each fluid from HBM's retrieve its texture from the atlas
                if (ModFluidRegistry.isBlackListed(fluidType)) continue;
                String spriteName = FluidTranslator.MODID + ":" + fluidType.getName().toLowerCase() + "_texture";
                TextureAtlasSprite sprite = event.map.getAtlasSprite(spriteName);

                Fluid forgeFluid = ModFluidRegistry.getForgeFluid(fluidType);
                if (forgeFluid == null) continue;
                Block b = ModFluidRegistry.getForgeFluid(fluidType).getBlock();
                if (b instanceof CustomFluidBlock) {
                    CustomFluidBlock fluidBlock = (CustomFluidBlock) b;
                    fluidBlock.setIcons(sprite); // Apply the texture to the fluid's block
                }
            }
        } else if (event.map.getTextureType() == 1) {
            FluidType[] fluids = Fluids.getAll();
            for (FluidType fluidType : fluids) {
                if (ModFluidRegistry.isBlackListed(fluidType)) continue;
                String spriteName = FluidTranslator.MODID + ":" + fluidType.getName().toLowerCase() + "_bucket";
                TextureAtlasSprite sprite = event.map.getAtlasSprite(spriteName);

                Fluid forgeFluid = ModFluidRegistry.getForgeFluid(fluidType);
                if (forgeFluid == null) continue;
                GenericBucket bucket = GenericBucket.getBuckerForFluid(forgeFluid);
                if (bucket == null) continue;
                bucket.setIcon(sprite);
            }
        }
    }
}

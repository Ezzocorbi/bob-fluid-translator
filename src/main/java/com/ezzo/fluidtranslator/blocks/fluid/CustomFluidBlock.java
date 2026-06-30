package com.ezzo.fluidtranslator.blocks.fluid;

import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.inventory.fluid.FluidType;

import com.hbm.inventory.fluid.trait.FT_Poison;
import com.hbm.inventory.fluid.trait.FT_Toxin;
import com.hbm.inventory.fluid.trait.FT_VentRadiation;
import com.hbm.lib.ModDamageSource;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.EntityDamageUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

import java.util.*;

import static com.ezzo.fluidtranslator.ModFluidRegistry.getHBMFluid;


public class CustomFluidBlock extends BlockFluidClassic {
    @SideOnly(Side.CLIENT)
    protected IIcon stillIcon;

    private final Fluid fluid;
    final FluidType hbmFluid;

    public CustomFluidBlock(Fluid fluid, Material material, String name) {
        super(fluid, material);
        setBlockName(name);
        this.fluid = fluid;
        this.hbmFluid = getHBMFluid(fluid);
    }

    @Override
    public String getLocalizedName() {
        return StatCollector.translateToLocal(
                fluid.getUnlocalizedName());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return stillIcon;
    }

    public void setIcons(IIcon icon) {
        getFluid().setIcons(icon);
        this.stillIcon = icon;
    }

    public Fluid getFluid() {
        return this.fluid;
    }
    @Override
    public void updateTick(World world, int x, int y, int z, Random rand){
        rad(world, x, y, z);
        super.updateTick(world, x, y, z, rand);
    }
    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        affect(entity);
    }
    public void rad(World world, int x, int y, int z){
        if(hbmFluid.hasTrait(FT_VentRadiation.class)) {
            FT_VentRadiation trait = hbmFluid.getTrait(FT_VentRadiation.class);
            ChunkRadiationManager.proxy.incrementRad(world, (int) (double) x, (int) (double) y, (int) (double) z, trait.getRadPerMB() * 2);
        }
    }
    public void affect(Entity entity){
        if(hbmFluid.temperature >= 100) {
            EntityDamageUtil.attackEntityFromIgnoreIFrame(entity, new DamageSource(ModDamageSource.s_boil), 0.2F + (hbmFluid.temperature - 100) * 0.02F);

            if(hbmFluid.temperature >= 500) {
                entity.setFire(10); //afterburn for 10 seconds
            }
        }
        if (entity instanceof EntityLivingBase) {
            if (hbmFluid.temperature < -20) {
                EntityDamageUtil.attackEntityFromIgnoreIFrame(entity, new DamageSource(ModDamageSource.s_cryolator), 0.2F + (hbmFluid.temperature + 20) * -0.05F); //5 damage at -20°C with one extra damage every -20°C
                ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 100, 2));
                ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(Potion.digSlowdown.id, 100, 4));
            }
            if (hbmFluid.hasTrait(FT_VentRadiation.class)) {
                FT_VentRadiation trait = hbmFluid.getTrait(FT_VentRadiation.class);
                ContaminationUtil.contaminate((EntityLivingBase) entity, ContaminationUtil.HazardType.RADIATION, ContaminationUtil.ContaminationType.CREATIVE, trait.getRadPerMB() * 5);
            }
            if (hbmFluid.hasTrait(FT_Poison.class)) {
                FT_Poison trait = hbmFluid.getTrait(FT_Poison.class);
                ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(trait.isWithering() ? Potion.wither.id : Potion.poison.id, (int) (5 * 20 * 1)));
            }
            if (hbmFluid.hasTrait(FT_Toxin.class)) {
                FT_Toxin trait = hbmFluid.getTrait(FT_Toxin.class);
                trait.affect((EntityLivingBase) entity, 1);
            }
        }
    }


}

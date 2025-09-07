package com.fluidtranslator.blocks;

import api.hbm.block.IToolable;
import com.fluidtranslator.FluidTranslator;
import com.fluidtranslator.container.GuiIds;
import com.fluidtranslator.tileentity.TileEntityHBMAdapter;
import com.hbm.items.tool.ItemTooling;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockHBMAdapter extends BlockContainer {

    @SideOnly(Side.CLIENT)
    private IIcon frontIcon;
    @SideOnly(Side.CLIENT)
    private IIcon backIcon;
    @SideOnly(Side.CLIENT)
    private IIcon sideIcon;


    public BlockHBMAdapter() {
        super(Material.rock);
        setBlockName("hbmInterface");
        setBlockTextureName(FluidTranslator.MODID + ":hbm_interface_block");
        setHardness(2.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityHBMAdapter();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
                                    EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            if (player.isSneaking()) {
                TileEntity te = world.getTileEntity(x, y, z);
                if(te instanceof TileEntityHBMAdapter) {
                    TileEntityHBMAdapter hbmIf = (TileEntityHBMAdapter) te;
                    int fronSide = ForgeDirection.getOrientation(side).ordinal();
                    ForgeDirection targetDirection = ForgeDirection.getOrientation(Facing.oppositeSide[fronSide]);

                    if (hbmIf.setTankAtDirection(targetDirection)) {
                        world.setBlockMetadataWithNotify(x, y, z, fronSide, 2);
                        player.addChatMessage(new ChatComponentText("Changed input side to " + targetDirection));
                    } else {
                        player.addChatMessage(new ChatComponentText("Could not find a machine to connect"));
                    }
                }
            } else {
                player.openGui(FluidTranslator.instance, GuiIds.HBM_ADAPTER.ordinal, world, x, y, z);
            }
        }
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister reg) {
        frontIcon = reg.registerIcon(FluidTranslator.MODID + ":adapter_face_front");  // front
        backIcon = reg.registerIcon(FluidTranslator.MODID + ":adapter_face_back");   // back
        sideIcon = reg.registerIcon(FluidTranslator.MODID + ":adapter_face_side"); // bottom
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta) { // meta refers to front side
        if (meta < 0 || meta > 5) {
            return sideIcon; // fallback front
        }

        if (side == meta) {
            return frontIcon; // front
        }
        if (side == Facing.oppositeSide[meta]) {
            return backIcon; // back
        }

        return sideIcon;
    }

    public static int determineOrientation(World world, int x, int y, int z, EntityLivingBase player) {

        if (Math.abs(player.posX - x) < 2.0F && Math.abs(player.posZ - z) < 2.0F) {
            double eyeHeight = player.posY + player.getEyeHeight();

            if (eyeHeight - y > 2.0D) {
                return player.isSneaking() ? Facing.oppositeSide[1] : 1; // DOWN if sneaking, otherwise UP
            }
            if (y - eyeHeight > 0.0D) {
                    return player.isSneaking() ? Facing.oppositeSide[0] : 0; // UP if sneaking, otherwise DOWN
                }
            }

            int l = MathHelper.floor_double(player.rotationYaw * 4.0D / 360.0D + 0.5D) & 3;
            int meta = 3;

            if (l == 0) meta = 2; // NORTH
            if (l == 1) meta = 5; // EAST
            if (l == 2) meta = 3; // SOUTH
            if (l == 3) meta = 4; // WEST

            if (player.isSneaking()) {
                meta = Facing.oppositeSide[meta];
            }
            return meta; // fallback
        }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z,
                                EntityLivingBase player, ItemStack stack) {
        int meta = determineOrientation(world, x, y, z, player);
        System.out.println("Front face is facing: " + translateOrientation(meta));
        world.setBlockMetadataWithNotify(x, y, z, meta, 2);
    }

    private String translateOrientation (int meta) {
        if (meta == 0) return "Down";
        if (meta == 1) return "Up";
        if (meta == 2) return "North";
        if (meta == 3) return "South";
        if (meta == 4) return "West";
        if (meta == 5) return "East";
        return "Invalid orientation";
    }
}

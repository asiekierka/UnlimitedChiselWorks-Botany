/*
 * Copyright (c) 2017 Adrian Siekierka
 *
 * This file is part of Unlimited Chisel Works.
 *
 * Unlimited Chisel Works is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Unlimited Chisel Works is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Unlimited Chisel Works.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.ucw;

import binnie.botany.api.genetics.EnumFlowerColor;
import binnie.core.block.TileEntityMetadata;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class BlockUCWBotanyBase extends BlockUCWProxy implements IUCWCustomVariantHandler, ITileEntityProvider {
    protected static final List<BlockUCWBotanyBase> BLOCKS = new ArrayList<>();
    protected boolean variantsRegistered = false;
    protected ThreadLocal<TileEntity> tiles = new ThreadLocal();

    public BlockUCWBotanyBase() {
        BLOCKS.add(this);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("meta")) {
            TileEntity tile = worldIn.getTileEntity(pos);
            if (tile instanceof TileEntityMetadata) {
                ((TileEntityMetadata) tile).setTileMetadata(stack.getTagCompound().getInteger("meta"), false);
            }
        }
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return getStack(state, world.getTileEntity(pos));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityMetadata();
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int id, int param) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity != null && tileentity.receiveClientEvent(id, param);
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
        tiles.set(te);
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        tiles.set(null);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        drops.add(getStack(state, tiles.get()));
    }

    public ItemStack getStack(IBlockState state, TileEntity tile) {
        if (tile instanceof TileEntityMetadata) {
            ItemStack stack = new ItemStack(state.getBlock(), 1, damageDropped(state));
            stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setInteger("meta", ((TileEntityMetadata) tile).getTileMetadata());
            return stack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        return getStack(state, tiles.get());
    }
}
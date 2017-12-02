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
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;

public class UCWBotanyGlassColorMultiplier implements IBlockColor, IItemColor {
    public static final UCWBotanyGlassColorMultiplier INSTANCE = new UCWBotanyGlassColorMultiplier();

    private UCWBotanyGlassColorMultiplier() {

    }

    public int colorMultiplier(int meta) {
        return 0xFF000000 | EnumFlowerColor.get(meta).getFlowerColorAllele().getColor(false);
    }

    @Override
    public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
        if (tintIndex != 0 || worldIn == null || pos == null) {
            return -1;
        }
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileEntityMetadata) {
            return colorMultiplier(((TileEntityMetadata) tile).getTileMetadata());
        } else {
            return -1;
        }
    }

    @Override
    public int colorMultiplier(ItemStack stack, int tintIndex) {
        if (tintIndex != 0 || !stack.hasTagCompound() || !stack.getTagCompound().hasKey("meta")) {
            return -1;
        }
        return colorMultiplier(stack.getTagCompound().getInteger("meta"));
    }
}

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
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;

public class BlockUCWBotanyCeramic extends BlockUCWBotanyBase {
    @Override
    public void registerVariants(String groupName, IBlockState origState, List<ItemStack> stacks) {
        List<ItemStack> grouping = new ArrayList<>();
        grouping.add(new ItemStack(origState.getBlock()));
        grouping.addAll(stacks);

        for (EnumFlowerColor color : EnumFlowerColor.VALUES) {
            String gn = groupName + "_" + color.ordinal();
            for (ItemStack stack : grouping) {
                stack = stack.copy();
                if (stack.getItem() instanceof ItemUCWBotanyBase) {
                    stack.setTagCompound(new NBTTagCompound());
                    stack.getTagCompound().setInteger("meta", color.ordinal());
                } else {
                    stack.setItemDamage(color.ordinal());
                }
                UCWCompatUtils.addChiselVariation(gn, stack);
            }
        }
    }
}

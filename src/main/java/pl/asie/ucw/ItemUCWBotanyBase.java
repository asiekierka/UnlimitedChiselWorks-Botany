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
import binnie.core.util.I18N;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;

import java.util.ArrayList;
import java.util.List;

public abstract class ItemUCWBotanyBase extends ItemUCWProxy {
    protected static final List<ItemUCWBotanyBase> ITEMS = new ArrayList<>();

    public ItemUCWBotanyBase(Block block) {
        super(block);
        ITEMS.add(this);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        try {
            if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("meta")) {
                return this.getUnlocalizedName();
            }

            ItemStack proxyStack = stack.copy();
            proxyStack.setItemDamage(stack.getTagCompound().getInteger("meta"));
            return this.getItemFrom().getItemStackDisplayName(proxyStack);
        } catch (Exception var3) {
            var3.printStackTrace();
            return this.getUnlocalizedName();
        }
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!((BlockUCWBotanyBase) block).variantsRegistered) {
            super.getSubItems(tab, items);
        } else {
            NonNullList<ItemStack> base = NonNullList.create();
            super.getSubItems(tab, base);
            if (base.size() > 0) {
                for (EnumFlowerColor color : EnumFlowerColor.VALUES) {
                    for (ItemStack stack : base) {
                        stack = stack.copy();
                        stack.setTagCompound(new NBTTagCompound());
                        stack.getTagCompound().setInteger("meta", color.ordinal());
                        items.add(stack);
                    }
                }
            }
        }
    }
}

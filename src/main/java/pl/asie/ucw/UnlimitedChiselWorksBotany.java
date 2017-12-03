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
import binnie.botany.blocks.BlockCeramic;
import binnie.botany.blocks.BlockStainedGlass;
import binnie.botany.tile.TileCeramic;
import binnie.core.block.TileEntityMetadata;
import forestry.core.proxy.ProxyCommon;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import team.chisel.Chisel;
import team.chisel.api.IChiselItem;
import team.chisel.api.carving.CarvingUtils;
import team.chisel.api.carving.ICarvingGroup;
import team.chisel.api.carving.ICarvingRegistry;
import team.chisel.api.carving.ICarvingVariation;
import team.chisel.client.ClientUtil;
import team.chisel.common.util.NBTUtil;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

@Mod(
		modid = "unlimitedchiselworks_botany",
		version = "${version}",
		dependencies = "required-after:botany;required-after:unlimitedchiselworks"
)
public class UnlimitedChiselWorksBotany {
	@SidedProxy(serverSide = "pl.asie.ucw.ProxyBotanyCommon", clientSide = "pl.asie.ucw.ProxyBotanyClient")
	public static ProxyBotanyCommon proxy;
	public static boolean replaceBotanyGlassTexture;

	private Configuration config;

	@Mod.EventHandler
	public void onPreInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(proxy);

		config = new Configuration(event.getSuggestedConfigurationFile());
		replaceBotanyGlassTexture = config.getBoolean("replaceBotanyGlassTexture", "general", true, "Set to true to replace Botany's default Pigmented Glass texture to match vanilla/your resource pack's stained glass.");

		if (config.hasChanged()) {
			config.save();
		}
	}

	@Mod.EventHandler
	public void onInit(FMLInitializationEvent event) {
		proxy.init();
	}

	private void damageItem(ItemStack stack, EntityPlayer player, EnumHand hand) {
		stack.damageItem(1, player);
		if (stack.isEmpty()) {
			player.setHeldItem(hand, ItemStack.EMPTY);
			ForgeEventFactory.onPlayerDestroyItem(player, stack, hand);
		}
	}

	private int getMetaFromStack(ItemStack stack) {
		Block block = Block.getBlockFromItem(stack.getItem());
		if (block instanceof BlockCeramic) {
			return stack.getItemDamage();
		} else if (stack.hasTagCompound()) {
			return stack.getTagCompound().getInteger("meta");
		} else {
			return 0;
		}
	}

	private boolean placeCeramicBlockWithEffects(World world, BlockPos pos, IBlockState currState, ItemStack stack, EntityPlayer player, ItemStack held) {
		if (placeCeramicBlockIfDiffers(world, pos, currState, stack)) {
			if (world.isRemote) {
				ClientUtil.playSound(world, player, held, currState);
				ClientUtil.addDestroyEffects(world, pos, currState);
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean placeCeramicBlockIfDiffers(World world, BlockPos pos, IBlockState currState, ItemStack stack) {
		IBlockState state = Block.getBlockFromItem(stack.getItem()).getDefaultState();
		if (state.getBlock() instanceof IUCWBlock) {
			state = state.getBlock().getStateFromMeta(stack.getItemDamage());
		}
		int meta = getMetaFromStack(stack);

		if (state == currState) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileEntityMetadata) {
				if (((TileEntityMetadata) tile).getTileMetadata() == meta) {
					return false;
				} else {
					((TileEntityMetadata) tile).setTileMetadata(meta, true);
					return true;
				}
			} else if (tile instanceof TileCeramic) {
				if (((TileCeramic) tile).getColor().ordinal() == meta) {
					return false;
				} else {
					((TileCeramic) tile).setColor(EnumFlowerColor.get(meta));
					world.notifyBlockUpdate(pos, state, state, 3);
					return true;
				}
			}
		}

		world.setBlockState(pos, state);
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileEntityMetadata) {
			((TileEntityMetadata) tile).setTileMetadata(meta, !world.isRemote);
		} else if (tile instanceof TileCeramic) {
			((TileCeramic) tile).setColor(EnumFlowerColor.get(meta));
			if (!world.isRemote) {
				world.notifyBlockUpdate(pos, state, state, 3);
			}
		}

		return true;
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent.LeftClickBlock event) {
		ItemStack held = event.getItemStack();

		if (!held.isEmpty() && held.getItem() instanceof IChiselItem) {
			IBlockState state = event.getWorld().getBlockState(event.getPos());
			if (state.getBlock() instanceof BlockStainedGlass || state.getBlock() instanceof BlockCeramic || state.getBlock() instanceof BlockUCWBotanyBase) {
				try {
					event.setCanceled(true);
					event.setUseBlock(Event.Result.DEFAULT);
					event.setUseItem(Event.Result.DEFAULT);

					IChiselItem item = (IChiselItem) held.getItem();
					if (!item.canChiselBlock(event.getWorld(), event.getEntityPlayer(), event.getHand(), event.getPos(), state)) {
						return;
					}

					int meta = -1;
					TileEntity tile = event.getWorld().getTileEntity(event.getPos());
					if (tile instanceof TileEntityMetadata) {
						meta = ((TileEntityMetadata) tile).getTileMetadata();
					} else if (tile instanceof TileCeramic) {
						meta = ((TileCeramic) tile).getColor().ordinal();
					} else {
						return;
					}

					ItemStack source = new ItemStack(state.getBlock());

					if (state.getBlock() instanceof BlockCeramic) {
						source.setItemDamage(meta);
					} else {
						if (state.getBlock() instanceof IUCWBlock) {
							source.setItemDamage(state.getBlock().getMetaFromState(state));
						}

						source.setTagCompound(new NBTTagCompound());
						source.getTagCompound().setInteger("meta", meta);
					}

					ItemStack target = NBTUtil.getChiselTarget(held);
					ICarvingRegistry registry = CarvingUtils.getChiselRegistry();
					ICarvingGroup group = registry.getGroup(source);

					if (!target.isEmpty()) {
						ICarvingGroup targetGroup = registry.getGroup(target);

						if (group == targetGroup) {
							ICarvingVariation variation = CarvingUtils.getChiselRegistry().getVariation(target);
							if (variation != null) {
								ItemStack targetStack = variation.getStack();
								if (placeCeramicBlockWithEffects(event.getWorld(), event.getPos(), state, targetStack, event.getEntityPlayer(), held)) {
									damageItem(held, event.getEntityPlayer(), event.getHand());
								}
							} else {
								Chisel.logger.warn("Found stack {} in group {}, but could not find variation!", target, group.getName());
							}
						}
					} else {
						List<ItemStack> stacks = registry.getItemsForChiseling(source);
						ItemStack next = null;

						for (int i = 0; i < stacks.size(); i++) {
							if (ItemStack.areItemStacksEqual(source, stacks.get(i))) {
								next = stacks.get((i + 1) % stacks.size());
								break;
							}
						}

						if (next != null) {
							if (placeCeramicBlockWithEffects(event.getWorld(), event.getPos(), state, next, event.getEntityPlayer(), held)) {
								damageItem(held, event.getEntityPlayer(), event.getHand());
							}
						}
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
	}
}

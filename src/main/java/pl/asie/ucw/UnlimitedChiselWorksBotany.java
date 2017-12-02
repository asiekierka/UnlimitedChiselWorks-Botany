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

import forestry.core.proxy.ProxyCommon;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(
		modid = "unlimitedchiselworks_botany",
		version = "${version}",
		dependencies = "required-after:botany;required-after:unlimitedchiselworks"
)
public class UnlimitedChiselWorksBotany {
	@SidedProxy(serverSide = "pl.asie.ucw.ProxyBotanyCommon", clientSide = "pl.asie.ucw.ProxyBotanyClient")
	public static ProxyBotanyCommon proxy;

	@Mod.EventHandler
	public void onPreInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(proxy);
	}

	@Mod.EventHandler
	public void onInit(FMLInitializationEvent event) {
		proxy.init();
	}
}

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

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.function.Function;

public class ProxyBotanyClient extends ProxyBotanyCommon {
    private static final ResourceLocation GLASS_WHITE_LOC = new ResourceLocation("minecraft", "blocks/glass_white");

    public void init() {
        super.init();
        for (BlockUCWBotanyBase block : BlockUCWBotanyGlass.BLOCKS) {
            Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(UCWBotanyGlassColorMultiplier.INSTANCE, block);
        }
        for (ItemUCWBotanyBase item : ItemUCWBotanyGlass.ITEMS) {
            Minecraft.getMinecraft().getItemColors().registerItemColorHandler(UCWBotanyGlassColorMultiplier.INSTANCE, item);
        }
    }

    @SubscribeEvent
    public void onTextureStitchPre(TextureStitchEvent.Pre event) {
        if (UnlimitedChiselWorksBotany.replaceBotanyGlassTexture) {
            event.getMap().setTextureEntry(new TextureAtlasSprite("botany:blocks/stained") {
                @Override
                public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
                    return true;
                }

                @Override
                public boolean load(IResourceManager manager, ResourceLocation location, Function<ResourceLocation, TextureAtlasSprite> textureGetter) {
                    TextureAtlasSprite fromTex = textureGetter.apply(GLASS_WHITE_LOC);

                    setIconWidth(fromTex.getIconWidth());
                    setIconHeight(fromTex.getIconHeight());
                    int[][] data = UCWMagic.createBaseForColorMultiplier(fromTex, false);

                    clearFramesTextureData();
                    for (int i = 0; i < fromTex.getFrameCount(); i++) {
                        int[][] pixels = new int[Minecraft.getMinecraft().gameSettings.mipmapLevels + 1][];
                        pixels[0] = data[i];
                        framesTextureData.add(pixels);
                    }

                    return false;
                }

                @Override
                public java.util.Collection<ResourceLocation> getDependencies() {
                    return ImmutableList.of(GLASS_WHITE_LOC);
                }
            });
        }
    }
}

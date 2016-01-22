/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.lepidopterology.render;

import java.io.IOException;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

import forestry.api.lepidopterology.ButterflyManager;
import forestry.api.lepidopterology.IButterfly;
import forestry.core.utils.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IRetexturableModel;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;

public class ModelButterflyItem implements ISmartItemModel {

	public IRetexturableModel modelButterfly;
	public ResourceLocation location;

	public ModelButterflyItem(ResourceLocation location) {
		this.location = location;
	}

	@Override
	public List getFaceQuads(EnumFacing p_177551_1_) {
		return null;
	}

	@Override
	public List getGeneralQuads() {
		return null;
	}

	@Override
	public boolean isAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean isGui3d() {
		return true;
	}

	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}
	
	@Override
	public TextureAtlasSprite getParticleTexture() {
		return null;
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return null;
	}

	@Override
	public IBakedModel handleItemState(ItemStack item) {
		try {
			modelButterfly = (IRetexturableModel) ModelLoaderRegistry.getModel(location);
		} catch (IOException e) {
			Log.warning(
					"Failed to find Butterfly Model for (" + location.toString() + ") in the Forestry registry.");
		}
		IButterfly butterfly = ButterflyManager.butterflyRoot.getMember(item);
		if (modelButterfly == null) {
			return null;
		}
		if (butterfly == null) {
			butterfly = ButterflyManager.butterflyRoot.templateAsIndividual(ButterflyManager.butterflyRoot.getDefaultTemplate());
		}
		return bakeModel(butterfly);
	}

	public IBakedModel bakeModel(IButterfly butterfly) {
		Function<ResourceLocation, TextureAtlasSprite> textureGetter = new Function<ResourceLocation, TextureAtlasSprite>() {
			@Override
			public TextureAtlasSprite apply(ResourceLocation location) {
				return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
			}
		};
		ImmutableMap.Builder<String, String> textures = ImmutableMap.builder();
		textures.put("#ButterflyGE", butterfly.getGenome().getSecondary().getEntityTexture().toString().replace(".png", "").replace("textures/", ""));
		modelButterfly = (IRetexturableModel) modelButterfly.retexture(textures.build());
		return modelButterfly.bake(ModelRotation.X270_Y90, DefaultVertexFormats.ITEM, textureGetter);
	}

	public static float getIrregularWingYaw(long flapping, float flap) {
		long irregular = flapping / 1024;
		float wingYaw;

		if (irregular % 11 == 0) {
			wingYaw = 0.75f;
		} else {
			if (irregular % 7 == 0) {
				flap *= 4;
				flap = flap % 1;
			} else if (irregular % 19 == 0) {
				flap *= 6;
				flap = flap % 1;
			}
			wingYaw = getRegularWingYaw(flap);
		}

		return wingYaw;
	}

	private static float getRegularWingYaw(float flap) {
		return flap < 0.5 ? 0.75f + flap : 1.75f - flap;
	}

}

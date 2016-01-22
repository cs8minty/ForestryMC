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
package forestry.arboriculture.items;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import forestry.api.arboriculture.EnumWoodType;
import forestry.arboriculture.IWoodTyped;
import forestry.arboriculture.tiles.TileWood;
import forestry.core.config.Constants;
import forestry.core.items.ItemBlockForestry;
import forestry.core.render.model.ModelManager;
import forestry.core.utils.BlockUtil;
import forestry.core.utils.StringUtil;

public class ItemBlockWood extends ItemBlockForestry {
	private static final String LEGACY_WOOD_TYPE_KEY = "WoodType";

	public ItemBlockWood(Block block) {
		super(block);
	}

	public static boolean placeWood(ItemStack stack, IBlockState newState, @Nullable EntityPlayer player, World world, BlockPos pos) {
		EnumWoodType woodType = getWoodType(stack);

		return placeWood(stack, woodType, newState, player, world, pos);
	}

	public static boolean placeWood(ItemStack stack, EnumWoodType woodType, IBlockState state, @Nullable EntityPlayer player, World world, BlockPos pos) {
		boolean placed = world.setBlockState(pos, state, Constants.FLAG_BLOCK_SYNCH_AND_UPDATE);
		if (!placed) {
			return false;
		}

		Block worldBlock = BlockUtil.getBlock(world, pos);
		if (!Block.isEqualTo(state.getBlock(), worldBlock)) {
			return false;
		}

		TileEntity tile = world.getTileEntity(pos);
		if (!(tile instanceof TileWood)) {
			world.setBlockToAir(pos);
			return false;
		}

		if (player != null) {
			worldBlock.onBlockPlacedBy(world, pos, state, player, stack);
		}

		((TileWood) tile).setWoodType(woodType);
		return true;
	}

	public static void saveToItemStack(EnumWoodType woodType, ItemStack itemStack) {
		int ordinal = woodType.ordinal();
		itemStack.setItemDamage(ordinal);
	}

	public static EnumWoodType getWoodType(ItemStack stack) {
		EnumWoodType woodType = convertLegacyWood(stack);
		if (woodType == null) {
			int typeOrdinal = stack.getItemDamage();
			woodType = getFromOrdinal(typeOrdinal);
		}

		return woodType;
	}

	// legacy handling of wood that has wood type saved to NBT
	private static EnumWoodType convertLegacyWood(ItemStack itemStack) {
		if (!itemStack.hasTagCompound()) {
			return null;
		}

		NBTTagCompound compound = itemStack.getTagCompound();
		if (!compound.hasKey(LEGACY_WOOD_TYPE_KEY)) {
			return null;
		}

		int typeOrdinal = compound.getInteger(LEGACY_WOOD_TYPE_KEY);
		EnumWoodType woodType = getFromOrdinal(typeOrdinal);
		if (woodType != null) {
			compound.removeTag(LEGACY_WOOD_TYPE_KEY);
			if (compound.hasNoTags()) {
				itemStack.setTagCompound(null);
			}
			saveToItemStack(woodType, itemStack);
		}
		return woodType;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity player, int p_77663_4_, boolean p_77663_5_) {
		super.onUpdate(stack, world, player, p_77663_4_, p_77663_5_);
		convertLegacyWood(stack);
	}

	private static EnumWoodType getFromOrdinal(int ordinal) {
		if (ordinal >= 0 && ordinal < EnumWoodType.VALUES.length) {
			return EnumWoodType.VALUES[ordinal];
		}
		return EnumWoodType.LARCH;
	}

	@Override
	public int getMetadata(int i) {
		return 0;
	}
	
	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
		return placeWood(stack, newState, player, world, pos);
	}

	@Override
	public String getItemStackDisplayName(ItemStack itemstack) {
		Block block = getBlock();
		if (!(block instanceof IWoodTyped)) {
			return super.getItemStackDisplayName(itemstack);
		}

		EnumWoodType woodType = getWoodType(itemstack);
		if (woodType == null) {
			return super.getItemStackDisplayName(itemstack);
		}

		IWoodTyped wood = (IWoodTyped) block;
		String blockKind = wood.getBlockKind();

		String displayName;
		String customUnlocalizedName = blockKind + "." + woodType.ordinal() + ".name";
		if (StringUtil.canTranslateTile(customUnlocalizedName)) {
			displayName = StringUtil.localizeTile(customUnlocalizedName);
		} else {
			String woodGrammar = StringUtil.localize(blockKind + ".grammar");
			String woodTypeName = StringUtil.localize("trees.woodType." + woodType);

			displayName = woodGrammar.replaceAll("%TYPE", woodTypeName);
		}

		if (wood.isFireproof()) {
			displayName = StringUtil.localizeAndFormatRaw("tile.for.fireproof", displayName);
		}

		return displayName;
	}
	
	public static ResourceLocation[] getVariants(IWoodTyped typed) {
		List variants = new ArrayList<String>();
		for (EnumWoodType type : EnumWoodType.values())
			variants.add(new ResourceLocation("forestry",  typed.getBlockKind() + "/" + type.getName().toLowerCase()));
		return (ResourceLocation[]) variants.toArray(new ResourceLocation[variants.size()]);
	}
	
	@SideOnly(Side.CLIENT)
	public static class WoodMeshDefinition implements ItemMeshDefinition {

		public String modifier;

		public WoodMeshDefinition(IWoodTyped typed) {
			this.modifier = typed.getBlockKind();
		}

		@Override
		public ModelResourceLocation getModelLocation(ItemStack stack) {
			EnumWoodType type = getWoodType(stack);
			return ModelManager.getInstance().getModelLocation(stack.getItem(), 0, modifier, type.name().toLowerCase());
		}

	}
}

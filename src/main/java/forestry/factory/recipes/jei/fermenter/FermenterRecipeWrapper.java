package forestry.factory.recipes.jei.fermenter;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import forestry.api.recipes.IFermenterRecipe;
import forestry.api.recipes.IVariableFermentable;
import forestry.core.recipes.jei.ForestryRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class FermenterRecipeWrapper extends ForestryRecipeWrapper<IFermenterRecipe>{
	
	@Nonnull
	private final ItemStack fermentable;
	
	public FermenterRecipeWrapper(@Nonnull IFermenterRecipe recipe, @Nonnull ItemStack fermentable) {
		super(recipe);
		this.fermentable = fermentable;
	}
	
	@Override
	public List getInputs() {
		return Collections.singletonList(fermentable);
	}
	
	@Override
	public List<FluidStack> getFluidInputs() {
		return Collections.singletonList(recipe.getFluidResource());
	}
	
	@Override
	public List<FluidStack> getFluidOutputs() {
		int amount = Math.round(recipe.getFermentationValue() * recipe.getModifier());
		if (fermentable.getItem() instanceof IVariableFermentable) {
			amount *= ((IVariableFermentable) fermentable.getItem()).getFermentationModifier(fermentable);
		}
		FluidStack output = new FluidStack(recipe.getOutput(), amount);
		return Collections.singletonList(output);
	}

}

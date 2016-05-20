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
package forestry.arboriculture.worldgen;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import forestry.api.world.ITreeGenData;

public class WorldGenPine extends WorldGenTree {

	public WorldGenPine(ITreeGenData tree) {
		super(tree, 6, 4);
	}

	@Override
	public void generate(World world) {
		generateTreeTrunk(world, height, girth);

		List<BlockPos> branchCoords = new ArrayList<>();
		for (int yBranch = 2; yBranch < height - 2; yBranch++) {
			branchCoords.addAll(generateBranches(world, yBranch, 0, 0, 0.05f, 0.1f, Math.round((height - yBranch) * 0.25f), 1, 0.25f));
		}
		for (BlockPos branchEnd : branchCoords) {
			generateAdjustedCylinder(world, branchEnd, 2, 1, leaf, EnumReplaceMode.AIR);
		}

		int leafSpawn = height + 1;
		float diameterchange = 1.25f / height;
		int leafSpawned = 2;

		generateAdjustedCylinder(world, leafSpawn--, 0, 1, leaf);
		generateAdjustedCylinder(world, leafSpawn--, 1, 1, leaf);

		while (leafSpawn > 1) {
			generateAdjustedCylinder(world, leafSpawn--, 3 * diameterchange * leafSpawned, 1, leaf);
			generateAdjustedCylinder(world, leafSpawn--, 2 * diameterchange * leafSpawned, 1, leaf);
			leafSpawned += 2;
		}

		if (hasPods()) {
			generatePods(world, height, girth);
		}
	}

}

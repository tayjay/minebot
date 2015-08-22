/*******************************************************************************
 * This file is part of Minebot.
 *
 * Minebot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Minebot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Minebot.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package net.famzangl.minecraft.minebot.ai.path;

import net.famzangl.minecraft.minebot.ai.path.world.BlockSet;
import net.famzangl.minecraft.minebot.ai.path.world.BlockSets;
import net.famzangl.minecraft.minebot.ai.task.DestroyInRangeTask;
import net.famzangl.minecraft.minebot.ai.task.WaitTask;
import net.famzangl.minecraft.minebot.ai.task.place.PlantSaplingTask;
import net.famzangl.minecraft.minebot.build.block.WoodType;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

public class TreePathFinder extends MovePathFinder {
	private static final BlockSet TREE_STUFF = new BlockSet(Blocks.log, Blocks.log2, Blocks.leaves, Blocks.leaves2);
	private final WoodType type;
	private final boolean replant;

	public TreePathFinder(WoodType type, boolean replant) {
		this.type = type;
		this.replant = replant;
		shortFootBlocks = shortFootBlocks.unionWith(TREE_STUFF);
		shortHeadBlocks = shortHeadBlocks.unionWith(TREE_STUFF);
	}

	private static final int TREE_HEIGHT = 7;
	
	@Override
	protected float rateDestination(int distance, int x, int y, int z) {
		int points = 0;
		if (isTree(x, y, z)) {
			points++;
		}
		if (isTree(x, y + 1, z)) {
			points++;
		}
		for (int i = 2; i < TREE_HEIGHT; i++) {
			if (!BlockSets.safeSideAndCeilingAround(world, x, y + i, z)) {
				break;
			} else if (isTree(x, y + i, z)) {
				points++;
			}
		}

		return points == 0 ? -1 : distance + 20 - points * 2;
	}

	private boolean isTree(int x, int y, int z) {
		final Block block = helper.getBlock(x, y, z);
		if (type == null) {
			return Block.isEqualTo(block, Blocks.log)
					|| Block.isEqualTo(block, Blocks.log2);
		} else {
			return Block.isEqualTo(block, type.block)
					&& (0x3 & world.getBlockIdWithMeta(
							x, y, z) & 0xf) == type.lowerBits;
		}
	}

	@Override
	protected void addTasksForTarget(BlockPos currentPos) {
		int mineAbove = 0;

		for (int i = 2; i < TREE_HEIGHT; i++) {
			if (isTree(currentPos.getX(), currentPos.getY() + i, currentPos.getZ())) {
				mineAbove = i;
			}
		}
		int max = 0;
		for (int i = 2; i <= mineAbove; i++) {
			BlockPos pos = currentPos.add(0,i,0);
			if (!BlockSets.safeSideAndCeilingAround(world, pos)) {
				break;
			}
			if (!BlockSets.AIR.isAt(world, pos)) {
				max = i;
			}
		}
		if (max > 0) {
			addTask(new DestroyInRangeTask(currentPos.add(0,2,0), currentPos.add(0,max,0)));
		}
		
		if (replant) {
			addTask(new PlantSaplingTask(currentPos, type));
		}
		addTask(new WaitTask(mineAbove * 2));
	}
}

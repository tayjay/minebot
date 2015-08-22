package net.famzangl.minecraft.minebot.ai.path.world;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;

public class RecordingWorld extends WorldWithDelta {

	private static final int TIME_TO_PLACE = 5;
	private int timeInTicks = 0;
	private EntityPlayer blockBreaker;

	public RecordingWorld(WorldData currentWorld, EntityPlayer blockBreaker) {
		super(currentWorld);
		this.blockBreaker = blockBreaker;
	}

	@Override
	public void setBlock(int x, int y, int z, int blockId, int meta) {
		int currentBlock = getBlockIdWithMeta(x, y, z);
		if (currentBlock >> 4 != 0) {
			timeInTicks += getTimeToDestroy(new BlockPos(x, y, z), currentBlock);
		}
		
		if (blockId != 0) {
			timeInTicks += TIME_TO_PLACE;
		}
		super.setBlock(x, y, z, blockId, meta);
	}

	private int getTimeToDestroy(BlockPos blockPos, int blockWithMeta) {
		Block block = Block.getBlockById(blockWithMeta >> 4);
		//how much damage to give the block per tick. The block is destroyed on damage >= 1;
		float hardness = block.getPlayerRelativeBlockHardness(blockBreaker, getBackingWorld(), blockPos);
		System.out.println("TIME to destroy block at " + blockPos + ": " + 1 / hardness);
		return (int) Math.ceil(1 / hardness);
	}

	@Override
	public void setPlayerPosition(BlockPos playerPosition) {
		timeInTicks += timeToWalk(playerPosition, getPlayerPosition());

		super.setPlayerPosition(playerPosition);
	}

	private int timeToWalk(BlockPos p1, BlockPos p2) {
		if (p1.equals(p2)) {
			return 5;
		} else {
		double hDist = Math.hypot(p1.getX() - p2.getX(), p1.getY() - p2.getY());
		double time = 1 + hDist / 4;
		return (int) Math.ceil(time * 20);
		}
	}
	
	public int getTimeInTicks() {
		return timeInTicks;
	}
}

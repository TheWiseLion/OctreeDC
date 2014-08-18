package VoxelSystem.Operators;

import VoxelSystem.Data.VoxelExtraction.VoxelExtractor;

public interface CSGOperator {
	public VoxelExtractor operate(VoxelExtractor ... arguements);
}

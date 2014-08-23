package VoxelSystem.threading;

import VoxelSystem.SparseData.controller.LODController;

public interface PriorityRunner{
	
	public void run(LODController lod);
	public int getPriority(LODController lod);
	
}

package VoxelSystem.SparseData;

import VoxelSystem.SparseData.ChunkData.Chunk;

/***
 * Manages notification when chunk data has changed
 * 
 */
public interface ChunkRequester {

	
	/***
	 * Called when a chunks underlying data has changed.
	 * The chunk's OctreeNode will contain the appropriate dirty flags
	 */
	public void update(Chunk changedNode);
	
	
	//TODO: max LOD needed for a chunk
}

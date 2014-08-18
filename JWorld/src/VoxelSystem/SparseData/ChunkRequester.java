package VoxelSystem.SparseData;

import VoxelSystem.SparseData.ChunkData.Chunk;

/***
 * Used by data layer to indicate the level 
 * of detail needed in a particular area of octree.
 * 
 * The data layer will always seeks to make this level of 
 * detail available.
 * 
 * 
 */
public interface ChunkRequester {
//	public boolean shouldSplit(OctreeNode node);
	
	/***
	 * Called when a chunks underlying data has changed.
	 * The chunk's OctreeNode will contain the appropriate dirty flags
	 */
	public void update(Chunk changedNode);
}

/**
 * Chunk handler maintains the interaction between the data layer
 * and the rendering layer. At its core the render needs a certain amount of 
 * data at a specific time. However this data may not yet have been generated.
 * The chunk handlers guide the data layer to generate only needed data.
 * It also provides the render with updates when the state of the data has been changed.
 * This way only portions of the chunk may need to be re-rendered
 *
 */
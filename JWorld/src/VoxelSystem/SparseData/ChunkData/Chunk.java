package VoxelSystem.SparseData.ChunkData;

import VoxelSystem.Data.Storage.OctreeNode;
import VoxelSystem.Data.VoxelExtraction.VoxelExtractor;
import VoxelSystem.meshing.lod.VoxelMesh;

/***
 * 
 * @author 0xFFFF
 *
 */
public abstract class Chunk {
	/**
	 * Holds the state of a chunk.
	 * None indicates that none of this chunk is generated
	 * Partial indicates some levels of this chunk is generated
	 * Fully indicates that this chunk is fully representative of all data
	 * @author 0xFFFF
	 *
	 */
	public static enum ChunkState{
		NONE,
		PARTIAL,
		FULLY
	};
	
	
//	public Chunk getParent();
	public abstract ChunkState getChunkState();	
	public abstract OctreeNode getChunkContents();
	public abstract void setChunkContents(OctreeNode contents);
	public abstract int getMaxLevel();
	public abstract VoxelMesh getRenderNode();
	public abstract VoxelExtractor getExtractor();
	public abstract void setExtractor(VoxelExtractor ve);
	
	
	public final int cx,cy,cz;
	public Chunk(int cx, int cy, int cz){
		this.cx = cx;
		this.cy = cy;
		this.cz = cz;	
	}
	
}

package VoxelSystem.SparseData;

import java.util.List;

import VoxelSystem.VoxelSystemTables.AXIS;
import VoxelSystem.SparseData.ChunkData.Chunk;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;

/**
 * At its core it partitions world space into 
 * segments. Its initial parameters will usually
 * be how big each individual chunk/segment will be.
 * 
 * It also stores potential functions that are still pending on sections of the world. 
 * This brings the advantage of not needing to generate all the data of a chunk a one time.
 * 
 * Another potential advantage is chunks can be choose to be store their voxel values directly or 
 * simple the functions the composed them. However to fully utilize this capability one will need
 * a way computing the potential costs associated whether it's better store the functions or store the
 * voxel values.
 * 
 */
public interface SparseInterface {
	
	/***
	 * Returns list of chunks intersecting given bounding box
	 * @param bv
	 * @return
	 */
	public List<Chunk> getIntersecting(BoundingBox bv);
	
//	/***
//	 * Returns VoxelExtractor associated to a particular chunk
//	 * 
//	 * Note if the chunk's state is "FULLY" then the VoxelExtractor returned
//	 * is the contents of the chunk.
//	 * 
//	 * @param c - the chunk you want the associated VoxelExtractor for
//	 * @return
//	 */
//	public VoxelExtractor getPendingResults(Chunk c);
	
//	/***
//	 * Applies the operator with the given args to the region defined by 
//	 * the bounding volume.
//	 * 
//	 * Updates all prevalent chunks. Note this should not apply the operation
//	 * but instead update all prevelents chunk's "PendingResults"
//	 * 
//	 * @param operator
//	 */
//	public void applyOperation(CSGOperations operator, BoundingVolume region, int level, VoxelExtractor... additionalArgs);
	
	/***
	 * Sets the chunk requester for a particular chunk.
	 * returns true if the chunk isn't already registered 
	 */
	public boolean registerChunkRequester(Chunk c, ChunkRequester ce);
	
	/***
	 * Unregisters tracking of chunk
	 * @param ce
	 */
	public void unregisterChunkRequester(Chunk c);
	
	/***
	 * Returns list of chunks that are associated with a chunk requester
	 * @return
	 */
	public Chunk[] getTrackedChunks();
	
	/***
	 * returns chunk associated to the given index.
	 * If chunk has not been created returns null;
	 * @param cx
	 * @param cy
	 * @param cz
	 * @return
	 */
	public Chunk getChunk(int cx, int cy, int cz);
	
	/***
	 * dimensions of a chunk
	 * @return
	 */
	public Vector3f getChunkSize();
}

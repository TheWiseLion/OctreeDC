package VoxelSystem.meshing.lod;

import java.util.ArrayList;
import java.util.List;

import VoxelSystem.Misc.CameraInfo;
import VoxelSystem.SparseData.ChunkRequester;
import VoxelSystem.SparseData.SparseInterface;
import VoxelSystem.SparseData.ChunkData.Chunk;
import VoxelSystem.SparseData.controller.BasicLODController;
import VoxelSystem.SparseData.controller.LODController;
import VoxelSystem.VoxelMaterials.MaterialRegistry;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/***
 * This class gets update notifications from
 * the data layer when there has been a change in
 * one of the meshes that it is currently tracking.
 *
 */
public class VoxelMeshing implements ChunkRequester{
	//List of chunks that needed to be re-rendered
	//update camera info
	//re-mesh operation with a given about run time
	
//	BoundingBox bb;
	CameraInfo cI;
	LODController lc;
	private Vector3f lastProcessedPos;
//	PriorityQueue<Chunk> dirtyChunks;//chunks that need to remeshed
	List<Chunk> dirtyChunks;
	public VoxelMeshing(){
		cI = new CameraInfo();
		lc = new BasicLODController(.00014f, 1f);
		dirtyChunks = new ArrayList<Chunk>();
		 lastProcessedPos = new Vector3f();
	}
	
	public void update(SparseInterface sdi, Node root){
		
		Vector3f currentPos = cI.getPosition(new Vector3f());
		if(currentPos.subtract(lastProcessedPos).lengthSquared() > 100){
			lastProcessedPos.set(currentPos);
			Chunk currentlyTrackedChunks[] = sdi.getTrackedChunks();
			BoundingBox bb = new BoundingBox();
			bb = cI.getViewBox(bb);
			
			for(Chunk c : currentlyTrackedChunks){
				if(!c.getChunkContents().intersects(bb)){
					c.getRenderNode().deleteMesh(root);
	//				sdi.unregisterChunkRequester(c);
				}
			}
			
			//Get Overlapping chunks
			List<Chunk> chunksToTrack = sdi.getIntersecting(bb);
			for(Chunk c : chunksToTrack){
				//Register all overlapping chunks
				//if newly tracked node mark dirty?
				sdi.registerChunkRequester(c, this);
				dirtyChunks.add(c);
			}
		}
	
	}
	
	public void updateMeshes(SparseInterface sdi, MaterialRegistry mr, Node root){
		for(Chunk c : dirtyChunks){
			c.getRenderNode().genCell(c, sdi, lc);
		}
		
		for(Chunk c : dirtyChunks){
			c.getRenderNode().genEdges(c, sdi, lc);
		}
		
		
		for(Chunk c : dirtyChunks){
			c.getRenderNode().meshTriangles(c, sdi, lc, mr, root);
		}
		dirtyChunks.clear();
	}
	
	public CameraInfo getCameraInfo(){
		return this.cI;
	}
	
	@Override
	public synchronized void update(Chunk changedNode) {
		dirtyChunks.add(changedNode);
	}

		
}

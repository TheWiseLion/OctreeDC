package VoxelSystem.SparseData;

import java.util.LinkedHashSet;
import java.util.List;

import VoxelSystem.Misc.CameraInfo;
import VoxelSystem.Misc.ChunkMeshManager;
import VoxelSystem.SparseData.ChunkData.Chunk;
import VoxelSystem.SparseData.ChunkData.RenderNode;
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
public class VoxelMesher implements ChunkRequester{
	//List of chunks that needed to be re-rendered
	//update camera info
	//re-mesh operation with a given about run time
	
//	BoundingBox bb;
	CameraInfo cI;
	
	LinkedHashSet<Chunk> dirtyChunks;//chunks that need to remeshed
	private boolean mustRegen = true;
	public VoxelMesher(Vector3f viewDistance, float fallOffSpeed){
		
		cI = new CameraInfo();
		cI.viewDistance = new Vector3f(viewDistance);
		cI.position = new Vector3f();
		dirtyChunks = new LinkedHashSet<Chunk>();
	}
 	
	public void updateCameraInfo(Vector3f cameraPos, SparseInterface sdi, Node root){
		
//		System.out.println(""+cameraPos.subtract(cI.position).lengthSquared() +" vs "+cI.recomputeThreshold*cI.recomputeThreshold);
		//if significant change in position:
		if(cameraPos.subtract(cI.position).lengthSquared() > cI.recomputeThreshold*cI.recomputeThreshold || mustRegen){
			cI.position.set(cameraPos);
			BoundingBox bb = new BoundingBox();
			bb.setCenter(cI.position); //View Box
			bb.setXExtent(cI.viewDistance.x/2f);
			bb.setYExtent(cI.viewDistance.y/2f);
			bb.setZExtent(cI.viewDistance.z/2f);
			
			
			
			//Unregister nodes no longer needed
			Chunk currentlyTrackedChunks[] = sdi.getTrackedChunks();
			for(Chunk c : currentlyTrackedChunks){
				if(!c.getChunkContents().intersects(bb)){
					deleteRenderNode(c.getRenderNode(), root);
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
		mustRegen = false;
		
	}
	
	public void update(MaterialRegistry mr, SparseInterface sdi, Node root){
		//Process Dirty List
			//if dirtyChunk in view distance...
		for(Chunk c : dirtyChunks){
			recomputeVertices(c, sdi, mr);
		}
		
		for(Chunk c : dirtyChunks){
			recomputeMeshes(c, sdi, mr, root);
		}
		
		if(dirtyChunks.size()>0){
			System.out.println("Checked "+dirtyChunks.size());
			dirtyChunks.clear();
		}
	}


	/***
	 * Returns true if a chunk is finished being re-meshed
	 * and can be removed from dirty list
	 * @param c
	 * @return
	 */
	public void recomputeVertices(Chunk c, SparseInterface sdi, MaterialRegistry mr){
		RenderNode rn = c.getRenderNode();
		ChunkMeshManager cm = rn.meshManager;
//		int dirtyNodes = rn.getDirtyCount(node, cI)
		int verts = RenderNode.getLeafCount(c.getChunkContents(), cI);
		
		//If the difference is 30% of the current # of leaves
		int dV = Math.abs(rn.vertexCount-verts);
		if(dV > 0 && dV >= Math.ceil((float)verts*.1f)){ //TODO: move to a function in cameraLOD controller
//			System.out.println("recome main");
			System.out.println("Leaf Count: "+ verts +" vs "+rn.vertexCount +" :: " + Math.abs(rn.vertexCount-verts));
			System.out.println(cI.position);
			rn.meshManager.genVerts(c, sdi, cI, mr);
			rn.vertexCount = verts;
			
		}else if(cm.recomputeInterVerts()){
			
			rn.meshManager.genInterMeshVerts(c, sdi, cI, mr);
		}
	}

	public void recomputeMeshes(Chunk c, SparseInterface sdi, MaterialRegistry mr,Node geoNode){
		RenderNode rn = c.getRenderNode();
		ChunkMeshManager cm = rn.meshManager;
		if(cm.recomputeAllMesh()){
//			System.out.println("recome all");
			rn.meshManager.buildAllMeshes(c, sdi, mr, geoNode);
		}else if(cm.recomputeInterMesh()){
//			System.out.println("recome iterverts");
			rn.meshManager.buildInterChunkMeshes(c, sdi, mr, geoNode);
		}
		
	}
	
	public CameraInfo getCameraInfo(){
		return this.cI;
	}
	
	@Override
	public synchronized void update(Chunk changedNode) {
		dirtyChunks.add(changedNode);
	}
	
	//Helpers:
	private void deleteRenderNode(RenderNode rn, Node root){
		
	}
	
}

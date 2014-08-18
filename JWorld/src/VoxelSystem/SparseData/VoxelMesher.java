package VoxelSystem.SparseData;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import VoxelSystem.VoxelSystemTables.AXIS;
import VoxelSystem.Data.Storage.OctreeNode;
import VoxelSystem.Misc.CameraInfo;
import VoxelSystem.Misc.MeshOutput;
import VoxelSystem.SparseData.ChunkData.Chunk;
import VoxelSystem.SparseData.ChunkData.RenderNode;
import VoxelSystem.SurfaceExtractors.DualContour;
import VoxelSystem.VoxelMaterials.MaterialRegistry;
import VoxelSystem.VoxelMaterials.VoxelMaterial;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
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
	
	BoundingBox bb;
	CameraInfo cI;
	
	LinkedHashSet<Chunk> dirtyChunks;//chunks that need to remeshed
	private boolean mustRegen = true;
	public VoxelMesher(Vector3f viewDistance, float fallOffSpeed){
		bb = new BoundingBox();
		bb.setXExtent(viewDistance.x/2f);
		bb.setYExtent(viewDistance.y/2f);
		bb.setZExtent(viewDistance.z/2f);
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
			bb.setCenter(cI.position); //View Box
			
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
			remeshChunk(c, sdi, mr, root);
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
	public void remeshChunk(Chunk c, SparseInterface sdi, MaterialRegistry mr, Node root){
		RenderNode rn = c.getRenderNode();
		
//		int dirtyNodes = rn.getDirtyCount(node, cI)
		int verts = RenderNode.getLeafCount(c.getChunkContents(), cI);
		
		//If the difference is 30% of the current # of leaves
		int dV = Math.abs(rn.vertexCount-verts);
		if(dV > 0 && dV >= Math.ceil((float)verts*.1f)){ //TODO: move to a function in cameraLOD controller
			
			System.out.println("Leaf Count: "+ verts +" vs "+rn.vertexCount +" :: " + Math.abs(rn.vertexCount-verts));
			System.out.println(cI.position);
			 new HashMap<Integer,MeshOutput>();
			for(Geometry g : rn.meshes){
				root.detachChild(g);
			}
			rn.meshes.clear();
			
			Map<Integer, MeshOutput> meshes = rn.meshManager.genVerts(c, sdi, cI, mr);
			
			for(int i : meshes.keySet()){
				Geometry g = new Geometry();
				Mesh mesh = meshes.get(i).compile(false)[0];
				System.out.println("Triangles: "+mesh.getTriangleCount());
				g.setMesh(mesh);
				VoxelMaterial v = mr.getMaterial(i);
				g.setMaterial(v.jMEMaterial);
				root.attachChild(g);
				rn.meshes.add(g);
			}
//			root.updateGeometricState();
			root.updateModelBound();
			rn.vertexCount = verts;
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

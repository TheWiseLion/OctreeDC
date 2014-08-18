package VoxelSystem.SparseData.ChunkData;

import java.util.ArrayList;
import java.util.List;

import VoxelSystem.Data.Storage.OctreeNode;
import VoxelSystem.Misc.CameraInfo;
import VoxelSystem.Misc.ChunkMeshManager;

import com.jme3.scene.Geometry;

/***
 * Associated with a single chunk.
 * Because the mesh shares edges with adjacent chunks *some* triangles must be cached
 * so that they can easily recalculate the vertex normals and tangents.
 * 
 * Note this manages all the interchuk meshes (meshes shared between different chunks)
 */
public class RenderNode {
	public List<Geometry> meshes = new ArrayList<Geometry>();
	public int vertexCount = 0; //vertex count from last construction
	public ChunkMeshManager meshManager;
	public OctreeNode node;
	
	public RenderNode(){
		meshManager = new ChunkMeshManager();
	}
	
	/***
	 * Returns number of vertices based on camera information
	 * @param node
	 * @param cI
	 * @return
	 */
	public static int getLeafCount(OctreeNode node, CameraInfo cI){
		if(node.isLeaf() || cI.shouldBeLeaf(node)){
			if(node.getIsopoint()!=null){
				return 1;
			}else{
				return 0;
			}
		}else{
			OctreeNode [] children = node.getChildren();
			int sum = 0;
			for(OctreeNode n : children){
				sum += getLeafCount(n,cI);
			}
			return sum;
		}
	}
	
	/***
	 * Returns number of dirty leaves based on camera information
	 * @param node
	 * @param cI
	 * @return
	 */
	public int getDirtyCount(CameraInfo cI){
		return 0;
	}
	
	
	
}

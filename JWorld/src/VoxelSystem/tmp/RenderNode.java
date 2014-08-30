package VoxelSystem.tmp;

import VoxelSystem.Data.Storage.OctreeNode;
import VoxelSystem.SparseData.controller.LODController;
import VoxelSystem.VoxelMaterials.MaterialRegistry;
import VoxelSystem.meshing.lod.VoxelMesh;
import VoxelSystem.threading.GeometryManager;

import com.jme3.scene.Node;

public abstract class RenderNode {
	private OctreeNode data;
	
	public RenderNode(OctreeNode data){
		this.data = data;
	}
	
	/***
	 * Note:
	 * When adjacent meshes are altered, normals and other such data *may* need to be recomputed.
	 */
	public abstract void genCell(OctreeNode node, AdaptiveMeshInterface ami, LODController lod);
	
	/***
	 * When adjacent meshes are altered, normals and other such data *may* need to be recomputed.
	 */
	public abstract void genEdges(OctreeNode node, AdaptiveMeshInterface ami, LODController lod);
	
	/***
	 * When adjacent meshes are altered, normals and other such data *may* need to be recomputed.
	 */
	public abstract void meshTriangles(OctreeNode node, AdaptiveMeshInterface ami, LODController lod,  MaterialRegistry mr, GeometryManager gm);
	
	/***
	 * Notification from an adjacent Mesh 
	 * @param dx - 
	 * @param dy
	 * @param dz
	 * @param rn
	 */
	public abstract void updateFromAdjacentNode(int dx, int dy, int dz, RenderNode rn);
	public abstract void deleteMesh(GeometryManager gm);
	
	public OctreeNode getRepresentedData(){
		return data;
	}
	
	
}

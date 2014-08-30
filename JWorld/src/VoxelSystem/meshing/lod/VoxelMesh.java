package VoxelSystem.meshing.lod;

import VoxelSystem.SparseData.SparseInterface;
import VoxelSystem.SparseData.ChunkData.Chunk;
import VoxelSystem.SparseData.controller.LODController;
import VoxelSystem.VoxelMaterials.MaterialRegistry;

import com.jme3.scene.Node;

/***
 * Associated with a single chunk.
 * Because the mesh shares edges with adjacent chunks *some* triangles must be cached
 * so that they can easily recalculate the vertex normals and tangents.
 * 
 * Note this manages all the interchuk meshes (meshes shared between different chunks)
 */
public interface VoxelMesh {
	/***
	 * When adjacent meshes are altered, normals and other such data *may* need to be recomputed.
	 */
	public abstract void genCell(Chunk node, SparseInterface si, LODController lod);
	
	/***
	 * When adjacent meshes are altered, normals and other such data *may* need to be recomputed.
	 */
	public abstract void genEdges(Chunk node, SparseInterface si, LODController lod);
	
	/***
	 * When adjacent meshes are altered, normals and other such data *may* need to be recomputed.
	 */
	public abstract void meshTriangles(Chunk node, SparseInterface si, LODController lod,  MaterialRegistry mr, Node root);
	
	/***
	 * Notification from an adjacent Mesh 
	 * @param dx - 
	 * @param dy
	 * @param dz
	 * @param rn
	 */
	public abstract void updateFromAdjacentNode(int dx, int dy, int dz, VoxelMesh rn);
//	public abstract List<Geometry> getData();
	public abstract void deleteMesh(Node root);
	


}

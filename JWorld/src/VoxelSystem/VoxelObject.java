package VoxelSystem;

import VoxelSystem.Misc.BasicCollapsePolicy;
import VoxelSystem.SparseData.SparseChunks;
import VoxelSystem.VoxelMaterials.MaterialRegistry;
import VoxelSystem.meshing.lod.VoxelMeshing;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/***
 * Contains a sparse tree of octree's
 * Mesh level of detail changes based on camera position 
 * and other parameters
 *
 */
public class VoxelObject {
	
	SparseChunks voxelData;
	VoxelMeshing meshData;
	Node meshCollection;
	MaterialRegistry materials;
	
	
	public VoxelObject(float miniumVoxelSize, float maxVoxelSize){
		voxelData = new SparseChunks(miniumVoxelSize, maxVoxelSize,new BasicCollapsePolicy(.00005f));
		meshData = new VoxelMeshing();
		meshCollection = new Node();
	}
	
	public Node getObjectNode(){
		return meshCollection;
	}
	
	public void update(){
		meshData.update(voxelData, meshCollection);
		voxelData.update();
		meshData.updateMeshes(voxelData, materials, meshCollection);
	}
	
	public VoxelMeshing getMesher(){
		return meshData;
	}
	
	public SparseChunks getVoxelData(){
		return voxelData;
	}
	
	public MaterialRegistry getMaterialRegistry(){
		return materials;
	}
	
	public void setMaterialRegistry(MaterialRegistry materials){
		this.materials = materials;
	}
	
}

package VoxelSystem;

import VoxelSystem.Misc.BasicCollapsePolicy;
import VoxelSystem.SparseData.BasicDataLayer;
import VoxelSystem.SparseData.VoxelMesher;
import VoxelSystem.VoxelMaterials.MaterialRegistry;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/***
 * Contains a sparse tree of octree's
 * Mesh level of detail changes based on camera position 
 * and other parameters
 *
 */
public class VoxelObject {
	
	BasicDataLayer voxelData;
	VoxelMesher meshData;
	Node meshCollection;
	MaterialRegistry materials;
	private boolean test = false;
	
	public VoxelObject(float miniumVoxelSize, Vector3f viewDistance, float LODSpeed){
		voxelData = new BasicDataLayer(miniumVoxelSize, new BasicCollapsePolicy(.00025f));
		meshData = new VoxelMesher(viewDistance, LODSpeed);
		meshCollection = new Node();
	}
	
	public Node getObjectNode(){
		return meshCollection;
	}

	public void update(Vector3f camera){
		meshData.updateCameraInfo(camera, voxelData, meshCollection);
//		if(!test){
			voxelData.update();
//			test = true;
//		}
		meshData.update(materials, voxelData, meshCollection);
	}
	
	public VoxelMesher getMesher(){
		return meshData;
	}
	
	public BasicDataLayer getVoxelData(){
		return voxelData;
	}
	
	public MaterialRegistry getMaterialRegistry(){
		return materials;
	}
	
	public void setMaterialRegistry(MaterialRegistry materials){
		this.materials = materials;
	}
	
}

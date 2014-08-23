package VoxelSystem.tmp;

import VoxelSystem.SparseData.controller.LODController;
import VoxelSystem.threading.GeometryManager;

import com.jme3.material.Material;


public interface AdaptiveMeshInterface {
	//Needs to provide number of vertices at different levels
	//Needs to provide neighbors (face/edge proc)
	//Needs to quickly be able to clean mesh data.
	//Quickly get number of changes in leaf nodes since last meshing.
	
	//TODO: Threads
	public void update(LODController lod, GeometryManager gm, Material Register);
	public RenderNode getNeighbor(int x, int y,int z, RenderNode rn);
	
}

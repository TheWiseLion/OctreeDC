package VoxelSystem.Misc;

import VoxelSystem.VoxelSystemTables.AXIS;
import VoxelSystem.Data.Storage.OctreeNode;

public interface MeshInterface {
	public void addTriangle(OctreeNode v1, OctreeNode v2, OctreeNode v3, int material, AXIS axis);
}

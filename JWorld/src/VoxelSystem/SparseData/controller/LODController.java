package VoxelSystem.SparseData.controller;

import VoxelSystem.Data.Storage.OctreeNode;
import VoxelSystem.Misc.CameraInfo;

/***
 * This is the controller class for level of detail.
 *
 */
public interface LODController {
	/***
	 * Returns true if the node should be a leaf based on level 
	 * of detail.
	 * @param node
	 * @return true if node should be a leaf
	 */
	public boolean shouldBeLeaf(OctreeNode node);
	
	/***
	 * @param deltaVerticies - the difference in vertices if meshing is performed
	 * @param c - the node being considered for re-meshing 
	 * @return
	 */
	public boolean shouldRemesh(int verts, int deltaVerts, OctreeNode c);
	
	/***
	 * Called when camera info is changed;
	 * @param cI
	 */
	public void update(CameraInfo cI);
}

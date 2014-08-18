package VoxelSystem.Data.VoxelExtraction;

import VoxelSystem.Data.VoxelCube;
import VoxelSystem.Data.Storage.OctreeNode;

import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingVolume;
import com.jme3.math.Vector3f;

/***
 * This interface supports *fast* extraction of voxel data.
 * Extraction can only exist directly on the Nodes of the tree.
 * This interface supports super sampling 
 * 
 */
public class StrictOctreeExtractor implements VoxelExtractor{
	OctreeNode root;
	OctreeNode cache1;//Should be parent of cahce2  [64]
	OctreeNode cache2;//Parent of last query result [8]
	BoundingBox bb;
	
	
	public StrictOctreeExtractor(OctreeNode parent){
		this.root = parent;
		this.cache1 = root;
		this.cache2 = root;
		float l = root.getCubeLength();
		bb = new BoundingBox(root.getCorner(),root.getCorner().addLocal(l,l,l));
	}
	
	@Override
	public VoxelCube getVoxels(Vector3f cubeCorner, float len) {
		//Check cache2
			//return
		//Check cache1
			//set new cache 2
			//return
		
		//Perform search from root
		return searchRoot(cubeCorner,len);
	}

	@Override
	public BoundingBox getVolume() {
		return bb;
	}
	
	@Override
	public boolean shouldSplitQuery(Vector3f cubeCorner, float len) {
		if(searchRoot(cubeCorner,len) != null){
			return true;
		}
		return false;
	}
	
	private OctreeNode searchRoot(Vector3f cubeCorner, float len){
		OctreeNode c = root;
		
		while(true){
			if(c.containsCube(cubeCorner)){ //*Could* this node contain the cube we are looking for?
				if(c.getCubeLength() == len){ //This is the cube we want
					return c;
				}else if(c.isLeaf()){ // does the node have children that could contain the cube?
					boolean found = false;
					for(OctreeNode n : c.getChildren()){
						if(n.containsCube(cubeCorner)){
							cache1 = cache2;
							cache2 = c;
							c = n;
							found = true;
							break;
						}
					}
					
					if(!found){
						return null;
					}
					
				}else{
					return null;
				}
				
			}else{
				throw new RuntimeException("Query not aligned to octree");
			}
		}
	}



	

}

package VoxelSystem.Data.Storage;

import VoxelSystem.ExtractorUtils;
import VoxelSystem.VoxelSystemTables;
import VoxelSystem.Data.CollapsePolicy;
import VoxelSystem.Data.EditableVoxelCube;
import VoxelSystem.Data.VoxelCube;
import VoxelSystem.Data.VoxelExtraction.VoxelExtractor;

import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingVolume;
import com.jme3.math.Vector3f;


/***
 * This structure contains data useful for 
 * both rendering and voxel storage purposes.
 *
 */
public abstract class OctreeNode implements EditableVoxelCube{
	//Cache Results from Dual Contour:
	private Vector3f isopoint;
	private float isopointError;
	
	private float geometricError;
	//Octree Data
	protected final Vector3f minBound;
	protected float length; //cube length
	protected int depth;
	protected boolean topologicallySafe;
	
	protected boolean dirty;//Voxel data of children have changed
	protected OctreeNode[] children;

	
	public OctreeNode(Vector3f lowestCorner,float length, int depth){
		this.minBound = lowestCorner;
		this.length = length;
        this.depth = depth;
        isopoint = new Vector3f();
        geometricError = 0;
        topologicallySafe = true;
        isopointError = -1;
    }

    
    /**
     * Creates the eight children of this node.
     */
    public void subdivide(){
      float newLen = length/2f;
      children = new OctreeNode[8];
      children[0] = produceChild(getChildCorner(0, new Vector3f()), newLen, depth + 1);
      children[1] = produceChild(getChildCorner(1, new Vector3f()), newLen, depth + 1);
      children[2] = produceChild(getChildCorner(2, new Vector3f()),newLen, depth + 1);
      children[3] = produceChild(getChildCorner(3, new Vector3f()), newLen, depth + 1);
      children[4] = produceChild(getChildCorner(4, new Vector3f()), newLen, depth + 1);
      children[5] = produceChild(getChildCorner(5, new Vector3f()), newLen, depth + 1);
      children[6] = produceChild(getChildCorner(6, new Vector3f()), newLen, depth + 1);
      children[7] = produceChild(getChildCorner(7, new Vector3f()), newLen, depth + 1);
    }

    public abstract OctreeNode produceChild(Vector3f lowestCorner,float length, int depth);
    
    
    ///GETTERS///
    /**
	 * @return the depth
	 */
	public int getDepth() {
		return depth;
	}

	/**
	 * @return the children
	 */
	public OctreeNode[] getChildren() {
		return children;
	}

	/**
	 * @return the minBound
	 */
	public Vector3f getMinBound() {
		return minBound;
	}
	
	public boolean isLeaf(){
		return children == null;
	}
	
	/**
	 * if lower then zero then there is no isopoint
	 * hence no such geometric error exists. 
	 * @return
	 */
	public float getGeometricError(){
		return geometricError;
	}
	
	public Vector3f getIsopoint(){
		if(isopointError < 0){
			return null;
		}else{
			return this.isopoint;
		}
	}
	
	public boolean topographicallySafe(){
		return this.topologicallySafe;
	}

	public void setSafty(boolean safty){
		this.topologicallySafe = safty;
	}
	
	/**
	 * Returns true iff the defining point of cube could be contained by this node.
	 */
	public boolean containsCube(Vector3f point) {
		return point.x >= minBound.x 
				&& point.y >= minBound.y
				&& point.z >= minBound.z 
				&& point.x < minBound.x + length
				&& point.y < minBound.y + length
				&& point.z < minBound.z + length;
	}

    /** Returns the length of the cube's diagonal. */
    public float getCubeDiagonal(){
    	//sqrt(3) * lenght 
        return 1.732050f*this.length;
    }
    
    /***
     * Returns nearest OctreeNode at target level or at leaf.
     * @param target - lowest corner of the cube (index 0)
     */
    public OctreeNode getCube(Vector3f target, int targetLevel){
    	if(this.depth == targetLevel || isLeaf()){
    		//get nearest.
    		throw new RuntimeException("TODO.");
    	}else{
    		//Choose correct child
    		throw new RuntimeException("TODO.");
    	}
    }
    
    public boolean intersects(BoundingVolume bv){
    	BoundingBox bb = new BoundingBox(this.minBound,this.minBound.add(length,length,length));
		return bb.intersects(bv);
    }
    
    public float intersects(OctreeNode bv){
    	BoundingBox bb1 = new BoundingBox(this.minBound,this.minBound.add(length,length,length));
    	BoundingBox bb2 = new BoundingBox(bv.minBound,bv.minBound.add(bv.length,bv.length,bv.length));
//    	System.out.println(bb1.getCenter().subtract(bb2.getCenter()).length());
		return bb1.getCenter().subtract(bb2.getCenter()).length();
    }
    
    /***
     * True if data of children has been changed.
     * Here purely for the purpose of making re-meshing easier
     * when modifications have been made.
     */
    public boolean isDirty(){
    	return dirty;
    }
    
    /**
     * Sets the dirty flag to false
     */
    public void markClean(){
    	dirty = false;
    }
    
    /***
     * Recomputes value of isopoint.
     * If node is a leaf node geometric error is also set.
     */
    public void recomputeIsopoint(){
    	float error = ExtractorUtils.surfaceContour(this, this.getCubeDiagonal()/1000f, this.isopoint);
    	this.isopointError = error;
    	
    	
    	if(error < 0){
    		error = 0;
    	}
    	
    	if(isLeaf()){
    		this.geometricError = error;
    	}
    }
    
    public BoundingBox getBox(){
    	return new BoundingBox(this.minBound, this.minBound.add(length,length,length));
    }
    
    /// 
    /// OPERATIONS
    ///
    
    
    /***
     * Returns true if there has been a change in one of the leaves.
     * Appropriate nodes will be collapsed and marked dirty.
     * Should only be called if it actually intersects the the volume of the given
     * extractor
     * 
     * @param ve
     * @param maxLevel
     * @return true if there has been change in a leaf node
     */
    public boolean extractVoxelData(VoxelExtractor ve, int maxLevel,  CollapsePolicy cp){
    	
    	//TODO: if intersects.....
		if (maxLevel > this.depth) {
			// check if *can* query children
			boolean qChild = ve.shouldSplitQuery(getCorner(),getCubeLength());

			if (qChild) {// Query children
				boolean change = false; //True if a change has occurred
				boolean newChildren = false; //True if newly subdivided
				if(isLeaf()){ // make sure its a leaf
					newChildren = true; //new children! Check for collapse
					this.subdivide();
				}
				
				float childError = 0f;
				//TODO: Consider case- all children are leaves. All children are homogeneous. 
				for(int i =0; i< 8; i++){
					boolean nodeChange = false;
					if(children[i].intersects(ve.getVolume())){
						nodeChange =  children[i].extractVoxelData(ve, maxLevel, cp);
						change = change || nodeChange;
						childError += children[i].getGeometricError();
					}
					children[i].dirty = nodeChange || newChildren; //should still mark dirty...
				}
				
				change = change || newChildren;
//				System.out.println(""+childError +" vs "+change);
				//PREFORM COLLAPSE (if there has been a change)
				if(change){
					this.topologicallySafe = cp.collapse(getChildren(), this);
					this.recomputeIsopoint();
					//We have error of children and error of this nodes isopoint
					//Set geometric error to which ever is larger.
					if(childError > this.isopointError){
						this.geometricError = childError;
					}else{
						this.geometricError = this.isopointError;
					}

					if(this.topologicallySafe && this.geometricError <= cp.getMiniumGeometricError()){
						children = null;
					}
				}
				
				//TODO: edge case ---> produce children but re-collapses to same value. 
				//Dirty flag shouldn't be marked
				this.dirty = change;
				
				return change;
			}

		}
    	
		//IF LEAF
		this.topologicallySafe = true; //always true for leaves...
		children = null;
		boolean change = this.set(ve.getVoxels(new Vector3f(getCorner()), getCubeLength()));
		if(change){
			this.dirty = true;
		}
		return change;
    }

    /****
     * Sets value of this node to values of 
     * the given VoxelCube. If there is a change
     * in value new isopoint and geometric errors
     * are computed.
     * @param vc
     * @return
     */
    public boolean set(VoxelCube vc){
    	boolean change = false;
    	
    
    	//Set Types
    	for(int i=0; i < 8; i++){
    		boolean c = this.setType(i,vc.getType(i));
    		change = change || c;
    	}
    	
    	int edgeInfo = VoxelSystemTables.getEdgeFromMaterials(this);
    	//Set Edges
    	for(int i=0; i < 12; i++){
    		if ((edgeInfo & (1 << i)) == 0){
				continue;
			}
    		
    		boolean c = this.setIntersection(i, vc.getIntersection(i));
    		change = change ||c;
    		
    		c = this.setNormal(i, vc.getNormal(i, new Vector3f()));
    		change = change || c;
    	}
    	
    	if(change){
    		//RECOMPUTE ISOPOINT + GEO
    		recomputeIsopoint();
    	}
    	
    	
    	return change;
    }
    
   /// Private Helpers ///
    private Vector3f getChildCorner(int c, Vector3f v){
        float newLen = length/2f;
        switch(c){
			case 0:
				return v.set(this.minBound);
			case 1:
				return v.set(minBound.x + newLen, minBound.y, minBound.z);
			case 2:
				return v.set(minBound.x, minBound.y + newLen, minBound.z);
			case 3:
				return v.set(minBound.x + newLen, minBound.y + newLen, minBound.z);
			case 4:
				return v.set(minBound.x, minBound.y, minBound.z + newLen);
			case 5:
				return v.set(minBound.x + newLen, minBound.y, minBound.z + newLen);
			case 6:
				return v.set(minBound.x, minBound.y + newLen, minBound.z + newLen);
			case 7:
				return v.set(minBound.x + newLen, minBound.y + newLen, minBound.z + newLen);
        	
        }
        throw new RuntimeException("Invalid Query. Child " + c +" it not a valid child");
    }
    
}
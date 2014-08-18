package VoxelSystem.Operators;


import VoxelSystem.Data.VoxelExtraction.VoxelExtractor;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

/***
 * The range of DensityVolume and HermiteExtractors are often bounded and
 * thus aren't able to be queried outside their bounding boxes. To make the writing of operators simpler
 * I've constructed this class to deal with the breaking down of the different edge cases.
 * 
 * It breaks down all 6 basic cases for any operation between 2 volumes.
 * Case 1 - Query points both lie in side intersection volume
 * Case 2 - One point lies on the intersection
 * Case 3 - One point in each volume (neither in intersection)
 * Case 4 - Edge lies in (or on) one of the volumes
 * Case 5 - Neither query points lie in either of the volumes
 */
public abstract class OperatorBase implements VoxelExtractor{
	protected VoxelExtractor v1;
	protected VoxelExtractor v2;
	protected BoundingBox bb1;
	protected BoundingBox bb2;
	protected boolean intersect;
	protected BoundingBox intersection;
	protected boolean only1;
	protected boolean only2;
	
	public OperatorBase(VoxelExtractor d1,VoxelExtractor d2){
		bb1 = d1.getVolume();
		bb2 = d2.getVolume();
		
		this.v1 = d1;
		this.v2 = d2;
		
		intersection = new BoundingBox();
		Boolean b = CSGHelpers.getIntersection(bb1, bb2, intersection);
		if(b == null){
			intersect = true;
			intersection = null;
		}else if(b == false){
			intersect = false;
		}else{
			intersect = true;
		}
	}
	
	

}

package VoxelSystem.Operators;

import VoxelSystem.Data.VoxelCube;
import VoxelSystem.Data.VoxelExtraction.VoxelExtractor;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;

public class CSGHelpers {
	public final static float CLAMPVAL=1.0f;
	
	/***
	 * Can be infinite -> returns null
	 * Can be finite -> returns true ['output' will be modified]
	 * May not intersect -> returns false
	 */
	public static final Boolean getIntersection(BoundingBox bv1,BoundingBox bv2,BoundingBox output){
		Vector3f min = new Vector3f();
		Vector3f max = new Vector3f();
		boolean hasVolume1 = bv1 != null;
		boolean hasVolume2 = bv2 != null;
		
		if(hasVolume1 && hasVolume2){
			if(!bv1.intersects(bv2)){
				return false;
			}
			min = bv1.getMin(min);
	    	max = bv1.getMax(max);
	    	min.maxLocal(bv2.getMin(new Vector3f()));
	    	max.minLocal(bv2.getMax(new Vector3f()));
	    	output.setMinMax(min, max);
	    	return true;
		}else if(hasVolume1){
			output.setMinMax(bv1.getMin(min), bv1.getMax(max));
			return true;
		}else if(hasVolume2){
			output.setMinMax(bv2.getMin(min), bv2.getMax(max));
			return true;
		}else{
			return null;
		}
	}
	
	/***
	 * Returns the minimal union between 2 bounding volumes.
	 */
	public static final BoundingBox getUnion(BoundingBox bv1,BoundingBox bv2){
		if(bv1 == null || bv2 == null){
			return null;
		}
		Vector3f min = bv1.getMin(new Vector3f());
    	Vector3f max = bv1.getMax(new Vector3f());
    	min.minLocal(bv2.getMin(new Vector3f()));
    	max.maxLocal(bv2.getMax(new Vector3f()));
    	return new BoundingBox(min,max);
	}
	
	public static float clamp(float val) {
	    return Math.max(-CLAMPVAL, Math.min(CLAMPVAL, val));
	}
	
	public static float clamp(float val,float clampV) {
	    return Math.max(-clampV, Math.min(clampV, val));
	}
	
	public static class UnionExtactor extends OperatorBase{
		boolean overWrite;
		public UnionExtactor(VoxelExtractor d1, VoxelExtractor d2,boolean overWrite) {
			super(d1, d2);
			this.overWrite = overWrite;
		}

		@Override
		public BoundingBox getVolume() {
			return CSGHelpers.getUnion(this.bb1,this.bb2);
		}

		private int getType(int t1, int t2, boolean overwrite){
			if(overwrite){
				if(t2 != -1 ){
					return t2;
				}else{
					return t1;
				}
			}else{
				if(t1 != -1){
					return t1;
				}else{
					return t2;
				}
			}
		}

		@Override
		public VoxelCube getVoxels(Vector3f cubeCorner, float len) {
			
			return null;
		}

		@Override
		public boolean shouldSplitQuery(Vector3f cubeCorner, float len) {
			
			return false;
		}

	}
	
	
		
		
	}

	

//}

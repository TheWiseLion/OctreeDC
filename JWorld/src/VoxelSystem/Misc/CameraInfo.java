package VoxelSystem.Misc;

import VoxelSystem.Data.Storage.OctreeNode;

import com.jme3.math.Vector3f;

public class CameraInfo {
	public Vector3f position;
	public Vector3f viewDistance;
	float baseError;
	float fallOffSpeed;
	public float recomputeThreshold = 10f;
	// ErrorNode = max(Distance/fallOffSpeed, 1) * baseError
	
	public boolean shouldBeLeaf(OctreeNode n){
		float f = n.getCubeLength()/2f;
		Vector3f v = n.getCorner().add(f, f, f).subtract(position);
		float dSquard = v.lengthSquared() * v.lengthSquared();

		float error = Math.min(dSquard / 1000f * .0001f, .1f);
//		// .001 every 100 meters
//
//		if(!n.isLeaf()){
//		 System.out.println("Error Check: "+error+" vs "+n.getGeometricError()+" vs "+dSquard+" vs "+dSquard/1000f);
//		}
		
//		if (error > n.getGeometricError() && n.topographicallySafe()) {
//			return true;
//		} else {
			return false;
//		}
		
//			int maxDepth = (int) Math.max(1, 8 - dSquard/10000f);
////			System.out.println(maxDepth);
//			if(n.getDepth() > maxDepth && n.topographicallySafe()){
//				return true;
//			}else{
//				return false;
//			}
	}
	
	
}

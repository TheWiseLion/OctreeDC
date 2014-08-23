package VoxelSystem.SparseData.controller;

import VoxelSystem.Data.Storage.OctreeNode;
import VoxelSystem.DensityVolumes.Shapes.VolumeShape;
import VoxelSystem.Misc.CameraInfo;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;

/****
 * Based on the following function:
 * Minimum Geometric Error = coefficient * distance ^ exponent
 *
 */
public class BasicLODController implements LODController{
	float coefficient;
	float exponent;
	Vector3f position = new Vector3f();
	public BasicLODController(float coefficient, float exponent){
		this.coefficient = coefficient;
		this.exponent = exponent;
	}
	
	public float getCoefficient(){
		return coefficient;
	}
	
	public float getExponent(){
		return exponent;
	}
	
	public void setExponent(float exponent){
		this.exponent = exponent;
	}
	
	public void setCoefficient(float coefficient){
		this.coefficient = coefficient;
	}
	
	@Override
	public boolean shouldBeLeaf(OctreeNode n) {
		float f = n.getCubeLength()/2f;
//		Vector3f v = n.getCorner().add(f, f, f).subtract(position);
		float dist = distanceSQFromCube(position,n.getBox());//v.lengthSquared() * v.lengthSquared();
//		dist = dist*dist;
		float error = Math.min(dist / 1000f * .0001f, .1f);
//		if (error > n.getGeometricError() && n.topographicallySafe()) {
//			return true;
//		} else {
			return false;
//		}
//		return false;
	}

	@Override
	public boolean shouldRemesh(int verts, int deltaVerts, OctreeNode c) {
		if(deltaVerts >= Math.ceil(verts*.2)){
			return true;
		}else{
			return false;
		}
		
	}

	@Override
	public void update(CameraInfo cI) {
		cI.getPosition(position);
	}

	private float distanceSQFromCube(Vector3f pos, BoundingBox bb){
		Vector3f v = new Vector3f(pos);
		Vector3f min = bb.getMin(new Vector3f());
		Vector3f max = bb.getMax(new Vector3f());
		
		if(!VolumeShape.contains(bb, v)){//outside box
			//Get nearest point on box:
			if (v.x < min.x) {
				v.x = min.x;
			} else if (v.x > max.x) {
				v.x = max.x;
			}

			if (v.y < min.y) {
				v.y = min.y;
			} else if (v.y > max.y) {
				v.y = max.y;
			}

			if (v.z < min.z) {
				v.z = min.z;
			} else if (v.z > max.z) {
				v.z = max.z;
			}
			return -v.subtract(pos.x, pos.y, pos.z).lengthSquared();
		} else {
			// Get distance to closest face
			float d = Math.min(v.x - min.x, max.x - v.x);
			d = Math.min(d, Math.min(v.y - min.y, max.y - v.y));
			d = Math.min(d, Math.min(v.z - min.z, max.z - v.z));
			return d*d;
		}
	}
}

package VoxelSystem.meshing;

import com.jme3.math.Vector3f;

public class Triangle {
	public Triangle(Vector3f v1, Vector3f v2, Vector3f v3, int type) {
		super();
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
		this.type = type;
	}
	public final Vector3f v1, v2, v3;
	public final int type;
	private Vector3f normal;
	
	public Vector3f getNormal(){
		//Cache since it may be called often...
		if(normal == null){
			normal = computeNormal(v1, v2, v3);
		}
		
		return new Vector3f(normal);
	}
	
	private static Vector3f computeNormal(Vector3f v1, Vector3f v2, Vector3f v3) {
		Vector3f e1 = new Vector3f(v2);
		Vector3f e2 = new Vector3f(v3);
		e1 = e1.subtract(v1);
		e2 = e2.subtract(v1);
		return e1.cross(e2).normalize();
	}
	
}

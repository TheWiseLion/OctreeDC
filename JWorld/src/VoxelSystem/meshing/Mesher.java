package VoxelSystem.meshing;

import java.util.List;
import java.util.Map;

import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;

public interface Mesher {
	
	public void getTriangles(Vector3f v, List<Triangle> triangleList);
	public void addTriangle(Triangle t);
	public Map<Integer,Mesh> compile(List<Mesher> supportingTriangles);
}

package VoxelSystem.meshing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;

public class BasicMesher implements Mesher{
	public static final float threshold = .821f;
	private Map<Vector3f, List<Triangle>> vToT; //vertex to triangle
	private Set<Integer> types;
	
	public BasicMesher(){
		vToT = new HashMap<Vector3f,List<Triangle>>();
		types = new HashSet<Integer>();
	}
	
	@Override
	public void getTriangles(Vector3f v, List<Triangle> triangleList) {
		List<Triangle> triangles = vToT.get(v);
		if(triangles != null){
			triangleList.addAll(triangles);
		}
	}

	@Override
	public void addTriangle(Triangle t) {
		add(t.v1,t);
		add(t.v2,t);
		add(t.v3,t);
		types.add(t.type);
	}

	
	
	@Override
	public Map<Integer, Mesh> compile(List<Mesher> supportingTriangles) {
		
		
		
		
		return null;
	}
	
	private void add(Vector3f v, Triangle t){
		List<Triangle> triangles = vToT.get(v);
		if(triangles == null){
			triangles = new ArrayList<Triangle>();
			vToT.put(v, triangles);
		}
		triangles.add(t);
	}
}

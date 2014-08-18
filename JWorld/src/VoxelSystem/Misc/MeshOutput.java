package VoxelSystem.Misc;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

public class MeshOutput {
	
	public final Map<Integer,Vector3f> iToV;
	public final Map<Vector3f,Integer> vToI;
	public final List<Integer> triangles; //TODO: Int buffer
	public final List<Vector3f> verticies;//TODO: float buffer
	public final List<Vector3f> vertexNormals;  
	public final List<Vector3f> vertexTangents;
	public final List<Vector3f> vertexBiTangents;
	
	public MeshOutput(){
		iToV = new HashMap<Integer,Vector3f>();
		vToI = new HashMap<Vector3f, Integer>();
		triangles = new ArrayList<Integer>();
		verticies = new ArrayList<Vector3f>();
		vertexNormals = new ArrayList<Vector3f>();
		vertexTangents = new ArrayList<Vector3f>();
		vertexBiTangents = new ArrayList<Vector3f>();
	}
	
	/***
	 * Compiles mesh data. 
	 * if debug flag is set true tangent/normal/bitangent meshes will also be generated
	 * @param compileDebugInfo
	 * @return
	 */
	public Mesh[] compile(boolean compileDebugInfo){
		IntBuffer indicies = BufferUtils.createIntBuffer(this.triangles.size());
		FloatBuffer verticies = BufferUtils.createFloatBuffer(this.verticies.size()*3);
		FloatBuffer normals = BufferUtils.createFloatBuffer(this.vertexNormals.size()*3);
		FloatBuffer tangents = BufferUtils.createFloatBuffer(this.vertexTangents.size()*4);
		
		//TODO: Correct errors related to shared vertex
		
		for(Vector3f v : vertexNormals){
			
			v.normalizeLocal();
			normals.put(v.x);
			normals.put(v.y);
			normals.put(v.z);
		}
		
		for(int i =0; i< vertexTangents.size(); i++){
			Vector3f n = this.vertexNormals.get(i);
			
			Vector3f t = vertexTangents.get(i);
			Vector3f b = vertexBiTangents.get(i);
			
			// Calculate handedness
			float handedness = (n.cross(t).dot(b) < 0.0F) ? -1.0F : 1.0F;
			
			// Gram-Schmidt orthogonalize
	        t.set((t.subtract(n.mult(n.dot(t)))).normalize());
	        
			
			tangents.put(t.x);
			tangents.put(t.y);
			tangents.put(t.z);
			tangents.put(handedness);
		
		}
		
		indicies.rewind();
		for(int v : triangles){
			indicies.put(v);
		}
		
		verticies.rewind();
		for(int i =0; i < this.iToV.size();i++){
			Vector3f v = this.iToV.get(i);
			verticies.put(v.x);
			verticies.put(v.y);
			verticies.put(v.z);
		}
		
		if(compileDebugInfo){
			Mesh[] out = new Mesh[]{new Mesh(),new Mesh(),new Mesh(),new Mesh()};
			FloatBuffer extraNormals = BufferUtils.createFloatBuffer(this.vertexNormals.size()*3*2);
			FloatBuffer extraTangents = BufferUtils.createFloatBuffer(this.vertexTangents.size()*3*2);
			FloatBuffer extraBitangents = BufferUtils.createFloatBuffer(this.vertexTangents.size()*3*2);
			//Gen extra info
			for(int i = 0; i < this.vertexTangents.size(); i++){
				Vector3f vv = this.verticies.get(i);
				Vector3f n = this.vertexNormals.get(i);
				Vector3f vf = vertexTangents.get(i);
				Vector3f bi=n.cross(vf).mult(.25f).add(vv);
				Vector3f tan = vf.mult(.25f).add(vv);
				Vector3f norm = n.mult(.25f).add(vv);
				extraTangents.put(vv.x);
				extraTangents.put(vv.y);
				extraTangents.put(vv.z);
				extraNormals.put(vv.x);
				extraNormals.put(vv.y);
				extraNormals.put(vv.z);
				extraBitangents.put(vv.x);
				extraBitangents.put(vv.y);
				extraBitangents.put(vv.z);
				
				extraTangents.put(tan.x);
				extraTangents.put(tan.y);
				extraTangents.put(tan.z);
				
				extraNormals.put(norm.x);
				extraNormals.put(norm.y);
				extraNormals.put(norm.z);

				extraBitangents.put(bi.x);
				extraBitangents.put(bi.y);
				extraBitangents.put(bi.z);
			}
			
			out[0].setBuffer(Type.Index, 3, indicies);
			out[0].setBuffer(Type.Position, 3, verticies);
			out[0].setBuffer(Type.Normal, 3, normals);
			out[0].setBuffer(Type.Tangent, 4, tangents);
			
			out[1].setBuffer(Type.Position,  3, extraTangents);
			out[2].setBuffer(Type.Position,  3, extraNormals);
			out[3].setBuffer(Type.Position,  3, extraBitangents);
			
			return out;
		}else{
			Mesh m = new Mesh();
			m.setBuffer(Type.Index, 3, indicies);
			m.setBuffer(Type.Position, 3, verticies);
			m.setBuffer(Type.Normal, 3, normals);
			m.setBuffer(Type.Tangent, 4, tangents);
			return new Mesh[]{m};
		}
		
		
	}
	
	
	
	public Vector3f windingQuadToTriangle(Vector3f v1,Vector3f v2,Vector3f v3,  boolean support){
		boolean invalid = fuzzyEquals(v1, v2) || fuzzyEquals(v2, v3) || fuzzyEquals(v1, v3);
		if (invalid) {
			return null;
		}

		Vector3f lNormal;
//		Vector2f uv0 = new Vector2f();
//		Vector2f uv1 = new Vector2f();
//		Vector2f uv2 = new Vector2f();
		Vector3f tangent = new Vector3f();
		Vector3f biTangent = new Vector3f();

		lNormal = computeNormal(v1, v2, v3);
		generateTangent(v1, v2, v3, lNormal, tangent, biTangent);
		

		if (support) {
			getTriIndex(v1, lNormal, tangent, biTangent, this);
			getTriIndex(v2, lNormal, tangent, biTangent, this);
			getTriIndex(v3, lNormal, tangent, biTangent, this);
		}else{
			triangles.add(getTriIndex(v1, lNormal, tangent, biTangent, this));
			triangles.add(getTriIndex(v2, lNormal, tangent, biTangent, this));
			triangles.add(getTriIndex(v3, lNormal, tangent, biTangent, this));
		}
		//TODO: splitting of "bad cases" for vertex normals and tangents....
	
		return lNormal;
	}
	
	private static int getTriIndex(Vector3f v, Vector3f normal, Vector3f tangent,Vector3f bitangent, MeshOutput mo) {
		Integer i = mo.vToI.get(v);
		if (i == null) {
			i = mo.verticies.size();
			mo.iToV.put(i, v);
			mo.vToI.put(v, i);
			mo.verticies.add(v);
			mo.vertexNormals.add(new Vector3f(normal));
			mo.vertexTangents.add(new Vector3f(tangent));
			mo.vertexBiTangents.add(new Vector3f(bitangent));
		}else{
			mo.vertexNormals.get(i).addLocal(normal);
			mo.vertexTangents.get(i).addLocal(tangent);
			mo.vertexBiTangents.get(i).addLocal(bitangent);
		}
		
		
		return i;
	}

	private static int generateUV(Vector3f v1, Vector3f normal,Vector2f store) {
		int i = 0;
		if (Math.abs(normal.y) > Math.abs(normal.z)) {
			
			//Y,Z dom -
			
			if (Math.abs(normal.y) > Math.abs(normal.x)) {// y dominate
				if(Math.abs(normal.z) > Math.abs(normal.x)){
					store.set(v1.x, v1.z);
				}else{
					store.set(v1.z, v1.x);
				}
				
			} else {// x dominate
				store.set(v1.y, v1.z);
			}
		} else {
			if (Math.abs(normal.z) > Math.abs(normal.x)) { // z dominant
				store.set(v1.y, v1.x);
			} else { // x dominant y,z
				store.set(v1.y, v1.z);
			}
		}
		return i;
	}

	private static void generateTangent(Vector3f v0, Vector3f v1, Vector3f v2,Vector3f normal, Vector3f tangent, Vector3f bitangent) {
		// Edges of the triangle : postion delta
		Vector2f uv0 = new Vector2f();
		Vector2f uv1 = new Vector2f();
		Vector2f uv2 = new Vector2f();
		int a = generateUV(v0,normal,uv0);
		generateUV(v1,normal,uv1);
		generateUV(v2,normal,uv2);
		
		//Method from:
		//http://www.terathon.com/code/tangent.html
		
		 float x1 = v1.x - v0.x;
	     float x2 = v2.x - v0.x;
	     float y1 = v1.y - v0.y;
	     float y2 = v2.y - v0.y;
	     float z1 = v1.z - v0.z;
	     float z2 = v2.z - v0.z;
	        
	     float s1 = uv1.x - uv0.x;
	     float s2 = uv2.x - uv0.x;
	     float t1 = uv1.y - uv0.y;
	     float t2 = uv2.y - uv0.y;

		float r = 1.0f / (s1 * t2 - s2 * t1);
		
		tangent.set((t2 * x1 - t1 * x2) * r, (t2 * y1 - t1 * y2) * r, (t2 * z1 - t1 * z2) * r);
		bitangent.set((s1 * x2 - s2 * x1) * r, (s1 * y2 - s2 * y1) * r, (s1 * z2 - s2 * z1) * r);
	}

	private static Vector3f computeNormal(Vector3f v1, Vector3f v2, Vector3f v3) {
		Vector3f e1 = new Vector3f(v2);
		Vector3f e2 = new Vector3f(v3);
		e1 = e1.subtract(v1);
		e2 = e2.subtract(v1);
		return e1.cross(e2).normalize();
	}
	
	private static boolean fuzzyEquals(Vector3f v1, Vector3f v2){
		if(v1.subtract(v2).lengthSquared() < .0001f){
			return true;
		}else{
			return false;
		}
	}
	
}
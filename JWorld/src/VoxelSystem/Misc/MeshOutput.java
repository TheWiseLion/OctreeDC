package VoxelSystem.Misc;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;
import com.jme3.util.TangentBinormalGenerator;

public class MeshOutput {
	public static final float threshold = .821f;
	public final Map<Integer,Vector3f> iToV;
	public final Map<Vector3f,Integer> vToI;
	public final List<Integer> triangles; //TODO: Int buffer
	public final List<Vector3f> verticies;//TODO: float buffer
	public final List<Vector3f> vertexNormals;  
//	public final List<Vector3f> vertexTangents;
//	public final List<Vector3f> vertexBiTangents;
	
	public MeshOutput(){
		iToV = new HashMap<Integer,Vector3f>();
		vToI = new HashMap<Vector3f, Integer>();
		triangles = new ArrayList<Integer>();
		verticies = new ArrayList<Vector3f>();
		vertexNormals = new ArrayList<Vector3f>();
//		vertexTangents = new ArrayList<Vector3f>();
//		vertexBiTangents = new ArrayList<Vector3f>();
	}
	
	
	public void add(MeshOutput additionalTriangles){
		List<Integer> triangles = additionalTriangles.triangles;
		List<Vector3f> verticies = additionalTriangles.verticies;
		Vector3f zero = new Vector3f();
		for(int i=0; i< triangles.size(); i+=3){
			Vector3f v1 = additionalTriangles.iToV.get(triangles.get(i));
			Vector3f v2 = additionalTriangles.iToV.get(triangles.get(i+1));
			Vector3f v3 = additionalTriangles.iToV.get(triangles.get(i+2));
			
			this.triangles.add(getTriIndex(v1, zero, zero, zero, this));
			this.triangles.add(getTriIndex(v2, zero, zero, zero, this));
			this.triangles.add(getTriIndex(v3, zero, zero, zero, this));
		}
		
		for(int i=0; i < verticies.size(); i++){
			if(vToI.containsKey(verticies.get(i))){
				int index = vToI.get(verticies.get(i));
				this.vertexNormals.get(index).addLocal(additionalTriangles.vertexNormals.get(i));
//				this.vertexTangents.get(index).addLocal(additionalTriangles.vertexTangents.get(i));
//				this.vertexBiTangents.get(index).addLocal(additionalTriangles.vertexBiTangents.get(i));
			}
		}
		
	}
	
	/***
	 * Compiles mesh data. 
	 * if debug flag is set true tangent/normal/bitangent meshes will also be generated
	 * @param compileDebugInfo
	 * @return
	 */
	public Mesh[] compile(ArrayList<MeshOutput> supportVerts, boolean compileDebugInfo){
		IntBuffer indicies = BufferUtils.createIntBuffer(this.triangles.size());
		FloatBuffer verticies = BufferUtils.createFloatBuffer(this.triangles.size()*3);
		FloatBuffer normals = BufferUtils.createFloatBuffer(this.triangles.size()*3);
		FloatBuffer uvs = BufferUtils.createFloatBuffer(this.triangles.size()*2);

		

		
		//TODO: Correct errors related to shared vertex
//		for(int v : triangles){
//			indicies.put(v);
//		}
		
	
		for(int z =0; z < this.triangles.size();z++){
			int i = this.triangles.get(z);
			Vector3f v = this.iToV.get(i);
			indicies.put(z);
			Vector3f n = new Vector3f(this.vertexNormals.get(i));
//			Vector3f t = new Vector3f(this.vertexTangents.get(i));
//			Vector3f b = new Vector3f(this.vertexBiTangents.get(i));
			
			if(supportVerts != null){
				for(MeshOutput mo : supportVerts){
					if(mo.vToI.get(v)!=null){
//						System.out.println("Here");
						int index = mo.vToI.get(v);
						n.addLocal(mo.vertexNormals.get(index));
//						t.addLocal(mo.vertexTangents.get(index));
//						b.addLocal(mo.vertexBiTangents.get(index));
					}
				}
			}
			
			int dex = (z/3)*3;
			Vector3f other = computeNormal(this.iToV.get(this.triangles.get(dex)), this.iToV.get(this.triangles.get(dex+1)), this.iToV.get(this.triangles.get(dex+2)));

			
			n.normalizeLocal();
			Vector2f uv1 = generateUV(v, n, new Vector2f());
			Vector2f uv2 = generateUV(v, other, new Vector2f());
//			
//			
			if(Math.abs(other.dot(n)) < threshold){
				System.out.println(other.dot(n));
				System.out.println(uv1 +" :: "+uv2);
				
				n = other;
				uvs.put(uv2.x);
				uvs.put(uv2.y);
			}else{
				uvs.put(uv1.x);
				uvs.put(uv1.y);
			}
			
	        
			normals.put(n.x);
			normals.put(n.y);
			normals.put(n.z);
			
			verticies.put(v.x);
			verticies.put(v.y);
			verticies.put(v.z);
			

			
		}
		
		
			Mesh m = new Mesh();
			m.setBuffer(Type.Index, 3, indicies);
			m.setBuffer(Type.Position, 3, verticies);
			m.setBuffer(Type.Normal, 3, normals);
			m.setBuffer(Type.TexCoord, 2, uvs);
			TangentBinormalGenerator.generate(m,true,true);
			return new Mesh[]{m};		
	}
	
	public Vector3f addTriangle(Vector3f v1,Vector3f v2,Vector3f v3,  boolean support){
		boolean invalid = fuzzyEquals(v1, v2) || fuzzyEquals(v2, v3) || fuzzyEquals(v1, v3);
		if (invalid) {
			return null;
		}

		Vector3f lNormal;
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
//			mo.vertexTangents.add(new Vector3f(tangent));
//			mo.vertexBiTangents.add(new Vector3f(bitangent));
		}else{
			mo.vertexNormals.get(i).addLocal(normal);
//			mo.vertexTangents.get(i).addLocal(tangent);
//			mo.vertexBiTangents.get(i).addLocal(bitangent);
		}
		
		
		return i;
	}

	private static Vector2f generateUV(Vector3f v1, Vector3f normal,Vector2f store) {
		if (Math.abs(normal.y) > Math.abs(normal.z)) {
			
			//Y,Z dom -
			
			if (Math.abs(normal.y) > Math.abs(normal.x)) {// y dominate
//				if(Math.abs(normal.z) > Math.abs(normal.x)){
					store.set(v1.x, v1.z);
//				}else{
//					store.set(v1.z, v1.x);
//				}
				
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
		return store;
	}

	private static void generateTangent(Vector3f v0, Vector3f v1, Vector3f v2,Vector3f normal, Vector3f tangent, Vector3f bitangent) {
		// Edges of the triangle : postion delta
		Vector2f uv0 = new Vector2f();
		Vector2f uv1 = new Vector2f();
		Vector2f uv2 = new Vector2f();
		generateUV(v0,normal,uv0);
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
	
	private float dot(Vector4f v1, Vector4f v2){
		return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
	}
	
}

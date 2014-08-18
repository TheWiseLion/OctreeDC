package VoxelSystem.Misc;

import java.util.HashMap;
import java.util.Map;

import VoxelSystem.VoxelSystemTables.AXIS;
import VoxelSystem.Data.Storage.OctreeNode;
import VoxelSystem.SparseData.SparseInterface;
import VoxelSystem.SparseData.ChunkData.Chunk;
import VoxelSystem.SurfaceExtractors.DualContour;
import VoxelSystem.VoxelMaterials.MaterialRegistry;
import VoxelSystem.VoxelMaterials.VoxelMaterial;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;

/***
 * Because the there is a high amount of inter-dependcy between chunks
 * this class helps break down the different cases that can occur.
 * 
 * Here are the cases for REMESHING (produces new vertices):
 * - Nearest neighbor remeshed --> must run FACE + EDGE PROC
 * - Next Nearest neighbor remeshed --> must run EDGE PROC
 * - Associated Chunk needs to be remeshed ---> must run CELL + FACE + EDGE PROC
 * 
 * Here are cases for RECOMPUTING (vertices normals and tangents change)
 * - Nearest neighbor remeshed --> must recalculate FACE + EDGE + CORNER vertices
 * - Next Nearest neighbor remeshed --> must recalculate CORNER vertices
 * 
 * So far possibly one of the dirtiest classes in this whole thing.
 * 
 */
public class ChunkMeshManager {
	//MESH WHOLE THING TOGETHER
	//EDGE PROC-ETC
	//CACHE verts in each proc stage
	
	//Triangles that share no vertices in interchunk meshes
	Map<Integer, MeshOutput> interiorTriangles;
	
	//Triangles that (may) share vertices in interchunk meshes
	Map<Integer, MeshOutput> interiorInterTriangles;
	
	//Triangles generated during interTriangles
	Map<Integer, MeshOutput> interTriangles;
	
	public ChunkMeshManager(){
		interiorTriangles = new HashMap<Integer,MeshOutput>();
		interiorInterTriangles = new HashMap<Integer,MeshOutput>();
		interTriangles = new HashMap<Integer,MeshOutput>();
	}
	
	
	/**
	 * Recomputes all vertices
	 */
	public Map<Integer, MeshOutput> genVerts(Chunk c, SparseInterface sdi, CameraInfo cI, MaterialRegistry mr){
		interiorTriangles.clear();
//		interiorInterTriangles.clear();
//		interTriangles.clear();
		final Map<Integer,MeshOutput> meshes = new HashMap<Integer,MeshOutput>();
		MeshInterface cellProc = new MeshInterface(){
			@Override
			public void addTriangle(OctreeNode v1, OctreeNode v2, OctreeNode v3, int material, AXIS axis) {
				
				//if a is on face || b || c
				//	add to intraTriangles
				//else if a is on edge.....
					//ad too intraTriangles
				//else
					//add to interior triangles
				MeshOutput mesh = meshes.get(material);
				if(mesh == null){
					mesh = new MeshOutput();
					meshes.put(material,mesh);
				}
				mesh.windingQuadToTriangle(v1.getIsopoint(),v2.getIsopoint(),v3.getIsopoint(),false);
			}
		};
		
		
		//Mesh Chunk Edges (if being tracked)
		//X+
		Chunk cx = sdi.getChunk(c.cx+1, c.cy, c.cz);
		//Y+
		Chunk cy = sdi.getChunk(c.cx, c.cy+1, c.cz);
		//Z+
		Chunk cz = sdi.getChunk(c.cx, c.cy, c.cz+1);
		
		Chunk cxz = sdi.getChunk(c.cx+1, c.cy, c.cz+1);
		Chunk cxy = sdi.getChunk(c.cx+1, c.cy+1, c.cz);
		Chunk cyz = sdi.getChunk(c.cx, c.cy+1, c.cz+1);
		
		OctreeNode x = cx == null? null : cx.getChunkContents();
		OctreeNode y = cy == null? null : cy.getChunkContents();
		OctreeNode z = cz == null? null : cz.getChunkContents();
		OctreeNode yz = cyz == null? null : cyz.getChunkContents();
		
		OctreeNode xz = cxz == null? null : cxz.getChunkContents();
		OctreeNode xy = cxy == null? null : cxy.getChunkContents();
		
		
		DualContour.cellProc(c.getChunkContents(), cI, cellProc, null);
		
		if(x != null){
			DualContour.faceProc(c.getChunkContents(),x,AXIS.X, cI, cellProc, null);
		}
//		
		if(y != null){
			DualContour.faceProc(c.getChunkContents(),y,AXIS.Y, cI, cellProc, null);
		}
//		
		if(z != null){
			DualContour.faceProc(c.getChunkContents(),z,AXIS.Z, cI, cellProc, null);
		}

//		//TODO: Edge Proc
		if(y !=null && yz!=null && z!=null){
			DualContour.edgeProc(c.getChunkContents(),y,yz,z,AXIS.X, cI, cellProc, null);
		}
		
		if(x != null && xz !=null && z!=null){
			DualContour.edgeProc(c.getChunkContents(),x,xz,z,AXIS.Y, cI, cellProc, null);
		}
		
		if(y != null && xy !=null && x!=null){
			DualContour.edgeProc(c.getChunkContents(),x,xy,y,AXIS.Z, cI, cellProc, null);
		}
		
	
		return meshes;
		
	}
	
	/***
	 * Recomputes only boundary vertices
	 */
	public void genInterVerts(OctreeNode n){
		interTriangles.clear();
		interiorInterTriangles.clear();
		
	}
	
	public void buildMeshes(){
		//Compile interiorTriangles.
		//Next for each neighbor(grab data about any shared vertices 26 neighbors) 
	}

	//Private Helpers:
	private static int planes[]= new int[]{
		0,3,2,1, //Z-
		4,7,6,5, //Z+
		0,3,7,4, //X-
		1,2,6,5, //X+
		0,4,5,1, //Y-
		3,7,6,2 //Y+
	};
	private static AXIS axii[]= new AXIS[]{
		AXIS.Z, //Z-
		AXIS.Z, //Z+
		AXIS.X, //X-
		AXIS.X, //X+
		AXIS.Y, //Y-
		AXIS.Y //Y+
	};
	
	public boolean onFace(OctreeNode root, OctreeNode leaf){
		for(int i=0; i< 6; i++){
			Vector3f v1 = root.getCorner(planes[i*4], new Vector3f());
			Vector3f v2 = root.getCorner(planes[i*4+1], new Vector3f());
			Vector3f v3 = root.getCorner(planes[i*4+2], new Vector3f());
			Vector3f v4 = root.getCorner(planes[i*4+3], new Vector3f());
			boolean on = true;
			for(int z=0; z < 4; z++){
				if( !onFace(v1,v2,v3,v4,leaf.getCorner(planes[i*4+z],new Vector3f()),axii[i]) ){
					on = false;
				}
			}
			
			if(on){
				return on;
			}
			
		}
		return false;
	}
	
	public boolean onEdge(OctreeNode root, OctreeNode leaf){
		for(int i=0; i < 12; i++){
			Vector3f v1 = root.getCorner(planes[i*4], new Vector3f());
			Vector3f v2 = root.getCorner(planes[i*4+1], new Vector3f());
			boolean on = true;
			for(int z=0; z < 4; z++){
//				if( !onFace(v1,v2,v3,v4,leaf.getCorner(planes[i*4+z],new Vector3f()),axii[i]) ){
//					on = false;
//				}
			}
			
			if(on){
				return on;
			}
			
		}
		return false;
	}
	
	//Checks if v3 is between v1 and v2
	private boolean onFace(Vector3f v1, Vector3f v2, Vector3f v3,Vector3f v4, Vector3f p, AXIS a){
		if(a == AXIS.X && v1.x != p.x){
			return false;
		}else if(a == AXIS.Y && v1.y != p.y){
			return false;
		}else if(a == AXIS.Z && v1.z != p.z){
			return false;
		}
		
		if(a == AXIS.Z){
			if(p.y >= v1.y && p.y <= v2.y && p.x >= v1.x && p.x <= v4.x){
				return true;
			}
		}else if(a == AXIS.Y){
			if(p.z >= v1.z && p.z <= v2.z && p.x >= v1.x && p.x <= v4.x){
					return true;
			}
		}else if(a == AXIS.X){
			if (p.y >= v1.y && p.y <= v2.y && p.z >= v1.z && p.z <= v4.z) {
				return true;
			}
		}
		return false;
	}
	
	private boolean onEdge(Vector3f v1, Vector3f v2, Vector3f p, AXIS a){
		if(a  == AXIS.X){
			if(v1.x <= p.x && v2.x >= p.x){
				return true;
			}
		}else if(a == AXIS.Y){
			if(v1.y <= p.y && v2.y >= p.y){
				return true;
			}
		}else{
			if(v1.z <= p.z && v2.z >= p.z){
				return true;
			}
		}
		return false;
		
	}
}



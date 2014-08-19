package VoxelSystem.Misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import com.jme3.scene.Mesh.Mode;
import com.jme3.scene.Node;

public class ChunkMeshManager {
	//Triangles that share no vertices in interchunk meshes
	Map<Integer, MeshOutput> interiorTriangles;
	
	//Triangles that (may) share vertices in interchunk meshes
	Map<Integer, MeshOutput> interiorInterTriangles;
	
	//Triangles generated during interTriangles
	Map<Integer, MeshOutput> interTriangles;
	
	List<Geometry> interiorMeshes;
	List<Geometry> interChunkMeshes;
	
	public ChunkMeshManager(){
		interiorTriangles = new HashMap<Integer,MeshOutput>();
		interiorInterTriangles = new HashMap<Integer,MeshOutput>();
		interTriangles = new HashMap<Integer,MeshOutput>();
		interiorMeshes = new ArrayList<Geometry>();
		interChunkMeshes = new ArrayList<Geometry>();
	}
	
	boolean recomputeInterVerts;
	boolean recomputeInterMesh;
	boolean recomputeAllMesh;
	
	
	/**
	 * Recomputes all vertices
	 */
	public void genVerts(Chunk c, SparseInterface sdi, CameraInfo cI, MaterialRegistry mr){
		interiorTriangles.clear();
		interiorInterTriangles.clear();
//		interTriangles.clear();
		final OctreeNode root = c.getChunkContents();
		MeshInterface mI = new MeshInterface(){
			@Override
			public void addTriangle(OctreeNode v1, OctreeNode v2, OctreeNode v3, int material, AXIS axis) {
				
				//if a is on face || b || c
					//add to interTriangles
				//else
					//add to interior triangles
				if(onFace(root, v1)|| onFace(root, v2)|| onFace(root, v3)){
					ChunkMeshManager.addTriangle(v1, v2, v3, material, interiorTriangles, true);
					ChunkMeshManager.addTriangle(v1, v2, v3, material, interiorInterTriangles, false);
				}else{
					ChunkMeshManager.addTriangle(v1, v2, v3, material, interiorInterTriangles, true);
					ChunkMeshManager.addTriangle(v1, v2, v3, material, interiorTriangles, false);
				}
					
				
				
			}
		};
		
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = -1; z <= 1; z++) {
					if (x == 0 && z == 0 && y == 0) {
						continue;
					}
					
					Chunk neighbor = sdi.getChunk(c.cx + x, c.cy + y, c.cz + z);
					if(neighbor != null){
						neighbor.getRenderNode().meshManager.recomputeInterVerts = true;
					}
				}
			}
		}
		
		recomputeAllMesh = true;
		DualContour.cellProc(c.getChunkContents(), cI, mI, null);
		genInterMeshVerts(c,  sdi,  cI, mr);

	}
	
	/***
	 * Recomputes only boundary vertices
	 */
	public void genInterMeshVerts(Chunk c, SparseInterface sdi, CameraInfo cI, MaterialRegistry mr){
		interTriangles.clear();
		recomputeInterMesh = true;
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
		
		MeshInterface mI = new MeshInterface(){
			@Override
			public void addTriangle(OctreeNode v1, OctreeNode v2, OctreeNode v3, int material, AXIS axis) {
				ChunkMeshManager.addTriangle(v1, v2, v3, material, interTriangles, false);
			}
			
		};
		
		if(x != null){
			cx.getRenderNode().meshManager.recomputeInterMesh = true;
			DualContour.faceProc(c.getChunkContents(),x,AXIS.X, cI, mI, null);
		}

		if(y != null){
			cy.getRenderNode().meshManager.recomputeInterMesh = true;
			DualContour.faceProc(c.getChunkContents(),y,AXIS.Y, cI, mI, null);
		}

		if(z != null){
			cz.getRenderNode().meshManager.recomputeInterMesh = true;
			DualContour.faceProc(c.getChunkContents(),z,AXIS.Z, cI, mI, null);
		}


		if(y !=null && yz!=null && z!=null){
			cyz.getRenderNode().meshManager.recomputeInterMesh = true;
			DualContour.edgeProc(c.getChunkContents(),y,yz,z,AXIS.X, cI, mI, null);
		}
		
		if(x != null && xz !=null && z!=null){
			cxz.getRenderNode().meshManager.recomputeInterMesh = true;
			DualContour.edgeProc(c.getChunkContents(),x,xz,z,AXIS.Y, cI, mI, null);
		}
		
		if(y != null && xy !=null && x!=null){
			cxy.getRenderNode().meshManager.recomputeInterMesh = true;
			DualContour.edgeProc(c.getChunkContents(),x,xy,y,AXIS.Z, cI, mI, null);
		}
		
		for(int i : interiorInterTriangles.keySet()){
			MeshOutput interior = interiorInterTriangles.get(i);
			MeshOutput exterior = interTriangles.get(i);
			if(exterior !=null){
				exterior.add(interior);
			}else{
				interTriangles.put(i, interior);
			}
		}
		recomputeInterVerts = false;
		recomputeInterMesh = true;
	}
	
	public void buildAllMeshes(Chunk c, SparseInterface sdi, MaterialRegistry mr, Node geoNode){
		//Compile interiorTriangles.
		for(Geometry g : this.interiorMeshes){
			geoNode.detachChild(g);
		}
		this.interiorMeshes.clear();
		
		for(int i : this.interiorTriangles.keySet()){
			
			
			Mesh meshes[] = this.interiorTriangles.get(i).compile(null,false);
			for(int z = 0; z < meshes.length; z++){
				Geometry g = new Geometry();
				Mesh mesh = meshes[z];
				g.setMesh(mesh);
				if(z == 0){
					VoxelMaterial v = mr.getMaterial(i);
					g.setMaterial(v.jMEMaterial);
				}else if(z == 1){
					g.setMaterial(mr.tangents);
					mesh.setMode(Mode.Lines);
				}else if(z == 2){
					g.setMaterial(mr.normals);
					mesh.setMode(Mode.Lines);
				}else if(z == 3){
					g.setMaterial(mr.bitangent);
					mesh.setMode(Mode.Lines);
				}
				
				
				geoNode.attachChild(g);
				this.interiorMeshes.add(g);
			}
			
			
			
			
		}
		
		buildInterChunkMeshes(c,  sdi,  mr,  geoNode);
		recomputeAllMesh = false;
	}

	public void buildInterChunkMeshes(Chunk c, SparseInterface sdi, MaterialRegistry mr, Node geoNode){
		//Compile interiorTriangles.
		for(Geometry g : this.interChunkMeshes){
			geoNode.detachChild(g);
		}
		this.interChunkMeshes.clear();
		
		//Next for each neighbor(grab data about any shared vertices 26 neighbors)
		ArrayList<Chunk> neighbors = new ArrayList<Chunk>();
		
		for (int x = -1; x <= 0; x++) {
			for (int y = -1; y <= 0; y++) {
				for (int z = -1; z <= 0; z++) {
					if (x == 0 && z == 0 && y == 0) {
						continue;
					}
					Chunk neighbor = sdi.getChunk(c.cx + x, c.cy + y, c.cz + z);
					if (neighbor != null) {
						neighbors.add(neighbor);
					}

				}
			}
		}
		
		
		ArrayList<MeshOutput> supportingVerts = new ArrayList<MeshOutput>();
		
		
		for(int i : this.interTriangles.keySet()){
			//Get supporting vertices for each of the neighbors
			supportingVerts.clear();
			for(Chunk neighbor : neighbors){
				MeshOutput mo = neighbor.getRenderNode().meshManager.interTriangles.get(i);
				if(mo !=null){
					supportingVerts.add(mo);
				}
			}
			
//			System.out.println("Mesh Supports: "+ supportingVerts.size());
			Mesh meshes[] = this.interTriangles.get(i).compile(supportingVerts,false);
			for(int z = 0; z < meshes.length; z++){
				Geometry g = new Geometry();
				Mesh mesh = meshes[z];
				g.setMesh(mesh);
				if(z == 0){
					VoxelMaterial v = mr.getMaterial(i);
					g.setMaterial(v.jMEMaterial);
				}else if(z == 1){
					g.setMaterial(mr.tangents);
					mesh.setMode(Mode.Lines);
				}else if(z == 2){
					g.setMaterial(mr.normals);
					mesh.setMode(Mode.Lines);
				}else if(z == 3){
					g.setMaterial(mr.bitangent);
					mesh.setMode(Mode.Lines);
				}
				
				
				geoNode.attachChild(g);
				this.interChunkMeshes.add(g);
			}
		}
		
		geoNode.updateGeometricState();
		geoNode.updateModelBound();
		this.recomputeInterMesh = false;
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
	
	public boolean recomputeInterMesh(){
		return recomputeInterMesh;
	}
	
	public boolean recomputeAllMesh(){
		return recomputeAllMesh;
	}
	
	public boolean recomputeInterVerts(){
		return recomputeInterVerts;
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
	
	private static void addTriangle(OctreeNode v1, OctreeNode v2, OctreeNode v3, int material, Map<Integer,MeshOutput> meshes,boolean support){
		MeshOutput mesh = meshes.get(material);
		if(mesh == null){
			mesh = new MeshOutput();
			meshes.put(material,mesh);
		}
		mesh.addTriangle(v1.getIsopoint(),v2.getIsopoint(),v3.getIsopoint(),support);
	}

}



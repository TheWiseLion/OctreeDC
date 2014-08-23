package VoxelSystem.tmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import VoxelSystem.VoxelSystemTables.AXIS;
import VoxelSystem.Data.Storage.OctreeNode;
import VoxelSystem.Misc.MeshInterface;
import VoxelSystem.Misc.MeshOutput;
import VoxelSystem.SparseData.SparseInterface;
import VoxelSystem.SparseData.ChunkData.Chunk;
import VoxelSystem.SparseData.controller.LODController;
import VoxelSystem.VoxelMaterials.MaterialRegistry;
import VoxelSystem.VoxelMaterials.VoxelMaterial;
import VoxelSystem.meshing.DualContour;
import VoxelSystem.meshing.VoxelMesh;
import VoxelSystem.threading.GeometryManager;

import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Mesh.Mode;
import com.jme3.scene.Node;

/***
 * This meshing has far too much complexity. This is mostly 
 * caused due to triangles being shared with 
 * nearest adjacent and diagonal chunks
 * 
 */
public class DualContourMesh extends RenderNode{
	private boolean dirtyCell; //Cell needs to be remeshed
	private boolean dirtyEdge; //edge normals need to be recalculated
	private boolean incorrectEdge; //edge triangles need to be recomputed
	private List<Geometry> edgeData;
	private List<Geometry> cellData;
	private int lastLeafCount = 0;

	//Triangles that share no vertices in interchunk meshes
	Map<Integer, MeshOutput> interiorTriangles;

	// Triangles that (may) share vertices in interchunk meshes
	Map<Integer, MeshOutput> interiorInterTriangles;

	//Triangles generated during interTriangles
	Map<Integer, MeshOutput> interTriangles;
	
	
	public DualContourMesh(OctreeNode data){
		super(data);
		cellData = new ArrayList<Geometry>();
		edgeData = new ArrayList<Geometry>();
		interiorTriangles = new HashMap<Integer,MeshOutput>();
		interiorInterTriangles = new HashMap<Integer,MeshOutput>();
		interTriangles = new HashMap<Integer,MeshOutput>();
		dirtyEdge = false;
		incorrectEdge = false;
	}
	
	@Override
	public void deleteMesh(GeometryManager gm) {
		gm.queueDelete(cellData);
		gm.queueDelete(edgeData);
	}



	@Override
	public void updateFromAdjacentNode(int dx, int dy, int dz, RenderNode rn) {
		int count = 0;
		if(dx>0){count++;};
		if(dy>0){count++;};
		if(dz>0){count++;};
		if(count == 1){
			incorrectEdge = true;
		}
		dirtyEdge = false;
	}

	/***
	 * Returns number of changed leaves based on camera information
	 * @param node
	 * @param cI
	 * @return
	 */
	public static int getLeafCount(OctreeNode node, LODController controller){
		if(node.isLeaf() || controller.shouldBeLeaf(node)){
			if(node.getIsopoint()!=null){
				return 1;
			}else{
				return 0;
			}
		}else{
			OctreeNode [] children = node.getChildren();
			int sum = 0;
			for(OctreeNode n : children){
				sum += getLeafCount(n,controller);
			}
			return sum;
		}
	}



	@Override
	public void genCell(OctreeNode c, AdaptiveMeshInterface sdi, LODController lod) {
		//if node is dirty...
		int leafCount = getLeafCount(c, lod);
		int diff = Math.abs(leafCount - lastLeafCount);

		if (lod.shouldRemesh(leafCount, c)) {
			interiorTriangles.clear();
			interiorInterTriangles.clear();
			
			final OctreeNode root = c;
			MeshInterface mI = new MeshInterface(){
				@Override
				public void addTriangle(OctreeNode v1, OctreeNode v2, OctreeNode v3, int material, AXIS axis) {
					
					//if a is on face || b || c
						//add to interTriangles
					//else
						//add to interior triangles
					if(onFace(root, v1)|| onFace(root, v2)|| onFace(root, v3)){
						DualContourMesh.addTriangle(v1, v2, v3, material, interiorTriangles, true);
						DualContourMesh.addTriangle(v1, v2, v3, material, interiorInterTriangles, false);
					}else{
						DualContourMesh.addTriangle(v1, v2, v3, material, interiorInterTriangles, true);
						DualContourMesh.addTriangle(v1, v2, v3, material, interiorTriangles, false);
					}
						
					
					
				}
			};
			
			for (int x = -1; x <= 1; x++) {
				for (int y = -1; y <= 1; y++) {
					for (int z = -1; z <= 1; z++) {
						
						if (x == 0 && z == 0 && y == 0) {
							continue;
						}
						
						RenderNode neighbor = sdi.getNeighbor(x, y, z, this);
						if(neighbor != null){
							neighbor.updateFromAdjacentNode(-x,-y,-z,this);
						}
						
					}
				}
			}
			
			DualContour.cellProc(c, lod, mI, null);
			dirtyCell = true;
			dirtyEdge = true;
			incorrectEdge = true;
		}
	}

	@Override
	public void genEdges(OctreeNode c, AdaptiveMeshInterface sdi, LODController lod) {
		if(incorrectEdge){
			interTriangles.clear();
			
			//Mesh Chunk Edges
			//X+
			RenderNode cx = sdi.getNeighbor(1,0,0, this);
			//Y+
			RenderNode cy = sdi.getNeighbor(0, 1, 0, this);
			//Z+
			RenderNode cz = sdi.getNeighbor(0, 0, 1, this);
			
			RenderNode cxz = sdi.getNeighbor(1, 0, 1, this);
			RenderNode cxy = sdi.getNeighbor(1, 1, 0, this);
			RenderNode cyz = sdi.getNeighbor(0, 1, 1, this);
			
			OctreeNode x = cx == null? null : cx.getRepresentedData();
			OctreeNode y = cy == null? null : cy.getRepresentedData();
			OctreeNode z = cz == null? null : cz.getRepresentedData();
			
			OctreeNode yz = cyz == null? null : cyz.getRepresentedData();
			OctreeNode xz = cxz == null? null : cxz.getRepresentedData();
			OctreeNode xy = cxy == null? null : cxy.getRepresentedData();
			
			MeshInterface mI = new MeshInterface(){
				@Override
				public void addTriangle(OctreeNode v1, OctreeNode v2, OctreeNode v3, int material, AXIS axis) {
					DualContourMesh.addTriangle(v1, v2, v3, material, interTriangles, false);
				}
				
			};
			
			if(x != null){
				cx.updateFromAdjacentNode(-1, 0, 0, this);
				DualContour.faceProc(c,x,AXIS.X, lod, mI, null);
			}

			if(y != null){
				cy.updateFromAdjacentNode(0, -1, 0, this);
				DualContour.faceProc(c,y,AXIS.Y, lod, mI, null);
			}

			if(z != null){
				cz.updateFromAdjacentNode(0, 0, -1, this);
				DualContour.faceProc(c,z,AXIS.Z, lod, mI, null);
			}


			if(y !=null && yz!=null && z!=null){
				cyz.updateFromAdjacentNode(0, -1, -1, this);
				DualContour.edgeProc(c,y,yz,z,AXIS.X, lod, mI, null);
			}
			
			if(x != null && xz !=null && z!=null){
				cxz.updateFromAdjacentNode(-1, 0, -1, this);
				DualContour.edgeProc(c,x,xz,z,AXIS.Y, lod, mI, null);
			}
			
			if(y != null && xy !=null && x!=null){
				cxy.updateFromAdjacentNode(-1, -1, 0, this);
				DualContour.edgeProc(c,x,xy,y,AXIS.Z, lod, mI, null);
			}
			
			for(int i : interiorInterTriangles.keySet()){
				MeshOutput interior = interiorInterTriangles.get(i);
				MeshOutput exterior = interTriangles.get(i);
				if(exterior !=null){
					exterior.add(interior);
				}else{
					//TODO: possible bug?
					interTriangles.put(i, interior);
				}
			}
			
			
			dirtyEdge = true;
		}
		
		incorrectEdge = false;
	}


	@Override
	public void meshTriangles(OctreeNode node, AdaptiveMeshInterface sdi, LODController lod,  MaterialRegistry mr, GeometryManager gm) {
		if(dirtyCell){
			gm.queueDelete(cellData);
			this.cellData.clear();
			
			for(int i : this.interiorTriangles.keySet()){
				Mesh meshes[] = this.interiorTriangles.get(i).compile(null,false);
				for(int z = 0; z < meshes.length; z++){
					Geometry g = new Geometry();
					Mesh mesh = meshes[z];
					g.setMesh(mesh);
					g.setShadowMode(ShadowMode.CastAndReceive);
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
					
					
					this.cellData.add(g);
				}
			}
			
			gm.queueAdd(cellData);
		}
		
		if(dirtyEdge){
			gm.queueDelete(edgeData);
			this.edgeData.clear();
		
			//Next for each neighbor(grab data about any shared vertices 26 neighbors)
			ArrayList<DualContourMesh> neighbors = new ArrayList<DualContourMesh>();
			
			for (int x = -1; x <= 1; x++) {
				for (int y = -1; y <= 1; y++) {
					for (int z = -1; z <= 1; z++) {
						if (x == 0 && z == 0 && y == 0) {
							continue;
						}
						DualContourMesh neighbor = ((DualContourMesh)sdi.getNeighbor(x,y,z, this));
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
				for(DualContourMesh neighbor : neighbors){
					MeshOutput mo = neighbor.interTriangles.get(i);
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
					g.setShadowMode(ShadowMode.CastAndReceive);
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
					
					
					this.edgeData.add(g);
				}
				
				gm.queueAdd(edgeData);
			}
		}
		
		dirtyCell = false;
		dirtyEdge = false;
	}
	
	
	/////////////////////////////////////////////
	////////////////////////////////////////////
	// Code that calls the recursive meshing between this node and its neighbors
	///////////////////////////////////////////
	//////////////////////////////////////////
	
	
	
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
	
	private boolean onFace(OctreeNode root, OctreeNode leaf){
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

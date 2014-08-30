package VoxelSystem.meshing.lod;


import java.util.ArrayList;
import java.util.List;

import VoxelSystem.VoxelSystemTables.AXIS;
import VoxelSystem.Data.Storage.OctreeNode;
import VoxelSystem.Misc.MeshInterface;
import VoxelSystem.SparseData.controller.LODController;
import VoxelSystem.VoxelMaterials.MaterialRegistry;

import com.jme3.math.Vector3f;

public class DualContour{
	/**
     * Recursive polygonisation function that handles cubes and, if the cube is
     * not a leaf of the octree, calls other functions on its subcells, edges
     * and faces.
     *
     * @param q is an octree node to process.
     */
    public static void cellProc(OctreeNode q, LODController cI, MeshInterface meshes, MaterialRegistry mr)
    {
        if (q != null && !isLeaf(q,cI)){ //if !leaf or geometric error ('effectively leaf')
        
            OctreeNode[] kids = q.getChildren();
            // 8 calls to cellProc
            for (OctreeNode child : kids)
            {
                cellProc(child, cI, meshes, mr);
            }

            // 12 calls to faceProc
            faceProc(kids[0], kids[1], AXIS.X, cI, meshes, mr);
            faceProc(kids[2], kids[3], AXIS.X, cI, meshes, mr);
            faceProc(kids[4], kids[5], AXIS.X, cI, meshes, mr);
            faceProc(kids[6], kids[7], AXIS.X, cI, meshes, mr);
//
            faceProc(kids[0], kids[2], AXIS.Y, cI, meshes, mr);
            faceProc(kids[1], kids[3], AXIS.Y, cI, meshes, mr);
            faceProc(kids[4], kids[6], AXIS.Y, cI, meshes, mr);
            faceProc(kids[5], kids[7], AXIS.Y, cI, meshes, mr);

            faceProc(kids[0], kids[4], AXIS.Z, cI, meshes, mr);
            faceProc(kids[1], kids[5], AXIS.Z, cI, meshes, mr);
            faceProc(kids[2], kids[6], AXIS.Z, cI, meshes, mr);
            faceProc(kids[3], kids[7], AXIS.Z, cI, meshes, mr);


            // 6 calls to edgeProc
            edgeProc(kids[0], kids[2], kids[6], kids[4], AXIS.X, cI, meshes, mr);
            edgeProc(kids[1], kids[3], kids[7], kids[5], AXIS.X, cI, meshes, mr);
            
            edgeProc(kids[0], kids[1], kids[5], kids[4], AXIS.Y, cI, meshes, mr);
            edgeProc(kids[2], kids[3], kids[7], kids[6], AXIS.Y, cI, meshes, mr);
            
            edgeProc(kids[0], kids[1], kids[3], kids[2], AXIS.Z, cI, meshes, mr);
            edgeProc(kids[4], kids[5], kids[7], kids[6], AXIS.Z, cI, meshes, mr);

        }
    }
	

	/**
     * Recursive polygonisation function that handles faces and, if no cube is a
     * leaf of the octree, calls other functions on its edges and subfaces.
     *
     * @param q1 is the left-, bottom- or whatever-most octree node.
     * @param q2 is its neighbour.
     * @param axis is the axis of the face.
     */
    public static void faceProc(OctreeNode q1, OctreeNode q2, AXIS a, LODController cI, MeshInterface meshes, MaterialRegistry mr){
		if(q1 != null && q2 != null && (!isLeaf(q1, cI) ||  !isLeaf(q2, cI)) ){
			//Awkward statement but if we want it to be a leaf from the perspective of the camera then make it a leaf (w/o editing data of course)
			OctreeNode[] kids1 = q1.getChildren()==null || isLeaf(q1, cI) ?new OctreeNode[]{q1,q1,q1,q1,q1,q1,q1,q1}:q1.getChildren();
			OctreeNode[] kids2 = q2.getChildren()==null || isLeaf(q2, cI)?new OctreeNode[]{q2,q2,q2,q2,q2,q2,q2,q2}:q2.getChildren();
//
			switch(a){
				case X:// X axis
//				// X axis
//                // 4 calls to faceProc.
                faceProc(kids1[1], kids2[0], AXIS.X, cI, meshes, mr);
                faceProc(kids1[3], kids2[2], AXIS.X, cI, meshes, mr);
                faceProc(kids1[5], kids2[4], AXIS.X, cI, meshes, mr);
                faceProc(kids1[7], kids2[6], AXIS.X, cI, meshes, mr);
                edgeProc(kids1[1], kids2[0], kids2[2], kids1[3], AXIS.Z, cI, meshes, mr);
                edgeProc(kids1[5], kids2[4], kids2[6], kids1[7], AXIS.Z, cI, meshes, mr);
                
                edgeProc(kids1[1], kids2[0], kids2[4], kids1[5], AXIS.Y, cI, meshes, mr);
                edgeProc(kids1[3], kids2[2], kids2[6], kids1[7], AXIS.Y, cI, meshes, mr);
                break;
				case Y:
//				// 4 calls to faceProc.
                faceProc(kids1[2], kids2[0], AXIS.Y, cI, meshes, mr);
                faceProc(kids1[3], kids2[1], AXIS.Y, cI, meshes, mr);
                faceProc(kids1[6], kids2[4], AXIS.Y, cI, meshes, mr);
                faceProc(kids1[7], kids2[5], AXIS.Y, cI, meshes, mr);
//                
//                // 4 calls to edgeProc
                edgeProc(kids1[2], kids2[0], kids2[4], kids1[6], AXIS.X, cI, meshes, mr);
                edgeProc(kids1[3], kids2[1], kids2[5], kids1[7], AXIS.X, cI, meshes, mr);
                edgeProc(kids1[2], kids1[3], kids2[1], kids2[0], AXIS.Z, cI, meshes, mr);
                edgeProc(kids1[6], kids1[7], kids2[5], kids2[4], AXIS.Z, cI, meshes, mr);
                break;
//                
				case Z:
				 faceProc(kids1[4], kids2[0], AXIS.Z, cI, meshes, mr);
                 faceProc(kids1[5], kids2[1], AXIS.Z, cI, meshes, mr);
                 faceProc(kids1[6], kids2[2], AXIS.Z, cI, meshes, mr);
                 faceProc(kids1[7], kids2[3], AXIS.Z, cI, meshes, mr);
                 
                 // 4 calls to edgeProc
                 edgeProc(kids1[4], kids1[5], kids2[1], kids2[0], AXIS.Y, cI, meshes, mr);
                 edgeProc(kids1[6], kids1[7], kids2[3], kids2[2], AXIS.Y, cI, meshes, mr);
                 
                 edgeProc(kids1[4], kids1[6], kids2[2], kids2[0], AXIS.X, cI, meshes, mr);
                 edgeProc(kids1[5], kids1[7], kids2[3], kids2[1], AXIS.X, cI, meshes, mr); 
                 break;
			}
//
		}


	}


    public static void edgeProc(OctreeNode q0,OctreeNode q1,OctreeNode q2,OctreeNode q3, AXIS a, LODController cI,MeshInterface meshes, MaterialRegistry mr){
		  if (q0 != null && q1 != null && q2 != null && q3 != null){
	            // If all cubes are leaves, stop recursion.
	            if (isLeaf(q0, cI) && isLeaf(q1, cI) && isLeaf(q2, cI) && isLeaf(q3, cI)){
	            		boolean cw = false;
		            	int qr[] = null;
	            		
	            		if(a == AXIS.X){
		            		qr = new int[]{
		            				7,6,
		            				4,5,
		            				0,1,
		            				3,2,
		            		};
		            	}else if(a == AXIS.Y){
		            		qr = new int[]{
		            				6,5,
		            				7,4,
		            				3,0,
		            				2,1,
		            		};
		            	}else{
		            		qr = new int[]{
			            			2,6,
			            			3,7,
			            			0,4,
			            			1,5
		            		};
		            	}
	            		
	            		int nodeIndex = 0;
	            		List<OctreeNode> qs = new ArrayList<OctreeNode>(4);
	            		OctreeNode [] nodes = new OctreeNode[]{q0,q1,q2,q3};
	            		qs.add(q0);
	            		OctreeNode q = q0;
	            		//Get Smallest of Nodes
	            		//if all smallest of nodes have proper edges...
	            		for(int i=0;i<4;i++){
	            			if(nodes[i].getCubeLength() < q.getCubeLength()){
	            				nodeIndex = i;
	            				q = nodes[i];
	            				qs.clear();
	            				qs.add(nodes[i]);
	            			}else if(nodes[i].getCubeLength() == q.getCubeLength() && nodes[i].getIsopoint()==null){
	            				qs.add(nodes[i]);
	            			}
	            		}
	            		for(OctreeNode nod:qs){
	            			if(nod.getIsopoint() == null){
	            				return;
	            			}
	            		}

		            		if(q.getType(qr[nodeIndex*2]) != q.getType(qr[nodeIndex*2+1])){
					            		boolean subSurfaceIntersection = false;
					            		int t1 = -1;
					            		int t2 = -1;
					            		if(q.getType(qr[nodeIndex*2])==-1){
					            			cw = false;
					            			t1 = q.getType(qr[nodeIndex*2+1]);
					            		
					            		}else if(q.getType(qr[nodeIndex*2+1])==-1){
					            			cw = true;
					            			t1 = q.getType(qr[nodeIndex*2]);
					            			
					            		}else{ //Check for if its a sub-surface material
					            			t1 = q.getType(qr[nodeIndex*2]);
					            			t2 = q.getType(qr[nodeIndex*2+1]);
					            			subSurfaceIntersection = true;
					            		}
					            		
					            		if(!subSurfaceIntersection){
					            			meshBasic(q0, q1, q2, q3, cw, t1,a, meshes);
					            		}else{
					            			//TODO
					            		}
					            		return;
				            }

				}else{ //Not all leaves. 
					OctreeNode[] kids1 = q0.getChildren()==null || isLeaf(q0, cI)?new OctreeNode[]{q0,q0,q0,q0,q0,q0,q0,q0}:q0.getChildren();
					OctreeNode[] kids2 = q1.getChildren()==null || isLeaf(q1, cI)?new OctreeNode[]{q1,q1,q1,q1,q1,q1,q1,q1}:q1.getChildren();
					OctreeNode[] kids3 = q2.getChildren()==null || isLeaf(q2, cI)?new OctreeNode[]{q2,q2,q2,q2,q2,q2,q2,q2}:q2.getChildren();
					OctreeNode[] kids4 = q3.getChildren()==null || isLeaf(q3, cI)?new OctreeNode[]{q3,q3,q3,q3,q3,q3,q3,q3}:q3.getChildren();
		
					switch (a) {
						case X:
							edgeProc(kids1[6], kids2[4], kids3[0], kids4[2], AXIS.X, cI, meshes, mr);
							edgeProc(kids1[7], kids2[5], kids3[1], kids4[3], AXIS.X, cI, meshes, mr);
							break;
						case Y:
							edgeProc(kids1[5], kids2[4], kids3[0], kids4[1], AXIS.Y, cI, meshes, mr);
							edgeProc(kids1[7], kids2[6], kids3[2], kids4[3], AXIS.Y, cI, meshes, mr);
							break;
						case Z:
							edgeProc(kids1[3], kids2[2], kids3[0], kids4[1], AXIS.Z, cI, meshes, mr);
							edgeProc(kids1[7], kids2[6], kids3[4], kids4[5], AXIS.Z, cI, meshes, mr);
							break;
					}

			}
		  }
	}
	
	private static void meshBasic(OctreeNode v1, OctreeNode v2, OctreeNode v3, OctreeNode v4, boolean cw, int type,AXIS a, MeshInterface meshes){
		int nullCount = 0;
		if(v1.getIsopoint() == null){ nullCount++;}
		if(v2.getIsopoint() == null){ nullCount++;}
		if(v3.getIsopoint() == null){ nullCount++;}
		if(v4.getIsopoint() == null){ nullCount++;}
		
		//Some REALLY fugly code.
		if(nullCount < 2){
			if (v1.getIsopoint() == null) {
				if (cw) {
					meshes.addTriangle(v2, v3, v4, type, a);
				} else {
					meshes.addTriangle(v2, v4, v3, type, a);
				}
			} else if (v2.getIsopoint() == null) {
				if (cw) {
					meshes.addTriangle(v1, v3, v4, type, a);
				} else {
					meshes.addTriangle(v1, v4, v3, type, a);
				}
			} else if (v3.getIsopoint() == null) {
				if (cw) {
					meshes.addTriangle(v1, v2, v4, type, a);
				} else {
					meshes.addTriangle(v1, v4, v2, type, a);
				}
			} else if (v4.getIsopoint() == null) {
				if (cw) {
					meshes.addTriangle(v1, v2, v3, type, a);
				} else {
					meshes.addTriangle(v1, v3, v2, type, a);
				}
			} else {
				if (cw) {
					meshes.addTriangle(v1, v2, v3, type, a);
					meshes.addTriangle(v3, v4, v1, type, a);
				} else {
					meshes.addTriangle(v3, v2, v1, type, a);
					meshes.addTriangle(v1, v4, v3, type, a);
				}
				//TODO: I think there is a case where e.g. v2==v3
				
			}
		}
		
	}
	
	
	public static boolean isLeaf(OctreeNode n, LODController cI){
		return n.isLeaf() || cI.shouldBeLeaf(n);// || n.getGeometricError() < geoError;
	}
	
	public static boolean fuzzyEquals(Vector3f v, OctreeNode o, float d){
		if(o.getIsopoint()!=null){
			if(v.subtract(o.getIsopoint()).length()<d){
				return true;
			}
		}
		return false;
	}
	
}

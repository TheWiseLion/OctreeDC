package VoxelSystem.SparseData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import VoxelSystem.VoxelSystemTables;
import VoxelSystem.Data.CollapsePolicy;
import VoxelSystem.Data.Storage.BasicVoxelTree;
import VoxelSystem.Data.Storage.OctreeNode;
import VoxelSystem.Data.VoxelExtraction.StrictOctreeExtractor;
import VoxelSystem.Data.VoxelExtraction.VoxelExtractor;
import VoxelSystem.Misc.CameraInfo;
import VoxelSystem.SparseData.ChunkData.Chunk;
import VoxelSystem.SparseData.ChunkData.RenderNode;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.Mesh.Mode;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

public class BasicDataLayer implements SparseInterface{
	private float minVoxelSize;
	private int maxDepth;
	
	private Map<Chunk,ChunkRequester> trackedChunks;
	private Map<Integer,Chunk> octreeChunks;
	private List<Chunk> dirtyChunks;
	
	private final float chunkSize;//currently cubic in nature. 
	private final int CHUNK_DIM = 512;
	private final int bitOffSet = 10;
	private final int CHUNK_MASK = 0x3FF;//10 bits -> 1024 chunks
	private final int CHUNK_OFF = CHUNK_MASK/2;
	
	private CollapsePolicy cp;
	private VoxelExtractor globalExtractor;
	private int depth = 0;
	
	public BasicDataLayer(float minVoxelSize, CollapsePolicy cp){
		//make each chunk 512x512x512 of base depth
		this.minVoxelSize = minVoxelSize;
		this.maxDepth = 8;
		chunkSize = CHUNK_DIM*minVoxelSize;
		dirtyChunks = new ArrayList<Chunk>();
		trackedChunks = new HashMap<Chunk,ChunkRequester>();
		octreeChunks = new HashMap<Integer,Chunk>();
		this.cp = cp;
	}
	
	
	@Override
	public List<Chunk> getIntersecting(BoundingBox bv) {
		List<Integer> keys = getIntersectingChunks(bv);
		ArrayList<Chunk> chunks = new ArrayList<Chunk>();
		
		for(int i : keys){
			Chunk c = this.octreeChunks.get(i);
			if(c == null){
				c = createChunkFromHash(i);
				this.octreeChunks.put(i, c);
			}
			
			chunks.add(c);
		}
		
		return chunks;
	}



	@Override
	public boolean registerChunkRequester(Chunk c, ChunkRequester ce) {
		if(trackedChunks.get(c)!=ce){
			trackedChunks.put(c, ce);
			dirtyChunks.add(c);
			return true;
		}
		return false;
	}

	@Override
	public void unregisterChunkRequester(Chunk c) {
		trackedChunks.remove(c);
	}
	
	@Override
	public Chunk[] getTrackedChunks() {
		return trackedChunks.keySet().toArray(new Chunk[0]);
	}
	
	@Override
	public Vector3f getChunkSize() {
		return new Vector3f(chunkSize,chunkSize,chunkSize);
	}

//	public void applyOperation(CSGOperator operator, BoundingBox bv, int level, VoxelExtractor ... args){
//		if(bv == null){
//			throw new RuntimeException("Hold on!");
//		}else{
//			//Build Maximal Bounding Box by iterative search
//			//Start at center and iterate outward by chunk...
//			List<Integer> keys = getIntersectingChunks(bv);
//			for(Integer key : keys){
//				Chunk c = this.octreeChunks.get(key);
//				if(c == null){
//					c = createChunkFromHash(key);
//					this.octreeChunks.put(key, c);
//				}
//				
//				
//				//
//				//Not sure..
//				//Should do operator.operate(this.getVoxelExtractor(),otherArgs)
//				//Which then outputs a bounding volume...
//				//
//				VoxelExtractor [] args2;
//				if(args != null){
//					args2 = new VoxelExtractor[args.length+1]; 
//					
//					for(int i = 0; i< args.length; i++){
//						args2[i+1]=args[i];
//					}
//				}else{
//					args2 = new VoxelExtractor[1];
//				}
//				args2[0] = c.getExtractor();
//				VoxelExtractor ve = operator.operate(args2);
//				//TODO: do during update!!
//				BoundingBox intersection = new BoundingBox();
//				BoundingBox chunk = new BoundingBox();
//				float f = c.getChunkContents().getCubeLength();
//				chunk.setCenter(c.getChunkContents().getCorner().add(f,f,f));
//				chunk.setXExtent(f/2f);chunk.setYExtent(f/2f);chunk.setZExtent(f/2f);
//				CSGHelpers.getIntersection(bv,chunk, intersection);
//				c.getChunkContents().extractVoxelData(ve, level, intersection, cp);
//				c.setExtractor(new StrictOctreeExtractor(c.getChunkContents()));
//				//
//				//
//				
//				dirtyChunks.add(c);
//			}
//			
//		}
//	}
	
	public void set(VoxelExtractor ve, int depth){
		if(depth > maxDepth){
			depth = maxDepth;
		}
		this.depth = depth;
		
		this.globalExtractor = ve;
	}
	

	public void update(){
		for(Chunk c : dirtyChunks){
			boolean change = c.getChunkContents().extractVoxelData(globalExtractor, depth, cp);
			
			if(change){
				ChunkRequester cr = this.trackedChunks.get(c);
				if(cr != null){
					cr.update(c);
				}
			}
		}
		dirtyChunks.clear();
	}
	
	public Mesh getOctreeMesh(boolean onlyLeaves,boolean onlyChunks, CameraInfo cI){
		Mesh m = new Mesh();
		m.setMode(Mode.Lines);
		ArrayList<Vector3f> children = new ArrayList<Vector3f>();
		for(Chunk c : this.trackedChunks.keySet()){
			recursiveGrabTree(c.getChunkContents(), onlyLeaves, onlyChunks, cI, children);
		}
		m.setBuffer(Type.Position, 3,  BufferUtils.createFloatBuffer(children.toArray(new Vector3f[0])) );
		return m;
		
	}
	
	public Mesh getOctreeVerts(float f){
		Mesh m = new Mesh();
		m.setMode(Mode.Points);
		ArrayList<Vector3f> children = new ArrayList<Vector3f>();
		for(Chunk c : this.trackedChunks.keySet()){
			recursiveGrabPoint(c.getChunkContents(), children,f);
		}
		m.setBuffer(Type.Position, 3,  BufferUtils.createFloatBuffer(children.toArray(new Vector3f[0])) );
		m.setPointSize(5f);
		return m;
	}
	
	
	
	//
	//Private Helpers:
	//
	private List<Integer> getIntersectingChunks(BoundingBox bb){
		Vector3f min = bb.getMin(new Vector3f());
		Vector3f max = bb.getMax(new Vector3f());
	
		//First clamp to voxel space.
		int minX = ((int) (min.x/minVoxelSize)) - 1;
		int minY = ((int) (min.y/minVoxelSize)) - 1;
		int minZ = ((int) (min.z/minVoxelSize)) - 1;
		int maxX = ((int) (max.x/minVoxelSize)) + 1;
		int maxY = ((int) (max.y/minVoxelSize)) + 1;
		int maxZ = ((int) (max.z/minVoxelSize)) + 1;
		
		//Then clamp to chunk space:
		minX = (int) Math.floor((float)minX/CHUNK_DIM);
		minY = (int) Math.floor((float)minY/CHUNK_DIM);
		minZ = (int) Math.floor((float)minZ/CHUNK_DIM);
		
		maxX = (int) Math.floor((float)maxX/CHUNK_DIM);
		maxY = (int) Math.floor((float)maxY/CHUNK_DIM);
		maxZ = (int) Math.floor((float)maxZ/CHUNK_DIM);
		
		
		//Then iterate over all the chunks...]
		ArrayList<Integer> chunks = new ArrayList<Integer>();
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					chunks.add(hash(x, y, z));
				}
			}
		}
		
		return chunks;
	}

	private BasicChunk createChunkFromHash(int hash){
		int oX = (hash & CHUNK_MASK);
		int oY = ((hash>>bitOffSet) & CHUNK_MASK);
		int oZ = ((hash>>(bitOffSet*2)) & CHUNK_MASK);
		oX -= CHUNK_OFF;
		oY -= CHUNK_OFF;
		oZ -= CHUNK_OFF;
		
		BasicChunk c = new BasicChunk(oX,oY,oZ,hash);
		Vector3f lowCorner = new Vector3f(chunkSize*oX,chunkSize*oY,chunkSize*oZ);
		c.setChunkContents(new BasicVoxelTree(lowCorner, chunkSize, 0));
		
		if(globalExtractor !=null){
			c.ve = globalExtractor;
		}else{
			c.ve = new StrictOctreeExtractor(c.getChunkContents());
		}
		
		return c;
	}
	
	/***
	 * Takes chunk space coords
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	private int hash(int x, int y, int z){
		return  ((x+CHUNK_OFF)& CHUNK_MASK) |
				((y+CHUNK_OFF)<<bitOffSet) |
				((z+CHUNK_OFF) << (bitOffSet*2));
	}
	
	/// End Of Private Helpers
	
	//Internal Implementation of chunk
	private class BasicChunk extends Chunk{
		ChunkState state;
		RenderNode rn;
		OctreeNode node;
		VoxelExtractor ve;
		int hash;
		public BasicChunk(int x, int y, int z, int hashCode){
			super(x,y,z);
			state = ChunkState.NONE;
			this.hash = hashCode;
			rn = new RenderNode();
			
		}
		
		@Override
		public ChunkState getChunkState() {
			return state;
		}
	
		@Override
		public OctreeNode getChunkContents() {
			return node;
		}
	
		@Override
		public void setChunkContents(OctreeNode contents) {
			this.node = contents;
			rn.node = contents;
		}
	
		@Override
		public int getMaxLevel() {
			return maxDepth;
		}
	
		@Override
		public RenderNode getRenderNode() {
			return rn;
		}
		
		public int hashCode(){
			return hash;
		}

		
		@Override
		public VoxelExtractor getExtractor() {
			return ve;
		}

		@Override
		public void setExtractor(VoxelExtractor ve) {
			this.ve = ve;
		}
	}

	/**
	 * Helper for displaying debug info from currently tracked octree's
	 */
	private void recursiveGrabPoint(OctreeNode node, ArrayList<Vector3f> points,  float f){
		if(node.isLeaf() ){//|| node.getGeometricError() < f
			if(node.getIsopoint() != null){
				points.add(node.getIsopoint());
			}
		}else{
			for(OctreeNode c : node.getChildren()){
				recursiveGrabPoint(c, points,f);
			}
		}
	}
	
	/**
	 * Helper for displaying debug info from currently tracked octree's
	 */
	private void recursiveGrabTree(OctreeNode node, boolean onlyLeaves, boolean onlyChunks,CameraInfo cI, ArrayList<Vector3f> points){
		if(onlyChunks || node.isLeaf()||cI.shouldBeLeaf(node)){
			if(onlyLeaves){
				if(node.getIsopoint() != null){
					
					for(int i =0; i<12; i++){ //draw all edges :D
						int p1 = VoxelSystemTables.iTable[i*2];
						int p2 = VoxelSystemTables.iTable[i*2 + 1];
						points.add(node.getCorner(p1, new Vector3f()));
						points.add(node.getCorner(p2, new Vector3f()));
					}
					
				}
			}else{
				for(int i =0; i<12; i++){ //draw all edges :D
					int p1 = VoxelSystemTables.iTable[i*2];
					int p2 = VoxelSystemTables.iTable[i*2 + 1];
					points.add(node.getCorner(p1, new Vector3f()));
					points.add(node.getCorner(p2, new Vector3f()));
				}
			}
		}else{
			for(OctreeNode c : node.getChildren()){
				recursiveGrabTree(c, onlyLeaves,onlyChunks,cI,points);
			}
		}
	}


	@Override
	public Chunk getChunk(int cx, int cy, int cz) {
		int hash = hash(cx,cy,cz);
		return octreeChunks.get(hash);
	}
	


}


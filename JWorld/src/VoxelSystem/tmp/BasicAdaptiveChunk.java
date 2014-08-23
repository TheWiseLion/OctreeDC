package VoxelSystem.tmp;

import java.util.HashMap;
import java.util.Map;

import VoxelSystem.SparseData.SparseInterface;
import VoxelSystem.SparseData.ChunkData.Chunk;
import VoxelSystem.SparseData.controller.LODController;
import VoxelSystem.threading.GeometryManager;

import com.jme3.material.Material;

public class BasicAdaptiveChunk implements AdaptiveMeshInterface{
	
	Chunk c;
	SparseInterface si;
	Map<RenderNode, RenderNode []> neighborMap;
	public BasicAdaptiveChunk(Chunk chunk, SparseInterface si){
		this.c = chunk;
		this.si = si;
		neighborMap = new HashMap<RenderNode, RenderNode[]>();
	}
	
	@Override
	public void update(LODController lod, GeometryManager gm, Material Register) {
		
	}
	
	@Override
	public RenderNode getNeighbor(int x, int y, int z, RenderNode rn) {
		RenderNode [] neighbors = neighborMap.get(rn);
		return neighbors[x + y<<1 + z <<2 ];
	}
	
	
}

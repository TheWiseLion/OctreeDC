package VoxelSystem.SurfaceExtractors;

import java.util.List;

import com.jme3.math.Vector3f;

import VoxelSystem.Data.Storage.OctreeNode;

public interface SurfaceExtractor {
	public List<Vector3f> extractSurface(OctreeNode tree, float geoError);
}

package VoxelSystem.Misc;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;

/**
 * Very simple class that holds view box.
 * Its very questionable whether or not this is actually needed.
 * Might be useful if more info is ever needed.
 *
 */
public class CameraInfo {
	private volatile BoundingBox viewBox;
	
	public CameraInfo(){
		viewBox =new BoundingBox();
	}
	
	public Vector3f getPosition(Vector3f store){
		viewBox.getCenter(store);
		return store;
	}
	
	
	public void setPosition(Vector3f pos){
		viewBox.setCenter(pos);
	}
	
	public Vector3f getViewDistance(Vector3f store){
		return viewBox.getExtent(store).multLocal(2);
	}
	
	public void setViewDistance(Vector3f viewDistance){
		viewBox.setXExtent(viewDistance.x);
		viewBox.setYExtent(viewDistance.y);
		viewBox.setZExtent(viewDistance.z);
		
	}
	
	public BoundingBox getViewBox(BoundingBox out){
		out.setCenter(viewBox.getCenter());
		out.setXExtent(viewBox.getXExtent());
		out.setYExtent(viewBox.getYExtent());
		out.setZExtent(viewBox.getZExtent());
		return out;
	}
}

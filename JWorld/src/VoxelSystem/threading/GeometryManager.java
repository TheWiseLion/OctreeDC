package VoxelSystem.threading;

import java.util.ArrayList;
import java.util.List;

import com.jme3.scene.Geometry;

/***
 * Rendering Node must only be modified during update.
 * This holds references geometrys that want to either be added or removed from a Node.
 */
public class GeometryManager {
	List<Geometry> addList = new ArrayList<Geometry>();
	List<Geometry> removeList = new ArrayList<Geometry>();
	
	
	public synchronized void queueDelete(List<Geometry> node) {
		addList.addAll(node);
	}
	
	public synchronized void queueAdd(List<Geometry> node) {
		removeList.addAll(node);
	}
	
	//returns remove list and clears the list contained in this manager
	public synchronized Geometry[] grabRemoveList() {
		Geometry[] geometry =  removeList.toArray(new Geometry[0]);
		removeList.clear();
		return geometry;
	}
	
	//returns add list and clears the list contained in this manager
	public synchronized Geometry[] grabAddList() {
		Geometry[] geometry =  addList.toArray(new Geometry[0]);
		addList.clear();
		return geometry;
	}
	
}

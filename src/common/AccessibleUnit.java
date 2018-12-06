package common;

import java.util.ArrayList;
import java.util.List;

public class AccessibleUnit {
	private String start;
	private String end;
	private List<ArrayList<String>> paths = new ArrayList<ArrayList<String>>();
	private List<AllowedRoom> allowedSpace = new ArrayList<AllowedRoom>();
	public AccessibleUnit(String start, String end) {
		this.setStart(start);
		this.setEnd(end);
	}
	
	//可能返回null，也可能返回allowedRoom
	public AllowedRoom findAllowedRoom(ArrayList<String> pathOne) {
		AllowedRoom allowed = null;
		for(int i=0; i<this.paths.size();i++) {
			if(this.comparePath(this.paths.get(i), pathOne)){
				allowed = this.allowedSpace.get(i);
				break;
			}
		}
		return allowed;
	}

	private boolean comparePath(ArrayList<String> pathOne, ArrayList<String> pathTwo){
		if(pathOne.size()!= pathTwo.size()) {
			return false;
		}
		int size = pathOne.size();
		for(int i=0;i<size;i++) {
			if(pathOne.get(i) != pathTwo.get(i)) {
				return false;
			}
		}
		return true;
	}
	
	public void add(ArrayList<String> path, AllowedRoom allowedRoom) {
		if(path==null || allowedRoom==null) {
			return;
		}
		paths.add(path);
		allowedSpace.add(allowedRoom);
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}

	public List<ArrayList<String>> getPaths(){
		return this.paths;
	}
	
	public List<AllowedRoom> getAllowedRoom(){
		return this.allowedSpace;
	}
	
}

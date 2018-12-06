package common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/*
 * 1. SearchAllPath a = new SearchAllPath()
 * 2. a.start("topo.json")
 * 3. a.copy(String[] hostArray)
 * 4. a.getAccessibleRoom()
 * 5  a.getAccessibleRoomBySrcDst()
 * 6  a.getAllowedRoom(String hostSrc, String hostDst)
 * 7  a.getAllAccessiblePath(String hostSrc, String hostDst)
 * 
 */
public class SearchAllPath {
	//switchName, 连接的switch
	private Map<String, Map<String, String>> switchMap;
	private Map<String, SearchUnit> switchSearch;
	private AccessibleUnit[][] accessibleRoom;
	private String[] hostArray;
	
	public Map<String, Map<String, String>> getSwitchs(){
		return switchMap;
	}

	public SearchAllPath() {
		switchMap = new HashMap<String, Map<String,String>>();
		switchSearch = new HashMap<String, SearchUnit>();
	}
	
	public void start(String fileName) {
		try {
			JSONObject topo = JSONFile.getJSONObjectByString(fileName);
			for(Object switchBody: topo.keySet()) {
				Map<String, String> switchPort = new HashMap<String, String>();
				String switchName = String.valueOf(switchBody);
				//System.out.println("switchName is" + switchName);
				JSONObject switchInfo = topo.getJSONObject(switchName);
				JSONObject connections = switchInfo.getJSONObject("connections");
				JSONArray flows = switchInfo.getJSONArray("flows");
				List<String> portMapNum = new ArrayList<String>();
				for(Object connection: connections.keySet()) {
					String connectName = String.valueOf(connection);
					JSONObject connectInfo = connections.getJSONObject(connectName);
					String node = connectInfo.getString("node");
					String port = connectInfo.getString("port");
					portMapNum.add(connectName);
					switchPort.put(node, port);
				}
				AllowedRoom[][] allowedSequence = this.genAllowedSpace(flows, portMapNum);
				switchMap.put(switchName, switchPort);
				SearchUnit unit = new SearchUnit(switchName, switchPort, switchSearch
						, allowedSequence, portMapNum);
				switchSearch.put(switchName, unit);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private AllowedRoom[][] genAllowedSpace(JSONArray flows, List<String> portMapNum){
		//System.out.println("portMap is");
		//System.out.println(portMapNum);
		int size = portMapNum.size();
		AllowedRoom[][] returnRoom = new AllowedRoom[size][size];
		for(int i=0;i<size;i++) {
			returnRoom[i] = new AllowedRoom[size];
			for(int j=0;j<size;j++) {
				returnRoom[i][j] = new AllowedRoom();
			}
		}
		for(Object flow: flows) {
			JSONObject flowObj = JSONObject.fromObject(flow);
			if(String.valueOf(flowObj.get("priority")).equals("0")) {
				String inputPort = flowObj.getString("port");
				//System.out.println("inputPort is" + inputPort);
				String match = flowObj.getString("match");
				String outputPort = flowObj.getString("action");
				//System.out.println("outputPort is" + outputPort);
				if(portMapNum.contains(outputPort)) {
					int inputIndex = portMapNum.indexOf(inputPort);
					//System.out.println("input index is" + inputIndex);
					int outputIndex = portMapNum.indexOf(outputPort);
					//System.out.println("output index is " + outputIndex);
					returnRoom[inputIndex][outputIndex].add(match);
				}
			}
		}
		return returnRoom;
		
		
	}
	
	//一般都是两个主机
	private void search(String start, String end, AccessibleUnit returnUnit) {
		SearchUnit startUnit = this.switchSearch.get(start);
		if(startUnit==null) {
			System.out.println("start unit is null");
		}
		AllowedRoom startRoom = new AllowedRoom();
		//!!!!error
		//System.out.println("start is " + start);
		//System.out.println("end is " + end);
		startRoom.add("xxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		ArrayList<String> intList = new ArrayList<String>();
		startUnit.search(intList, end, startRoom,"", returnUnit);
	}
	
	public void copy(String[] hostArray) {
		this.hostArray = hostArray;
		int hostNum = hostArray.length;
		AccessibleUnit[][] accessibleRoom = new AccessibleUnit[hostNum][hostNum];
		for(int i=0;i<hostNum;i++) {
			accessibleRoom[i] = new AccessibleUnit[hostNum];
			String hostSrc = hostArray[i];
			for(int j=0;j<hostNum;j++) {
				String hostDst = hostArray[j];
				accessibleRoom[i][j] = new AccessibleUnit(hostSrc, hostDst);
				if(i==j) {
					accessibleRoom[i][j].add(null, null);
				}else {
					this.search(hostSrc, hostDst, accessibleRoom[i][j]);
				}
			}
		}
		this.setAccessibleRoom(accessibleRoom);
	}

	public AccessibleUnit[][] getAccessibleRoom() {
		return accessibleRoom;
	}

	public void setAccessibleRoom(AccessibleUnit[][] accessibleRoom) {
		this.accessibleRoom = accessibleRoom;
	}
	
	public AccessibleUnit getAccessibleRoomBySrcDst(String hostSrc, String hostDst) throws Exception {
		int src = this.indexOfHost(hostSrc);
		int dst = this.indexOfHost(hostDst);
		if(src==-1||dst==-1) {
			throw new Exception("找不到对应的主机");
		}
		return this.accessibleRoom[src][dst];
	}
	
	public List<ArrayList<String>>  getAllAccessiblePath(String hostSrc, String hostDst){
		try {
			AccessibleUnit unit = getAccessibleRoomBySrcDst(hostSrc, hostDst);
			return unit.getPaths();
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<AllowedRoom>  getAllowedRoom(String hostSrc, String hostDst){
		try {
			AccessibleUnit unit = getAccessibleRoomBySrcDst(hostSrc, hostDst);
			return unit.getAllowedRoom();
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private int indexOfHost(String host) {
		int size = this.hostArray.length;
		int index = -1;
		for(int i=0;i<size;i++) {
			if(host.equals(this.hostArray[i])) {
				index = i;
				break;
			}
		}
		return index;
	}

/*	public static void main(String[] args) {
		SearchAllPath searchAllPath = new SearchAllPath();
		searchAllPath.start("topo.json");
		System.out.println("from hostOne to hostTwo");
		searchAllPath.search("hostOne", "hostTwo");
		
		System.out.println("from hostTwo to hostOne");
		searchAllPath.search("hostTwo", "hostOne");
		
	}*/
}

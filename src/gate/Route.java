package gate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.SearchAllPath;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class Route {
	public static int globalNum = 0;
	//String is :vnfcA-vnfcB 包含的路径
	private Map<String,List<Integer>> usefulRoom = new HashMap<String,List<Integer>>();
	
	//vnfcName hostName
	private Map<String, String> vnfcToHost = new HashMap<String, String>();
	
	//通过contruct构建出发往topo的请求
	private JSONArray routeChoice;
	
	private String routeChoiceName;
	private SearchAllPath searchEngine;
	public Route(SearchAllPath searchEngine) {
		this.searchEngine = searchEngine;
		this.setRouteChoiceName("routeChoice" + String.valueOf(globalNum));
		globalNum++;
	}
	
	public void add(String src, String dst, List<Integer> usefulRoomNum
			, String hostSrc, String hostDst) {
		String route = src + "-" + dst;
		String hostEnd = hostSrc + "-" + hostDst;
		usefulRoom.put(route, usefulRoomNum);
		vnfcToHost.put(route, hostEnd);
	}
	
	public int size() {
		return usefulRoom.size();
	}
	
	public void print() {
		for(String room: usefulRoom.keySet()) {
			System.out.println("route part is " + room);
			System.out.println(usefulRoom.get(room));
		}
	}
	
	public void construct(List<Double> band) {
		JSONArray routeChoice = new JSONArray();
		int routeCount = 0 ;
		for(String route: usefulRoom.keySet()) {
			JSONObject routeObj = new JSONObject();
			String routeName = "route" + String.valueOf(routeCount);
			routeObj.put("routeName", routeName);
			routeObj.put("band", band.get(routeCount));
			routeCount++;

			String hostInfo = this.vnfcToHost.get(route);
			String hostSrc = hostInfo.split("-")[0];
			String hostDst = hostInfo.split("-")[1]; 
			List<ArrayList<String>> allList = this.searchEngine.getAllAccessiblePath(hostSrc, hostDst);
			
			JSONObject paths = new JSONObject();
			String routeSrc = null;
			String routeDst = null;
			for(Integer num: this.usefulRoom.get(route)) {
				String pathName = "path" +String.valueOf(num);
				JSONArray path = new JSONArray();
				
				ArrayList<String> listAvail = allList.get(num);
				int listSize = listAvail.size();
				String[] array =new String[listSize];
				listAvail.toArray(array);

				for(int i=0; i<listSize-2; i++) {
					String nodeOne = array[i];
					String nodeTwo = array[i+1];//真正的switch
					String nodeThree = array[i+2];
					String portInput = searchEngine.getSwitchs().get(nodeOne).get(nodeTwo);
					if(i==0) {
						routeSrc = nodeTwo + "-" + portInput;
					}
					String portOutput = searchEngine.getSwitchs().get(nodeThree).get(nodeTwo);
					if(i==(listSize-3)) {
						routeDst = nodeTwo + "-" + portOutput;
					}
					JSONObject switchObj = new JSONObject();
					switchObj.put("start", portInput);
					switchObj.put("end", portOutput);
					switchObj.put("switch", nodeTwo);
					path.add(switchObj);
				}
				paths.put(pathName, path);
			}
			routeObj.put("paths", paths);
			routeObj.put("src", routeSrc);
			routeObj.put("dst", routeDst);
			//还需要添加带宽
			routeChoice.add(routeObj);
		}
		this.setRouteChoice(routeChoice);
	}

	public String getRouteChoiceName() {
		return routeChoiceName;
	}

	public void setRouteChoiceName(String routeChoiceName) {
		this.routeChoiceName = routeChoiceName;
	}

	public JSONArray getRouteChoice() {
		return routeChoice;
	}

	public void setRouteChoice(JSONArray routeChoice) {
		this.routeChoice = routeChoice;
	}
}

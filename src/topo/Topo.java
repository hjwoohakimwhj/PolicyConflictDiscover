package topo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import Switch.Link;
import Switch.Space;
import Switch.Switch;
import common.JSONFile;
import common.SearchAllPath;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/*
 * 初始化
 * 1 Topo a = new Topo("topo.json")
 * 2 a.copyTopo()
 * 
 * 动态响应
 * copyRoutes
 * 
 */
public class Topo {
	JSONObject topo;
	public HashMap<String, Switch> switchs = new HashMap<String, Switch>();
	
	public Topo() {
	}
	
	public Topo(String fileName) {
		try {
			topo = JSONFile.getJSONObjectByString(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * 
	 * switchName:
	 * 		flows:
	 * 		connections:
	 * 			portName:	
	 * 				node: switch1
	 * 				port: port1
	 * 				bandTotal:
	 * 				bandUsed:
	 * 
	 */
	public void copyTopo() {
		for(Object switchName: topo.keySet()) {
			String switchStr = String.valueOf(switchName);
			JSONObject switchJSON = JSONObject.fromObject(topo.get(switchName));
			JSONArray flows = JSONArray.fromObject(switchJSON.get("flows"));
			JSONObject portJSON = switchJSON.getJSONObject("connections");
			Map<String, Link> portLink = new HashMap<String, Link>();
			for(Object portName: portJSON.keySet()) {
				String portStr = String.valueOf(portName);
				JSONObject connection = JSONObject.fromObject(portJSON.get(portName));
				String switchDst = connection.getString("node");
				String portDst = connection.getString("port");
				Double bandTotal = connection.getDouble("bandTotal");
				Double bandUsed = connection.getDouble("bandUsed");
				Link link = new Link(switchStr, portStr, switchDst, portDst, bandTotal, bandUsed);
				portLink.put(portStr, link);
			}
			Switch switchNew = new Switch(switchStr, portLink, flows);
			switchs.put(switchStr, switchNew);
		}
	}
	
	public boolean copyRoutesMapNoRule(JSONObject choices,ArrayList<ArrayList<String>> securitySpaces
			, SearchAllPath searchEngine) {
		//其实只有一个choice
		for(Object choice : choices.keySet()) {
			System.out.println("choice is " + String.valueOf(choice));
			JSONArray choiceOne = JSONArray.fromObject(choices.get(choice));
			boolean success = this.copyOneRouteMapNoRule(choiceOne,securitySpaces, searchEngine);
			if(!success) {
				return false;
			}
		}
		return true;
	}
	
	
	public boolean copyRoutesRuleNoMap(JSONObject choices,ArrayList<ArrayList<String>> securitySpaces
			, SearchAllPath searchEngine) {
		//其实只有一个choice
		for(Object choice : choices.keySet()) {
			System.out.println("choice is " + String.valueOf(choice));
			JSONArray choiceOne = JSONArray.fromObject(choices.get(choice));
			boolean success = this.copyOneRouteRuleNoMap(choiceOne,securitySpaces, searchEngine);
			if(!success) {
				return false;
			}
		}
		return true;
	}
	
	public boolean copyOneRouteRuleNoMap(JSONArray routes,ArrayList<ArrayList<String>> securitySpaces
			, SearchAllPath searchEngine) {
		for(int i=0;i<securitySpaces.size()-1;i++) {
			JSONObject pathOptions = JSONObject.fromObject(routes.get(i)).getJSONObject("paths");
			//System.out.println("***********paths is ***********");
			//System.out.println(pathOptions);
			//其实只有一条
			for(Object pathObj: pathOptions.keySet()) {
				String pathName = String.valueOf(pathObj);
				Path path = new Path(pathName);
				JSONArray nodes = pathOptions.getJSONArray(pathName);
				boolean success = path.ruleNoMapCheckAvail(nodes, securitySpaces.get(i), switchs);
				if(!success) {
					return false;
				}
			}
		}
		for(int i=0;i<securitySpaces.size()-1;i++) {
			JSONObject pathOptions = JSONObject.fromObject(routes.get(i)).getJSONObject("paths");
			//System.out.println("***********paths is ***********");
			//System.out.println(pathOptions);
			//其实只有一条
			for(Object pathObj: pathOptions.keySet()) {
				String pathName = String.valueOf(pathObj);
				Path path = new Path(pathName);
				JSONArray nodes = pathOptions.getJSONArray(pathName);
				path.ruleNoMapAddFlows(nodes, securitySpaces.get(i), switchs);
			}
		}
		return true;
	}
	
	public boolean copyOneRouteMapNoRule(JSONArray routes,ArrayList<ArrayList<String>> securitySpaces
			, SearchAllPath searchEngine) {
		
		for(int i=0;i<securitySpaces.size()-1;i++) {
			JSONObject pathOptions = JSONObject.fromObject(routes.get(i)).getJSONObject("paths");
			//System.out.println("***********paths is ***********");
			//System.out.println(pathOptions);
			//其实只有一条
			for(Object pathObj: pathOptions.keySet()) {
				String pathName = String.valueOf(pathObj);
				Path path = new Path(pathName);
				JSONArray nodes = pathOptions.getJSONArray(pathName);
				boolean success = path.mapNoRuleCheckAvail(nodes, securitySpaces.get(i), switchs);
				if(!success) {
					return false;
				}
			}
		}
		//！！！！！！在这个地方其实已经成功了
		for(int i=0;i<securitySpaces.size()-1;i++) {
			JSONObject pathOptions = JSONObject.fromObject(routes.get(i)).getJSONObject("paths");
			//System.out.println("***********paths is ***********");
			//System.out.println(pathOptions);
			//其实只有一条
			for(Object pathObj: pathOptions.keySet()) {
				String pathName = String.valueOf(pathObj);
				Path path = new Path(pathName);
				JSONArray nodes = pathOptions.getJSONArray(pathName);
				path.mapNoRuleAddFlows(nodes, securitySpaces.get(i), switchs);
			}
		}
		return true;
	}

	/*
	 * choice1:
	 * 		route:
	 * 			route...
	 * 		paths:
	 * 			paths...
	 */
	public ArrayList<String> copyRoutes(JSONObject choices,ArrayList<ArrayList<String>> securitySpaces) {
		Double bandLeast = (double)1000;
		String choiceRoute = null;
		ArrayList<String> routeChoosenPaths = null;
		for(Object choice : choices.keySet()) {
			System.out.println("choice is " + String.valueOf(choice));
			JSONArray choiceOne = JSONArray.fromObject(choices.get(choice));
			ArrayList<String> routePaths = new ArrayList<String>();
			Double bandTotal = this.copyOneRoute(choiceOne,securitySpaces, routePaths);
/*			if(bandTotal<0) {
				System.out.println("route choice失效");
				continue;
			}*/
			System.out.println("choice " + String.valueOf(choice) + "band total is" + bandTotal);
			if(bandTotal<0) {
				System.out.println("这个route choice的端口带宽不够");
				continue;
			}
			if(bandTotal < bandLeast) {
				bandLeast = bandTotal;
				choiceRoute = String.valueOf(choice);
				routeChoosenPaths = routePaths;
			}
		}
		if(routeChoosenPaths==null && bandLeast>100) {
			System.out.println("所有的routeChoice都不行");
			return null;
		}
		System.out.println("Topo copyRoutes!!!!!! routeChoice is" + choiceRoute +" " + "bandList is " + bandLeast);
		System.out.println("这个route choice 的路径是 " + routeChoosenPaths);
		//选择到最优的路由时
		if(routeChoosenPaths==null) {
			return null;
		}
		//只有真正找到最优路径，才需要进行下面的步骤
		JSONArray paths = choices.getJSONArray(choiceRoute);
		int count = 0;
		ArrayList<String> hostNameList = new ArrayList<String>();
		for(Object pathObj : paths) {
			JSONObject pathJSON = JSONObject.fromObject(pathObj);
			this.getHostName(pathJSON.getString("src"),pathJSON.getString("dst"), hostNameList);
			JSONArray pathArray = pathJSON.getJSONObject("paths").getJSONArray(routeChoosenPaths.get(count));
			Path path = new Path(routeChoosenPaths.get(count));
			path.addFlows(pathArray, securitySpaces.get(count), switchs, pathJSON.getDouble("band"));
			//System.out.println("==============success==============");
			count++;
		}
		System.out.println("映射的主机是   " + hostNameList);
		//消耗映射到的主机上的资源
		System.out.println("==============success==============");
		return hostNameList;
	}

	private void getHostName(String src, String dst, ArrayList<String> hostNameList) {
		String switchSrc = src.split("-")[0];
		String portSrc = src.split("-")[1];
		String hostNameOne = this.switchs.get(switchSrc).getPortLink().get(portSrc).getDstSwitch();
		if(!hostNameList.contains(hostNameOne)) {
			hostNameList.add(hostNameOne);
		}
		
		String switchDst = dst.split("-")[0];
		String portDst = dst.split("-")[1];
		String hostNameTwo = this.switchs.get(switchDst).getPortLink().get(portDst).getDstSwitch();
		hostNameList.add(hostNameTwo);
	}
	
	/*
	 * 	route结构如下
	 * 	route1:
	 * 		src: A
	 * 		dst: B
	 * 		band: xx
	 *  route2:
	 *  	src: B
	 *  	dst: C
	 *  	band: yy
	 */
	public Double copyOneRoute(JSONArray routes, ArrayList<ArrayList<String>> securitySpaces
			, ArrayList<String> routePaths) {
		boolean breakFlag = false;
		Double bandTotal = (double)0.0;
		int vnfcOder = 0;
		Map<String, HashMap<String, Double>> switchPortUse = new HashMap<String, HashMap<String, Double>>();
		for(Object route: routes) {
			JSONObject pathOptions = JSONObject.fromObject(route).getJSONObject("paths");
			String path = this.searchLeastFlows(pathOptions, securitySpaces.get(vnfcOder));
			if(path.equals("")) {
				System.out.println("该条routeChoice失效，因为存在与wrongPortSpace冲突的表项");
				routePaths.add(path);
				return -1.0;
			}
			vnfcOder++;
			System.out.println("path choice is " + path);
			JSONArray switches = pathOptions.getJSONArray(path);
			int linkNumber = switches.size() + 1;
			Double band = JSONObject.fromObject(route).getDouble("band");
			
			for(Object switchOne: switches) {
				JSONObject switchJSON = JSONObject.fromObject(switchOne);
				String switchName = switchJSON.getString("switch");
				String start = switchJSON.getString("start");
				String end = switchJSON.getString("end");
				HashMap<String, Double> ports = switchPortUse.get(switchName);
				if(ports==null) {
					ports = new HashMap<String, Double>();
					ports.put(start, band);
					ports.put(end, band);
					switchPortUse.put(switchName,ports); 
					continue;
				}
				if(ports.get(start)==null) {
					ports.put(start, band);
				}else {
					ports.put(start,ports.get(start)+band);
				}

				if(ports.get(end)==null) {
					ports.put(end, band);
				}else {
					ports.put(end,ports.get(end)+band);
				}
			}
			for(String switchName: switchPortUse.keySet()) {
				Map<String,Link> portLink = this.switchs.get(switchName).getPortLink();
				for(String portName: switchPortUse.get(switchName).keySet()) {
					Double bandReq = switchPortUse.get(switchName).get(portName);
					//System.out.println("band req is " + bandReq);
					Double bandFree = portLink.get(portName).getBandTotal() - portLink.get(portName).getBandUsed();
					//System.out.println("band free is " + bandFree);
					if(bandReq > bandFree) {
						System.out.println("该端口带宽不足");
						breakFlag = true;
						break;
					}
				}
				if(breakFlag) {
					break;
				}
			}
			if(breakFlag) {
				break;
			}
			
			Double routeBand = band * linkNumber;
			bandTotal += routeBand;
			//确定每一段路由的具体路径
			routePaths.add(path);
		}
		if(breakFlag) {
			return -1.0;
		}else {
			return bandTotal;
		}
	}
	
	
/*	public Double mapping(JSONObject route, JSONObject path) {
		Double bandTotal = (double)0;
		for(Object routePart: route.keySet()) {
			JSONObject routeBody = route.getJSONObject(String.valueOf(routePart));
			String dst = routeBody.getString("dst");
			Double band = routeBody.getDouble("band");
			int linkNumber = 0;
			for(Object switchObject : path.keySet()) {
				JSONObject dstPath = JSONObject.fromObject(path.get(switchObject));
				if(dstPath.getString("dst").equals(dst)) {
					linkNumber++;
				}
			}
			Double bandAggre = band * linkNumber;
			bandTotal += bandAggre;
		}
		return bandTotal;
	}*/
	
	/*
	 *  paths的结构如下
	 * 	path1:
	 * 		switch1:
	 * 			start:	end:  dst:B
	 * 		switch2:
	 * 			start:  end:  dst:B
	 * 		......
	 * 
	 * 	path2:
	 * 	 	switch1:
	 * 			start:	end:
	 * 		switch2:
	 * 			start:  end:
	 * 
	 */
	public String searchLeastFlows(JSONObject paths, ArrayList<String> securitySpaces) {
		int flowLeast = 1000;
		String pathChoosen = "";
		for(Object pathObject: paths.keySet()) {
			String pathName = String.valueOf(pathObject);
			Path path = new Path(pathName);
			int flows = path.computeFlowsModified(paths.getJSONArray(pathName), securitySpaces, switchs);
			System.out.println("path name is " + pathName + " modified flows is " + flows);
			if(flows < flowLeast) {
				flowLeast = flows;
				pathChoosen = pathName;
			}
		}
		if(pathChoosen.equals("")) {
			System.out.println("所有可行的路径都因为和wrongPortSpace冲突，而无法部署");
		}
		return pathChoosen;
	}
	
/*	public static void main(String[] args) throws IOException {
		Topo topo = new Topo("topo.json");
		topo.copyTopo();
		
		ArrayList<String> securitySpaces = new ArrayList<String>();
		securitySpaces.add("10000x");
		System.out.println("securitySpaces is " + securitySpaces);
		
		JSONObject choices = JSONFile.getJSONObjectByString("choice.json");
		topo.copyRoutes(choices, securitySpaces);
		
	}*/
}

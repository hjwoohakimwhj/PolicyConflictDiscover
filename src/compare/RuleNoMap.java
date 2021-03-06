package compare;

import java.util.ArrayList;
import java.util.HashMap;

import common.JSONFile;
import gate.Host;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class RuleNoMap {
	private JSONArray hostFile;
	private JSONObject topoFile;
	private HashMap<String, RuleNoMapUnit> unitMap = new HashMap<String, RuleNoMapUnit>();
	private HashMap<String, RuleNoMapHost> hostMap = new HashMap<String, RuleNoMapHost>();
	private ArrayList<String> hostList = new ArrayList<String>();
	public RuleNoMap(String hostFilePath, String topoFile) {
		try {
			this.hostFile = JSONFile.getJSONArrayByString(hostFilePath);
			this.topoFile = JSONFile.getJSONObjectByString(topoFile);
		}catch(Exception e) {
			e.printStackTrace();
		}	
		this.copy();
	}
	
	private void copy() {
		for(Object nodeObj: this.topoFile.keySet()) {
			String nodeName = String.valueOf(nodeObj);
			JSONObject node = this.topoFile.getJSONObject(nodeName);
			JSONObject connections = node.getJSONObject("connections");
			RuleNoMapUnit unit = new RuleNoMapUnit(nodeName);
			unit.init(connections);
			unitMap.put(nodeName, unit);
		}
		
		for(Object hostObj: this.hostFile) {
			JSONObject hostJSON = JSONObject.fromObject(hostObj);
			RuleNoMapHost host = new RuleNoMapHost(hostJSON.getString("name"));
			host.setDisk(hostJSON.getDouble("disk"));
			host.setRam(hostJSON.getDouble("ram"));
			host.setvCPUs(hostJSON.getInt("vCPUs"));
			host.setPhysical(hostJSON.getInt("physical"));
			this.hostMap.put(hostJSON.getString("name"), host);
			this.hostList.add(hostJSON.getString("name"));
		}
	}
	
	public void start(String requestFile) {
		try {
			JSONObject req = JSONFile.getJSONObjectByString(requestFile);
			this.start(req);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//此处修改为随即选择host
	private String  findAvailHosts(ArrayList<String> vnfcHosts){
		String hostName;
		while(true) {
			int hostNum = this.hostList.size();
			int index = (int)(Math.random()*hostNum);
			hostName = this.hostList.get(index);
			if(vnfcHosts.contains(hostName)) {
				continue;
			}else {
				break;
			}
		}
		return hostName;
	}
	
	private boolean hostCheck(String host, JSONObject vnfc) {
		Double disk = vnfc.getDouble("disk");
		Double ram = vnfc.getDouble("ram");
		Integer cpus = vnfc.getInt("vCPUs");
		Integer physical = vnfc.getInt("physical");
		return this.hostMap.get(host).check(disk, ram, cpus, physical);
	}
	
	public ArrayList<String> find(String src, String dst, Double band) {
			ArrayList<String> path = this.search(src, dst, band);
			//path可能是null
			return path;
	}
	
	public String getHostName(String nodeName, String port) {
		return this.unitMap.get(nodeName).getLink(port).getDstNode();
	}
	
	public void refresh(JSONObject routeChoices, JSONObject req) {
		for(Object routeChoice: routeChoices.keySet()) {
			JSONArray choice = routeChoices.getJSONArray(String.valueOf(routeChoice));
			int routeNum = 0;
			for(Object route: choice) {
				JSONObject vnfcSrc = JSONObject.fromObject(req.getJSONArray("order").get(routeNum));
				JSONObject vnfcDst = JSONObject.fromObject(req.getJSONArray("order").get(routeNum + 1));
				
				JSONObject routeJSON = JSONObject.fromObject(route);
				if(routeNum==0) {
					String src = routeJSON.getString("src");
					String srcSwitch = src.split("-")[0];
					String srcPort = src.split("-")[1];
					String srcHost = this.getHostName(srcSwitch, srcPort);
					this.hostMap.get(srcHost).decrease(vnfcSrc.getDouble("disk")
							, vnfcSrc.getDouble("ram"), vnfcSrc.getInt("vCPUs"));
				}
				routeNum++;
				String dst = routeJSON.getString("dst");
				String dstSwitch = dst.split("-")[0];
				String dstPort = dst.split("-")[1];
				String dstHost = this.getHostName(dstSwitch, dstPort);
				this.hostMap.get(dstHost).decrease(vnfcDst.getDouble("disk")
						, vnfcDst.getDouble("ram"), vnfcDst.getInt("vCPUs"));



				Double band = routeJSON.getDouble("band");
				JSONObject paths = routeJSON.getJSONObject("paths");
				for(Object path: paths.keySet()) {
					JSONArray switches = paths.getJSONArray(String.valueOf(path));
					for(Object switchOne: switches) {
						JSONObject switchJSON = JSONObject.fromObject(switchOne);
						String start = switchJSON.getString("start");
						String end = switchJSON.getString("end");
						String switchEntry = switchJSON.getString("switch");
						this.unitMap.get(switchEntry).decrease(band, start);
						this.unitMap.get(switchEntry).decrease(band, end);
					}
				}
			}
			break;
		}
		System.out.println("消耗资源结束");
	}
	
	public ArrayList<String> search(String hostSrc, String hostDst, Double band) {
		ArrayList<String> hostBack = new ArrayList<String>();
		ArrayList<ArrayList<String>> resultList = new ArrayList<ArrayList<String>>();
		unitMap.get(hostSrc).search("", band, hostDst, hostBack, this.unitMap, resultList);
		//System.out.println("===============result is ===================");
		int size = 1000;
		ArrayList<String> path = null;
		for(ArrayList<String> result : resultList) {
			int num = result.size();
			if(num < size) {
				size = num;
				path = result;
			}
			//System.out.println(result);
		}
		if(size==1000) {
			System.out.println("主机 " + hostSrc + "到主机" + hostDst + "之间没有合适的路径");
			return null;
		}else {
			//System.out.println("找到总带宽消耗最小的路径了， 他是");
			//System.out.println(path);
			return path;
		}
	}
	
	public JSONObject start(JSONObject request) {
		//检查所有可以用的主机，选择其中的一对，这样就定下了起始的主机和终止的主机了
		JSONArray order = request.getJSONArray("order");
		int size = order.size();
		ArrayList<String> vnfcHosts = new ArrayList<String>();
		ArrayList<String> vnfcs = new ArrayList<String>();
		ArrayList<Double> bandList = new ArrayList<Double>();
		for(int i=0;i<size;i++) {
			JSONObject vnfc = order.getJSONObject(i);
			String vnfcName = vnfc.getString("vnfc");
			String host = findAvailHosts(vnfcHosts);
			System.out.println("vnfc is " + vnfcName + " hosts can be " + host);
			vnfcHosts.add(host);
			vnfcs.add(vnfcName);
			bandList.add(vnfc.getDouble("band"));
			boolean valid = this.hostCheck(host, vnfc);
			if(!valid) {
				System.out.println("所选择的主机不符合");
				return null;
			}
		}
		ArrayList<ArrayList<String>> suitPaths = new ArrayList<ArrayList<String>>();
		for(int i=0; i<size-1; i++) {
			//String srcName = vnfcs.get(i);
			//String dstName = vnfcs.get(i+1);
			String srcList = vnfcHosts.get(i);
			String dstList = vnfcHosts.get(i+1);
			ArrayList<String> path = this.find(srcList, dstList, bandList.get(i));
			if(path==null) {
				return null;
			}
			suitPaths.add(path);
		}
		
		if(suitPaths.size()==0) {
			//System.out.println("============error================");
			return null;
		}
		JSONObject routeChoices = new JSONObject();
		JSONArray routes = new JSONArray();
		int routeNum = 0;
		for(ArrayList<String> pathOne: suitPaths) {
			//System.out.println("============final result =============");
			//System.out.println(pathOne);
			JSONObject route = new JSONObject();
			route.put("routeName", "route" + routeNum);
			route.put("src", this.getByHost(pathOne.get(0)));
			route.put("dst", this.getByHost(pathOne.get(pathOne.size()-1)));
			route.put("band", bandList.get(routeNum));

			JSONObject paths = new JSONObject();
			JSONArray path = this.copyPath(pathOne);
			paths.put("path1", path);
			route.put("paths", paths);
		
			routes.add(route);
			routeNum++;
		}
		routeChoices.put("routeChoiceOne", routes);
		return routeChoices;
	}
	
	private String getByHost(String host) {
		String hostPortName = this.unitMap.get(host).getHostPort();
		RuleNoMapLink  link = this.unitMap.get(host).getLink(hostPortName);
		String switchNode = link.getDstNode();
		String switchPort = link.getDstPort();
		String src = switchNode + "-" + switchPort;
		return src;
	}
	
	private JSONArray copyPath(ArrayList<String> path) {
		JSONArray switches = new JSONArray();
		int nodeNum = path.size();
		for(int i=1;i<nodeNum-1;i++) {
			JSONObject switchOne = new JSONObject();
			String hostBefore = path.get(i-1);
			RuleNoMapUnit host = this.unitMap.get(path.get(i));
			String hostAfer = path.get(i+1);
			String portSrc = host.getPortSelf(hostBefore);
			String portDst = host.getPortSelf(hostAfer);
			switchOne.put("start", portSrc);
			switchOne.put("end", portDst);
			switchOne.put("switch", path.get(i));
			switches.add(switchOne);
		}
		return switches;
	}
}

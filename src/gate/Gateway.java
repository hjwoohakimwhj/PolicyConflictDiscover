package gate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import common.JSONFile;
import common.SearchAllPath;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import sender.Sender;
import topo.Topo;
import compare.MapNoRule;
import compare.RuleNoMap;

public class Gateway {
	//所有通过host.json配置文件创建的主机，用主机名作为索引，值为Host类型
	public Map<String, Host> hostList = new HashMap<String, Host>();
	
	//对所有请求进行存储，key为请求的名字，value为请求的信息
	public Map<String, Request> requests = new HashMap<String, Request>();
	
	public SearchAllPath searchEngine;
	public Topo topo;
	public MapNoRule mapNoRule;
	public RuleNoMap ruleNoMap;

	public Gateway() {
		searchEngine = new SearchAllPath();
		searchEngine.start("topoInfo_lab.json");
		topo = new Topo("topoInfo_lab.json");
		
/*		mapNoRule = new MapNoRule("host.json", "topo_lace.json");
		ruleNoMap = new RuleNoMap("host.json", "topo_lace.json");*/
		
		mapNoRule = new MapNoRule("hostInfo_lab.json", "topoInfo_lab.json");
		ruleNoMap = new RuleNoMap("hostInfo_lab.json", "topoInfo_lab.json");
		
	}
	
	/*
	 *configFile必须包含现有哪些主机，以及主机上的硬件资源情况 ,如何文件host.json
	 */
	public void init(String configFile) throws IOException {
		JSONArray hostArray = JSONFile.getJSONArrayByString(configFile);
		for(Object hostInfo: hostArray) {
			JSONObject hostJSON = JSONObject.fromObject(hostInfo);
			boolean physical = false;
			if(hostJSON.getInt("physical")==1) {
				physical = true;
			}
			Host host = new Host(hostJSON.getString("name"), hostJSON.getInt("vCPUs")
					, hostJSON.getDouble("ram"), hostJSON.getDouble("disk"), physical);
			hostList.put(hostJSON.getString("name"), host);
		}
		int hostNum = hostList.size();
		String[] hosts = new String[hostNum];
		int count = 0;
		for(String host: hostList.keySet()) {
			hosts[count] = host;
			count++;
		}
		//System.out.println(hosts[0]);
		this.searchEngine.copy(hosts);
		topo.copyTopo();
	}
	
	/*
	 * 开始接受请求，此处的请求是一个JSON配置文件
	 */
	public void start(String configFile) throws Exception {
		JSONObject request = JSONFile.getJSONObjectByString(configFile);
		this.start(request);
	}
	
	public boolean startRuleNoMap(JSONObject req) throws Exception {
		JSONObject routeChoices = this.ruleNoMap.start(req);
		if(routeChoices==null) {
			System.out.println("error");
			return false;
		}
		System.out.println(routeChoices);
		ArrayList<ArrayList<String>> securitySpaces = new ArrayList<ArrayList<String>>();
		for(Object vnfc: req.getJSONArray("order")) {
			ArrayList<String> flow = new ArrayList<String>();
			JSONObject vnfcJSON = JSONObject.fromObject(vnfc);
			JSONArray rules = vnfcJSON.getJSONArray("rules");
			for(Object rule: rules) {
				JSONObject ruleJSON = JSONObject.fromObject(rule);
				String match = ruleJSON.getString("match");
				flow.add(match);
			}
			securitySpaces.add(flow);
		}
		boolean result = this.topo.copyRoutesRuleNoMap(routeChoices, securitySpaces, this.searchEngine);
		if(!result) {
			System.out.println("已经选好的路径存在策略冲突，失败");
			return false;
		}
		System.out.println("成功,需要同步更新RuleNoMap模块里的结构");
		this.ruleNoMap.refresh(routeChoices, req);
		return true;
	}
	
	public boolean startMapNoRule(JSONObject req) throws Exception {
		JSONObject routeChoices = this.mapNoRule.start(req);
		if(routeChoices==null) {
			System.out.println("error");
			return false;
		}
		System.out.println(routeChoices);
		ArrayList<ArrayList<String>> securitySpaces = new ArrayList<ArrayList<String>>();
		for(Object vnfc: req.getJSONArray("order")) {
			ArrayList<String> flow = new ArrayList<String>();
			JSONObject vnfcJSON = JSONObject.fromObject(vnfc);
			JSONArray rules = vnfcJSON.getJSONArray("rules");
			for(Object rule: rules) {
				JSONObject ruleJSON = JSONObject.fromObject(rule);
				String match = ruleJSON.getString("match");
				flow.add(match);
			}
			securitySpaces.add(flow);
		}
		boolean result = this.topo.copyRoutesMapNoRule(routeChoices, securitySpaces, this.searchEngine);
		if(!result) {
			System.out.println("已经选好的路径存在策略冲突，失败");
			return false;
		}
		System.out.println("成功,需要同步更新MapNoRule模块里的结构");
		this.mapNoRule.refresh(routeChoices, req);
		return true;
	}
	
	/*
	 * 
	 * request must be:
	 * "name":"xxx",
	 * "order":[
	 * 		{
	 * 			"vnfc":"xxx",(字符串)
	 * 			"band":xxx,(浮点数)
	 * 			"disk":xxx,(浮点数)
	 * 			"vCPUs":xxx,(整数),
	 * 			"ram":xxx,(浮点数),
	 * 			"rules:{
	 * 			},
	 * 			"physical":0/1
	 * 		}
	 * 		
	 * 			
	 * 
	 * return boolean true->success
	 * false-> fail
	 * 
	 */
	public boolean start(JSONObject request) throws Exception{
		String requestName = request.getString("name");
		JSONArray order = request.getJSONArray("order");
		
		//存储vnfc之间的连接顺序
		List<String> vnfcOrder = new ArrayList<String>();
		HashMap<String, SecuritySpace> vnfcSpace = new HashMap<String, SecuritySpace>();
		
		//vnfcName, hostList,每一个vnfc都会为其寻找到所有可以映射的集合
		Map<String, HashSet<String>> vnfcAvailList = new HashMap<String, HashSet<String>>();
		List<Double> bandList = new ArrayList<Double>();
		for(Object vnfcObj: order) {
			JSONObject vnfcJSON = JSONObject.fromObject(vnfcObj);
			HashSet<String> availList = getAllAvailHost(vnfcJSON);

			if(availList.size()==0) {
				throw new Exception("无法找到合适的主机，映射失败");
			}
			String vnfcName = vnfcJSON.getString("vnfc");
			JSONArray vnfcRules = vnfcJSON.getJSONArray("rules");
			SecuritySpace space = new SecuritySpace(vnfcRules);
			Double band = vnfcJSON.getDouble("band");
			bandList.add(band);
			//System.out.println("find avail host is" + availList);
			vnfcAvailList.put(vnfcName, availList);
			vnfcOrder.add(vnfcName);
			vnfcSpace.put(vnfcName, space);
		}
		Request requestVNF = new Request(requestName, this.hostList, vnfcOrder, this.searchEngine, bandList);
		requestVNF.setVnfcAvailHosts(vnfcAvailList);
		requestVNF.setSecuritySpaces(vnfcSpace);
		requestVNF.findAllPaths();
		
		JSONObject req = requestVNF.generateReq();
		
		ArrayList<ArrayList<String>> inputSpaces = this.genInputSpace(vnfcSpace, vnfcOrder);
		
		ArrayList<String> hostNameList = topo.copyRoutes(req, inputSpaces);
		if(hostNameList==null) {
			System.out.println("映射失败");
			return false;
		}else {
			int vnfcOrderNum = 0;
			for(Object vnfcObj : order) {
				JSONObject vnfcJSON = JSONObject.fromObject(vnfcObj);
				String hostName = hostNameList.get(vnfcOrderNum);
				String vnfcName = vnfcJSON.getString("vnfc");
				Host host = this.hostList.get(hostName);
				System.out.println("主机 " + hostName + " 在映射了 vnfc之前：" + vnfcName 
						+ "剩余的CPU个数为" + host.getvCPUs()
						+ "剩余的disk为 " + host.getDisk()
						+ "剩余的ram为" + host.getRam());				
				host.consumeCPU(vnfcJSON.getInt("vCPUs"));
				host.consumeDisk(vnfcJSON.getDouble("disk"));
				host.consumeRAM(vnfcJSON.getDouble("ram"));
				System.out.println("主机 " + hostName + " 因为映射了 vnfc：" + vnfcName 
						+ "剩余的CPU个数为" + host.getvCPUs()
						+ "剩余的disk为 " + host.getDisk()
						+ "剩余的ram为" + host.getRam());
				vnfcOrderNum++;
			}
		}
		
		//！！！！！！！！！！！！！！this place 发送请求给 topo
		
		System.out.println(req);
		return true;
	}
	
	private ArrayList<ArrayList<String>> genInputSpace(HashMap<String, SecuritySpace> vnfcSpace,
			List<String> vnfcOrder ){
		ArrayList<ArrayList<String>> inputSpaces = new ArrayList<ArrayList<String>>();
		for(String vnfc: vnfcOrder) {
			ArrayList<String> inputSpace = vnfcSpace.get(vnfc).getAllowedSpace();
			System.out.println("vnfc is " + vnfc);
			System.out.println("input space is" + inputSpace);
			inputSpaces.add(inputSpace);
		}
		return inputSpaces;
	}
	
	private HashSet<String> getAllAvailHost(JSONObject vnfcJSON){
		HashSet<String> hostAvail = new HashSet<String>();
		Double diskReq = vnfcJSON.getDouble("disk");
		Double ramReq = vnfcJSON.getDouble("ram");
		Integer vCPUs = vnfcJSON.getInt("vCPUs");
		Integer physical = vnfcJSON.getInt("physical");
		for(String hostName: this.hostList.keySet()) {
			if(hostList.get(hostName).checkAvail(vCPUs, diskReq, ramReq, physical)) {
				hostAvail.add(hostName);
			}else {
				//#System.out.println("host is " + hostName + " is not avail");
			}
		}
		return hostAvail;
	}
	
	public static void main(String[] args) {
		long startTime=System.currentTimeMillis();
		Gateway gate = new Gateway();
		try {
			gate.init("hostInfo_lab.json");
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		//gate.start("request.json");
		
		int userNumber = 8; 
		Sender sender = new Sender(userNumber);
		Map<String, List<JSONObject>> usersReq = sender.getUserReq();
		int currentNumber = 0;
		int successCount = 0;
		ArrayList<Integer> successArray = new ArrayList<Integer>();

		for(String userName: usersReq.keySet()) {
			List<JSONObject> oneUserReqs = usersReq.get(userName);
			for(JSONObject req: oneUserReqs) {
				currentNumber++;
				System.out.println("old req is ");
				System.out.println(req);
				JSONArray order = req.getJSONArray("order");
				JSONArray newOrder = new JSONArray();
				for(Object vnfc: order) {
					JSONObject vnfcJSON = JSONObject.fromObject(vnfc);
					JSONArray rules = Cleaner.clean(vnfcJSON.get("rules"));
					vnfcJSON.put("rules", rules);
					newOrder.add(vnfcJSON);
				}
				req.put("order", newOrder);
				//#System.out.println("new req is ");
				//#System.out.println(req);
				try {
					boolean flag = gate.start(req);
					if(flag) {
						successCount++;
					}
/*					boolean flag = gate.startMapNoRule(req);
					if(flag) {
						successCount++;
					}
					*/
/*					boolean flag = gate.startRuleNoMap(req);
					if(flag) {
						successCount++;
					}*/
				}catch(Exception e) {
					e.printStackTrace();
				}
				if(currentNumber==8 || currentNumber== 32 || currentNumber==64
						|| currentNumber==96 || currentNumber==128 || currentNumber==160
						|| currentNumber==192 || currentNumber==224) {
					long endTime=System.currentTimeMillis();
					System.out.println("currentNumber is" + currentNumber + "程序运行时间： "+(endTime-startTime)+"ms");
					successArray.add(successCount);
				}
			}
		}
		System.out.println("=================================success number is " + successArray);
	}
}

package Switch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import common.SetUtil;
import flow.Util;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/*
 * 
 * 初始化完成
 * 根据flows生成port-port的space
 * 
 * 
 */
public class Switch {
	//portName
	Map<String, List<Action>> portTable = new HashMap<String, List<Action>>();
	
	//仅仅为了矩阵的映射
	List<String> portName = new ArrayList<String>();
	
	//port以及连接的link的特性
	private Map<String,Link> portLink;
	//portIn portOut
	Space spaceArray[][];
	private String name;
	

	//portName
	public Switch(String switchName, Map<String, Link> ports, JSONArray flows) {
		this.setName(switchName);
		setPortLink(ports);
		int size = ports.size();
		for(String port : ports.keySet()) {
			List<Action> actions = new ArrayList<Action>();
			portTable.put(port, actions);
			portName.add(port);
		}
		refresh(flows);
		spaceArray = new Space[size][size]; 
		for(int i=0; i<size; i++) {
			spaceArray[i] = new Space[size];
			for(int j=0; j<size; j++) {
				spaceArray[i][j] = portToPort(portName.get(i),portName.get(j));
			}
		}
	}

	public ArrayList<Space> samePortSpace(String port){
		ArrayList<Space> samePortSpaces = new ArrayList<Space>();
		int index = this.portName.indexOf(port);
		int size = this.portName.size();
		for(int i=0;i<size;i++) {
			samePortSpaces.add(this.spaceArray[index][i]);
		}
		return samePortSpaces;
	}	
	/*
	 * flows是一个JSONArray
	 * 列表中的每一项的结构为
	 *  'port':
	 *  'match':
	 *  'action':drop
	 *  		:miss
	 *  		:port1
	 *  		:port2
	 *  		:nat:not空格xor-110011空格and-111000
	 * 
	 */
	private void refresh(JSONArray flows) {
		for(Object flow : flows) {
			JSONObject flowJSON = JSONObject.fromObject(flow);
			if(flowJSON.getString("priority").equals("0")) {
				continue;
			}
			String portName = flowJSON.getString("port");
			String match = flowJSON.getString("match");
			String actionStr = flowJSON.getString("action");
			Action action = new Action();
			action.setMatch(match);
			if(actionStr.contains(":")) {
				String operationSet = StringUtils.substringAfter(actionStr, ":");
				Operation operation = new Operation(operationSet,Util.NAT_LENGTH);
				action.setAction("nat", operation);
			}else {
				action.setAction(actionStr);
			}
			portTable.get(portName).add(action);
		}
	}
	
	public static void add(ArrayList<String> allowedSpace, String seq) {
		ArrayList<String> confiltList = new ArrayList<String>();
		boolean contain = false;
		for(String allowSeq: allowedSpace) {
			String interSect = Util.computeIntersect(seq, allowSeq);
			//System.out.println("interSect is" + interSect);
			//System.out.println("seq is" + seq);
			if(interSect.contains("z")) {
				//no interSect
			}else if (interSect.equals(seq)) {
				contain = true;
				break;
			}else {
				confiltList.add(interSect);
			}
		}
		if(contain) {
			return;
		}
		if(confiltList.size()==0) {
			allowedSpace.add(seq);
			return;
		}
		ArrayList<String> setList = new ArrayList<String>();
		setList.add(seq);
		//System.out.println("================seq is========== " + seq);
		for(String confilt: confiltList) {
			//System.out.println("conflict is" + confilt);
			ArrayList<String> newArray = new ArrayList<String>();
			for(String compared: setList) {
				System.out.println("compared is" + compared);
				ArrayList<String> setString = SetUtil.removeSect(confilt, compared);	
/*				for(String a: setString) {
					//System.out.println("==a");
					System.out.println(a);
					System.out.println("==b");
				}*/
				newArray.addAll(setString);
			}
			setList = newArray;
		}
		allowedSpace.addAll(setList);
	}
	
	private Space portToPort(String portIn, String portOut) {
		List<Action> actions = portTable.get(portIn);
		ArrayList<String> wrongPortSpace = new ArrayList<String>();
		ArrayList<String> allowSpace = new ArrayList<String>();
		ArrayList<String> natSpace = new ArrayList<String>();
		ArrayList<String> deniedSpace = new ArrayList<String>();
		HashMap<String, Operation> natOperations = new HashMap<String, Operation>();
		for(Action action: actions) {
			String matchTwo = action.getMatch();
			if(action.getAction().equals(portOut)) {
				if(allowSpace.size()==0) {
					allowSpace.add(matchTwo);
				}else {
					Switch.add(allowSpace, matchTwo);
				}
			//总是默认NAT空间互相不相交
			}else if (action.getAction().equals("nat")) {
				natOperations.put(matchTwo, action.getOperation());
				natSpace.add(matchTwo);
			}else {
				if(action.getAction().equals("drop")) {
					if(deniedSpace.size()==0) {
						deniedSpace.add(matchTwo);
					}else {
						Switch.add(deniedSpace, matchTwo);
					}
				}else {
					if(wrongPortSpace.size()==0) {
						wrongPortSpace.add(matchTwo);
					}else {
						Switch.add(wrongPortSpace, matchTwo);
					}
				}
				//drop or wrong port

			}
		}
		return new Space(allowSpace,wrongPortSpace, deniedSpace, natSpace, natOperations);
	}
	
	public Space getSpace(String portIn, String portOut) {
		int portInNum = portName.indexOf(portIn);
		int portOutNum = portName.indexOf(portOut);
		return this.spaceArray[portInNum][portOutNum];
	}
	
	public ArrayList<String> getAllowSpace(String portIn, String portOut) {
		int portInNum = portName.indexOf(portIn);
		int portOutNum = portName.indexOf(portOut);
		return this.spaceArray[portInNum][portOutNum].getAllowedSpace();
	}
	public ArrayList<String> getDeniedSpace(String portIn, String portOut) {
		int portInNum = portName.indexOf(portIn);
		int portOutNum = portName.indexOf(portOut);
		return this.spaceArray[portInNum][portOutNum].getDeniedSpace();
	}
	public ArrayList<String> getNatSpace(String portIn, String portOut) {
		int portInNum = portName.indexOf(portIn);
		int portOutNum = portName.indexOf(portOut);
		return this.spaceArray[portInNum][portOutNum].getNatSpace();
	}
	
	public void addAllowedSpace(String portIn, String portOut, String match) {
		int portInNum = portName.indexOf(portIn);
		int portOutNum = portName.indexOf(portOut);
		Space space = this.spaceArray[portInNum][portOutNum];
		space.addAllowedSpace(match);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public static void main(String[] args) {
		String srcSwitch = "switchOne";
		String portA = "portA";
		String portB = "portB";
		
		String dstSwitch = "switchTwo";
		String portC = "portC";
		String portD = "portD";
		
		Double bandTotal = 2.5;
		Double bandUsed = 1.0;
		
		String host = "hostA";
		String eth0 = "eth0";

		//以当前交换机为源
		Map<String, Link> ports = new HashMap<String, Link>();
		Link linkB = new Link(srcSwitch, portB, dstSwitch, portC, bandTotal, bandUsed);
		Link linkA = new Link(srcSwitch, portA, host, eth0, bandTotal, bandUsed);
		ports.put(portA, linkA);
		ports.put(portB, linkB);

		JSONArray flows = new JSONArray();
		
		JSONObject flow1 = new JSONObject();
		flow1.put("port", "portA");
		flow1.put("match", "100000");
		flow1.put("action","portB");
		
		JSONObject flow2 = new JSONObject();
		flow2.put("port", "portA");
		flow2.put("match", "xxxx01");
		flow2.put("action","drop");
		
		JSONObject flow3 = new JSONObject();
		flow3.put("port", "portA");
		flow3.put("match", "xxx10x");
		flow3.put("action","drop");
		//miss

		JSONObject flow4 = new JSONObject();
		flow4.put("port", "portA");
		flow4.put("match", "xxx01x");
		flow4.put("action","nat:xor-111010");
		
		flows.add(flow4);
		flows.add(flow3);
		flows.add(flow2);
		flows.add(flow1);
		
		Switch switch1 = new Switch(srcSwitch, ports, flows);
		System.out.println("portA->portB denied space is");
		System.out.println(switch1.getDeniedSpace(portA, portB));
		
		System.out.println("portA->portB allowed space is");
		System.out.println(switch1.getAllowSpace(portA, portB));
		
		System.out.println("portA->portB nat space is");
		System.out.println(switch1.getNatSpace(portA, portB));
		
		System.out.println("portA->portA allowed space");
		System.out.println(switch1.getAllowSpace(portA, portA));
		
		System.out.println("portB->portA denied space is");
		System.out.println(switch1.getDeniedSpace(portB, portA));
	}

	public Map<String,Link> getPortLink() {
		return portLink;
	}

	public void setPortLink(Map<String,Link> portLink) {
		this.portLink = portLink;
	}
}

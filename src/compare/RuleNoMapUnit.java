package compare;

import java.util.ArrayList;
import java.util.HashMap;

import net.sf.json.JSONObject;

public class RuleNoMapUnit {
	private String name;
	private HashMap<String, RuleNoMapLink> portToLink = new HashMap<String, RuleNoMapLink>();
	
	public RuleNoMapUnit(String nodeName) {
		this.name = nodeName;
	}
	
	public void init(JSONObject connections) {
		for(Object connection: connections.keySet()) {
			String portName = String.valueOf(connection);
			JSONObject con = connections.getJSONObject(portName);
			String portDst = con.getString("port");
			String nodeDst = con.getString("node");
			Double band = con.getDouble("bandTotal");
			RuleNoMapLink link = new RuleNoMapLink(this.name, portName, nodeDst, portDst, band);
			this.portToLink.put(portName, link);
		}
	}
	
	public Double getBandFree(String portName) {
		return this.portToLink.get(portName).getBand();
	}
	
	public boolean checkBand(Double band, String port) {
		return this.portToLink.get(port).checkBand(band);
	}

	//运行这个函数之前需要先检查band是否充足
	public void decrease(Double band, String port) {
		this.portToLink.get(port).decreaseBand(band);
	}
	
	public String getHostPort() {
		String portFirst = null;
		for(String port: this.portToLink.keySet()) {
			portFirst = port;
		}
		return portFirst;
	}
	
	public RuleNoMapLink getLink(String port) {
		return this.portToLink.get(port);
	}
	
	public String getPortSelf(String counterPart) {
		String port = null;
		for(String portName: this.portToLink.keySet()) {
			String dstNode = this.portToLink.get(portName).getDstNode();
			if(dstNode.equals(counterPart)) {
				port = portName;
				break;
			}
		}
		return port;
	}
	
	
	//带着vnfc的带宽来search
	public void search(String srcPort, Double band, String dstNode, ArrayList<String> nodesPassed
			, HashMap<String, RuleNoMapUnit> unitMap, ArrayList<ArrayList<String>> resultList) {
		if(dstNode.equals(this.name)) {
			nodesPassed.add(dstNode);
			//System.out.println(nodesPassed);
			resultList.add(nodesPassed);
			return;
		}
		
		if(nodesPassed.contains(this.name)) {
			//System.out.println("刚刚已经经过了这个节点，存在环");
			return;
		}
		
		nodesPassed.add(this.name);
		for(String portName: this.portToLink.keySet()) {
			if(!portName.equals(srcPort)) {
				if(this.portToLink.get(portName).getBand() > band) {
					String otherNode = this.portToLink.get(portName).getDstNode();
					String otherPort = this.portToLink.get(portName).getDstPort();
					RuleNoMapUnit unit = unitMap.get(otherNode);
					ArrayList<String> passedNew = copyList(nodesPassed);
					unit.search(otherPort, band, dstNode, passedNew, unitMap, resultList);
				}
			}
		}
	}
	
	public ArrayList<String> copyList(ArrayList<String> list){
		ArrayList<String> listOne = new ArrayList<String>();
		for(String entry: list) {
			listOne.add(entry);
		}
		return listOne;
	}
	
	

}

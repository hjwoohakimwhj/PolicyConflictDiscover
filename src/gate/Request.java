package gate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import common.AccessibleUnit;
import common.AllowedRoom;
import common.SearchAllPath;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import util.SetOper;

public class Request {
	private String request;
	private int count = 0;
	
	//hostName host
	private Map<String, Host> hostMap = new HashMap<String, Host>();
	
	//vnfcName
	private List<String> vnfcOrder = new ArrayList<String>();
	private HashMap<String, SecuritySpace> inputSpaces = new HashMap<String, SecuritySpace>();
	
	//vnfc hosts
	private Map<String,HashSet<String>> vnfcAvailHosts = new HashMap<String, HashSet<String>>();
	
	//可达路径
	private List<Route> paths = new ArrayList<Route>();
	
	public SearchAllPath searchEngine;
	
	private List<Double> bandList;
	
	public Request(String requestName, Map<String, Host> hostMap, List<String> vnfcOrder, 
			SearchAllPath searchEngine, List<Double> bandList) {
		this.setRequest(requestName);
		this.setHostMap(hostMap);
		this.vnfcOrder = vnfcOrder;
		this.searchEngine = searchEngine;
		this.bandList = bandList;
	}
	
	public void setVnfcAvailHosts(Map<String, HashSet<String>> vnfcAvailHosts) {
		this.vnfcAvailHosts = vnfcAvailHosts;
	}
	
	public void setSecuritySpaces(HashMap<String, SecuritySpace> inputSpaces) {
		this.setInputSpaces(inputSpaces);
	}
	
	public JSONObject generateReq() {
		JSONObject req = new JSONObject();
		for(Route route: paths) {
			String routeName = route.getRouteChoiceName();
			JSONArray routeChoice = route.getRouteChoice();
			req.put(routeName, routeChoice);
		}
		return req;
	}
	
	public void printPaths() {
		for(Route one: this.paths) {
			one.print();
		}
	}
	
	public void findAllPaths() {
		ArrayList<String> vnfcOrderCopy = this.getVnfcOrder();
		ArrayList<String> hosts = new ArrayList<String>();
		this.find(vnfcOrderCopy, hosts);
	}
	
	private void find(ArrayList<String> vnfcOrderCopy, ArrayList<String> hosts ) {
		//System.out.println("vnfcOrderCopy is" + vnfcOrderCopy);
		String vnfc = vnfcOrderCopy.get(0);
		//System.out.println("vnfc is" + vnfc);
		vnfcOrderCopy.remove(vnfc);
		for(String host: vnfcAvailHosts.get(vnfc)) {
			if(hosts.contains(host)) {
				continue;
			}
			
			//System.out.println("add host is" + host);
			hosts.add(host);
			if(vnfcOrderCopy.size()==0) {
				//全部遍历一次了，可以进行可达路径的选择
				//发送请求包括[主机A，主机B，主机C]
				//还包括[
/*				JSONArray req = this.construct(hosts);*/
				//发送给可达路径模块,返回就是可达或者不可达
				//如果是可达的话，则记录下来
/*				this.paths.add(hosts);
				hosts.remove(host);
				continue;*/
				System.out.println("==========find===========is " + hosts);
				Route route = new Route(this.searchEngine);
				for(int count=0; count<this.vnfcOrder.size()-1; count++) {
					String vnfcOne = this.vnfcOrder.get(count);
					String vnfcTwo = this.vnfcOrder.get(count+1);
					ArrayList<String> allowedSpace = this.inputSpaces.get(vnfcOne).getAllowedSpace();
					String src = hosts.get(count);
					String dst = hosts.get(count+1);
					try {
						AccessibleUnit unit = searchEngine.getAccessibleRoomBySrcDst(src,dst);
						List<AllowedRoom> unitRoom = unit.getAllowedRoom();
						List<Integer> usefulRoomNum = new ArrayList<Integer>();
						int number = 0;
						//这个序号代表了合法路径在AccessibleUnit中的序号
						for(AllowedRoom roomOne: unitRoom) {
							if(SetOper.contain(roomOne.get(),allowedSpace)) {
								usefulRoomNum.add(number);
							}
							number++;
						}
						if(usefulRoomNum.size()==0) {
							System.out.println("没有可达的路径");
							break;
						}
						route.add(vnfcOne, vnfcTwo, usefulRoomNum, src, dst);
					} catch (Exception e) {
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					}
				}
				if(route.size()==(this.vnfcOrder.size()-1)) {
					this.paths.add(route);
					route.construct(this.bandList);//形成JSON格式为了和topo.java相对接
				}
				hosts.remove(host);
				continue;
			}else {
				//System.out.println("find cur and vnfcOrder is" + vnfcOrderCopy);
				//System.out.println("hosts is " + hosts);
				find(vnfcOrderCopy, hosts);
				hosts.remove(host);
			}
		}
		vnfcOrderCopy.add(vnfc);
		if(vnfcOrderCopy.size()==vnfcOrder.size()) {
			return;
		}
	}
	
/*	private JSONArray construct(ArrayList<String> hosts) {
		JSONArray obj = new JSONArray();
		for(String host: hosts) {
			SecuritySpace space = inputSpaces.get(host);
			JSONObject hostJSON = new JSONObject();
			hostJSON.put("host", host);
			//
			obj.add(hostJSON);
		}
		return obj;
	}*/
	
	public ArrayList<String> getVnfcOrder(){
		ArrayList<String> vnfcs = new ArrayList<String>();
		for(String vnfc: vnfcOrder) {
			vnfcs.add(vnfc);
		}
		return vnfcs;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public Map<String, Host> getHostMap() {
		return hostMap;
	}

	public void setHostMap(Map<String, Host> hostMap) {
		this.hostMap = hostMap;
	}

	public HashMap<String, SecuritySpace> getInputSpaces() {
		return inputSpaces;
	}

	public void setInputSpaces(HashMap<String, SecuritySpace> inputSpaces) {
		this.inputSpaces = inputSpaces;
	}
}

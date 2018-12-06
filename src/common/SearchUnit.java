package common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchUnit {
	private String number;
	//switch port哪个交换机和那个交换机上的端口
	private Map<String, String> switchPort = new HashMap<String, String>();
	
	private Map<String, SearchUnit> link;
	
	private AllowedRoom[][] allowMatrix;
	
	private List<String> portMapNum;
	
	public String connectPort(String switchName) {
		return switchPort.get(switchName);
	}
	
	public SearchUnit(String number, Map<String, String> portArray, Map<String, SearchUnit> unitLink,
			AllowedRoom[][] allowedSequence, List<String> portMapNum) {
		this.number = number;
		this.switchPort = portArray;
		this.link = unitLink;
		allowMatrix = allowedSequence; 
		this.portMapNum = portMapNum;
	}
	
	private AllowedRoom genOutPutAllow(AllowedRoom sequence, String inputPort, String outputPort){
		//System.out.println("portMap Num ");
		//System.out.println(portMapNum);
		//System.out.println("inputPort is " + inputPort);
		//System.out.println("outputPort is " + outputPort);
		int indexInput = portMapNum.indexOf(inputPort);
		int indexOutput = portMapNum.indexOf(outputPort);
		AllowedRoom match = allowMatrix[indexInput][indexOutput];
		if(match.size()==0) {
			return sequence.copy();
		}
		AllowedRoom output = AllowedRoom.genAllowedRoom(sequence, match);
		return output;
	}
	
	//sequence是输入的流序列
	public void search(ArrayList<String> intList, String dest, AllowedRoom sequence
			,String inputPort, AccessibleUnit returnUnit) {
		//System.out.println("begin search unit search");
		if(dest.equals(this.number)) {
			intList.add(this.number);
			//#System.out.println("path is below");
			//#System.out.println(intList);
			//#System.out.println("allowed sequence is below");
			//#System.out.println(sequence.get());
			returnUnit.add(intList, sequence);
			return;
		}
		
		if(intList.contains(this.number)) {
			return;
		}
		
		//表明是主机
		if(intList.size()==0) {
			intList.add(this.number);
			for(String i: switchPort.keySet()) {
				String portName = switchPort.get(i);
				link.get(i).search(copy(intList),dest, sequence, portName, returnUnit);
			}
		}else {
			intList.add(this.number);
			for(String i: switchPort.keySet()) {
				if(!intList.contains(i)) {
					//System.out.println("genOutPutAllow!!!");
					//System.out.println("input is " + inputPort);
					//System.out.println("output is " + switchPort.get(i));
					//System.out.println("this number is " + this.number);
					String outputPort = this.link.get(i).connectPort(this.number);
					AllowedRoom newSeq = genOutPutAllow(sequence, inputPort, outputPort);
					String portName = switchPort.get(i);
					link.get(i).search(copy(intList),dest, newSeq, portName, returnUnit);
				}
			}
		}
	}
	
	private ArrayList<String> copy(ArrayList<String> intList){
		ArrayList<String> listCopy = new ArrayList<String>();
		for(String i1: intList) {
			listCopy.add(i1);
		}
		return listCopy;
	}
}

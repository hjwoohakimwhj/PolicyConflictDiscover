package Switch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import flow.Util;
import util.SetOper;

public class Space {
	//drop  wrongPort
	private ArrayList<String> allowedSpace;
	private ArrayList<String> wrongPortSpace;
	private ArrayList<String> deniedSpace;
	private ArrayList<String> natSpace;
	private Map<String, Operation> natOperation;
	private ArrayList<String> highPrioritySpace;

	public Space(ArrayList<String> allowedSpace, ArrayList<String> wrongPortSpace
			, ArrayList<String> deniedSpace
			, ArrayList<String> natSpace
			, HashMap<String, Operation> natOperation) {
		this.wrongPortSpace = wrongPortSpace;
		this.allowedSpace = allowedSpace;
		this.deniedSpace = deniedSpace;
		this.natSpace = natSpace;
		this.natOperation = natOperation;
		highPrioritySpace = new ArrayList<String>();
	}
	
	public ArrayList<String> copy(ArrayList<String> a){
		ArrayList<String> copySeq = new ArrayList<String>();
		for(String entry: a) {
			copySeq.add(entry);
		}
		return copySeq;
	}
	
	//同时sequence也应当删除交集才是
	public ArrayList<String> copyNatOperations(ArrayList<String> sequence){
		ArrayList<String> copySeq = this.copy(sequence);
		
		ArrayList<String> newAllowed = new ArrayList<String>();
		for(String seq: copySeq) {
			Map<String, ArrayList<String>> result = this.copyNatOperations(seq);
			if(result.containsKey("null")) {
				continue;
			}
			for(String resultNat: result.keySet()) {
				SetOper.addNoRepeat(newAllowed, resultNat);
				sequence.remove(seq);
				sequence.addAll(result.get(resultNat));
				break;
			}
		}
		//System.out.println("new allowed is " + newAllowed);
		return newAllowed;
	}
	
	/*
	 * 计算输入空间和NAT空间的交集，从NAT空间中去除交集
	 * 
	 * 
	 */
	public Map<String, ArrayList<String>> copyNatOperations(String sequence) {
		//newAllowedSpace是sequence经过该SPACE的所有NAT操作后的新增的输入空间，但是这个输入空间可能与旧的输入空间有交集
		//强制令每个端口只有一个NAT项
		Map<String, ArrayList<String>> returnMap = new HashMap<String,ArrayList<String>>();
		String newAllowed = "null";
		for(String mask: natOperation.keySet()) {
			String interSect = Util.computeIntersect(sequence, mask);
			if(!interSect.contains("z")) {
				System.out.println("use nat");
				newAllowed = natOperation.get(mask).copy(interSect);
				ArrayList<String> newSeq = new ArrayList<String>();
				newSeq = SetOper.removeSect(interSect, sequence);
				returnMap.put(newAllowed, newSeq);
				System.out.println("nat newSeq");
				System.out.println(newSeq);
			}else {
				ArrayList<String> newSeq = new ArrayList<String>();
				returnMap.put(newAllowed, newSeq);
			}
			//仅仅执行一次
			break;
		}
		return returnMap;
	}
	


	public ArrayList<String> getWrongPortSpace() {
		return wrongPortSpace;
	}

	public void setWrongPortSpace(ArrayList<String> wrongPortSpace) {
		this.wrongPortSpace = wrongPortSpace;
	}

	public ArrayList<String> getDeniedSpace() {
		return deniedSpace;
	}

	public void setDeniedSpace(ArrayList<String> deniedSpace) {
		this.deniedSpace = deniedSpace;
	}

	public ArrayList<String> getNatSpace() {
		return natSpace;
	}

	public void setNatSpace(ArrayList<String> natSpace) {
		this.natSpace = natSpace;
	}
	
	public void addWrongPortpace(String match) {
		Switch.add(this.wrongPortSpace, match);
	}
	
	public void refreshWrongPortSpace(ArrayList<String> wrongSpace) {
		for(String wrong: wrongSpace) {
			SetOper.addNoRepeatReturnInt(this.wrongPortSpace, wrong);
		}	
	}
	
	//actually add flows number
	public int addAllowedSpaces(ArrayList<String> allowedSpace) {
		int flows = 0;
		for(String allowed: allowedSpace) {
			flows += SetOper.addNoRepeatReturnInt(this.allowedSpace, allowed);
		}
		return flows;
	}
	
	public void addAllowedSpace(String match) {
		this.allowedSpace.add(match);
	}
	
	public void addHighPriority(ArrayList<String> high){
		this.highPrioritySpace.addAll(high);
	}
	
	public ArrayList<String> getAllowedSpace(){
		return this.allowedSpace;
	}
	
	public ArrayList<String> getHigh(){
		return highPrioritySpace;
	}
}

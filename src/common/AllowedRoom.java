package common;

import java.util.ArrayList;
import flow.Util;

public class AllowedRoom {
	private ArrayList<String> allowed = new ArrayList<String>();
	public AllowedRoom() {
	}
	
	public int size() {
		return this.allowed.size();
	}
	
	public AllowedRoom copy() {
		AllowedRoom allowedNew = new AllowedRoom();
		for(String entry: this.allowed) {
			allowedNew.add(entry,true);
		}
		return allowedNew;
	}
	
	public void add(String seq, boolean direct) {
		if(direct==true) {
			this.allowed.add(seq);
			this.refresh();
		}else {
			this.add(seq);
		}
	}
	
	public void add(String seq) {
		if(this.allowed.size()==0) {
			this.allowed.add(seq);
			return;
		}else {
			ArrayList<String> confiltList = new ArrayList<String>();
			boolean contain = false;
			for(String allowSeq: allowed) {
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
				this.allowed.add(seq);
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
/*					for(String a: setString) {
						//System.out.println("==a");
						System.out.println(a);
						System.out.println("==b");
					}*/
					newArray.addAll(setString);
				}
				setList = newArray;
			}
			allowed.addAll(setList);
		}
		this.refresh();
	}

	//合并掉可以合并的项
	public void refresh() {
		int size = this.allowed.size();
		if(size<=1) {
			return;
		}
		String prefer = this.allowed.get(size-1);
		this.allowed.remove(size-1);
		SetUtil.combine(this.allowed, prefer);
	}
	
	public ArrayList<String> get(){
		return this.allowed;
	}
	
	public static AllowedRoom genAllowedRoom(AllowedRoom a, AllowedRoom b) {
		AllowedRoom allowedNew = new AllowedRoom();
		ArrayList<String> allowedA = a.get();
		ArrayList<String> allowedB = b.get();
		for(String entryOne: allowedA) {
			for(String entryTwo: allowedB) {
				//System.out.println("entryo one is" + entryOne);
				//System.out.println("entryo two is" + entryTwo);
				String interSect = Util.computeIntersect(entryOne, entryTwo);
				if(!interSect.contains("z")) {
					allowedNew.add(interSect, true);
				}
			}
		}
		return allowedNew;
	}
	
	public static void main(String[] args) {
		AllowedRoom allowedRoom = new AllowedRoom();
		allowedRoom.add("0x1x0");
		allowedRoom.add("x1x1x");
		allowedRoom.add("010xx");
		allowedRoom.add("xxx10");
		for(String a: allowedRoom.get()) {
			System.out.println(a);
		}
	}
}

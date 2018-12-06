package util;

import java.util.ArrayList;
import java.util.Arrays;

import common.SetUtil;
import flow.Util;

public class SetOper {
	
	/*
	 * 字符串链表A 字符串B 
	 * B必须保障自己与A中所欲的字符串没有交集，或者交集全部去除后，才能插入到字符串链表中
	 * 此时新产生的链表一定没有可以合并的项
	 */
	public static void addNoRepeat(ArrayList<String> listOne, String seq) {
		ArrayList<String> seqList = SetOper.removeInterSect(listOne, seq);
		if(seqList!=null) {
			for(String newOne: seqList) {
				SetOper.combine(listOne, newOne);
			}
		}
	}
	
	public static int addNoRepeatList(ArrayList<String> allowedSpace, ArrayList<String> newAllowed) {
		ArrayList<String> copy = SetOper.copy(allowedSpace);
		int allFlows = 0;
		for(String newOne: newAllowed) {
			int flows = SetOper.addNoRepeatReturnInt(copy, newOne);
			allFlows += flows;
		}
		System.out.println("new allowed space may be");
		System.out.println(copy);
		return allFlows;
	}
	public static int addNoRepeatReturnInt(ArrayList<String> listOne, String seq) {
		ArrayList<String> seqList = SetOper.removeInterSect(listOne, seq);
		if(seqList!=null) {
			for(String newOne: seqList) {
				SetOper.combine(listOne, newOne);
			}
		}
		
		//完全没有交集的会返回null
		if(seqList==null) {
			return 1;
		}else {
			return seqList.size();
		}
	}
	
	/*
	 * each string in small must be contain in big
	 * 
	 */
	public static boolean contain(ArrayList<String> big, ArrayList<String> small) {
		boolean contain = true;
		for(String smallOne: small) {
			if(!contain(big,smallOne)) {
				contain = false;
				break;
			}
		}
		return contain;
	}
	
	//前提 a和b列表元素均是没有重复的
	public static boolean listEql(ArrayList<String> a, ArrayList<String> b) {
		if(a.size()!=b.size()) {
			return false;
		}
		for(String entry: a) {
			if(!b.contains(entry)) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean contain(ArrayList<String> big, String small) {
		boolean find = false;
		for(String bigOne: big) {
			if(Util.computeIntersect(small, bigOne).equals(small)) {
				find = true;
				break;
			}
		}
		return find;
	}
	
	public static ArrayList<String> removeInterSect(ArrayList<String> listOne, String seq){
		ArrayList<String> seqList = new ArrayList<String>();
		seqList.add(seq);
		if(listOne.size()==0) {
			//#listOne.add(seq);
			return seqList;
		}
		ArrayList<String> conflictList = new ArrayList<String>();
		for(String one: listOne) {
			String interSect = flow.Util.computeIntersect(one, seq);
			if(!interSect.contains("z")) {
				conflictList.add(interSect);
			}
		}
		if(conflictList.size()==0) {
			SetOper.combine(listOne, seq);
			return null;
		}
		for(String conflictOne: conflictList) {
			ArrayList<String> newArray = new ArrayList<String>();
			for(String seqOne: seqList) {
				ArrayList<String> newList = SetOper.removeSect(conflictOne, seqOne);
				newArray.addAll(newList);
			}
			seqList = newArray;
		}
		return seqList;
	}
	
	/*
	 * 从b中除去interSect，注意这里的interSect并不一定和b有交集！
	 * 如interSect为01110， b为x1x1x
	 * 
	 */
	public static ArrayList<String> removeSect(String interSect, String b){
		ArrayList<String> newSpace = new ArrayList<String>();
		String inter = Util.computeIntersect(interSect, b);
		if(inter.contains("z")) {
			//System.out.println("没有交集，无法从一空间删除另一个空间");
			newSpace.add(b);
			return newSpace;
		}
		//此时，交集必然是b的子集，我们需要从b中去除这个交集
		int length = b.length();
		char[] result = new char[length];
		for(int i=0;i<length; i++) {
			char bChar = b.charAt(i);
			char iChar = interSect.charAt(i);
			if(bChar==iChar) {
				result[i] = bChar;
			}else {
				if(iChar=='1') {
					String newString = String.valueOf(Arrays.copyOfRange(result, 0, i)) 
							+ "0" + b.substring(i+1, length);
					newSpace.add(newString);
					result[i] = '1';
				}else {
					String newString = String.valueOf(Arrays.copyOfRange(result, 0, i)) 
							+ "1" + b.substring(i+1, length);
					newSpace.add(newString);
					result[i] = '0';
				}
			}
		}
		return newSpace;
	}
	
	/*
	 * list因为newStr的引入而引发一系列的合并，前提list之前一定是不能合并的
	 * 
	 */
	public static void combine(ArrayList<String> list, String newStr) {
		int listNumber = 0;
		int listSize = list.size();
		for(String strOne: list) {
			int count = 0;
			
			//用这种方法的前提是，必然没有交集
			int index = Util.computeIntersect(strOne, newStr).indexOf("z");
			for(int i=0;i<newStr.length();i++) {
				if(i==index) {
					continue;
				}
				if(!(strOne.charAt(i)==newStr.charAt(i))) {
					break;
				}else {
					count++;
				}
			}
			if(count==newStr.length()-1) {
				list.remove(listNumber);
				String stringRep = newStr.substring(0,index)
						+ "x" + newStr.substring( index + 1,newStr.length());
				if(list.size()==0) {
					list.add(stringRep);
				}else {
					SetUtil.combine(list, stringRep);
				}
				break;
			}
			listNumber++;
		}
		
		//找不到可以合并的，直接添加
		if(listNumber==listSize) {
			list.add(newStr);
		}
	}
	
	public static void main(String[] args) {
		 ArrayList<String> listA = new  ArrayList<String>();
		 listA.add("0x1x0");
		 SetOper.addNoRepeat(listA, "x1x1x");
		 SetOper.addNoRepeat(listA, "0100x");
		 System.out.println(listA);
	}
	
	/*
	 * listOne内部互不相交，且已经不可再合并，listTwo也一样，他们的相交集合，一定是不可合并，且互不相交
	 */
	public static ArrayList<String> interSect(ArrayList<String> listOne, ArrayList<String> listTwo){
		ArrayList<String> interSectList = new ArrayList<String>();
		for(String listA: listOne) {
			for(String listB: listTwo) {
				String interSect = Util.computeIntersect(listA, listB);
				if(!interSect.contains("z")) {
					interSectList.add(interSect);
				}
			}
		}
		return interSectList;
	}
	
	public static ArrayList<String> copy(ArrayList<String> list){
		ArrayList<String> interSectList = new ArrayList<String>();
		for(String a: list) {
			interSectList.add(a);
		}
		return interSectList;
	}
	
	/*
	 * listOne 是hightPriority 输出是listTwo中没有在listOne中的集合
	 */
	public static ArrayList<String> noInterSect(ArrayList<String> listOne, ArrayList<String> listTwo){
		ArrayList<String> newList = new ArrayList<String>();
		if(listOne.size()==0) {
			return SetOper.copy(listTwo);
		}
		for(String listA: listTwo) {
			ArrayList<String> newA = SetOper.removeInterSect(listOne, listA);
			if(newA!=null) {
				newList.addAll(newA);
			}
		}
		return newList;
	}
	
	/*
	 * from a to remove b暗含着 b的所有项都是包含在a内部的
	 */
	public static ArrayList<String> removeInterFromList(ArrayList<String> a, ArrayList<String> b){
		ArrayList<String> c = SetOper.copy(a);
		for(String bOne: b) {
			for(String aOne: a) {
				String interSect = Util.computeIntersect(bOne, aOne);
				if(!interSect.contains("z")) {
					ArrayList<String> rement = SetOper.removeSect(interSect, aOne);
					c.remove(aOne);
					if(rement.size()!=0) {
						c.addAll(rement);
					}
					break;
				}
			}
		}
		return c;
	}
	
	public static boolean interSectWithWrongPort(ArrayList<String> wrongPort, ArrayList<String> input) {
		for(String inputOne: input) {
			for(String wrongOne: wrongPort) {
				String interSect = Util.computeIntersect(inputOne, wrongOne);
				if(!interSect.contains("z")) {
					return true;
				}
			}
		}
		return false;
	}
}

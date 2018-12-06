package common;

import java.util.ArrayList;
import java.util.Arrays;

import flow.Util;

public class SetUtil {
	//from a to remove b
	//a may xxxxx b may be xxx1x
	public static ArrayList<String> remove(String a, String b){
		ArrayList<String> newSpace = new ArrayList<String>();
		String interSect = Util.computeIntersect(a, b);
		if(interSect.contains("z")) {
			System.out.println("没有交集，无法从一空间删除另一个空间");
			return newSpace;
		}
		
		//此时，交集必然是b的子集，我们需要从b中去除这个交集
		int length = a.length();
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

	//newStr MUST not intersect with any entry in the list
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
/*		ArrayList<String> newArray = SetUtil.removeSect("11x10", "0x010");
		//交集是01x
		for(String a: newArray) {
			System.out.println(a);
		}*/
		ArrayList<String> newArray = new ArrayList<String>();
		newArray.add("1xxx");
		newArray.add("01xx");
		newArray.add("0001");
		SetUtil.combine(newArray, "0000");
		System.out.println(newArray);
	}
}

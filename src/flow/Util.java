package flow;

import java.util.ArrayList;

public class Util {
	public static String x8 = "xxxxxxxx";
	public static String x12 = x8.concat("xxxx");
	public static String x16 = x8.concat(x8);
	public static String x32 = x16.concat(x16);
	public static String x48 = x32.concat(x16);
	public static int NAT_LENGTH = 28;

	/*
	 * headerOnes里面都是不存在交集，也不能合并的match项目 
	 * 因为headerOnes都是不相交的match项，所有就算headerTwo和他们的交集是多个match项，也不会存在需要合并的问题
	 * 当交集的数目超过1的时候，表明一定会有漏掉的表项没有被包含在内，所以一定要添加这个表项
	 * 
	 */
	public static int computeRemnat(ArrayList<String> headerOnes, String headerTwo){
		ArrayList<String> interSectList = new ArrayList<String>();
		int interSectNumber = 0;
		for(String headerOne: headerOnes) {
			String interSect = Util.computeIntersect(headerOne, headerTwo);
			if(interSect.contains("z")) {
				//没有交集
			}else {
				interSectList.add(interSect);
				interSectNumber++;
				if(interSectNumber==2) {
					break;
				}
			}
		}
		if(interSectNumber==2) {
			return 1;
		}else {
			return 0;
		}
	}
	
	public static ArrayList<String> computeIntersect(ArrayList<String> headerOnes, String headerTwo){
		ArrayList<String> interSect = new ArrayList<String>();
		for(String headerOne: headerOnes) {
			String interSectOne = Util.computeIntersect(headerOne, headerTwo);
			if(!interSectOne.contains("z")) {
				interSect.add(headerOne);
			}
		}
		return interSect;
	}
	
	//header must be 48*2+12+32*2+8+16*2 = 212
	//contain 'z' is wrong
	public static String computeIntersect(String headerOne, String headerTwo) {
		//System.out.println("computeInterSect");
		//System.out.println("headerOne is " + headerOne);
		//System.out.println("headerTwo is " + headerTwo);
		int length = headerOne.length();
		char[] interSect = new char[length];
		for(int i=0;i<length; i++) {
			//System.out.println("headerOne is" + headerOne);
			//System.out.println("headerTwo is" + headerTwo);
			char result = computeChar(headerOne.charAt(i), headerTwo.charAt(i));
			//System.out.println(result);
			interSect[i] = result;
			if(result == 'z') {
				break;
			}
		}
		//System.out.println(interSect);
		return String.valueOf(interSect);
	}
	
	public static char computeChar(char a, char b) {
		char result;
		if( a == 'x' && b == 'x') {
			result = 'x';
		}else if ( a == 'x') {
			result = b;
		}else if ( b == 'x') {
			result = a;
		}else {
			if( a == b) {
				result = a;
			}else {
				result = 'z';
			}
		}
		return result;
	}
	
	//0-96 mac
	//0-48 macSrc
	//48-96 macDst
	//96-108 vlanId
	//108-172 ip
	//172-180 protocal
	//180-212 port
	public static boolean checkValid(String interSect) {
		//mac
		if(!check(interSect.substring(0, 48)) || !check(interSect.substring(48, 96))){
			return false;
		}
		
		//IP
		if(!check(interSect.substring(108,140)) || !check(interSect.substring(140,172))) {
			return false;
		}

		//vlanId must be defined
		if(interSect.substring(96,108).contains("x")) {
			return false;
		}		
		
		if(!check(interSect.substring(180, 196)) || !check(interSect.substring(196,212))) {
			return false;
		}
		
		return true;
		
	}
	
	//通配符和通配符的交集，末尾一定是x，而不是中间离散的x，除非是离散和通配符想交集，但不予考虑
	public static boolean check(String interSect) {
		if(!interSect.contains("x")) {
			//only one IP
			return true;
		}
		if(!interSect.endsWith("x")) {
			return false;
		}
		
		int index = interSect.length()-1;
		for(int i=interSect.length()-1; i< 0; i--) {
			if(interSect.charAt(i) == 'x') {
				index = i;
			}else {
				break;
			}
		}
		if(interSect.indexOf("x") != index) {
			return false;
		}
		
		return true;
	}
	
	
	//============================================//
	
	/*
	 * 	跟其他运算不同，第二个matchTwo代表nat表项的操作，所以不能是x
	 *	1->改变， 0->不改变
	 *	return 'z' means error 
	 *
	 */
	public static String xor(String matchOne, String matchTwo) {
		int length = matchOne.length();
		char[] resultArray = new char[length];
		if(matchTwo.contains("x")) {
			resultArray[0] = 'z';
		}else {
			for(int i=0;i<length; i++) {
				char result = xorChar(matchOne.charAt(i), matchTwo.charAt(i));
				//System.out.println(result);
				resultArray[i] = result;
			}
		}
		System.out.println(String.valueOf(resultArray));
		return String.valueOf(resultArray);
	}
	
	public static char xorChar(char a, char b) {
		if(a=='x') {
			return 'x';
		}else if (b=='1') {
			if(a=='1') {
				return '0';
			}else {
				return '1';
			}
		}else {
			if(a=='1') {
				return '1';
			}else {
				return '0';
			}
		}
	}
	
	/*
	 * never return 'z'  有点问题
	 */
	public static String union(String matchOne, String matchTwo) {
		int length = matchOne.length();
		char[] resultArray = new char[length];
		for(int i=0;i<length; i++) {
			char result = unionChar(matchOne.charAt(i), matchTwo.charAt(i));
			//System.out.println(result);
			resultArray[i] = result;
		}
		System.out.println(String.valueOf(resultArray));
		return String.valueOf(resultArray);
		
	}
	
	/*
	 * 有点问题
	 * 
	 */
	public static char unionChar(char a, char b) {
		if(a==b) {
			return a;
		}
		return 'x';
	}
	
	public static String not(String matchOne) {
		int length = matchOne.length();
		char[] resultArray = new char[length];
		for(int i=0;i<length; i++) {
			char charOne = matchOne.charAt(i);
			char result;
			if(charOne=='1') {
				result = '0';
			}else if (charOne=='0') {
				result = '1';
			}else {
				result = 'x';
			}
			//System.out.println(result);
			resultArray[i] = result;
		}
		System.out.println(String.valueOf(resultArray));
		return String.valueOf(resultArray);
	}
	
	public static String remove(String securitySpace, String interSect) {
		int length = securitySpace.length();
		char[] resultArray = new char[length];
		for(int i=0;i<length; i++) {
			char charOne = securitySpace.charAt(i);
			char result;
			if(charOne=='x') {
				if(interSect.charAt(i)=='1') {
					result = '0';
				}else if (interSect.charAt(i)=='0') {
					result = '1';
				}else {
					result = 'x';
				}
			}else {
				result = charOne;
			}
			//System.out.println(result);
			resultArray[i] = result;
		}
		System.out.println(String.valueOf(resultArray));
		return String.valueOf(resultArray);
	}
	
	public static void noRepeatAdd(ArrayList<String> newAllowedSpace, String seq) {
		int size = newAllowedSpace.size();
		ArrayList<String> contradict = new ArrayList<String>();
		int count = 0;
		if(newAllowedSpace.size()==0) {
			newAllowedSpace.add(seq);
			return;
		}
		for(String seqOne: newAllowedSpace) {
			String interSect = Util.computeIntersect(seq, seqOne);
			if(interSect.contains("z")) {
				count++;
				continue;
			}
			if(interSect.equals(seq)) {
				break;
			}
			contradict.add(interSect);
		}
		if(count==size) {
			newAllowedSpace.add(seq);
			return;
		}
		
	}
}

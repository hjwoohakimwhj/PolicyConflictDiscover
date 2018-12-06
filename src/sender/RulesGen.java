package sender;

import java.util.Random;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class RulesGen {
	public static Integer RULE_NUM_MIN = 1;
	public static Integer RULE_NUM_MAX = 4;
	public static Integer RULE_LENGTH = 6;
	
	public static final String actionAccept = "accept";
	
	//如果是drop的话，那么没有必要进行规则匹配了
	//public static final String actionDrop = "drop";

	private Integer ruleNum;
	private int vlanNum;
	private int vlanBitLength;
	private int varBitLength;
	private int varBitMax;
	private String vlanBinary;
	public RulesGen(int vlanNum, int vlanBitLength) {
		Random rand = new Random();
		this.ruleNum = rand.nextInt(RULE_NUM_MAX) + 1;
		this.setVlanNum(vlanNum);
		this.vlanBitLength = vlanBitLength;
		this.varBitLength = RULE_LENGTH - vlanBitLength;
		this.varBitMax = this.varBitLength/3;
		this.genVlanChar(vlanNum);
		//System.out.println("vlanBinary is " + this.vlanBinary);
	}
	
	private void genVlanChar(int vlanNum) {
		int maxVlanLimit = (int) Math.pow(2, this.vlanBitLength);
		if(vlanNum>=maxVlanLimit) {
			System.err.println("出错， vlan号过大");
			return;
		}
		String binary = Integer.toBinaryString(vlanNum);
		//System.out.println("binary is raw " + binary);
		if(binary.length()<this.vlanBitLength){
			String middleResult = binary;
			int lackZero = this.vlanBitLength - binary.length();
			for(int i=0;i<lackZero;i++) {
				middleResult = "0" + middleResult;
				//System.out.println("middle is " + middleResult);
			}
			this.vlanBinary = middleResult;
		}else {
			this.vlanBinary = binary;
		}

	}
	
	public JSONArray gen() {
		JSONArray rules = new JSONArray();
		for(int i=0;i<this.ruleNum;i++) {
			JSONObject rule = this.genRule();
			rules.add(rule);
		}
		return rules;
	}
	
	private JSONObject genRule() {
		JSONObject rule = new JSONObject();
		String match = genMatch();
		rule.put("match", match);
		rule.put("action", actionAccept);
		return rule;
	}

	private String genMatch() {
		Random rand = new Random();
		int varNumCur = rand.nextInt(this.varBitMax + 1);
		int[] indexs = this.genIntArray(varNumCur);
		char[] matchChar = new char[RULE_LENGTH];
		for(int i=0;i<RULE_LENGTH;i++) {
			if(i<this.vlanBitLength) {
				//添加vlan标识
				//System.out.println("i is " + i);
				matchChar[i] = this.vlanBinary.charAt(i);
			}else {
				if(RulesGen.hasInt(indexs,i)) {
					matchChar[i] = 'x';
				}else {
					if(rand.nextInt(2)==0) {
						matchChar[i] = '0';
					}else {
						matchChar[i] = '1';
					}
				}
				
			}
		}
		return String.valueOf(matchChar);
	}
	
	public static boolean hasInt(int[] array, int a) {
		int size = array.length;
		for(int i=0;i<size;i++) {
			if(array[i]==a) {
				return true;
			}
		}
		return false;
	}
	
	private int[] genIntArray(int varNumCur) {
		int[] array = new int[varNumCur];
		Random rand = new Random();
		for(int i=0;i<varNumCur;i++) {
			while(true) {
				int index = rand.nextInt(varBitLength) + this.vlanBitLength;
				if(RulesGen.hasInt(array, index)) {
					continue;
				}else {
					array[i] = index;
					break;
				}
			}
		}
		return array;
	}
	
	public static void main(String[] args) {
		//System.out.println("resutl one is" + Math.pow(2,2));
		RulesGen rule = new RulesGen(1, 1);
		JSONArray result = rule.gen();
		System.out.println(result);
	}

	public int getVlanNum() {
		return vlanNum;
	}

	public void setVlanNum(int vlanNum) {
		this.vlanNum = vlanNum;
	}
}

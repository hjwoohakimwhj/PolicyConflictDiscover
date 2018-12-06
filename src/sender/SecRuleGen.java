package sender;

import java.util.Arrays;
import java.util.Random;

import net.sf.json.JSONObject;

public class SecRuleGen {
	public static final int TATAL_LENGTH = 28;
	
	//0-2
	public static final int VLAN_LENGTH = 3;
	//3-10,11-18
	public static final int IP_LENGTH = 8;
	public static final int IP_X = 5;	

	//19
	public static final int PROTOCAL_LENGTH = 1;
	public static final int PROTOCAL_X = 2;
	
	//20-23,24-27
	public static final int PORT_LENGTH = 4;
	//1代表10%的可能性产生全xxxx，10代表百分百
	public static final int PORT_ALL_X = 2;
	private int vlanNumber;
	private char[] ruleResult = new char[TATAL_LENGTH];
	private boolean firewall;
	
	public SecRuleGen(int vlanNumber, boolean firewall) throws Exception {
		this.vlanNumber = vlanNumber;
		this.firewall = firewall;
		if(firewall) {
			this.genFirewall();
		}else {
			this.genFlow();
		}
	}
	private void genFlow() {
		this.genVlanFlow();
		this.genIPFlow();
		this.genProtocalFlow();
		this.genPortFlow();
	}
	
	private void genFirewall() throws Exception {
		this.genVlan();
		this.genIP();
		this.genProtocal();
		this.genPort();
	}
	private void genVlanFlow() {
		//all x
		for(int i=0;i<VLAN_LENGTH;i++) {
			this.ruleResult[i] = 'x';
		}
	}
	
	private void genIPFlow() {
		Random rand = new Random();
		for(int i=0;i<2;i++) {
			int xNumber = rand.nextInt(6) + 3;
			for(int j=0;j<IP_LENGTH;j++) {
				if(j>=IP_LENGTH-xNumber) {
					this.ruleResult[3+i*8+j] = 'x';
				}else {
					if(rand.nextInt(2)==0) {
						this.ruleResult[3+i*8+j] = '0';
					}else {
						this.ruleResult[3+i*8+j] = '1';
					}
				}
			}
		}
	}
	
	private void genVlan() throws Exception {
		String binary = Integer.toBinaryString(this.vlanNumber);
		//System.out.println("binary length is " + binary.length());
		//System.out.println(binary);
		//System.out.println("binary is raw " + binary);
		if(binary.length()<VLAN_LENGTH){
			int lackZero = VLAN_LENGTH  - binary.length();
			for(int i=0;i<lackZero;i++) {
				this.ruleResult[i] = '0';
				//System.out.println("middle is " + middleResult);
			}
			for(int i=lackZero;i<VLAN_LENGTH;i++) {
				this.ruleResult[i]=binary.charAt(i-lackZero);
			}
		}else if (binary.length()==VLAN_LENGTH) {
			char[] binaryChar = binary.toCharArray();
			for(int i=0;i<VLAN_LENGTH;i++) {
				this.ruleResult[i] = binaryChar[i];
			}
		}else {
			System.out.println("error the vlan is beyond the limit");
			throw new Exception("Class NetRuleGen ; Function genVlan");
		}
	}
	
	private void genIP() {
		Random rand = new Random();
		for(int j=0;j<2;j++) {
			for(int i=IP_LENGTH-1;i>=0;i--) {
				if(i!=IP_LENGTH-1&&this.ruleResult[3+j*8+i+1]!='x') {
					if(rand.nextInt(2)==0) {
						this.ruleResult[3+j*8+i] = '0';
					}else {
						this.ruleResult[3+j*8+i] = '1';
					}
					continue;
				}
				int randomNum = rand.nextInt(10)+1;
				if(randomNum <= IP_X) {
					this.ruleResult[3+j*8+i]= 'x';
				}else {
					if(rand.nextInt(2)==0) {
						this.ruleResult[3+j*8+i] = '0';
					}else {
						this.ruleResult[3+j*8+i] = '1';
					}
				}
			}
		}
	}
	
	
	private void genProtocal() {
		Random rand = new Random();
		int randomNum = rand.nextInt(10)+1;
		if(randomNum <= PROTOCAL_X) {
			this.ruleResult[19] = 'x';
		}else {
			if(rand.nextInt(2)==0) {
				this.ruleResult[19] = '0';
			}else {
				this.ruleResult[19] = '1';
			}
		}	
	}
	
	private void genProtocalFlow() {
		Random rand = new Random();
		int randomNum = rand.nextInt(10)+1;
		if(randomNum <= PROTOCAL_X + 7) {
			this.ruleResult[19] = 'x';
		}else {
			if(rand.nextInt(2)==0) {
				this.ruleResult[19] = '0';
			}else {
				this.ruleResult[19] = '1';
			}
		}	
	}
	
	private void genPortFlow() {
		for(int i=0;i<2;i++) {
			for(int j=0;j<4;j++) {
				this.ruleResult[i*4+j+20] = 'x';
			}
		}
	}
	
	private void genPort() {
		//
		Random rand = new Random();
		for(int i=0;i<2;i++) {
			//
			int randomNum = rand.nextInt(10)+1;
			if(randomNum <= PORT_ALL_X) {
				for(int j=0;j<4;j++) {
					this.ruleResult[i*4+j+20] = 'x';
				}
			}else {
				for(int j=0;j<4;j++) {
					if(rand.nextInt(2)==0) {
						this.ruleResult[i*4+j+20] = '0';
					}else {
						this.ruleResult[i*4+j+20] = '1';
					}
				}
			}
		}
	}
	
	public JSONObject get() {
		JSONObject rule = new JSONObject();
		String match =  String.valueOf(this.ruleResult);
		rule.put("match", match);
		Random rand = new Random();
		if(this.firewall) {
			rule.put("action", "accept");
		}else {
			if(rand.nextInt(2)==0) {
				rule.put("action", "accept");
			}else {
				rule.put("action", "drop");
			}
		}
		return rule;
	}
	
	public void printReadable() {
		System.out.println("===========可读形式=============");
		System.out.println("vlan is:");
		System.out.println(Arrays.copyOfRange(this.ruleResult, 0, 3));
		System.out.println("ip src is:");
		System.out.println(Arrays.copyOfRange(this.ruleResult, 3, 11));
		System.out.println("ip dst is:");
		System.out.println(Arrays.copyOfRange(this.ruleResult, 11, 19));
		System.out.println("protocal is:");
		System.out.println(this.ruleResult[19]);
		System.out.println("port src is:");
		System.out.println(Arrays.copyOfRange(this.ruleResult, 20, 24));
		System.out.println("port dst is:");
		System.out.println(Arrays.copyOfRange(this.ruleResult, 24, 28));
		System.out.println(this.ruleResult);
	}
	
	public static void main(String[] args) {
		try {
			for(int i=0;i<13;i++) {
				SecRuleGen rule = new SecRuleGen(1, false);
				System.out.println(rule.get());
			}
			//#rule.printReadable();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}

package flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlowGen {
	//vlanId is like 3437
	//Short.maxValue is 32767
	private Short vlanId;
	
	private String macSrc;
	private String macDst;

	private String ipSrc;
	private String ipDst;
	
	private String protocal;
	
	private Short portSrcStart;
	private Short portSrcEnd;

	private Short portDstStart;
	private Short portDstEnd;
	
	//0-96 mac
	//96-108 vlanId
	//108-172 ip
	//172-180 protocal
	//180-212 port
	public FlowGen(String bitsSequece) {
		this.copyVlanId(bitsSequece.substring(96, 108));
		this.ipSrc = this.copyIp(bitsSequece.substring(108,140));
		this.ipDst = this.copyIp(bitsSequece.substring(140,172));
		this.copyPort(bitsSequece.substring(180,196), this.portSrcStart, this.portSrcEnd);
		this.copyPort(bitsSequece.substring(196,212), this.portDstStart, this.portDstEnd);
		//this.copyMac(bitsSequece.substring(12,108));
		//this.copyProtocal(bitsSequece.substring(172,180));
		
	}
	
	private void copyVlanId(String vlan) {
		this.vlanId = Short.parseShort(vlan,2);
	}
	
	private void copyProtocal(String protocal) {
		
	}
	
	private void copyPort(String port, Short start, Short end) {
		if(port.indexOf("x")==0 ) {
			start = 0;
			end = 0;
		}
		if(!port.contains("x")) {
			start = Short.parseShort(port,2);
			end = Short.parseShort(port,2);
		}
		String srcString = port.replaceAll("x", "0");
		start = Short.parseShort(srcString,2);
		String dstString = port.replaceAll("x", "1");
		end = Short.parseShort(dstString,2);
	}

/*	private void copyMac(String mac) {
		String dst = mac.substring(0, 48);
		dst.co
	}
	
	private String transferMac(){
		
	}*/
	
	private boolean copyIpStep(StringBuilder result, String part, int mask, String zeros) {
		if(!part.contains("x")) {
			 result.append(String.valueOf(Short.parseShort(part,2)));
			 if(!(mask==8)) {
				 result.append(".");
				 return false;
			 }
			 //means finish
			 return true;
		}else{
			int index = part.indexOf("x");
			int maskRest = mask - index;
			String partOne = copyIp8bits(part);
			result.append(partOne + zeros + "/" + String.valueOf(maskRest)); 
			return true;
		}
	}
	
	/*
	 * return 192.168.0.0/16
	 */
	private String copyIp(String ip) {
		//对于IP没有什么要求，既默认为全x
		if(ip.startsWith("x")) {
			return "";
		}
		StringBuilder result = new StringBuilder();
		String ipOnePart = ip.substring(0, 8);
		if(this.copyIpStep(result, ipOnePart, 32, ".0.0.0")) {
			return result.toString();
		}
		String ipTwoPart = ip.substring(8,16);
		if(this.copyIpStep(result, ipTwoPart, 24, ".0.0")) {
			return result.toString();
		}
		String ipThreePart = ip.substring(16,24);
		if(this.copyIpStep(result, ipThreePart, 16, ".0")) {
			return result.toString();
		}
		
		String ipFourthPart = ip.substring(24,32);
		this.copyIpStep(result, ipFourthPart, 8, "0");
		return result.toString();
	}
	
	private String copyIp8bits(String bits) {
		String replaceBits = bits.replaceAll("x", "0");
		return String.valueOf(Short.parseShort(replaceBits,2));
	}
	
	
	
}

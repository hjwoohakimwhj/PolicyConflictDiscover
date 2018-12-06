package flow;

import java.util.HashMap;

public class FlowSet {
	public String macDst;
	public String macSrc;
	public String vlanId;
	public String ipDst;
	public String ipSrc;
	public String protocal;
	public String portDst;
	public String portSrc;
	public String header;

	public FlowSet() {
	}
	
	public void setFlowSet(HashMap<String, String> flow) {
		this.macDst = flow.get("macDst") == null ? Util.x48:flow.get("macDst");
		this.macSrc = flow.get("macSrc") == null ? Util.x48:flow.get("macSrc");
		this.vlanId = flow.get("vlanId") == null ? Util.x12:flow.get("vlanId");
		this.ipDst = flow.get("ipDst") == null ? Util.x32:flow.get("ipDst");
		this.ipSrc = flow.get("ipSrc") == null ? Util.x32:flow.get("ipSrc");
		this.protocal = flow.get("protocal") == null ? Util.x8:flow.get("protocal");
		this.portDst = flow.get("portDst") == null ? Util.x16:flow.get("portDst");
		this.portSrc = flow.get("portSrc") == null ? Util.x16:flow.get("portSrc");
		this.header = this.macDst + this.macSrc + this.vlanId + this.ipDst +
				this.ipSrc + this.protocal + this.portDst + this.portSrc;
	}
	
	public String getHeader() {
		return this.header;
	}
	
	public String getInterSect(FlowSet flowSetTwo) throws Exception{
		String headerTwo = flowSetTwo.getHeader();
		String interSect = Util.computeIntersect(this.header, headerTwo);
		if(!Util.checkValid(interSect)) {
			throw new Exception("the interSect is invalid");
		}
		return interSect;
	}
	
	public FlowGen createFlow(String interSect) {
		return new FlowGen(interSect);
	}
}

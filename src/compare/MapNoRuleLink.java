package compare;
public class MapNoRuleLink {
	private String srcNode;
	private String dstNode;
	private String srcPort;
	private String dstPort;
	private Double band;
	public MapNoRuleLink(String srcNode, String srcPort, String dstNode, String dstPort, Double band) {
		this.setSrcNode(srcNode);
		this.setDstNode(dstNode);
		this.setSrcPort(srcPort);
		this.setDstPort(dstPort);
		this.band = band;
	}
	
	public boolean checkBand(Double band) {
		return this.band > band;
	}
	
	public void decreaseBand(Double band) {
		this.band = this.band - band;
	}
	
	public Double getBand() {
		return this.band;
	}

	public String getSrcNode() {
		return srcNode;
	}

	public void setSrcNode(String srcNode) {
		this.srcNode = srcNode;
	}

	public String getDstNode() {
		return dstNode;
	}

	public void setDstNode(String dstNode) {
		this.dstNode = dstNode;
	}

	public String getSrcPort() {
		return srcPort;
	}

	public void setSrcPort(String srcPort) {
		this.srcPort = srcPort;
	}

	public String getDstPort() {
		return dstPort;
	}

	public void setDstPort(String dstPort) {
		this.dstPort = dstPort;
	}

}

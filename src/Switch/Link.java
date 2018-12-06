package Switch;

public class Link {
	private String srcSwitch;
	private String srcPort;
	private String dstSwitch;
	private String dstPort;
	private Double bandTotal;
	private Double bandUsed;
	
	public Link(String srcSwitch, String srcPort, String dstSwitch, String dstPort
			, Double bandTotal, Double bandUsed) {
		setDstPort(dstPort);
		setDstSwitch(dstSwitch);
		setSrcPort(srcPort);
		setSrcSwitch(srcSwitch);
		setBandTotal(bandTotal);
		setBandUsed(bandUsed);
	}
	
	public void consumeBand(Double band) {
		this.bandUsed += band;
	}

	public String getSrcSwitch() {
		return srcSwitch;
	}
	public void setSrcSwitch(String srcSwitch) {
		this.srcSwitch = srcSwitch;
	}
	public String getSrcPort() {
		return srcPort;
	}
	public void setSrcPort(String srcPort) {
		this.srcPort = srcPort;
	}
	public String getDstSwitch() {
		return dstSwitch;
	}
	public void setDstSwitch(String dstSwitch) {
		this.dstSwitch = dstSwitch;
	}
	public String getDstPort() {
		return dstPort;
	}
	public void setDstPort(String dstPort) {
		this.dstPort = dstPort;
	}

	public Double getBandTotal() {
		return bandTotal;
	}

	public void setBandTotal(Double bandTotal) {
		this.bandTotal = bandTotal;
	}

	public Double getBandUsed() {
		return bandUsed;
	}

	public void setBandUsed(Double bandUsed) {
		this.bandUsed = bandUsed;
	}
}

package compare;

import java.util.ArrayList;

public class MapNoRuleCluster {
	private String hostSrc;
	private String hostDst;
	private ArrayList<String> path;
	private Double band;
	public MapNoRuleCluster(String src, String dst, ArrayList<String> path, Double band) {
		this.setHostSrc(src);
		this.setHostDst(dst);
		this.setPath(path);
		this.setBand(band);
	}
	public boolean check(String src, String dst) {
		if(src.equals(hostSrc)&& dst.equals(hostDst)){
			return true;
		}
		return false;
	}
	
	public Double getBand() {
		return band * (path.size()-1);
	}
	public String getHostSrc() {
		return hostSrc;
	}
	public void setHostSrc(String hostSrc) {
		this.hostSrc = hostSrc;
	}
	public String getHostDst() {
		return hostDst;
	}
	public void setHostDst(String hostDst) {
		this.hostDst = hostDst;
	}
	public ArrayList<String> getPath() {
		return path;
	}
	public void setPath(ArrayList<String> path) {
		this.path = path;
	}
	public void setBand(Double band) {
		this.band = band;
	}
}

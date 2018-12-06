package compare;
import java.util.ArrayList;
public class MapNoRuleContain {
	private ArrayList<ArrayList<MapNoRuleCluster>> chain = new ArrayList<ArrayList<MapNoRuleCluster>>();
	private ArrayList<Double> bandList = null;
	
	public void add(ArrayList<MapNoRuleCluster> chainOne) {
		this.chain.add(chainOne);
	}
	public void addBandList(ArrayList<Double> bandList) {
		this.bandList = bandList;
	}
	
	public ArrayList<ArrayList<String>> compute(){
		//System.out.println("compute");
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		Double bandLeast = 1000.0;
		ArrayList<String> choice = new ArrayList<String>();
		MapNoRuleChoice choiceBag = new MapNoRuleChoice(bandLeast, choice);
		for(MapNoRuleCluster cluster: chain.get(0)) {
			ArrayList<String> occupied = new ArrayList<String>();
			String src = cluster.getHostSrc();
			String dst = cluster.getHostDst();
			occupied.add(src);
			occupied.add(dst);
			//System.out.println("occupied is " + occupied);
			if(chain.size()==1) {
				//System.out.println("**********");
				Double bandTotal = this.calBand(occupied);
				//System.out.println(bandTotal);
				choiceBag.refresh(bandTotal, occupied);
			}else {
				int index = 1;
				this.search(index, occupied, choiceBag);
			}
		}
		//System.out.println("================");
		
		for(int i=0;i<choiceBag.size()-1;i++) {
			//System.out.println("path is ");
			ArrayList<String> path = this.getPath(i, choiceBag.get(i), choiceBag.get(i+1));
			//System.out.println(path);
			result.add(path);
		}
		return result;
	}
	
	private void search(int index,  ArrayList<String> occupied, MapNoRuleChoice choice) {
		for(MapNoRuleCluster cluster: chain.get(index)) {
			String src = cluster.getHostSrc();
			String dst = cluster.getHostDst();
			if(!src.equals(occupied.get(occupied.size()-1))){
				continue;
			}
			if(occupied.contains(dst)) {
				continue;
			}
			ArrayList<String> copyOccupied = copyList(occupied);
			copyOccupied.add(dst);
			if(index==this.chain.size()-1) {
				//System.out.println("finish occupied is");
				//System.out.println(copyOccupied);
				Double bandCal = this.calBand(copyOccupied);
				//System.out.println("finish band is");
				//System.out.println(bandCal);
				choice.refresh(bandCal, copyOccupied);
			}else {
				this.search(index + 1, copyOccupied, choice);	
			}
		}
	}
	private Double calBand(ArrayList<String> hosts) {
		Double bandTotal = 0.0;
		for(int i=0; i<hosts.size()-1;i++) {
			bandTotal += this.getBand(i, hosts.get(i), hosts.get(i+1));
		}
		return bandTotal;
	}
	
	private Double getBand(int index, String src, String dst) {
		Double band = null;
		for(MapNoRuleCluster cluster: this.chain.get(index)) {
			if(cluster.check(src, dst)) {
				band = cluster.getBand();
				break;
			}
		}
		return band;
	}
	
	private ArrayList<String> getPath(int index, String src, String dst){
		ArrayList<String> path = null;
		for(MapNoRuleCluster cluster: this.chain.get(index)) {
			if(cluster.check(src, dst)) {
				path = cluster.getPath();
				break;
			}
		}
		return path;
	}
	
	public ArrayList<String> copyList(ArrayList<String> list){
		ArrayList<String> listOne = new ArrayList<String>();
		for(String entry: list) {
			listOne.add(entry);
		}
		return listOne;
	}
}

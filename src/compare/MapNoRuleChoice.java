package compare;

import java.util.ArrayList;

public class MapNoRuleChoice {
	private Double bandLeast;
	private ArrayList<String> choice;
	
	public MapNoRuleChoice(Double bandLeast, ArrayList<String> choice) {
		this.bandLeast = bandLeast;
		this.choice = choice;
	}
	
	public void refresh(Double bandLeast, ArrayList<String> choice) {
		if(bandLeast < this.bandLeast) {
			this.bandLeast = bandLeast;
			this.choice = choice;
		}
	}
	
	public String get(int index) {
		return choice.get(index);
	}
	
	public int size() {
		return choice.size();
	}
}

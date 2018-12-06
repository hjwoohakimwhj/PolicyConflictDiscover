package Switch;

import flow.Util;

public class OperUnit {
	private String type;
	private String mask;
	
	//二元操作符
	public OperUnit(String type, String mask) {
		this.setType(type);
		this.setMask(mask);
	}

	//一元操作符
	public OperUnit(String type) {
		this.setType(type);
		this.setMask("");
	}
	
	public String copy(String sequence) {
		String result;
		switch(type) {
			case "xor" :
				result = Util.xor(sequence, mask);
				break;
			case "or" :
				result = Util.union(sequence, mask);
				break;
			case "and" :
				result = Util.computeIntersect(sequence, mask);
				break;
			case "not":
				result = Util.not(sequence);
				break;
			default:
				result = sequence;
		}
		return result;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getMask() {
		return mask;
	}
	public void setMask(String mask) {
		this.mask = mask;
	}
	
}

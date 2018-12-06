package Switch;

public class Action {
	private String match;
	//portName or miss or nat or drop
	private String action;
	
	//不是NAT，则operation为空
	private Operation operation;
	public Action() {
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	
	public void setAction(String action, Operation operation) {
		this.action = action;
		this.operation = operation;
	}
	public String getMatch() {
		return match;
	}
	public void setMatch(String match) {
		this.match = match;
		this.operation = null;
	}
	public Operation getOperation() {
		return operation;
	}
	public void setOperation(Operation operation) {
		this.operation = operation;
	}
}

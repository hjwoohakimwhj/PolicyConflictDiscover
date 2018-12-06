package Switch;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class Operation {
	List<OperUnit> operUnits = new ArrayList<OperUnit>();
	private int length;
	
	//operationSet : not空格and-111000空格or-000011 
	public Operation(String operationSet, int length) {
		this.length = length;
		String[] operations = operationSet.split(" ");
		for(String operation: operations) {
			if(operation.equals("not")) {
				OperUnit operUnit = new OperUnit(operation);
				operUnits.add(operUnit);
			}else {
				String type = StringUtils.substringBefore(operation, "-");
				String mask = StringUtils.substringAfter(operation, "-");
				if(mask.length()==this.length) {
					OperUnit operUnit = new OperUnit(type, mask);
					operUnits.add(operUnit);
				}else {
					System.out.println("class Operation , function Operation, "
							+ "operation length conflicts with the mask");
				}
			}
		}
	}
	
	public String copy(String sequence) {
		String result = sequence;
		for(OperUnit unit: operUnits) {
			result = unit.copy(result);
		}
		return result;
	}
	
	public static void main(String[] args) {
		//want 000000->111111->000111->xxxx1x'
		String sequence = "000000";
		int size = 6;
		String operation = "not xor-111000 or-111010";
		Operation operationOne = new Operation(operation, size);
		String result = operationOne.copy(sequence);
		System.out.println(result);
	}
}

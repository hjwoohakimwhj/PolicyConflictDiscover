package flow;

public class Test {
	public static void main(String[] args) {
/*		String headerOne = "xxxx00x000000000000001";
		String headerTwo = "00000010000000000000x1";
		//System.out.println(headerOne.toCharArray());
		String result = Util.computeIntersect(headerOne, headerTwo);
		System.out.println(result);
		if(result.contains("z")) {
			System.out.println("no intersect");
		}
		FlowGen flow = new FlowGen(result);
		System.out.println(Short.MAX_VALUE);*/
		
		String strOne = "1011x0xx01xxx";
		System.out.println(strOne.indexOf("x"));
	}
}

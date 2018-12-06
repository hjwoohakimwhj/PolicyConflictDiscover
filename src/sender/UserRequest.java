package sender;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class UserRequest {
	public static Integer VNFC_NUM_MIN = 2;
	public static Integer VNFC_NUM_MAX = 3;
	
	public static Integer REQ_MIN = 3;
	public static Integer REQ_MAX = 6;

	public static Integer RAM_MIN = 1;
	public static Integer RAM_MAX = 5;
	
	public static Integer vCPUs_MIN = 1;
	public static Integer vCPUs_MAX = 5;
	
	public static Double DISK_MIN = 1.0;
	public static Double DISK_MAX = 10.0;
	
	public static Double BAND_MIN = 1.0;
	public static Double BAND_MAX = 3.0;
	
	public static Integer VLAN_MAX = 8;
	
	public static int vlanNumCur = 0;
	public static int vlanLength = 3;
	
	private String userName;
	private int vlanNum;
	private int reqNum;
	private List<JSONObject> reqList = new ArrayList<JSONObject>();
	
	public UserRequest(String userName) throws Exception {
		this.userName = userName;
		vlanNum = vlanNumCur;
		vlanNumCur++;
		if(vlanNumCur> (int)Math.pow(2, vlanLength)) {
			System.out.println("vlanNumberCur is " + vlanNumCur);
			System.out.println((int)Math.pow(2, vlanLength));
			throw new Exception("用户数目过多");
		}
		
		//随机产生请求数目
		Random rand = new Random();

		this.reqNum = 28;
		//this.reqNum = rand.nextInt(REQ_MAX)+1;
		System.out.println("req number is " + this.reqNum);
		
		for(int i=0;i<this.reqNum;i++) {
			this.genVnfReq(i+1);
		}
	}
	
	public List<JSONObject> getReqList(){
		return this.reqList;
	}
	
	private void genVnfReq(int reqNum) {
		JSONObject req = new JSONObject();

		String vnfName = this.userName + "-" + String.valueOf(reqNum);

		Random rand = new Random();
		
		//2-3个vnfc
		int vnfcNum = rand.nextInt(VNFC_NUM_MAX-1)+2;
		JSONArray order = new JSONArray();
		for(int i=0;i<vnfcNum;i++) {
			JSONObject vnfc = this.genVnfc(vnfName,i+1);
			order.add(vnfc);
		}

		req.put("name", vnfName);
		req.put("order", order);
		this.reqList.add(req);
	}
	
	private JSONObject genVnfc(String vnfName, int vnfcNum) {
		JSONObject vnfc = new JSONObject();
		String vnfcName = vnfName + "-" + "vnfc" + vnfcNum;
		vnfc.put("vnfc", vnfcName);

		Random rand = new Random();
		int vCpuNum = rand.nextInt(vCPUs_MAX)+1;	
		int ramNum = rand.nextInt(RAM_MAX)+1;
		int physical = 0;
		if(rand.nextInt(10)<2) {
			//百分之20的概率是映射到专用的设备上
			physical = 1;
		}
		vnfc.put("vCPUs", vCpuNum);
		vnfc.put("ram", ramNum);
		vnfc.put("physical", physical);
		
		Double disk = DISK_MIN + ((DISK_MAX - DISK_MIN) * new Random().nextDouble());
		Double band = BAND_MIN +((BAND_MAX - BAND_MIN) * new Random().nextDouble());
		vnfc.put("disk", (double)Math.round(disk*100)/100);
		vnfc.put("band", (double)Math.round(band*100)/100);
		
		//JSONArray rules = this.genRules(this.vlanNum);
		JSONArray rules = this.genRulesNet(this.vlanNum);
		vnfc.put("rules", rules);
		
		return vnfc;
	}
	
	private JSONArray genRules(int vlanNum) {
		//这是其中一种
		RulesGen ruleGen = new RulesGen(vlanNum, vlanLength);
		return ruleGen.gen();
	}
	
	private JSONArray genRulesNet(int vlanNum) {
		Random rand = new Random();
		JSONArray rules = new JSONArray();
		int ruleNum = rand.nextInt(3)+1;
		for(int i=0;i<ruleNum;i++) {
			//user request 只能是true
			SecRuleGen ruleGen;
			try {
				ruleGen = new SecRuleGen(vlanNum, true);
				JSONObject rule = ruleGen.get();
				rules.add(rule);
			} catch (Exception e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
		return rules;
	}

	
	public static void main(String[] args) {
		UserRequest user;
		try {
			user = new UserRequest("whj");
			System.out.println(user.getReqList());
		} catch (Exception e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
}

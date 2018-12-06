package sender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;


public class Sender {
	public static final String USER_COMMON = "user";
	private int userNumber;
	private Map<String, List<JSONObject>> userReqMap = new HashMap<String, List<JSONObject>>();
	public Sender(int userNumber) {
		this.userNumber = userNumber;
		try {
			this.copy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void copy() throws Exception {
		for(int i=0;i<this.userNumber;i++) {
			String userName = USER_COMMON + String.valueOf(i+1);
			UserRequest userRequest = new UserRequest(userName);
			List<JSONObject> reqList = userRequest.getReqList();
			this.userReqMap.put(userName, reqList);
		}
	}
	
	public Map<String,List<JSONObject>> getUserReq(){
		return this.userReqMap;
	}
}

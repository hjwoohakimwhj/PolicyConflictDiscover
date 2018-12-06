package gate;

import java.util.ArrayList;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import util.SetOper;

public class SecuritySpace {
	//这个list不存在可以合并的项
	ArrayList<String> allowedList = new ArrayList<String>();
	
	//仅仅计算出允许的空间
	public SecuritySpace(JSONArray rules) {
		copy(rules);
	}
	
	private void copy(JSONArray rules) {
		for(Object ruleObj: rules) {
			JSONObject ruleJSON = JSONObject.fromObject(ruleObj);
			if(ruleJSON.getString("action").equals("accept")) {
				String match = ruleJSON.getString("match");
				SetOper.addNoRepeat(allowedList, match);
			}
		}
	}
	
	public 	ArrayList<String> getAllowedSpace(){
		return allowedList;
	}
}

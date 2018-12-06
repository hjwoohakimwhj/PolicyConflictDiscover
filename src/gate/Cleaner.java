package gate;

import java.util.ArrayList;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import util.SetOper;

public class Cleaner {
	public static JSONArray clean(Object raw) {
		JSONArray rawArray = JSONArray.fromObject(raw);
		JSONArray newArray = new JSONArray();
		ArrayList<String> matches = new ArrayList<String>();
		for(Object ruleRaw: rawArray) {
			JSONObject ruleJSON = JSONObject.fromObject(ruleRaw);
			String match = ruleJSON.getString("match");
			if(matches.size()==0) {
				matches.add(match);
			}else {
				SetOper.addNoRepeat(matches, match);
			}
		}
		for(String match: matches) {
			JSONObject rule = new JSONObject();
			rule.put("match", match);
			rule.put("action", "accept");
			newArray.add(rule);
		}
		return newArray;
	}
}

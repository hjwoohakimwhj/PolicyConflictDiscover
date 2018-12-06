package common;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JSONFile {
	public static JSONObject getJSONObjectByString(String fileName) throws IOException {
		File directory = new File(".");
		String filePath = directory.getCanonicalPath();
		filePath = filePath + "/src/" + fileName;
		String input = FileUtils.readFileToString(new File(filePath), "UTF-8");
		JSONObject jsonObject = JSONObject.fromObject(input);	
		return jsonObject;
	}
	
	public static JSONArray getJSONArrayByString(String fileName) throws IOException {
		File directory = new File(".");
		String filePath = directory.getCanonicalPath();
		filePath = filePath + "/src/" + fileName;
		String input = FileUtils.readFileToString(new File(filePath), "UTF-8");
		JSONArray jsonObject = JSONArray.fromObject(input);	
		return jsonObject;
	}
	
/*	public static void main(String[] args) throws IOException {
		JSONArray obj = JSONFile.getJSONArrayByString("host.json");
	}*/
}

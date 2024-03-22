package config;

import java.net.URL;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;

public class DismissedTargets {
	public static final String ACTION_DONT_INTERCEPT = "Forward";
	public static final String ACTION_DROP = "Drop";

	public static HashMap<String, String> targets = new HashMap<String,String>();

	public static String whichAction(String rawUrl) {
		//输入URL 反向从 json文件中查找对应的动作 {"*.firefox.com":"Drop","*.mozilla.com":"Drop"}

		//获取 小写的 URL | HOST
		String url = rawUrl;
		String host = "";
		try {
			host = new URL(url).getHost().toLowerCase();
			if (url.contains("?")){
				url = url.substring(0,url.indexOf("?")).toLowerCase();
			}
		}catch (Exception e) {
			return "";
		}

		//从GUI读取配置，写入当前对象 填充 targets
		FromGUI();

		// 从 targets hashmap中进行查找 url 和 host 对应的动作
		if(targets == null || targets.isEmpty()) return "";

		for (String rawKey:targets.keySet()) {
			String key = rawKey.toLowerCase();
			//字符串过滤方案 url
			if (url.startsWith(key)) {
				return targets.get(key);
			}
			//字符串过滤方案 host
			if (host.equalsIgnoreCase(key)) {
				return targets.get(key);
			}

			//正则过滤方案 忽略大小写 匹配 原始 key 匹配原始 URL
			try {
				Pattern pattern = Pattern.compile(rawKey, Pattern.CASE_INSENSITIVE);
				Matcher matcher = pattern.matcher(rawUrl);
				if (matcher.find()) return targets.get(rawKey);
			} catch (Exception e) {
				// 处理正则表达式语法错误的情况
				e.getMessage();
			}

//			//简单的通配符过滤方案
//			if (key.startsWith("*.")){
//				String tmpDomain = key.replaceFirst("\\*","");
//				if (host.endsWith(tmpDomain)){
//					return targets.get(key);
//				}
//			}

		}
		return "";
	}
	/**
	 * 将Map转换为Json格式的字符串
	 * @return
	 */
	public static String ToJson(){//注意函数名称，如果是get set开头，会被认为是Getter和Setter函数，会在序列化过程中被调用。
		if( targets.isEmpty()){ return "{}"; }
		return new Gson().toJson(targets);
	}
	
	/**
	 * 将Json字符串转换为Map
	 * @param json
	 * @return
	 */
	public static HashMap<String,String> FromJson(String json){//注意函数名称，如果是get set开头，会被认为是Getter和Setter函数，会在序列化过程中被调用。
		return new Gson().fromJson(json, HashMap.class);
	}
	
	/**
	 * 从GUI读取配置，写入当前对象
	 */
	public static void FromGUI() {
		String dismissed  = GUI.tableModel.getConfigValueByKey("DismissedTargets");
		try {
			targets = DismissedTargets.FromJson(dismissed);
		}catch (Exception e) {
			targetsInit();
		}
		if (targets == null) {
			targetsInit();
		}
	}
	
	public static void targetsInit() {
		targets = new HashMap<String,String>();
	}
	
	/**
	 * 将当前配置显示到GUI
	 */
	public static void ShowToGUI() {
		GUI.tableModel.setConfigByKey("DismissedTargets",ToJson());
	}
	
}

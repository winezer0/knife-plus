package burp;

import java.awt.Component;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;

import com.google.gson.Gson;

import U2C.ChineseTabFactory;
import config.Config;
import config.ConfigEntry;
import config.ConfigTable;
import config.ConfigTableModel;
import config.DismissedTargets;
import config.GUI;
import knife.*;
import plus.*;

public class BurpExtender extends GUI implements IBurpExtender, IContextMenuFactory, ITab, IHttpListener,IProxyListener,IExtensionStateListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public static IBurpExtenderCallbacks callbacks;
	public IExtensionHelpers helpers;
	public static PrintWriter stdout;
	public static PrintWriter stderr;
	public IContextMenuInvocation invocation;
	public int proxyServerIndex=-1;

	public static String ExtensionName = "knife";
	public static String Version = bsh.This.class.getPackage().getImplementationVersion();
	public static String github = "https://github.com/bit4woo/knife";

	public static String knifeConfig = String.format("knife.%s.config", Version);
	public static String CurrentProxy = "";

	@Override
	public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
		BurpExtender.callbacks = callbacks;
		this.helpers = callbacks.getHelpers();
		flushStd();
		BurpExtender.stdout.println(getFullExtensionName());
		BurpExtender.stdout.println(github);

		callbacks.setExtensionName(getFullExtensionName());

		// [重要] 使用 SwingUtilities.invokeLater 解决操作过快 导致出现swing崩溃的问题
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				table = new ConfigTable(new ConfigTableModel());
				configPanel.setViewportView(table);
				String content = callbacks.loadExtensionSetting(knifeConfig);
				if (content!=null) {
					config = new Gson().fromJson(content, Config.class);
					showToUI(config);
				}else {
					showToUI(new Gson().fromJson(initConfig(), Config.class));
				}
				table.setupTypeColumn();//call this function must after table data loaded !!!!
				ChineseTabFactory chineseTabFactory = new ChineseTabFactory(null, false, helpers, callbacks);

				//各项数据初始化完成后在进行这些注册操作，避免插件加载时的空指针异常
				callbacks.addSuiteTab(BurpExtender.this);
				callbacks.registerContextMenuFactory(BurpExtender.this);// for menus
				callbacks.registerMessageEditorTabFactory(chineseTabFactory);// for Chinese
				callbacks.registerHttpListener(BurpExtender.this);
				callbacks.registerProxyListener(BurpExtender.this);
				callbacks.registerExtensionStateListener(BurpExtender.this);

				//自动加载用户指定的 Project Json文件,如果不存在会自动保存当前配置
				AdvScopeUtils.autoLoadProjectConfig(callbacks,true);

				BurpExtender.stdout.println("Load Extension Success ...");
			}
		});

	}


	private static void flushStd(){
		try{
			stdout = new PrintWriter(callbacks.getStdout(), true);
			stderr = new PrintWriter(callbacks.getStderr(), true);
		}catch (Exception e){
			stdout = new PrintWriter(System.out, true);
			stderr = new PrintWriter(System.out, true);
		}
	}

	public static PrintWriter getStdout() {
		flushStd();//不同的时候调用这个参数，可能得到不同的值
		return stdout;
	}

	public static PrintWriter getStderr() {
		flushStd();
		return stderr;
	}

	//name+version+author
	public static String getFullExtensionName(){
		return ExtensionName + "." + Version;
	}

	//JMenu 是可以有下级菜单的，而JMenuItem是不能有下级菜单的
	@Override
	public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {
		ArrayList<JMenuItem> menu_item_list = new ArrayList<JMenuItem>();

		this.invocation = invocation;
		//常用
		menu_item_list.add(new OpenWithBrowserMenu(this));
		menu_item_list.add(new CustomPayloadMenu(this));
		menu_item_list.add(new CustomPayloadForAllInsertpointMenu(this));

		//cookie身份凭证相关
		menu_item_list.add(new UpdateCookieMenu(this));
		menu_item_list.add(new UpdateCookieWithHistoryMenu(this));

		menu_item_list.add(new SetCookieMenu(this));
		menu_item_list.add(new SetCookieWithHistoryMenu(this));

		UpdateHeaderMenu updateHeader = new UpdateHeaderMenu(this);//JMenuItem vs. JMenu
		if (updateHeader.getItemCount()>0) {
			menu_item_list.add(updateHeader);
		}
		//配置文件相关 //手动更新用户指定的 Project Json 文件
		menu_item_list.add(new ProjectConfigLoadMenu(this));
		menu_item_list.add(new ProjectConfigSaveMenu(this));
		menu_item_list.add(new ProjectScopeClearMenu(this));
		menu_item_list.add(new AddHostToInScopeMenu(this));
		menu_item_list.add(new AddHostToInScopeAdvMenu(this));
		menu_item_list.add(new AddHostToExScopeMenu(this));
		menu_item_list.add(new AddHostToExScopeAdvMenu(this));

		//扫描攻击相关
		menu_item_list.add(new RunSQLMapMenu(this));
		menu_item_list.add(new DoActiveScanMenu(this));
		menu_item_list.add(new DoPortScanMenu(this));


		//不太常用的
		menu_item_list.add(new DismissMenu(this));
		menu_item_list.add(new DismissCancelMenu(this));

		menu_item_list.add(new ChunkedEncodingMenu(this));
		menu_item_list.add(new DownloadResponseMenu(this));
		//menu_item_list.add(new DownloadResponseMenu2(this));
		//menu_item_list.add(new ViewChineseMenu(this));
		//menu_item_list.add(new JMenuItem());
		//空的JMenuItem不会显示，所以将是否添加Item的逻辑都方法到类当中去了，以便调整菜单顺序。
		
		menu_item_list.add(new FindUrlAndRequest(this));

		Iterator<JMenuItem> it = menu_item_list.iterator();
		while (it.hasNext()) {
			JMenuItem item = it.next();
			if (item.getText()==null || item.getText().equals("")) {
				it.remove();
			}
		}

		String oneMenu  = this.tableModel.getConfigValueByKey("Put_MenuItems_In_One_Menu");
		if (oneMenu != null) {
			ArrayList<JMenuItem> Knife = new ArrayList<JMenuItem>();
			JMenu knifeMenu = new JMenu("^_^ Knife");
			Knife.add(knifeMenu);
			for (JMenuItem item : menu_item_list) {
				knifeMenu.add(item);
			}
			return Knife;
		}else {
			return menu_item_list;
		}
	}


	@Override
	public String getTabCaption() {
		return getFullExtensionName();
	}


	@Override
	public Component getUiComponent() {
		return this.getContentPane();
	}

	@Override
	public void extensionUnloaded() {
		callbacks.saveExtensionSetting(knifeConfig, getAllConfig());
	}

	@Override
	public String initConfig() {
		config = new Config("default");
		tableModel = new ConfigTableModel();
		return getAllConfig();
	}

	//IProxyListener中的方法，修改的内容会在proxy中显示为edited
	@Override
	public void processProxyMessage(boolean messageIsRequest, IInterceptedProxyMessage message) {
		//processHttpMessage(IBurpExtenderCallbacks.TOOL_PROXY,true,message.getMessageInfo());
		//same action will be executed twice! if call processHttpMessage() here.
		//请求和响应到达proxy时，都各自调用一次,如下部分是测试代码，没毛病啊！
		/*
		HashMap<String, HeaderEntry> cookieToSetMap = config.getSetCookieMap();
		IHttpRequestResponse messageInfo = message.getMessageInfo();
		if (messageIsRequest) {
			byte[] newRequest = CookieUtils.updateCookie(message.getMessageInfo(),"aaa=111111111");
			message.getMessageInfo().setRequest(newRequest);

			stderr.println("request called "+cookieToSetMap);
		}else{
		stderr.println("response called "+cookieToSetMap);
			Getter getter = new Getter(helpers);
			List<String> setHeaders = GetSetCookieHeaders("bbb=2222;");
			List<String> responseHeaders = getter.getHeaderList(false,messageInfo);
			byte[] responseBody = getter.getBody(false,messageInfo);
			responseHeaders.addAll(setHeaders);

			byte[] response = helpers.buildHttpMessage(responseHeaders,responseBody);

			messageInfo.setResponse(response);
		}
		cookieToSetMap.clear();
		 */
		if (CurrentProxy == null || CurrentProxy.equals("")) {
			//为了知道burp当前监听的接口。供“find url and request”菜单使用
			CurrentProxy = message.getListenerInterface();
		}
		
		HelperPlus getter = new HelperPlus(helpers);
		if (messageIsRequest && this.tableModel.getConfigValueByKey("DismissedTargets") != null) {
			//丢弃干扰请求
			String url = getter.getFullURL(message.getMessageInfo()).toString();
			String action = DismissedTargets.whichAction(url);
			if (action.equalsIgnoreCase(DismissedTargets.ACTION_DONT_INTERCEPT)){
				message.setInterceptAction(IInterceptedProxyMessage.ACTION_DONT_INTERCEPT);
				message.getMessageInfo().setComment("Auto Forwarded By Knife");
				message.getMessageInfo().setHighlight("gray");
			}
			if (action.equalsIgnoreCase(DismissedTargets.ACTION_DROP)){
				message.setInterceptAction(IInterceptedProxyMessage.ACTION_DROP);
				message.getMessageInfo().setComment("Auto Dropped by Knife");
				message.getMessageInfo().setHighlight("gray");
			}
			return;
		}

		/*setCookie的实现方案1。请求和响应数据包的修改都由processProxyMessage函数来实现。这种情况下：
		 * 在Proxy拦截处进行SetCookie的操作时，该函数已经被调用！这个函数的调用时在手动操作之前的。
		 * 即是说，当这个函数第一次被调用时，还没来得及设置cookie，获取到的cookieToSetMap必然为空，所以需要rehook操作。
		 *setCookie的实现方案2。主要目标是为了避免rehook，分两种情况分别处理。
		 * 情况一：当当前是CONTEXT_MESSAGE_EDITOR_REQUEST的情况下（比如proxy和repeater中），
		 * 更新请求的操作和updateCookie的操作一样，在手动操作时进行更新，而响应包由processProxyMessage来更新。
		 * 情况二：除了上面的情况，请求包和响应包的更新都由processProxyMessage来实现，非proxy的情况下也不需要再rehook。
		 *
		 */
		HashMap<String, HeaderEntry> cookieToSetMap = config.getSetCookieMap();
		//stdout.println("processProxyMessage called when messageIsRequest="+messageIsRequest+" "+cookieToSetMap);
		if (cookieToSetMap != null && !cookieToSetMap.isEmpty()){//第二次调用如果cookie不为空，就走到这里

			IHttpRequestResponse messageInfo = message.getMessageInfo();
			//String CurrentUrl = messageInfo.getHttpService().toString();//这个方法获取到的url包含默认端口！

			String CurrentUrl = HelperPlus.getShortURL(messageInfo).toString();
			//stderr.println(CurrentUrl+" "+targetUrl);
			HeaderEntry cookieToSet = cookieToSetMap.get(CurrentUrl);
			if (cookieToSet != null){

				String targetUrl = cookieToSet.getTargetUrl();
				String cookieValue = cookieToSet.getHeaderValue();

				if (messageIsRequest) {
					if (!cookieToSet.isRequestUpdated()) {
						byte[] newRequest = CookieUtils.updateCookie(messageInfo,cookieValue);
						messageInfo.setRequest(newRequest);
					}
				}else {
					List<String> responseHeaders = getter.getHeaderList(false,messageInfo);
					byte[] responseBody = HelperPlus.getBody(false,messageInfo);
					List<String> setHeaders = GetSetCookieHeaders(cookieValue);
					responseHeaders.addAll(setHeaders);

					byte[] response = helpers.buildHttpMessage(responseHeaders,responseBody);

					messageInfo.setResponse(response);
					cookieToSetMap.remove(CurrentUrl);//only need to set once
				}
			}

		}
		/*改用方案二，无需再rehook
		else {//第一次调用必然走到这里
			message.setInterceptAction(IInterceptedProxyMessage.ACTION_FOLLOW_RULES_AND_REHOOK);
			//让burp在等待用户完成操作后再次调用，就相当于再次对request进行处理。
			//再次调用，即使走到了这里，也不会再增加调用次数，burp自己应该有控制。
		}*/

	}

	//IHttpListener中的方法，修改的内容在Proxy中不可见
	@Override
	public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
		//stdout.println("processHttpMessage called when messageIsRequest="+messageIsRequest);
		try {
			if (messageIsRequest) {
				Getter getter = new Getter(helpers);
				URL url = getter.getFullURL(messageInfo);
				String host = getter.getHost(messageInfo);
				LinkedHashMap<String, String> headers = getter.getHeaderMap(messageIsRequest,messageInfo);
				byte[] body = getter.getBody(messageIsRequest,messageInfo);

				boolean isRequestChanged = false;

				//remove header
				List<ConfigEntry> configEntries = tableModel.getConfigByType(ConfigEntry.Action_Remove_From_Headers);
				for (ConfigEntry entry : configEntries) {
					String key = entry.getKey();
					if (headers.remove(key) != null) {
						isRequestChanged = true;
					}
				}

				//add/update/append header
				if (toolFlag == (toolFlag & checkEnabledFor())) {
					//if ((config.isOnlyForScope() && callbacks.isInScope(url))|| !config.isOnlyForScope()) {
					if (!config.isOnlyForScope()||callbacks.isInScope(url)){

						//请求时，自动进行内容替换
						List<ConfigEntry> updateOrAddEntries = tableModel.getConfigEntries();
						for (ConfigEntry entry : updateOrAddEntries) {
							String key = entry.getKey();
							String value = entry.getValue();

							//将 config面板中 value列 的 %host变量替换为 当前主机host
							if (value.contains("%host")) {
								value = value.replaceAll("%host", host);
								//stdout.println("3333"+value);
							}

							//将 config面板中 value列 的 %dnslogserver变量替换为 用户自己设置的 DNSlogServer 属性的值
							if (value.toLowerCase().contains("%dnslogserver")) {
								String dnslog = tableModel.getConfigValueByKey("DNSlogServer");
								// DNSlogServer 没有配置 获得 null 时, 会导致报错
								if (dnslog != null){
									Pattern p = Pattern.compile("(?u)%dnslogserver");
									Matcher m = p.matcher(value);
									while (m.find()) {
										String found = m.group(0);
										value = value.replaceAll(found, dnslog);
									}
								}
							}

							if (entry.getType().equals(ConfigEntry.Action_Add_Or_Replace_Header) && entry.isEnable()) {
								// 增加或替换请求头 行
								headers.put(key, value);
								isRequestChanged = true;
							} else if (entry.getType().equals(ConfigEntry.Action_Append_To_header_value) && entry.isEnable()) {
								// 增加或替换请求头的值
								String oldValue = headers.get(key);
								if (oldValue == null) {
									oldValue = "";
								}
								value = oldValue + value;
								headers.put(key, value);
								isRequestChanged = true;
								//stdout.println("2222"+value);
							} else if (entry.getKey().equalsIgnoreCase("Chunked-AutoEnable") && entry.isEnable()) {
								// 开启 自动分块
								headers.put("Transfer-Encoding", " chunked");
								isRequestChanged = true;

								try {
									boolean useComment = false;
									if (this.tableModel.getConfigValueByKey("Chunked-UseComment") != null) {
										useComment = true;
									}
									String lenStr = this.tableModel.getConfigValueByKey("Chunked-Length");
									int len = 10;
									if (lenStr != null) {
										len = Integer.parseInt(lenStr);
									}
									body = Methods.encoding(body, len, useComment);
								} catch (UnsupportedEncodingException e) {
									stderr.print(e.getStackTrace());
								}
							}
						}


						/// proxy function should be here 请求时，自动进行上游代理配置
						//reference https://support.portswigger.net/customer/portal/questions/17350102-burp-upstream-proxy-settings-and-sethttpservice
						String proxy = this.tableModel.getConfigValueByKey("Proxy-ServerList");
						String mode = this.tableModel.getConfigValueByKey("Proxy-UseRandomMode");

						if (proxy != null) {//if enable is false, will return null.
							List<String> proxyList = Arrays.asList(proxy.split(";"));//如果字符串是以;结尾，会被自动丢弃

							if (mode != null) {//random mode
								proxyServerIndex = (int) (Math.random() * proxyList.size());
								//proxyServerIndex = new Random().nextInt(proxyList.size());
							} else {
								proxyServerIndex = (proxyServerIndex + 1) % proxyList.size();
							}
							String proxyhost = proxyList.get(proxyServerIndex).split(":")[0].trim();
							int port = Integer.parseInt(proxyList.get(proxyServerIndex).split(":")[1].trim());

							messageInfo.setHttpService(helpers.buildHttpService(proxyhost, port, messageInfo.getHttpService().getProtocol()));

							String method = helpers.analyzeRequest(messageInfo).getMethod();
							headers.put(method, url.toString());
							isRequestChanged = true;
							//success or failed,need to check?
						}
					}
				}
				if (isRequestChanged){
					//set final request
					List<String> headerList = getter.headerMapToHeaderList(headers);
					messageInfo.setRequest(helpers.buildHttpMessage(headerList,body));
				}

				/*
				if (isRequestChanged) {
					//debug
					List<String> finalheaders = helpers.analyzeRequest(messageInfo).getHeaders();
					//List<String> finalheaders = editer.getHeaderList();//error here:bodyOffset getted twice are different
					stdout.println(System.lineSeparator() + "//////////edited request by knife//////////////" + System.lineSeparator());
					for (String entry : finalheaders) {
						stdout.println(entry);
					}
				}*/
			} else {//response
				Getter getter = new Getter(helpers);
				URL url = getter.getFullURL(messageInfo);
				if (toolFlag == (toolFlag & checkEnabledFor())) {
					if (!config.isOnlyForScope()||callbacks.isInScope(url)){
						//给 Options 方法的响应 添加 Content-Type: application/octet-stream 用于过滤
						if(this.tableModel.getConfigValueByKey("AddRespHeaderByReqMethod")!= null){
							AddRespHeaderByReqMethod(messageInfo);
						}

						//给没有后缀的图片URL添加响应头,便于过滤筛选
						if (this.tableModel.getConfigValueByKey("AddRespHeaderByReqURL")!= null){
							AddRespHeaderByReqUrl(messageInfo);
						}

						//给Json格式的请求的响应添加响应头,防止被Js过滤
						if (this.tableModel.getConfigValueByKey("AddRespHeaderByRespHeader")!= null){
							AddRespHeaderByRespHeader(messageInfo);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			stderr.print(e.getStackTrace());
		}
	}

	public static void confirmProxy() {
		String proxy = JOptionPane.showInputDialog("Confirm Proxy Of Burp", "127.0.0.1:8080");
		if (proxy != null) {
			BurpExtender.CurrentProxy = proxy.trim();
		}
	}
	
	public static String getProxyHost() {
		try {
			if (CurrentProxy == null ||CurrentProxy.equals("") || CurrentProxy.split(":").length!=2) {
				confirmProxy();
			}
			String proxyHost = CurrentProxy.split(":")[0];
			return proxyHost;
		} catch (Exception e) {
			e.printStackTrace();
			CurrentProxy="";//设置为空，以便重新获取。
			return null;
		}
	}
	
	public static int getProxyPort() {
		try {
			if (CurrentProxy == null ||CurrentProxy.equals("") || CurrentProxy.split(":").length!=2) {
				confirmProxy();
			}
			String proxyPort = CurrentProxy.split(":")[1];
			return Integer.parseInt(proxyPort);
		} catch (Exception e) {
			e.printStackTrace();
			CurrentProxy="";//设置为空，以便重新获取。
			return -1;
		}
	}

	public List<String> GetSetCookieHeaders(String cookies){
		if (cookies.startsWith("Cookie: ")){
			cookies = cookies.replaceFirst("Cookie: ","");
		}

		String[] cookieList = cookies.split("; ");
		List<String> setHeaderList= new ArrayList<String>();
		//Set-Cookie: SST_S22__WEB_RIGHTS=SST_S22_JT_RIGHTS_113_9; Path=/
		for (String cookie: cookieList){
			setHeaderList.add(String.format("Set-Cookie: %s; Path=/",cookie));
		}
		return setHeaderList;
	}

	public static IBurpExtenderCallbacks getCallbacks() {
		return callbacks;
	}

	private void msgInfoSetResponse(IHttpRequestResponse messageInfo, String addRespHeaderLine) {
		//进行实际处理
		if(addRespHeaderLine != null){
			HelperPlus helperPlus = new HelperPlus(callbacks.getHelpers());
			String respHeaderName = "Content-Type";
			String respHeaderValue = "application/octet-stream";
			if (addRespHeaderLine.contains(":")) {
				respHeaderName = addRespHeaderLine.split(":", 2)[0].trim();
				respHeaderValue = addRespHeaderLine.split(":", 2)[1].trim();
			}
			byte[] resp = helperPlus.addOrUpdateHeader(false, messageInfo.getResponse(), respHeaderName, respHeaderValue);
			messageInfo.setResponse(resp);
			messageInfo.setComment("Resp Add Header By Knife"); //在logger中没有显示comment
		}
	}

	private void AddRespHeaderByReqMethod(IHttpRequestResponse messageInfo){
		HelperPlus helperPlus = new HelperPlus(callbacks.getHelpers());
		// 获取 请求方法
		String curMethod = helperPlus.getMethod(messageInfo).toLowerCase();

		//获取对应的Json格式规则  {"OPTIONS":"Content-Type: application/octet-stream"}
		String addRespHeaderConfig = this.tableModel.getConfigValueByKey("AddRespHeaderByReqMethod");
		//解析Json格式的规则
		HashMap<String, String> addRespHeaderRuleMap = UtilsPlus.parseJsonRule2HashMap(addRespHeaderConfig, true);

		if(addRespHeaderRuleMap != null && addRespHeaderRuleMap.containsKey(curMethod)) {
			//获取需要添加的响应头 每个方法只支持一种动作,更多的动作建议使用其他类型的修改方式
			String addRespHeaderLine = addRespHeaderRuleMap.get(curMethod);
			msgInfoSetResponse(messageInfo, addRespHeaderLine);
		}
	}

	private void AddRespHeaderByReqUrl(IHttpRequestResponse messageInfo){
		HelperPlus helperPlus = new HelperPlus(callbacks.getHelpers());
		// 获取 请求URL
		String curUrl = helperPlus.getFullURL(messageInfo).toString().toLowerCase();

		//获取对应的Json格式规则 // {"www.baidu.com":"Content-Type: application/octet-stream"}
		String addRespHeaderConfig = this.tableModel.getConfigValueByKey("AddRespHeaderByReqURL");
		//解析Json格式的规则
		HashMap<String, String> addRespHeaderRuleMap = UtilsPlus.parseJsonRule2HashMap(addRespHeaderConfig, true);

		if (addRespHeaderRuleMap == null) return;

		//循环 获取需要添加的响应头 并 设置响应头信息
		for (String rule:addRespHeaderRuleMap.keySet()) {
			//获取需要添加的响应头 每个URL支持多种动作规则
			String addRespHeaderLine = UtilsPlus.getActionFromRuleMap(addRespHeaderRuleMap, rule, curUrl);
			msgInfoSetResponse(messageInfo, addRespHeaderLine);
		}
	}

	private void AddRespHeaderByRespHeader(IHttpRequestResponse messageInfo){
		HelperPlus helperPlus = new HelperPlus(callbacks.getHelpers());
		//获取对应的Json格式规则 // {"www.baidu.com":"Content-Type: application/octet-stream"}
		String addRespHeaderConfig = this.tableModel.getConfigValueByKey("AddRespHeaderByRespHeader");
		//解析Json格式的规则
		HashMap<String, String> addRespHeaderRuleMap = UtilsPlus.parseJsonRule2HashMap(addRespHeaderConfig,false);
		if (addRespHeaderRuleMap == null) return;

		//获取响应头
		List<String> responseHeaders = helperPlus.getHeaderList(false, messageInfo);
		for (String rule:addRespHeaderRuleMap.keySet()) {
			for (String responseHeader:responseHeaders){
				//获取需要添加的响应头 每个规则只处理一种响应头，支持多种动作规则
				String addRespHeaderLine = UtilsPlus.getActionFromRuleMap(addRespHeaderRuleMap, rule, responseHeader);
				if(addRespHeaderLine!=null){
					msgInfoSetResponse(messageInfo, addRespHeaderLine);
					break; 	//匹配成功后就进行下一条规则的匹配
				}
			}
		}
	}
}

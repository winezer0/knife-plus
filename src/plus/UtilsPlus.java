package plus;

import U2C.CharSetHelper;
import burp.IBurpExtenderCallbacks;
import burp.IPAddressUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import config.GUI;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class UtilsPlus {
    /**
     * 将[.]替换为[\.],便于进行正则精确匹配
     * @param host
     * @return
     */
    public static String dotToEscapeDot(String host ) {
        return host.replace(".","\\.");
    }


    /**
     * 将域名变为转移的上级域名转义正则 www.xxx.com -> .*\.xxx\.com IP仅转义.号
     * @param host
     * @return
     */
    public static String hostToWildcardHostWithDotEscape(String host) {
        if(isIPFormat(host)){
            return dotToEscapeDot(domainToSuperiorDomain(host));
        }else {
            return ".*" + "\\." + dotToEscapeDot(domainToSuperiorDomain(host));
        }
    }

    /**
     * 判断Host不是IPv4或者IPv6格式
     * @param host
     * @return
     */
    public static boolean isIPFormat(String host) {
        boolean isIpv4 = IPAddressUtil.isIPv4LiteralAddress(host);
        boolean isIpv6 = IPAddressUtil.isIPv6LiteralAddress(host);
        return isIpv4||isIpv6;
    }

    /**
     * 域名转为上级域名格式 www.baidu.com -> baidu.com
     * @param domain
     * @return
     */
    public static String domainToSuperiorDomain(String domain){
        // 获取上级域名 3级域名获取2级域名|2级域名获取主域名|主域名不操作
        String[] hostParts = domain.split("\\.");
        if (hostParts.length > 2) {
            String[] slicedArr = Arrays.copyOfRange(hostParts, 1, hostParts.length);
            domain = String.join(".", slicedArr);
        }
        return domain;
    }

    /**
     * 追加Auto_Append_Hosts表单设置到配置文件的排除列表中
     * @param callbacks
     */
    public static void addDefaultExcludeHosts(IBurpExtenderCallbacks callbacks) {
        String defaultExcludeHosts  = GUI.tableModel.getConfigValueByKey("Auto_Append_Hosts_To_Exclude_Scope");
        if (defaultExcludeHosts!=null && defaultExcludeHosts.trim().length()>0){
            HashSet<String> hashSet = new HashSet<>();
            //切割并整理输入
            List<String> defaultExcludeHostList = Arrays.asList(defaultExcludeHosts.split(","));
            for(String host:defaultExcludeHostList){
                hashSet.add(host.trim());
            }
            //添加主机名到排除列表
            AddHostToExScopeAdvByProjectConfig(callbacks, hashSet);
        }
    }


    /**
     * 清空所有Scope内容
     * @param callbacks
     */
    public static void ClearAllScopeAdvByProjectConfig(IBurpExtenderCallbacks callbacks) {
        // 1、读取当前的配置文件
        String configContent = callbacks.saveConfigAsJson();
        JsonObject jsonObject = JsonParser.parseString(configContent).getAsJsonObject();

        //生成IncludeJson元素 清空元素
        jsonObject.get("target").getAsJsonObject().get("scope").getAsJsonObject().add("include",new JsonArray());
        jsonObject.get("target").getAsJsonObject().get("scope").getAsJsonObject().add("exclude",new JsonArray());

        //加载生成的Json配置到应用
        String jsonObjectString = new Gson().toJson(jsonObject);
        callbacks.loadConfigFromJson(jsonObjectString);

        //根据用户设置,保存当前内存的配置到Json配置到文件
        autoSaveProjectConfigWithFlag(callbacks);
    }

    /**
     * 添加主机名到包含列表
     * @param callbacks
     * @param hostHashSet
     */
    public static void AddHostToInScopeAdvByProjectConfig(IBurpExtenderCallbacks callbacks, HashSet<String> hostHashSet) {
        //不处理没有获取到host的情况
        if(hostHashSet.size()>0){
            // 1、读取当前的配置文件
            String configContent = callbacks.saveConfigAsJson();
            JsonObject jsonObject = JsonParser.parseString(configContent).getAsJsonObject();
            //开起前置条件

            //高级模式开关 //设置高级模式
            jsonObject.get("target").getAsJsonObject().get("scope").getAsJsonObject().addProperty("advanced_mode",true);

            //生成IncludeJson元素 并循环添加到json对象中
            JsonArray includeJsonArray = jsonObject.get("target").getAsJsonObject().get("scope").getAsJsonObject().get("include").getAsJsonArray();
            for(String host:hostHashSet){
                HashMap<String,Object> aIncludeHashMap = new HashMap();
                aIncludeHashMap.put("enabled",true);
                aIncludeHashMap.put("host",host);
                aIncludeHashMap.put("protocol","any");
                String includeJsonString =new Gson().toJson(aIncludeHashMap);
                JsonObject includeJsonObject = JsonParser.parseString(includeJsonString).getAsJsonObject();
                includeJsonArray.add(includeJsonObject);
            }
            //去重Json对象的包含列表
            JsonArray removeDuplicateJsonArray = DeDuplicateJsonObjectJsonArray(includeJsonArray,"host");
            //删除包含列表里面.*的对象不然没有意义
            removeDuplicateJsonArray = RemoveJsonObjectJsonArray(removeDuplicateJsonArray,"host",".*");
            //将修改后的数据保存到json里面
            jsonObject.get("target").getAsJsonObject().get("scope").getAsJsonObject().add("include",removeDuplicateJsonArray);

            //去除排除列表中和包含列表相同的数据
            JsonArray excludeJsonArray = jsonObject.get("target").getAsJsonObject().get("scope").getAsJsonObject().get("exclude").getAsJsonArray();
            if(excludeJsonArray.size()>0){
                JsonArray removeJsonObjectJsonArray = RemoveJsonObjectJsonArray(excludeJsonArray,"host",hostHashSet);
                jsonObject.get("target").getAsJsonObject().get("scope").getAsJsonObject().add("exclude",removeJsonObjectJsonArray);
            }

            //加载生成的Json配置到应用
            String jsonObjectString = new Gson().toJson(jsonObject);
            callbacks.loadConfigFromJson(jsonObjectString);

            //根据用户设置,保存当前内存的配置到Json配置到文件
            autoSaveProjectConfigWithFlag(callbacks);
        }
    }

    /**
     * 当包含列表为空时 添加.*主机名到包含列表
     * @param callbacks
     */
    public static void AddAnyHostToInScopeAdvByProjectConfig(IBurpExtenderCallbacks callbacks) {
        // 1、读取当前的配置文件
        String configContent = callbacks.saveConfigAsJson();
        JsonObject jsonObject = JsonParser.parseString(configContent).getAsJsonObject();
        //高级模式开关 //设置高级模式
        jsonObject.get("target").getAsJsonObject().get("scope").getAsJsonObject().addProperty("advanced_mode",true);

        //判断包含列表是否为空  //如果include Scope为空需要修改为.* //不然全部删除
        //includeJsonArray 内存地址改变，需要重新获取,
        JsonArray includeJsonArray = jsonObject.get("target").getAsJsonObject().get("scope").getAsJsonObject().get("include").getAsJsonArray();
        if(includeJsonArray.size()<1){
            //设置include Scope为.*
            HashMap<String,Object> aIncludeHashMap = new HashMap();
            aIncludeHashMap.put("enabled",true);
            aIncludeHashMap.put("host",".*");
            aIncludeHashMap.put("protocol","any");
            String includeJsonString = new Gson().toJson(aIncludeHashMap);
            JsonObject includeJsonObject = JsonParser.parseString(includeJsonString).getAsJsonObject();
            includeJsonArray.add(includeJsonObject);

            //加载Json文件
            String jsonObjectString = new Gson().toJson(jsonObject);
            callbacks.loadConfigFromJson(jsonObjectString);

            //根据用户设置,保存当前内存的配置到Json配置到文件
            autoSaveProjectConfigWithFlag(callbacks);
        }
    }

    /**
     * 添加主机名到排除列表
     * @param callbacks
     * @param hostHashSet
     */
    public static void AddHostToExScopeAdvByProjectConfig(IBurpExtenderCallbacks callbacks, HashSet<String> hostHashSet) {
        //不处理没有获取到host的情况
        if(hostHashSet.size()>0){
            // 1、读取当前的配置文件
            String configContent = callbacks.saveConfigAsJson();
            JsonObject jsonObject = JsonParser.parseString(configContent).getAsJsonObject();
            //开起前置条件

            //高级模式开关 //设置高级模式
            jsonObject.get("target").getAsJsonObject().get("scope").getAsJsonObject().addProperty("advanced_mode",true);

            //生成ExcludeJson元素 并循环添加到json对象中
            JsonArray excludeJsonArray = jsonObject.get("target").getAsJsonObject().get("scope").getAsJsonObject().get("exclude").getAsJsonArray();
            for(String host:hostHashSet){
                HashMap<String,Object> aExcludeHashMap = new HashMap();
                aExcludeHashMap.put("enabled",true);
                aExcludeHashMap.put("host",host);
                aExcludeHashMap.put("protocol","any");
                String excludeJsonString =new Gson().toJson(aExcludeHashMap);
                JsonObject excludeJsonObject = JsonParser.parseString(excludeJsonString).getAsJsonObject();
                excludeJsonArray.add(excludeJsonObject);
            }

            //去重Json对象的排除列表
            JsonArray removeDuplicateJsonArray = DeDuplicateJsonObjectJsonArray(excludeJsonArray,"host");
            jsonObject.get("target").getAsJsonObject().get("scope").getAsJsonObject().add("exclude",removeDuplicateJsonArray);

            //判断包含列表是否存在和排除列表相同的数据
            JsonArray includeJsonArray = jsonObject.get("target").getAsJsonObject().get("scope").getAsJsonObject().get("include").getAsJsonArray();
            if(includeJsonArray.size()>0){
                //去除包含列表中和排除列表相同的数据
                JsonArray removeJsonObjectJsonArray = RemoveJsonObjectJsonArray(includeJsonArray,"host",hostHashSet);
                jsonObject.get("target").getAsJsonObject().get("scope").getAsJsonObject().add("include",removeJsonObjectJsonArray);
            }

            //判断包含列表是否为空  //如果include Scope为空需要修改为.* //不然全部删除
            //includeJsonArray 内存地址改变，需要重新获取,
            includeJsonArray = jsonObject.get("target").getAsJsonObject().get("scope").getAsJsonObject().get("include").getAsJsonArray();
            if(includeJsonArray.size()<1){
                //设置include Scope为.*
                HashMap<String,Object> aIncludeHashMap = new HashMap();
                aIncludeHashMap.put("enabled",true);
                aIncludeHashMap.put("host",".*");
                aIncludeHashMap.put("protocol","any");
                String includeJsonString = new Gson().toJson(aIncludeHashMap);
                JsonObject includeJsonObject = JsonParser.parseString(includeJsonString).getAsJsonObject();
                includeJsonArray.add(includeJsonObject);
            }
            //加载Json文件
            String jsonObjectString = new Gson().toJson(jsonObject);
            callbacks.loadConfigFromJson(jsonObjectString);

            //根据用户设置,保存当前内存的配置到Json配置到文件
            autoSaveProjectConfigWithFlag(callbacks);
        }
    }

    /**
     * 去除JsonArray里面指定键 并且 值包含在hastset中的元素
     * @param jsonObjectJsonArray
     * @param jsonObjectKey
     * @param hashSet
     * @return
     */
    public static JsonArray RemoveJsonObjectJsonArray(JsonArray jsonObjectJsonArray , String jsonObjectKey, HashSet<String> hashSet){
        JsonArray resultJsonArray = new JsonArray();
        List<JsonObject> list = new ArrayList<>();
        for (int i = 0; i < jsonObjectJsonArray.size(); i++){
            JsonObject jsonObject = jsonObjectJsonArray.get(i).getAsJsonObject();
            String jsonElement = jsonObject.get(jsonObjectKey).getAsString();
            if (!hashSet.contains(jsonElement)){
                list.add(jsonObject);
            }
        }
        for (JsonObject jsonObject : list){
            resultJsonArray.add(jsonObject);
        }
        return resultJsonArray;
    }

    /**
     * 去除JsonArray里面指定键 并且 值的元素
     * @param jsonObjectJsonArray
     * @param jsonObjectKey
     * @param jsonObjectValue
     * @return
     */
    public static JsonArray RemoveJsonObjectJsonArray(JsonArray jsonObjectJsonArray , String jsonObjectKey, String jsonObjectValue){
        JsonArray resultJsonArray = new JsonArray();
        List<JsonObject> list = new ArrayList<>();
        for (int i = 0; i < jsonObjectJsonArray.size(); i++){
            JsonObject jsonObject = jsonObjectJsonArray.get(i).getAsJsonObject();
            String jsonElement = jsonObject.get(jsonObjectKey).getAsString();
            if (!jsonObjectValue.equals(jsonElement)){
                list.add(jsonObject);
            }
        }
        for (JsonObject jsonObject : list){
            resultJsonArray.add(jsonObject);
        }
        return resultJsonArray;
    }

    /**
     * 去重JsonArray,输入的Array里面时Json对象
     * @param jsonObjectJsonArray
     * @param jsonObjectKey
     * @return
     */
    public static JsonArray DeDuplicateJsonObjectJsonArray(JsonArray jsonObjectJsonArray , String jsonObjectKey){
        HashSet<String> hashSet = new HashSet<>();
        JsonArray resultJsonArray = new JsonArray();
        List<JsonObject> list = new ArrayList<>();
        for (int i = 0; i < jsonObjectJsonArray.size(); i++){
            JsonObject jsonObject = jsonObjectJsonArray.get(i).getAsJsonObject();
            String jsonElement = jsonObject.get(jsonObjectKey).getAsString();
            if (!hashSet.contains(jsonElement)){
                list.add(jsonObject);
                hashSet.add(jsonElement);
            }
        }
        for ( JsonObject jsonObject : list){
            resultJsonArray.add(jsonObject);
        }
        return resultJsonArray;
    }

    /**
     * 根据变量判断是否保存当前的项目配置修改到Json文件中
     * @param callbacks
     */
    public static void autoSaveProjectConfigWithFlag(IBurpExtenderCallbacks callbacks){
        String autoSaveFlag  = GUI.tableModel.getConfigValueByKey("Auto_Save_Config_After_Update_Scope");
        if(autoSaveFlag!=null){
            autoSaveProjectConfig(callbacks);
        }
    }

    /**
     * 保存当前的项目配置Json文件中,会覆盖旧文件
     * @param callbacks
     */
    public static void autoSaveProjectConfig(IBurpExtenderCallbacks callbacks) {
        String configPath  = GUI.tableModel.getConfigValueByKey("Auto_Load_Project_Config_On_Startup");
        if(configPath!=null){
            String systemCharSet = CharSetHelper.getSystemCharSet();
            File file = new File(configPath);
            try{
                //自动根据当前的配置存储配置文件
                String configAsJson = callbacks.saveConfigAsJson();
                FileUtils.write(file,configAsJson,systemCharSet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 从Json文件中自动加载项目配置,可能会生成新文件,追加表单配置
     * @param callbacks
     */
    public static void autoLoadProjectConfig(IBurpExtenderCallbacks callbacks, boolean addDefaultExcludeHosts) {
        String configPath  = GUI.tableModel.getConfigValueByKey("Auto_Load_Project_Config_On_Startup");
        if (configPath!=null){
            //自动加载burp项目Json的配置 // Project.Config.json 支持相对(BurpSuitePro)和绝对路径
            String systemCharSet = CharSetHelper.getSystemCharSet();
            // 判断功能是否打开|功能打开后进行加载操作
            File file = new File(configPath);
            try{
                if (!file.exists() && !file.isDirectory()){
                    //配置文件不存在时,自动根据当前的配置生成
                    String configAsJson = callbacks.saveConfigAsJson();
                    FileUtils.write(file,configAsJson,systemCharSet);
                }else {
                    // 配置文件存在时,加载启动时加载项目配置文件
                    callbacks.loadConfigFromJson(FileUtils.readFileToString(file, systemCharSet));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            //是否添加用户输入的配置文件
            if(addDefaultExcludeHosts){
                addDefaultExcludeHosts(callbacks);
            }
        }
    }
}

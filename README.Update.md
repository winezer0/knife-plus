# 更新记录(本分支)
#####  knife 2.1.6

```
增加 Auto_Save_Config_After_Update_Scope 变量。
当 变量值为false时，不会将高级按键操作 
(Add Host To ExScope Adv、 
Add Host To Scope Adv、
Clear All Scope Adv ）和
启动追加操作
（add Default Exclude Hosts）
的行为 操作过后的Json配置 保存到config文件，
仅在内存中修改。

上一版本对于这些操作默认是会写入config配置文件.
```

#####  knife 2.1.5

```
在Auto_Load_Project_Config为True的情况下,
支持右键清空scope范围
支持右键添加普通排除范围
支持右键设置scope高级范围，默认添加都scope的是前缀，通过本功能可以添加HOST过滤，会自动启用高级模式

在 Add_Hosyts_Exclude_Scope为True的情况下,支持配置默认加入排除scope的域名变量.（每次插件启动时会加载）
```



knife v2.1.5版本以前修改版本下载请访问:
https://github.com/winezer0/knife/releases



#####  knife 2.1.4

```
在Auto_Load_Project_Config为True的情况下,
支持每次插件启动时进行加载项目配置文件
支持右键菜单手动保存和加载。

注意： 对于以前使用过knife的用户，使用前请先点击【restore default】，否则将无法显示菜单项

新增Auto_Load_Project_Config变量,
新增^^ Project Config Load 和 ^^ Project Config Save 右键菜单。

当 Auto_Load_Project_Config 设置为true时，每次插件启动会自动加载指定的BurpSuite项目配置文件
当 Auto_Load_Project_Config 设置为true时，点击 ^^ Project Config Load 将重新加载项目配置文件。
当 Auto_Load_Project_Config 设置为true时，点击^^ Project Config Save 将重新保存项目配置文件。

项目文件的加载保存路径也由 Auto_Load_Project_Config 变量的值指定，默认为Project.Config.json 相对路径 保存在burp主jar包路径下

项目配置文件用途：如：
固定Scope设置， scope 高级设置 白名单 .* 黑名单 .baidu. 的情况下，可以 在history通过只显示scope来过滤baidu域名流量，同时不影响baidu网站的访问。

后续可能添加功能，右键自动将域名加入scope黑名单（通过编辑和重新加载配置文件的方式）
注意：直接使用的add scope功能是添加url到scope普通模式，不支持正则功能。


```

#####  knife 2.1.3

```
新增Knife基本变量Coding_Set_From|Coding_Set_Using，
让用户能够 自定义 内容解码的原编码和目标编码 的范围.

String coding1 = "GBK,GB2312,UTF-8,GB18030,Big5,Big5-HKSCS,UNICODE,ISO-8859-1";
String codingSetFrom = burp.tableModel.getConfigValueByKey("Coding_Set_From");

String coding2 = "GBK,GB2312,UTF-8,GB18030,Big5,Big5-HKSCS,UNICODE,ISO-8859-1";
String codingSetUsing = burp.tableModel.getConfigValueByKey("Coding_Set_Using");

未启用时,可存在8*8=64中解码编码方式.

修复系统编码变量覆盖问题

```

#####  knife 2.1.2

```
进一步优化UI编码显示问题

增加基础变量Display_Coding，在其中可指定当前burp指定的显示编码，使得插件能够获取当前指定的(非默认)显示编码。

例:
java启动编码指定gbk.则burp默认编码为gbk。
当此时指定utf-8作为ui显示编码时，自动解码结果会出乱码，
可以通过在Display_Coding指定编码为utf-8,并设置为true，修复该问题，

```

#####  knife 2.1.1

```
修复2.1的unicode解码问题
修复2.1编码后的json不格式化问题
修复2.1的响应包编码和burpsuite显示编码不一致导致的乱码问题

注意,先设置burpsute显示编码为defaul

```

##### 


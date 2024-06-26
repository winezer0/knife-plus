# 更新记录(本分支)
**knife 2.1.23**
```
1、解决swing不安全加载导致burp崩溃的严重BUG
```

**knife 2.1.22**
```
1、增加响应修改的范围限定
2、尝试区别不同版本knife的配置文件名，防止不同版本的相同配置错误
3、关闭默认inscope的要求,默认对Proxy进行修改
```

**knife 2.1.21**
```
1、添加基于响应行的响应行修改功能, 并优化函数

基于URL修改响应头 测试通过 响应包可见
基于响应头修改响应头 测试通过 响应包可见
基于请求方法修改响应头 测试通过 响应包可见
添加请求头测试 测试通过，但在proxy面板看不到，得在 logger里面才能看到
删除请求头测试 测试通过，但在proxy面板看不到，得在 logger里面才能看到
```

**knife 2.1.20**
```
1、修复dnslogserver未设置时的bug
```

**knife 2.1.19**
```
1、移动代码函数到文件
2、优化 add host to In scope 和 add host to to ex scope 功能的代码，兼容adv模式的修改，
对于 Issue问题 暂时没有解决，在burp 高版本(2023.12.1 jdk17) 中，new URL() 提示报错信息, 但不影响实际使用，暂未解决。
【BUG】burp 高版本(2023.12.1) Add Host to Scope 功能报错 · Issue #77 · bit4woo/knife
bit4woo/knife#77
```

**knife 2.1.18**
```
1、移动添加的代码的路径 无实际更新
```

**knife 2.1.17**

```
1、优化 OPtions Add Header 功能 为 AddRespHeaderByReqMethod，
实现 支持为任意请求方法添加 任意格式的响应头

{"OPTIONS":"Content-Type: application/octet-stream"}
在 OPTIONS 请求方法的响应中 添加 Content-Type: application/octet-stream 头

2、添加 AddRespHeaderByReqURL 功能, 实现为 任意请求URL 添加 任意格式的响应头
{"picture":"Content-Type: application/octet-stream"}
在 URL包含picture的请求的响应中 添加 Content-Type: application/octet-stream 头
支持关键字和正则表达式,一般关键字就够用了 默认关闭该功能
```

**knife 2.1.16**

```
1、基于使用习惯更新面板默认设置。

2、汉化功能描述 并重新排序

3、优化 DismissedTargets,修改 过滤方案

修改 host前缀+url前缀+简单通配符 为 host前缀+url前缀+URL关键字+正则

注意 host前缀+url前缀 匹配的是 小写 host 和 小写 无参数 url

注意 URL关键字和 正则 匹配的是原始URL ( 含参数)

一般来讲 前缀+关键字批评 已经足够

```

**knife 2.1.15**

```
1、添加功能 为options方法自定义添加响应头 用于在history 界面过滤类型 （默认开启功能）

默认在OPTIONS方法的响应中 添加 Content-Type: application/octet-stream 头
如需要添加其他头部，请配置格式为 xxxheader: xxxxValue
```
**knife 2.1.14**

```
1、内置IPAddressUtil copy from sun.net.util.IPAddressUtil

测试记录：
burpsuite 2023.12.1 + jdk17 knife-2.1.14-jdk17.jar 正常
burpsuite 2023.12.1 + jdk17 knife-2.1.14-jdk8.jar 正常
```
**knife 2.1.13**

```
1、修改一些功能的默认开关【基于目前用户的常见习惯来设置】
设置所有必须配置的属性为false
设置配置文件常用的属性为true
```

**knife 2.1.12**

```
1、修复DismissedTargets功能逻辑bug

默认情况下 会drop掉 *.firefox.com *.mozilla.com的报文
为空、为false也拦截，最终导致无法正常访问这两个通配符域名,
该情况应该仅出现在2.1版本中，2.2版本中已经修改了diss的配置面板

由于pom.xml 中使用了 maven-compiler-plugin 插件，可能上传的几个版本的jdk版本非预期，请自行编译，或私聊需求
注：自己编译前需要手动编译 api 0.1.5版本,该版本bit4没有发布release（我没找到）
```


**knife 2.1.11**

```
1、修改 burp-api-common 依赖版本为 0.1.5 （已手动安装到本地仓库）
```

**knife 2.1.10**

```
1、修复 bp高版本 插件加载异常NullPointerException
```

**knife 2.1.9**

```
1、修改逻辑为 在点击按钮时 及 tav加载时 自动进行json格式化和unicode解码
```

**knife 2.1.8**

```
1、修复Add to Ex Scope时，In Scope为空 导致全部过滤的BUG。
```

**knife 2.1.7**

```
1、增加 Scope_Set_Base_On_Wildcard_SubDomain 变量。
开启该开关时(为true开启)，右键设置InScope Adv 或 ExScope Adv将会把Host改为子域名通配符模式
如：www.baidu.com -> .*\\.baidu\\.com

2、优化添加到Scope的HOST，.号将自动转义，更精确的匹配
如：www.baidu.com -> www\\.baidu\\.com

3、规范右键命名和文件命名
```

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


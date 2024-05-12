# knife | Burp多功能辅助插件

## 0x00 原始的项目路径 

https://github.com/bit4woo/knife   首先感恩大佬开源!!!

## 基于2.3版本的增强分支 

https://github.com/winezer0/knife-branch

## 0x01 项目简介

```
项目地址：https://github.com/bit4woo/knife

项目简介：knife是一个Burp Suite插件，主要目的是对Burp做一些小的改进，让使用更方便。就像用一把小刀对Burp进行小小的雕刻，故名“knife”。

项目作者：bit4woo

本分支主要进行功能改进
```



## 0x02 功能介绍(本分支)

```
支持响应包json格式化
优化响应包unicode解码
修复响应包编码和burpsuite显示编码不一致导致的乱码问题

在Auto_Load_Project_Config为True的情况下:
支持每次插件启动时进行加载项目配置文件
支持右键菜单手动保存项目配置文件
支持右键菜单手动加载项目配置文件

支持右键增加scope include域名 (正则)
支持右键增加scope exclude域名 (正则)
支持右键清空scope 所有范围

在 Add_Hosts_Exclude_Scope为True的情况下:
支持通过配置添加scope exclude域名 (正则)

优化 dismiss功能，支持前缀|关键字|正则表达式
支持 OPTIONS 等方法过滤（通过添加请求头）
提示 高级的 AddRespHeader XXXIII 功能，可以基于 请求方法|请求URL|响应头行来对响应头进行修改，最终实现过滤 OPTIONS等功能

```

本分支更多使用细节请查看 README.Update.md



原始项目基本使用方案请访问：https://github.com/bit4woo/knife



## 0xxx NEED STAR And ISSUE

```
1、右上角点击Star支持更新.
2、ISSUE或NOVASEC公众号联系提更新需求.
```


## 简介

安全屋定制版本资源替换脚本GUI版本

### 维护人员
- 马腾蛟

### 重要变更
| 日期 | 内容 | 人员
|--|--|--|
| 2021-11-20 | 完成资源文件自动替换；实现XML文件资源的子节点自动替换； 实现自动打包；| 马腾蛟|

### 核心概念
对于需要替换资源文件的定制项目，使用脚本实现自动化替换，提高效率同时防止手动替换出现错换、漏换的情况。
### 整体架构
| 资源替换脚本 ||
|--|--|
| GUI |  接收设置的参数  |
|文件替换  |  可直接进行替换的文件，直接执行删除与拷贝 |
|XML文件更新|  `dom4j`解析XML文件，对比需要替换的子节点，对XML文件进行更新 |
|自动打包 |   java执行CMD命令行，进行应用打包  |

## 功能介绍

1. 点击 运行 “资源替换自动打包工具.exe”
2. 点击选择待替换的资源文件目录
3. 点击选择需替换的目标项目目录
4. 按需勾选 自动打包（可同时勾选多个，或者不选）
5. 点击开始。执行结果栏，会实时显示执行的结果日志
6. 执行结束后，执行的日志文件会自动保存到 第二步选择的目录下面

   ![在这里插入图片描述](https://img-blog.csdnimg.cn/7c89c331ec5b4ce491b022dc83aca1f0.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA5biF5rCU55qE6ZOF56yU,size_18,color_FFFFFF,t_70,g_se,x_16)


### 主要开发流程

1. 待替换的资源 (文件夹:　/res/values/strings.xml)
2. 目标项目
3. 资源替换 (遍历资源，替换到目标项目中)
4. 打包 （执行 目标项目/app/.gradlew :app:assembleRelease）
5. 重置 （执行 git reset --hard）

## 依赖库

`argparse4j`: Java命令行参数解析器

`dom4j`: XML文件解析
### 测试依赖
`- java运行环境 `
`- 安装和配置应用打包环境 `
## 参考链接
项目地址： [http://192.168.3.71/mtjsoft/AutoScriptGui](http://192.168.3.71/mtjsoft/AutoScriptGui)
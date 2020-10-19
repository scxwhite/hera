
# 前言
>最近发现我总是站在我的角度来使用hera,每个功能都很清楚，但是对于使用者，他们是不清楚的，所以提供一篇hera操作文档。有问题可以在下面回复

[开源地址,请点个start,谢谢](https://github.com/scxwhite/hera)
# 操作文档

## 登录和注册
在 `hera`上登录和注册其实分为两个部分，即用户和用户组（如果使用的是`hera2.4`版本以下的没这个功能）
### 用户
用户的登录url地址为 `/login`，页面效果如图
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191015191930463.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
请注意看提示，用户名为你注册的邮箱的前缀。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191015192026639.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)

- 邮箱：任务失败（手动恢复或者自动调度触发并且达到任务重试次数）后发送邮件
- 手机号：任务失败（手动恢复或者自动调度触发并且达到任务重试次数）后打电话
- 工号：任务失败（手动恢复或者自动调度触发并且达到任务重试次数）后发送钉钉/企业微信消息
- 部门： 这里的部门，即是用户组，每一个用户一定属于一个用户组。用户注册后可以联系管理员修改组
***Tip:这里的工号在数据库中只设置了长度为5的字符，如果需要更长，请联系管理员修改***
### 用户组
用户组对应的 `url` 为 /`login/admin`，页面效果如下
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191015192824629.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
用户组也可以登录，使用你注册的用户名和密码登录即可。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191015192847295.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
- 账号： 注意，这里的用户组账号对应到 `linux` 机器上的用户。在每个任务执行的时候都会切换到该用户组来执行任务，来实现 `linux` 多租户的功能
- 邮箱：所属于该用户组的用户所创建的任务失败（手动恢复或者自动调度触发并且达到任务重试次数）后发送邮件
- 手机：暂时无用
- 账号描述：部门描述等额外信息

### 总结
用户组和用户的含义在公司里解释：
>用户组对应我们公司的各个部门，每个部门的 `leader` 持有该账户。
用户对应部门下的员工，每个员工持有该账户

用户组和用户的含义在 `hera` 里的解释：
>用户组表示一大堆任务的所有者，`hera` 里面任务的权限粒度只到用户组级别，也就是说，所属于同一个用户组的用户可以访问彼此的任务。



## 首页
目前首页放的是一系列的任务信息，简单介绍以下
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191015194314337.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
![在这里插入图片描述](https://img-blog.csdnimg.cn/2019101519442779.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
- 今日总任务数： 从今天凌晨到此刻，在 `hera` 上执行的总的任务数
- 失败任务数：从今天凌晨到此刻，在 `hear` 上执行的失败的任务数
- 实时任务状态：此刻，在 `hera` 集群实时运行的任务数量及状态统计
- 任务执行状态：最近7填任务的数量的折线图
- 任务时长`TOP10`：昨日和今日任务的执行时长对比柱状图（排序方式为：今天任务的执行时长耗时）

总结
>通过今日总任务数清楚集群执行了多少任务
>通过失败任务数来查看任务的失败，以便处理失败任务。
>通过实时任务状态来判断集群是否有任务在执行。
>通过任务的执行状态来大致查看任务最近几天的执行状态
>通过任务的时长 `TOP10` 来优化那些耗时长的任务



## 机器组监控
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191015195912940.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191030164012243.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
主要是查看机器的相关信息，没什么好说的

## 系统管理
系统管理页面只有赫拉的**超级管理员**才能查看，主要包含用户、用户组的审核及编辑，机器组的新建与删除、机器组中机器的创建与删除，下面一一介绍
### 用户管理
#### 用户组
用户组即是在 `/login/admin` 地址中注册的用户，注册后会展示在这里等待管理员审核
![在这里插入图片描述](https://img-blog.csdnimg.cn/201910152000319.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
用户组的操作有审核通过、审核拒绝以及在编辑中修改用户组的邮箱和手机号信息。
#### 用户
用户组即是在 `/login` 地址中注册的用户，注册后会展示在这里等待管理员审核
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191015200112107.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
用户操作主要是对用户组所属的用户进行审核通过、审核拒绝以及对用户的工号、邮箱、电话、所属用户组的修改

#### 总结
该页面主要是 `hera` 超级管理员的操作，默认为 `hera` 组的所有成员，用来审核用户以及修改用户信息

### 监控管理
监控管理页面的效果图如下
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191017192355322.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
监控管理页面的功能主要是：

- 添加任务监控人
- 移除任务监控人 

>解释一下什么是监控人，每一个任务都有一个或多个监控人（关注人），一旦该任务执行失败（手动恢复/自动调度）就会通过短信、电话、邮件（如果发现无告警，联系管理员自行扩展 `AlarmCenter.class`的电话和微信告警）的方式通知该任务的所有监控人

### 机器组管理&worker管理
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191017193106889.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
该页面是为 `hera` 的所有 `work` 进行分组，假设 `hera` 有4台 `work` 分别为`A,B,C,D`。其中 `A、B` 两台机器执行spark任务，`C、D` 两台机器执行默认任务。我们就可以在机器组页面创建两个机器组分别为默认组和 `spark` 组。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191024192314877.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)


然后在 `worker `管理页面分别添加 `A、B` 机器的 `ip` 到 `spark` 组，`C、D` 两台机器到默认组。
然后就可以在添加任务的时候选择所需要执行的机器组了。



<font color='red'>
注意：
1.机器组管理在`emr`集群上目前是不支持分组的，也就是说你的分组无效。因为 `emr` 是 `ssh` 到远程集群，并不是在本地执行。
2.在 `worker` 页面添加机器 `ip` 后不是实时生效的，需要等待半个小时或者手动重启 `master `。
</font>




## 任务管理
任务管理主要是对调度中心任务的一些信息检索及查看，主要包含：任务详情、任务依赖、任务搜索、日志记录等
### 任务详情
界面展示如图
![在这里插入图片描述](https://img-blog.csdnimg.cn/2019102419512526.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
该界面展示所有任务的执行的状态，包括执行次数，执行时长，执行人，执行机器等信息。可以通过右上角的状态进行勾选想要查看的状态
- 状态 选项有全部、成功、失败、运行中、等待等任务的几种状态
- 日期 默认选择今天，可供使用者自己勾选

同时，每一条记录都是支持点击查看运行记录详情，点击后的展开图如下
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191024194800115.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
该详情其实与调度中心的运行日志界面基本一致，主要是展示运行的运行记录，运行结果，运行时长等基本信息
### 任务依赖
任务依赖，顾名思义就是任务的上下游关系，该界面主要是为了查看某个任务的上下游任务状态。
任务状态由不同的颜色来表示
>绿色：执行成功
黄色：正在运行
红色：失败或未执行
灰色：关闭

具体操作是：先填入任务 `ID`，之后点击上游任务链或者下游任务链按钮，此时展示全部会变为不可点击状态，等待展示全部变为点击状态时，点击一下，任务的依赖关系图就会绘制完成

#### 上游任务
上游任务一般用来排查当前任务为什么没执行？任务在上游执行到哪里了？上游哪个任务失败了需要我手动恢复？等等
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191030171030964.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
#### 下游任务
下游任务一般看当前任务的执行进度，以及哪些任务依赖当前任务。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191030170506873.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)

tips:
- 任务依赖图也可以在调度中心直接点击，无需填写任务 `ID` 号
- 依赖图展示完毕后可以把鼠标放在某个任务上面，此时右侧的任务状态信息就会变化
- 任务依赖图中的任务支持点击查看
- `0` 号任务没有实际意义，仅仅为了标识依赖图的开始节点。即：如果你点击的是上有任务链，那么 `0` 号任务在最下方，如果你点击的是下游任务链，那么 `0` 号任务在最上方


### 任务搜索

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191024195520190.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
该界面主要根据脚本内容、任务名称、描述内容、变量内容、任务类型、任务是否开启等任务条件来模糊搜索该平台的所有任务，很简单，就不再介绍。

### 日志记录
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191024195718621.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
该界面主要展示任务的所有操作记录，点击记录右侧的查看可以看到操作的详细内容

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191024195920373.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
主要是为了方便对比或者找回曾经修改的脚本

>有一点需要注意，超级管理员可以看所有用户组中用户的操作记录。如果是普通用户只能看该组内的用户操作记录

## 开发中心

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191024200116228.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
开发中心，顾名思义，是我们日常开发任务的地方。

在任务栏最外层有两个文件夹，分别为个人文档和共享文档，区别如下：

- 个人文档： 属于同一个组的用户共同使用个人文档内创建的任务
- 共享文档： 任意组的人都能查看在共享文档内创建的任务

### 新建任务/新建文件夹

![在这里插入图片描述](https://img-blog.csdnimg.cn/2019102420103166.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
在任务栏处可以通过把鼠标放在某个文件夹上然后右键来调出任务操作。主要包含增加文件夹、新建 `Hive` 任务、新建 `Shell` 任务、新建 `Spark` 任务、重命名、删除等操作。

### 执行任务
大家可能发现脚本区的上方有几个按钮，其中执行和执行选中的代码，就是用来执行脚本内容的。
当在文件夹中新建任务后，就可以写脚本执行任务了。通过鼠标点击左侧的任务可以进行编辑任务，比如我点击左侧的 `echo.sh` 任务然后代码内容写为 `echo "hello world"` ，再然后点击工具栏的执行按钮，此时脚本下方会实时输出任务的执行结果

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191024202030984.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)

如果脚本内容分很多段，我只想执行某一部分怎么办？
此时可以通过鼠标选中你要执行的代码，然后点击上方的执行选中的代码按钮即可


![在这里插入图片描述](https://img-blog.csdnimg.cn/20191024202239582.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)

### 日志查看
在开发中心，日志查看有两种方式
1.第一种就是上面那种，通过任务执行，直接在下方查看
2.点击最下方的历史日志

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191024202414844.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191024202506563.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
然后就能看详细的历史日志了

### 上传资源
点击脚本上方工具栏的上传资源按钮会弹出上传文件的弹框，通过选择自己要上传的文件，点击上传按钮即可完成上传

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191024202724571.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)

注意
>1.上传的文件目前仅支持`'py','jar','sql','hive','sh','js','txt','png','jpg','gif'` 等扩展名，如果需要其它文件，请联系管理员修改
>2.上传成功后会返回一个路径，请copy下你上传后的文件地址，以便后面使用。目前还未有界面来维护上传的资源，在计划开发中

### 同步任务
该功能开发中，后续会自动关联调度中心，关联调度中心某个任务的配置

### 保存脚本
脚本会自动保存，如果不放心的话，就点一下吧

## 调度中心

调度中心，可谓是赫拉的核心操作的地方。在这里主要用来开发任务，控制任务的开启/关闭状态，设置任务的定时时间，任务的依赖，手动执行、手动恢复任务、查看运行日志，配置告警，查看组下任务状态等等。下面一一来介绍。


### 总览
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191030172436777.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
调度中心主要分三部分，红框标注的为任务树，绿框标注的为任务的基本信息，蓝框标注的为任务的操作按钮（任务的操作按钮和任务组的操作按钮不一致，此处截取的是任务的操作按钮）

### 任务树
大家可以通过上面截图中的红框内容发现，最上方有两个按钮，分别为：我的调度任务和全部调度任务。
- 我的调度任务：我所属的用户组所创建的所有任务
- 全部调度任务：所有用户组用户所创建的所有任务

任务树中的图标主要有三种：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191030173550856.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
1.大目录 
>大目录下只能创建大目录或者小目录

2.小目录
>小目录下面只能创建文件(任务)

3.文件
>文件的内容就是任务的脚本


<font color='red'>提示：上图中可以看到点击98号任务后右侧显示编辑和删除的按钮，这两个按钮均为临时重命名和删除，刷新后还会存在。需要知道的是，调度中心所有的操作按钮都在最右侧，即总览所附图中蓝色框标注的部分</font>


### 任务基本信息

当你在任务树中点击某个任务后，任务的基本信息就会改变
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191030174046849.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
包含的内容比较多，我会把主要部分信息介绍一下

- 任务`id` ：系统自动生成，任务的唯一标识
- 名称：新建任务时用户填写，最好见名知意，尽可能短
- 任务类型：编辑任务时可选`shell、hive、spark`三种类型
- 自动调度： 自动调度是否开启，可以通过右侧的开启/关闭按钮来操作
- 任务优先级：编辑任务时可选`low,medium,high`，当队列中任务很多时会根据该优先级进行分发任务执行
- 描述：必填项，任务信息的描述
- 调度类型：编辑任务时可选定时调度、依赖调度，定时调度时需要填写`cron` 表达式，依赖调度时需要选择所依赖的任务 `ID`
- 定时表达式：调度类型为定时调度时所填写的 `cron` 表达式
- 依赖任务：调度类型为依赖调度时所选择的依赖任务 `ID` 的列表
- 重试次数：任务自动调度/手动恢复失败时重试的次数
- 重试间隔：任务自动调度/手动恢复失败后需要等待多久进行重试
- 预计时长：整数，`0`表示无限大。当任务执行超出预计时长时会进行告警
- 报警类型：任务任务自动调度/手动恢复失败，并且重试次数已经用完进行邮件、企业微信、电话告警，告警级别依次升高。当选择电话告警时，三种告警都会触发
- 所有人：任务创建者所属的用户组
- 关注人员：任务失败后告警的人员，默认是任务的创建者。其它用户也可以在这里点击关注或者管理员在监控管理界面添加
- 管理员：该任务还允许哪些用户组的用户操作，可以通过操作按钮的配置管理员进行添加
- 重复执行：是否运行同一时间允许两个或两个以上实例执行。比如：每个小时触发的任务，任务的执行时间超出了一个小时，如果设置为否，则下个小时的任务不会执行，直到漏跑检测到才会执行
- 机器组：该任务需要在哪个机器组执行。机器组的创建请参考上面的机器组管理
- 区域：任务需要在哪个区域执行。默认是 `all` ，如果你们的 `hera` 数据库做了多区同步，那么可以选择需要执行的区域，在不是所选择的区域执行该任务时会直接设置为成功。如果需要支持，可以联系我加入赫拉开源群


### 任务配置项以及脚本内容
上面简单表述了一下任务的基本信息，下面需要说明任务的配置信息、脚本等
![在这里插入图片描述](https://img-blog.csdnimg.cn/2019103119563747.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
从上往下依次是：任务配置项、脚本内容、继承的配置项

> - 任务的配置项：是专属于该任务的配置，以 `key=value` 对的形式存在，换行分割。
>- 脚本内容：供开发者编写的脚本内容，脚本内容要根据任务的类型来写，如果是 `spark` 任务就写 `spark-sql`，如果是 `hive` 任务就写 `hive-sql`，如果是 `shell` 任务就写 `shell` 脚本
> - 继承的配置项：在 `hera` 中允许配置项的继承，凡是任务都能够使用其所有父级的目录，所以 `hera` 可以把一些公共的配置配置到目录上。如果任务的配置项和继承配置项有重复 `key` 的，那么以`最近原则`为准，取任务的配置而忽略继承的配置。

---
有一点需要说明的是，`hera` 本身支持区域代码前缀的变量，那么该变量就会在指定区域使用
我们公司共有五个区域
- AY：中国
- EU：欧洲
- US：美国
- IND：印度
- UE：美东
我的配置项里有两个配置：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191031201249164.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)

然后我选择的执行区域为美国和欧洲，配置项里只有欧洲和美国开头的`name` 变量，然后我在不同区域执行该任务时：
- 中国区域
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191031201649194.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
由于执行区域只有欧洲（`EU`）和美国（`US`），所以在中国（`AY`）执行直接设置为成功，然后通知下游任务执行

- 欧洲区域
![在这里插入图片描述](https://img-blog.csdnimg.cn/201910312018432.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
由于我们设置的配置项的  `EU.name=欧洲` 所以欧洲区域读取的变量内容为欧洲

- 美国区域
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191031202053759.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
美国区域读取的就是 `US.name=美国` 的变量内容

---
脚本内容
脚本内容其实不想说的，不过又担心大家不懂，还是举几个例子

- shell任务
其实 `shell` 任务可以执行任务任务，只要是使用 `shell` 脚本可以操作的，这里都可以操作，就举个 `shell` 实现 `spark-submit` 的任务
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191031203056883.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
- hive任务
`hive` 任务就是使用 `hive sql` 写的脚本，也举个例子

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191031203430689.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)

- spark任务
`spark` 任务就是使用 `spark sql` 写的任务。`spark` 任务和 `hive` 任务差不多，不过对于 `spark` 任务通常我们会配置 `--executor-memory --num-executors` 等参数。`hera` 默认已经配置了这些参数，当你觉得资源不够时，可以通过在配置项增加 `hera.spark.conf` 变量覆盖默认参数

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191031203904878.png)

### 操作按钮
操作按钮包含了调度中心的所有点击操作，任务操作按钮和组操作按钮有些许不同，下面一一介绍
#### 组操作按钮
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191031210133265.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
基本信息模块与任务模块差不多，不再介绍

- 任务总览
任务总览可以查看该组下的所有任务执行状态信息
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191031210342473.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
通过该功能我们可以知道那些任务执行了，那些任务没执行的原因是上游哪个任务失败了，右侧按钮可以进行再次过滤
- 正在运行
与任务总览类似，只不过只看正在运行的任务
- 失败记录
与任务总览类似，只不过只看失败的任务
- 添加组
该功能在鼠标放在大目录上时显示，能够在该组下面创建新的组

- 编辑
编辑该组配置信息和基本信息

- 添加任务
该功能在放在小目录上时显示，能够在该组下面创建新的任务

- 删除
字面意思
- 配置管理员
当其它用户组需要在该组下添加任务时，需要有该组的权限。通过配置管理员的方式可以实现
#### 任务操作按钮
- 运行日志
顾名思义，运行日志查看任务的执行日志信息，当点击该任务后会弹出一个模态框
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191031204357319.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
该模态框包含了该任务所有的历史执行记录，主要包含开始时间、结束时间、执行的机器ip、执行人、执行时长等信息。左侧的`+`号按钮可以展开查看详细的执行日志。

- 操作记录
操作记录主要记录了该任务的历史所有操作记录
![在这里插入图片描述](https://img-blog.csdnimg.cn/2019103120483137.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
主要包含操作类型、操作人、操作时间等信息，左侧的 `+` 号同样支持展开
查看详细操作
比如，我展开更新脚本内容
![在这里插入图片描述](https://img-blog.csdnimg.cn/2019103120485064.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9zY3gtd2hpdGUuYmxvZy5jc2RuLm5ldA==,size_16,color_FFFFFF,t_70)
左侧为历史的脚本内容，右侧为最新的脚本内容。如果想查找历史脚本可以从这里恢复



- 版本生成
在赫拉中任务的执行需要版本一说，所以每个任务如果要执行都需要版本。赫拉本身会在每个小时的整点附近自动生成版本，如果某人想立刻生成版本，需要点击一下版本生成按钮（目前2.4及以下版本不支持依赖任务版本生成，仅支持定时任务，如果需要的话，可以先把任务设置为定时任务，生成版本后再切换回去）。
- 依赖图
依赖图与上面介绍的一样，不再叙述
- 编辑
默认情况下 **基本信息** 、 **脚本配置项** 与**脚本**是只读的状态，只有点击编辑后才能够进行编辑这些信息
- 手动执行
执行一次该任务，执行成功不会通知下游依赖任务。手动执行的时候需要选择一个版本，如果没有版本就要等待版本生成或者手动生成
- 手动恢复
执行一次该任务，执行成功通知下游依赖任务。其它与手动执行一致
- 开启/关闭
任务是否开启自动调度，默认是关闭状态。开启状态的任务失败会进行告警。

- 失效
可以理解为关闭。
- 删除
字面含义
- 配置管理员
该功能一般在该任务需要交接给其它部门的人时使用，凡是在配置管理员内的用户组的用户都能操作该任务
- 关注该任务
当前登录用户关注该任务的失败告警通知




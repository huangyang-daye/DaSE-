![image](https://github.com/user-attachments/assets/68db2eed-ed5c-4b15-8f7f-cba217123f33)# 使用PageRank对MapReduce和Spark架构的性能对比分析

小组成员及分工：
- 黄杨（实验设计，环境搭建，执行实验 25%）
- 吕策（实验设计，记录数据，处理数据 25%）
- 张晏玮（实验设计，数据处理，汇报 25%）
- 姚之远（实验设计，数据处理，PPT和文档写作 25%）

## 一.实验目标

- 使用MapReduce和Spark执行PageRank算法的的分布式运算
- 观察两个架构在同一任务下的运算速度差异，CPU使用率差异，内存占用差异和I/O差异
- 分析这种差异出现的原因，理解两种架构的原理



## 二.实验设计

### 数据源

我们选择Google Web Graph数据集作为实验数据

### 实验内容

（1）分别在MapReduce下和Spark下运行PageRank算法，用命令监控并记录每个节点的DataNode进程的情况，并将cpu使用率，内存占用率，读写情况等数据整理出来并绘成图表，以观察情况。

（2）在源代码中获取迭代开始与结束时刻的时间戳，分别计算算法在两个框架下的迭代效率，以此来对比其性能差异。

## 三.实验流程

### 1.装配实验环境

 虚拟机：VMware® Workstation 17 Pro 17.6.1 build-24319023

操作系统：ubuntu-24.04.1-desktop-amd64（三台虚拟机）

Hadoop：hadoop 3.3.6

Spark：spark 3.5.3

Java：1.8.0_391

内存：4GB/每台

处理器：2

每个处理器内核数量：1

硬盘：40GB

### 2.启动服务

（1） 在hadoop目录下用``./sbin/start-all.sh``启动Hadoop服务和yarn服务；

（2） 使用``./bin/hdfs dfs -put ~/web_Google.txt``将数据上传到hdfs中；

（3） ``cd /export/servers/spark``切换到spark目录下，``./sbin/start-all.sh``启动spark服务；

（4） 在每个节点下使用``jps``查看节点的进程状态

<img src=".\imgs\jps_hadoop01.png" alt="jps_hadoop01" style="zoom:33%;" />

<img src=".\imgs\jps_hadoop02.png" alt="jps_hadoop02" style="zoom:33%;" />

<img src=".\imgs\jps_hadoop03.png" alt="jps_hadoop03" style="zoom:33%;" />

可以看到，这里我们把hadoop01节点设置为了主节点和master节点，将hadoop02设置成了SecondaryNameNode，hadoop01、hadoop02、hadoop03三个节点均为worker节点。

### 运行程序

1. 在hadoop路径下，执行以下命令：


```shell
hadoop jar ~/pagerankMR.jar  web_Google.txt mapreduce/14 20 875713 \
~/MapReduceOut.txt 
```

设定20轮迭代，数据共875713页

代码中在迭代开始和结束的地方加入了截取时间的功能，计算时间差并将各个时间点存下来输出到指定文件中，得到了：

<img src=".\imgs\Hadoop_iter_time.png" alt="Hadoop_iter_time" style="zoom:33%;" />

可以看到，在MapReduce中，每一轮迭代都要进行数十秒的时间，因此整个程序执行的时间长达数十分钟。

2. 在spark路径下执行以下命令：

```
./spark-submit --class PageRank --master spark://hadoop01:7077 \
--executor-memory 512MB \
~/pagerankSpark.jar 20 875713 ~/out.txt
```

设定20轮迭代，875713页，3个executor，每个executor拥有2个核，每个节点分配512MB的Memory

<img src=".\imgs\spark_job_settings_01.png" alt="image-20241215194229115" style="zoom:33%;" />

但是当我们看到下图所示的执行结果时，感觉到了不正常的地方

<img src=".\imgs\spark_iter_time.png" alt="image-20241216024819362" style="zoom:33%;" />

显然，尽管我们知道spark处理任务的速度很快，但在几百甚至几十毫秒以内完成一轮迭代显然是不现实也不正常的，在Job UI中的stages页面所观察到的也是如下所示的运行情况：

<img src=".\imgs\spark_job_result_512M.png" alt="image-20241215195119535" style="zoom:33%;" />

可以看到每个任务的运行时间(Duration)，尽管这并不是我们想要的那种每轮迭代的时间，而是mapToPair和flatmapToPair任务运行的时间，但这也能大致推测出每轮迭代的运行情况了，很显然在spark中，每轮迭代的时间被缩减到了十几秒，远远快于mapreduce的几十秒。最终程序也在三至四分钟完成了，运行时间远小于mapreduce的十五分钟左右。

3. 除此之外，我们在每次运行程序前都开启了对datanode的监控，每隔1秒取一次当前时刻的数据，绘制成了图表：

这是程序在mapreduce下运行的cpu利用率，内存利用率，和I/O，每个executor分配512M内存
<img src=".\hadoop\logs\log_hadoop_512M.png" style="zoom:33%;" />

这是程序在spark下运行的cpu利用率，内存利用率，和I/O，每个executor分配512MB内存

<img src=".\spark\logs\log_spark_512M.png" style="zoom:33%;" />

对比前两个图的kB_wr/s的数据可以看到，由于mapreduce对数据的训练任务是在磁盘上执行的，在执行任务的时候产生了大量的写的过程，这两点导致了训练时间的大大增加；

而spark训练的时候几乎全程在内存中，并且由于spark框架的DAG计算模型可以在大多数情况下减少shuffle的次数，因此当没有涉及到节点之间的数据交换的时候，数据计算的任务都会在内存当中完成，无需写到磁盘中，所以spark的训练时间远少于mapreduce。


这是程序在spark下运行的cpu利用率，内存利用率，和I/O，每个executor分配1G内存

<img src=".\spark\logs\log_spark_1G.png" style="zoom:33%;" />

这是程序在spark下运行的cpu利用率，内存利用率，和I/O，每个executor分配2G内存

<img src=".\spark\logs\log_spark_2G.png" style="zoom:33%;" />

通过对比分析spark在分配不同内存情况下的训练数据我们发现，内存大小的改变并没有对训练的时间产生非常大的变化，这可能是由于数据量不够大，也有可能实际的情况确实如此。也有可能在当前实验条件下，Spark的内存管理机制使得其在一定范围内对内存变化不敏感。


## 五.实验反思
本次实验分析了PageRank算法在Hadoop和Spark架构下的性能差异，经过本次实验，我们对Hadoop，Map Reduce和Spark架构有了进一步的认识和理解。

但同时，由于实验环境不完善（如内核数不够、数据量太小等）、实验设置不全面、对架构理解的不够深入等多方面的原因，实验并没有完全得到我们想要的结果，而且实验出现了一些我们没有办法完全理解，没有办法解释明白的现象，因此我们认为，我们的实验并不是完全成功的。可能还需要更进一步的深入了解才能够完全得到我们想要的结果，这点还有待改进和完善。

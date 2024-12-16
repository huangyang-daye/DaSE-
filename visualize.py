import pandas as pd
import matplotlib.pyplot as plt

plt.subplots(4,4,figsize=(20,10))
df2 = pd.read_csv(f"spark\\log2\\spark_1G_master_01_log.csv")

cpu_data_m = df2["%CPU"]
mem_data_m = df2["%MEM"]
rd_data_m = df2["kB_rd/s"]
wr_data_m = df2["kB_wr/s"]
x = range(len(cpu_data_m))
plt.subplot(4,4,1)
plt.plot(x, cpu_data_m)
plt.xlabel("Index")
plt.ylabel("%CPU")
plt.title(f"master: %CPU over time")
plt.subplot(4,4,2)
plt.plot(x, cpu_data_m)
plt.xlabel("Index")
plt.ylabel("%MEM")
plt.title(f"master: %MEM over time")
plt.subplot(4,4,3)
plt.plot(x, cpu_data_m)
plt.xlabel("Index")
plt.ylabel("kB_rd/s")
plt.title(f"master: kB_rd/s over time")
plt.subplot(4,4,4)
plt.plot(x, cpu_data_m)
plt.xlabel("Index")
plt.ylabel("kB_wr/s")
plt.title(f"master: kB_wr/s over time")

for i in range (1,4):
    print(i)
    # df = pd.read_csv(f"hadoop\\logs\\hadoop_512M_0{i+1}_log.csv")
    # df = pd.read_csv(f"spark\\logs\\new_0{i+1}_log.csv")
    df = pd.read_csv(f"spark\\log2\\spark_1G_0{i}_log.csv")
    # df = pd.read_csv(f"spark\\logs\\spark_512M_0{i+1}_log.csv")
    #usr_data = df["%usr"]
    cpu_data = df["%CPU"]
    mem_data = df["%MEM"]
    rd_data = df["kB_rd/s"]
    wr_data = df["kB_wr/s"]
    x = range(len(cpu_data))

    # plt.subplot(3,1,1+i)
    # plt.plot(x, Min_data)
    # plt.xlabel("Index")
    # plt.ylabel("minflt/s")
    # plt.title(f"node: {i+1} minflt/s over time")
    
    # 绘制折线图


    plt.subplot(4,4,1+4*(i))
    plt.plot(x, cpu_data)
    plt.xlabel("Index")
    plt.ylabel("%CPU")
    plt.title(f"node {i+1}: %CPU over time")
    

    plt.subplot(4,4,2+4*(i))
    plt.tight_layout()
    plt.plot(x, mem_data)
    plt.xlabel("Index")
    plt.ylabel("%MEM")
    plt.title(f"node {i+1}: %MEM over time")
    
    plt.subplot(4,4,3+4*(i))
    plt.tight_layout()
    plt.plot(x, rd_data)
    plt.xlabel("Index")
    plt.ylabel("kB_rd/s")
    plt.title(f"node {i+1}: kB_rd/s over time")

    plt.subplot(4,4,4+4*(i))
    plt.tight_layout()
    plt.plot(x, wr_data)
    plt.xlabel("Index")
    plt.ylabel("kB_wr/s")
    plt.title(f"node {i+1}: kB_wr/s over time")
    
#plt.savefig("spark\\logs\\log_spark_1G.png")
#plt.savefig("spark\\logs\\log_spark.png")
plt.savefig("spark\\log2\\log_spark_1G.png")

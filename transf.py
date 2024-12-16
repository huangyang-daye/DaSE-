for i in range (3):
    
    # with open(f"spark\\logs\\spark_512M_0{i+1}_log.txt", "r") as file:
    # with open(f"spark\\logs\\spark_1G_0{i+1}_log.txt", "r") as file:
    with open(f"spark\\log2\\spark_1G_0{i+1}_log.txt", "r") as file:
    # with open(f"hadoop\\logs\\hadoop_512M_0{i+1}_log.txt", "r") as file:
        lines = file.readlines()
    header = lines[0].strip()
    new_lines = [line.strip() for line in lines[1:] if not line.startswith("#")]
    import csv
    with open(f"spark\\log2\\spark_1G_0{i+1}_log.csv", "w", newline="") as csvfile:
        writer = csv.writer(csvfile)
        writer.writerow(header.split())
        for line in new_lines:
            writer.writerow(line.split())
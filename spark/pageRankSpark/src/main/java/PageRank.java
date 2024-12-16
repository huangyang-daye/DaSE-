
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.*;
import scala.Tuple2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class PageRank {
    public static void run(String[] args){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        SparkConf conf = new SparkConf();
        conf.setAppName("PageRank");
        JavaSparkContext sc = new JavaSparkContext(conf);

        int iterateNum = Integer.parseInt(args[0]); // 指定迭代次数
        //int iterateNum = 20;
        double factor = 0.85;
        long[] arrTimes = new long[iterateNum];
        String[] startTimes = new String[iterateNum];
        String[] endTimes = new String[iterateNum];
        //JavaRDD<String> text = sc.textFile(args[0]);
        JavaRDD<String> text = sc.textFile("web_Google.txt");
        JavaPairRDD<String, List<String>> links =
                text.mapToPair(
                                new PairFunction<String, String, List<String>>() {
                                    @Override
                                    public Tuple2<String, List<String>> call(String line) throws Exception {
                                        String[] tokens = line.split(" ");
                                        List<String> list = new ArrayList<>();
                                        for (int i = 2; i < tokens.length; i+=2) {
                                            list.add(tokens[i]);
                                        }
                                        return new Tuple2<>(tokens[0], list);
                                    }
                                })
                        .cache(); // 持久化到内存

        long N = Long.parseLong(args[1]); // 从输入中获取网页总数N

        // 初始化每个页面的排名值，得到[网页, 排名值]键值对
        JavaPairRDD<String, Double> ranks =
                text.mapToPair(
                        new PairFunction<String, String, Double>() {
                            @Override
                            public Tuple2<String, Double> call(String line) throws Exception {
                                String[] tokens = line.split(" ");
                                return new Tuple2<>(tokens[0], Double.valueOf(tokens[1]));
                            }
                        });

        // 执行iterateNum次迭代计算
        for (int iter = 0; iter < iterateNum; iter++) {
            /*获取本轮迭代的开始时间并保存*/
            Date start = new Date();
            long starttime = start.getTime();
            String startTimeString = sdf.format(start);
            startTimes[iter] = startTimeString;

            JavaPairRDD<String, Double> contributions =
                    links
                            // 将links和ranks做join，得到[网页, {{链接列表}, 排名值}]
                            .join(ranks)
                            // 计算出每个网页对其每个链接网页的贡献值
                            .flatMapToPair(
                                    new PairFlatMapFunction<
                                            Tuple2<String, Tuple2<List<String>, Double>>, String, Double>() {
                                        @Override
                                        public Iterator<Tuple2<String, Double>> call(
                                                Tuple2<String, Tuple2<List<String>, Double>> t) throws Exception {
                                            List<Tuple2<String, Double>> list = new ArrayList<>();
                                            for (int i = 0; i < t._2._1.size(); i++) {
                                                // 网页排名值除以链接总数
                                                list.add(new Tuple2<>(t._2._1.get(i), t._2._2 / t._2._1.size()));
                                            }
                                            return list.iterator();
                                        }
                                    });

            ranks =
                    contributions
                            // 聚合对相同网页的贡献值，求和得到对每个网页的总贡献值
                            .reduceByKey(
                                    new Function2<Double, Double, Double>() {
                                        @Override
                                        public Double call(Double r1, Double r2) throws Exception {
                                            return r1 + r2;
                                        }
                                    })
                            // 根据公式计算得到每个网页的新排名值
                            .mapValues(
                                    new Function<Double, Double>() {
                                        @Override
                                        public Double call(Double v) throws Exception {
                                            return (1 - factor) * 1.0 / N + factor * v;
                                        }
                                    });

            Date end = new Date();
            long endtime = end.getTime();
            String endTimeString = sdf.format(end);
            endTimes[iter] = endTimeString;
            long diff = (endtime - starttime);
            arrTimes[iter] = diff;
        }
        // 对排名值保留5位小数，并打印最终网页排名结果
        ranks.foreach(new VoidFunction<Tuple2<String, Double>>() {
            @Override
            public void call(Tuple2<String, Double> t) throws Exception {
                System.out.println(t._1 + " " + String.format("%.5f", t._2));
            }
        });
        File file = new File(args[2]);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {

            FileWriter fw = new FileWriter(args[2]);

            for(int i=0;i<iterateNum;i++){

                fw.write(startTimes[i]+"\t"+endTimes[i]+"\t"+arrTimes[i]+"\n");

            }
            fw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        /* 步骤3：关闭SparkContext */
        sc.stop();
    }
    public static void main(String[] args) {
        run(args);

    }
}

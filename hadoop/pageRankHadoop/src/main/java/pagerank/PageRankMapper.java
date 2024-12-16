package pagerank;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class PageRankMapper extends Mapper<LongWritable, Text, Text, ReducePageRankWritable> {
    @Override
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {
        String[] pageInfo = value.toString().split(" ");
        double pageRank = Double.parseDouble(pageInfo[1]);
        int outLink  = (pageInfo.length - 2) / 2;
        ReducePageRankWritable writable;
        writable = new ReducePageRankWritable();
        writable.setData(String.valueOf(pageRank / outLink));
        writable.setTag(ReducePageRankWritable.PR_L);
        for(int i=2;i<pageInfo.length;i+=2){
            context.write(new Text(pageInfo[i]),writable);
        }
        writable = new ReducePageRankWritable();
        writable.setTag(ReducePageRankWritable.PAGE_INFO);
        context.write(new Text(pageInfo[0]),writable);
    }
}

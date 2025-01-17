package pagerank;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.Writable;

public class ReducePageRankWritable implements Writable {

    /// 保存贡献值或网页信息
    private String data;
    // 标识data保存的是贡献值还是网页信息
    private String tag;

    // 用于标识的常量
    public static final String PAGE_INFO = "1";
    public static final String PR_L = "2";

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeUTF(tag);
        dataOutput.writeUTF(data);
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        tag = dataInput.readUTF();
        data = dataInput.readUTF();
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}

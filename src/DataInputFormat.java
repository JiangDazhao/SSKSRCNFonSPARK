import java.io.IOException;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

//Hadoop没有高光谱数据读取类，新建HSIInputFormat
public class DataInputFormat extends FileInputFormat<Integer, byte[]> {
    public DataInputFormat() {
    }
    // 读取整个block数据，读出来即分片好。
// key分片数据在原始数据的offset，值pixel为高光谱分片数据，即block原始数据byte
    public RecordReader<Integer, byte[]> createRecordReader(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
        return new DataRecordReader();
    }
}
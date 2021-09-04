import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

//RecordReader作用就是把数据切分成key/value的形式 然后作为输入传给Mapper
public class DataRecordReader extends RecordReader<Integer, byte[]> {
    //逻辑概念，block不连续，标记下start，length，filepath
    private FileSplit split;
    private Configuration conf;
    private long start;
    private int length;
    private boolean flag;
    private int key;
    //fileIn屏蔽了顶层的不同block之前的切换
    FSDataInputStream fileIn;
    private byte[] data = null;

    public DataRecordReader() {
    }

    //initialize和nextKeyValue突破一行数据处于不同的split
    /*
    * 将InputSplit转换成FileSplit
    获取每一行能读取的最大长度默认是Integer.MAX_VALUE
    获取当前FileSplit的开始位置
    获取当前FileSplit的结束位置
    获取当前FileSplit的文件路径*/
    //isplit是分块的句柄
    public void initialize(InputSplit isplit, TaskAttemptContext context) throws IOException, InterruptedException {
        this.split = (FileSplit)isplit;
        this.flag = true;
        this.conf = context.getConfiguration();
        this.start = this.split.getStart();
        this.length = (int)this.split.getLength();
        FileSystem fs = this.split.getPath().getFileSystem(this.conf);
        this.key = (int)this.start;
        this.fileIn = fs.open(this.split.getPath());
        this.fileIn.seek(this.start);
    }

    //判断是否有下一个key/value(block)
    public boolean nextKeyValue() throws IOException, InterruptedException {
        if (this.flag) {
            this.data = new byte[this.length];
            this.fileIn.read(this.data);
            this.flag = false;
            return true;
        } else {
            return false;
        }
    }

    public Integer getCurrentKey() throws IOException, InterruptedException {
        return this.key;
    }

    public byte[] getCurrentValue() throws IOException, InterruptedException {
        return this.data;
    }

    public float getProgress() throws IOException, InterruptedException {
        return 0.0F;
    }

    public void close() throws IOException {
    }
}

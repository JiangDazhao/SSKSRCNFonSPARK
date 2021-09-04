import java.io.*;
import java.nio.file.Path;

public class HSIhdr {
    //从hdr头文件中读出来的参数
    private String name;
    private String path;
    private int row;
    private int col;
    private int bands;
    private short datatype;
    private String inter;

    //默认构造函数,标识文件名和路径,以及对头文件参数进行读取
    public HSIhdr(String filename, String path) throws IOException {
        //文件分割,分出文件名
        this.name = filename + "_hsihdr.hdr";//头文件.hdr组装

        this.path = path;// 文件所在目录路径

        this.ReadInformation(); //重写读取函数
    }

    public void ReadInformation() throws IOException {
//        /*客户端读取HDFS数据*/
//        //返回实例文件系统HDFS的fs
//        Configuration conf = new Configuration();
//        FileSystem fs = FileSystem.get(URI.create(this.path + this.name), conf);
//        //有了fs实例后，调用open来获取文件输入流din
//        DataInputStream din = fs.open(new Path(this.path + this.name));
//        //客户端对输入流调用read方法
//        BufferedReader read = new BufferedReader(new InputStreamReader(din));

        File src = new File(this.path + this.name);
        DataInputStream din =new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(src)
                )
        );

        BufferedReader read = new BufferedReader(new InputStreamReader(din));

        String strline;
        try {
            while((strline = read.readLine()) != null) {
                int pos = strline.indexOf(61);
                if (pos >= 0) {
                    //属性
                    String strleft = strline.substring(0, pos).trim();
                    //值
                    String strright = strline.substring(pos + 1, strline.length()).trim();
                    if (strleft.equals("samples")) {
                        this.col = Integer.parseInt(strright);
                    } else if (strleft.equals("lines")) {
                        this.row = Integer.parseInt(strright);
                    } else if (strleft.equals("bands")) {
                        this.bands = Integer.parseInt(strright);
                    } else if (strleft.equals("data type")) {
                        this.datatype = Short.parseShort(strright);
                    } else if (strleft.equals("interleave")) {
                        this.inter = strright;
                    }
                }
            }
        } catch (FileNotFoundException var9) {
            System.out.println("failed to read");
            var9.printStackTrace();
        }

    }

    public int getRow() {
        return this.row;
    }

    public int getCol() {
        return this.col;
    }

    public int getBands() {
        return this.bands;
    }

    public short getDatatype() {
        return this.datatype;
    }

    public String getInter() {
        return this.inter;
    }

    @Override
    public String toString() {
        return "HSIhdr{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", row=" + row +
                ", col=" + col +
                ", bands=" + bands +
                ", datatype=" + datatype +
                ", inter='" + inter + '\'' +
                '}';
    }

}

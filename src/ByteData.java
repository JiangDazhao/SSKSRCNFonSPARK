import java.io.IOException;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;

import com.csvreader.CsvWriter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class ByteData {
    private int bands;
    private int rows;
    private int cols;
    private short datatype;
    private String mainfilename;
    private String filepath;
    private short[][] rawimg2D;
    private double[][] img2D;
    private short[][] img_gt;
    private short []trainidx2D;
    private short []testidx2D;
    private int []trainlab;
    private int []testlab;
    private int [] totallab;

    public ByteData(String mainfilename, String filepath,int bands,int rows,int cols,short datatype) throws IOException {

        this.mainfilename = mainfilename;
        this.filepath = filepath;
        this.bands=bands;
        this.rows=rows;
        this.cols=cols;
        this.datatype=datatype;
        this.readData();
    }

    public void readData() throws IOException {
        String imgadd=filepath+mainfilename+"_img";
        String gtadd=filepath+mainfilename+"_gt";
        String trainidxadd=filepath+mainfilename+"_train";
        String testidxadd=filepath+mainfilename+"_test";

        //get rawimg2D
        byte[] data = null;
        int size;
        int len;
        int pixel;
        int n;
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(URI.create(imgadd), conf);
        DataInputStream is = fs.open(new Path(imgadd));

        size= is.available();
        data = new byte[size];
        for(int i=0;i<size;i++){
            data[i]= (byte) is.read();
        }
        len=data.length;
        pixel=len/(datatype*bands);
        rawimg2D=new short[bands][pixel];
        n=0;
        for(int i=0;i<pixel;i++){
            for(int j=0;j<bands;j++) {
                //两位byte转short
                rawimg2D[j][i]=(short)((data[n++]&0xff) | (data[n++] <<8));
            }
        }

        //get img2D
        //line_dat
        double[] sortX = new double[rows*cols*bands];
        for(int i=0;i<rawimg2D.length;i++)
            for(int j=0;j<rawimg2D[0].length;j++)
            {
                sortX[i*rawimg2D[0].length+j]=rawimg2D[i][j];
            }
        Arrays.sort(sortX);
        int sortXL=sortX.length;
        double rdown = 0.001;
        double rup = 0.999;
        double lmin= sortX[Math.max((int)Math.ceil(sortXL*rdown),1)];
        //System.out.println(lmin);
        double lmax= sortX[Math.min((int)Math.floor(sortXL*rup),sortXL)];
        //System.out.println(lmax);

        this.img2D = new double[bands][rows*cols];
        for(int i=0;i<rawimg2D.length;i++)
            for(int j=0;j<rawimg2D[0].length;j++)
            {
                if(rawimg2D[i][j]<lmin) img2D[i][j]=lmin;
                else if(rawimg2D[i][j]>lmax) img2D[i][j]=lmax;
                img2D[i][j]=(rawimg2D[i][j]-lmin)/lmax;
            }

        // get gt
         fs = FileSystem.get(URI.create(gtadd), conf);
         is = fs.open(new Path(gtadd));
        size= is.available();
        data = new byte[size];
        for(int i=0;i<size;i++){
            data[i]= (byte) is.read();
        }
        img_gt=new short[rows][cols];
        n=0;
        for(int i=0;i<cols;i++){
            for(int j=0;j<rows;j++) {
                //两位byte转short
                img_gt[j][i]=(short)((data[n++]&0xff) | (data[n++] <<8));
            }
        }

        //get trainidx2D
        int trainidxlen;
        fs = FileSystem.get(URI.create(trainidxadd), conf);
        is = fs.open(new Path(trainidxadd));
        size= is.available();
        data = new byte[size];
        for(int i=0;i<size;i++){
            data[i]= (byte) is.read();
        }
        len=data.length;
        trainidxlen=len/datatype;
        trainidx2D=new short[trainidxlen];
        n=0;
        for (int i=0;i<trainidxlen;i++)
            trainidx2D[i]=(short)((data[n++]&0xff) | (data[n++] <<8));

        //get testidx2D
        int testidxlen;
        fs = FileSystem.get(URI.create(testidxadd), conf);
        is = fs.open(new Path(testidxadd));
        size= is.available();
        data = new byte[size];
        for(int i=0;i<size;i++){
            data[i]= (byte) is.read();
        }
        len=data.length;
        testidxlen=len/datatype;
        testidx2D=new short[testidxlen];
        n=0;
        for (int i=0;i<testidxlen;i++)
            testidx2D[i]=(short)((data[n++]&0xff) | (data[n++] <<8));

        //trainlab
        this.trainlab=new int[trainidx2D.length];
        for(int i=0;i<trainlab.length;i++){
            int gdrow= trainidx2D[i]%rows;
            int gdcol=trainidx2D[i]/cols;
            trainlab[i]=img_gt[gdrow][gdcol];
        }

        //testlab
        this.testlab=new int[testidx2D.length];
        for(int i=0;i<testlab.length;i++){
            int gdrow= testidx2D[i]%rows;
            int gdcol=testidx2D[i]/cols;
            testlab[i]=img_gt[gdrow][gdcol];
        }

        //totallab
        this.totallab=new int[trainidx2D.length+testidx2D.length];
        for(int i=0;i<trainlab.length;i++) totallab[i]=trainlab[i];
        for(int i=0;i<testlab.length;i++) totallab[trainidxlen+i]=testlab[i];


//        //readout test
//        CsvWriter csvWriter = new CsvWriter("./out/rawimg2D.csv", ',', Charset.forName("UTF-8"));
//        for(int i=0;i<bands;i++){
//            String[] onerow=new String[pixel];
//            for(int j=0;j<pixel;j++){
//                onerow[j]=String.valueOf(rawimg2D[i][j]);
//            }
//            csvWriter.writeRecord(onerow);
//        }
//        csvWriter.close();
//
//        csvWriter = new CsvWriter("./out/img2D.csv", ',', Charset.forName("UTF-8"));
//        for(int i=0;i<bands;i++){
//            String[] onerow=new String[pixel];
//            for(int j=0;j<pixel;j++){
//                onerow[j]=String.valueOf(img2D[i][j]);
//            }
//            csvWriter.writeRecord(onerow);
//        }
//        csvWriter.close();
//
//        csvWriter = new CsvWriter("./out/gt.csv", ',', Charset.forName("UTF-8"));
//        for(int i=0;i<rows;i++){
//            String[] onerow=new String[cols];
//            for(int j=0;j<cols;j++){
//                onerow[j]=String.valueOf(img_gt[i][j]);
//            }
//            csvWriter.writeRecord(onerow);
//        }
//        csvWriter.close();
//
//        csvWriter = new CsvWriter("./out/trainidx2D.csv", ',', Charset.forName("UTF-8"));
//        String[] onerow=new String[trainidxlen];
//        for(int i=0;i<trainidxlen;i++){
//            onerow[i]=String.valueOf(trainidx2D[i]);
//        }
//        csvWriter.writeRecord(onerow);
//        csvWriter.close();
//
//        csvWriter = new CsvWriter("./out/testidx2D.csv", ',', Charset.forName("UTF-8"));
//        onerow=new String[testidxlen];
//        for(int i=0;i<testidxlen;i++){
//            onerow[i]=String.valueOf(testidx2D[i]);
//        }
//        csvWriter.writeRecord(onerow);
//        csvWriter.close();
//
//        csvWriter = new CsvWriter("./out/trainlab.csv", ',', Charset.forName("UTF-8"));
//        onerow=new String[trainidx2D.length];
//        for(int i=0;i<trainidx2D.length;i++){
//            onerow[i]=String.valueOf(trainlab[i]);
//        }
//        csvWriter.writeRecord(onerow);
//        csvWriter.close();
//
//        csvWriter = new CsvWriter("./out/testlab.csv", ',', Charset.forName("UTF-8"));
//        onerow=new String[testidx2D.length];
//        for(int i=0;i<testidx2D.length;i++){
//            onerow[i]=String.valueOf(testlab[i]);
//        }
//        csvWriter.writeRecord(onerow);
//        csvWriter.close();
//
//        csvWriter = new CsvWriter("./out/totallab.csv", ',', Charset.forName("UTF-8"));
//        onerow=new String[totallab.length];
//        for(int i=0;i<totallab.length;i++){
//            onerow[i]=String.valueOf(totallab[i]);
//        }
//        csvWriter.writeRecord(onerow);
//        csvWriter.close();

    }

    public short[][] getRawimg2D() {
        return rawimg2D;
    }

    public double[][] getImg2D() {
        return img2D;
    }

    public short[][] getImg_gt() {
        return img_gt;
    }

    public short[] getTrainidx2D() {
        return trainidx2D;
    }

    public short[] getTestidx2D() {
        return testidx2D;
    }

    public int[] getTrainlab() {
        return trainlab;
    }

    public int[] getTestlab() {
        return testlab;
    }

    public int[] getTotallab() {
        return totallab;
    }


}

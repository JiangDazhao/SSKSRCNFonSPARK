import Jama.Matrix;
import com.csvreader.CsvWriter;
import scala.Tuple2;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {
    public static void main(String[] args) throws IOException {
        int wind;
        double mu;
        double lam;
        double gam_w;
        double gam_K;

        wind=5;
        double e =Math.E;
        mu=Math.pow(e,-3);
        lam=Math.pow(e,-4);
        //gam_K=14.904789935208639;//sig
        gam_K=40.515419637876920;
        //gam_w=0.742065795512883;//sig0s
        gam_w=2.017143967463676;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        System.out.println("start time:"+df.format(new Date()));

        double t5=System.currentTimeMillis();
        KSRCNF.KSRCNFinit("Indian_pines_corrected.mat",
                "trainidx_540_61.mat",
                "testidx_450_61.mat",
                "Indian_gt.mat",
                wind,mu,lam,gam_w,gam_K);
        double t6=System.currentTimeMillis();
        System.out.println("readdata time:"+(t6-t5)*1.0/1000+"s");
        System.out.println(df.format(new Date()));

        int []trainidx2D=KSRCNF.trainidx2D;
        int []testidx2D=KSRCNF.testidx2D;
        int []trainlab=KSRCNF.trainlab;
        int []testlab=KSRCNF.testlab;
       // for(int i=0;i<trainlab.length;i++) System.out.println(trainlab[i]);
        int rows =KSRCNF.rows;
        int cols =KSRCNF.cols;
        double[][]img2D=KSRCNF.img2D;

        double t1=System.currentTimeMillis();
        Tuple2<double[][],double[][]> ATA_ATX= KSRCNF.ker_lwm();
        double t2=System.currentTimeMillis();
        System.out.println("ker_lwm time:"+(t2-t1)*1.0/1000+"s");
        System.out.println(df.format(new Date()));

        double [][] Ktrain=ATA_ATX._1;
        double [][] Ktest=ATA_ATX._2;

        int[] trainidx2Da=KSRCNF.trainidx2D;
          int[][] trainposa=KSRCNF.trainpos;
          int[] trainijw_sizea=KSRCNF.trainijw_size;
          double[][]trainijw2D_weighta=KSRCNF.trainijw2D_weight;
          int[][]train_ijw2Da=KSRCNF.train_ijw2D;

        double t3=System.currentTimeMillis();
        Matrix S = Tools.ADMM(ATA_ATX._1,ATA_ATX._2,mu,lam);
        double t4=System.currentTimeMillis();
        System.out.println("ADMM time:"+(t4-t3)*1.0/1000+"s");
        System.out.println(df.format(new Date()));


        double t7=System.currentTimeMillis();
        int [] pred;
        pred= Tools.classker_pred(ATA_ATX._1,ATA_ATX._2,S.getArrayCopy(),testlab,trainlab);
        double t8=System.currentTimeMillis();
        System.out.println("classker_pred time:"+(t8-t7)*1.0/1000+"s");
        System.out.println(df.format(new Date()));

        double OA;
        OA= Tools.classeval(pred,testlab);
        System.out.println("Overall Accuracy:"+String.format("%.2f",OA)+"%");

        System.out.println("end time:"+df.format(new Date()));

        CsvWriter csvWriter = new CsvWriter("./out/Ktrain.csv", ',', Charset.forName("UTF-8"));
        int imgrow=Ktrain.length;
        int imgcol=Ktrain[0].length;
        for(int i=0;i<imgrow;i++){
            String[] onerow=new String[imgcol];
            for(int j=0;j<imgcol;j++){
                onerow[j]=String.valueOf(Ktrain[i][j]);
            }
            csvWriter.writeRecord(onerow);
        }
        csvWriter.close();

        csvWriter = new CsvWriter("./out/Ktest.csv", ',', Charset.forName("UTF-8"));
        imgrow=Ktest.length;
        imgcol=Ktest[0].length;
        for(int i=0;i<imgrow;i++){
            String[] onerow=new String[imgcol];
            for(int j=0;j<imgcol;j++){
                onerow[j]=String.valueOf(Ktest[i][j]);
            }
            csvWriter.writeRecord(onerow);
        }
        csvWriter.close();
//
        csvWriter = new CsvWriter("./out/trainijw2D_weighta.csv", ',', Charset.forName("UTF-8"));
        imgrow=trainijw2D_weighta.length;
        imgcol=trainijw2D_weighta[0].length;
        for(int i=0;i<imgrow;i++){
            String[] onerow=new String[imgcol];
            for(int j=0;j<imgcol;j++){
                onerow[j]=String.valueOf(trainijw2D_weighta[i][j]);
            }
            csvWriter.writeRecord(onerow);
        }
        csvWriter.close();

        csvWriter = new CsvWriter("./out/train_ijw2Da.csv", ',', Charset.forName("UTF-8"));
        imgrow=train_ijw2Da.length;
        imgcol=train_ijw2Da[0].length;
        for(int i=0;i<imgrow;i++){
            String[] onerow=new String[imgcol];
            for(int j=0;j<imgcol;j++){
                onerow[j]=String.valueOf(train_ijw2Da[i][j]);
            }
            csvWriter.writeRecord(onerow);
        }
        csvWriter.close();

        csvWriter = new CsvWriter("./out/S.csv", ',', Charset.forName("UTF-8"));
        double[][]Sarray= S.getArrayCopy();
        imgrow=Sarray.length;
        imgcol=Sarray[0].length;
        for(int i=0;i<imgrow;i++){
            String[] onerow=new String[imgcol];
            for(int j=0;j<imgcol;j++){
                onerow[j]=String.valueOf(Sarray[i][j]);
            }
            csvWriter.writeRecord(onerow);
        }
        csvWriter.close();
    }
}

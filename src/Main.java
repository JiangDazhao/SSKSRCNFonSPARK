import Jama.Matrix;
import com.csvreader.CsvWriter;
import org.ujmp.jmatio.ImportMatrixMAT;
import scala.Tuple2;

import java.io.File;
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
        mu=1e-3;
        lam=1e-4;

        gam_K=0.272990750165721;//sig

        gam_w=2.489353418393197e-04;//sig0s
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        System.out.println("start time:"+df.format(new Date()));

        double t5=System.currentTimeMillis();
        KSRCNF.KSRCNFinit("Indian_pines_corrected.mat",
                "trainidx_540_61.mat",
                "testidx_450_61.mat",
                "Indian_gt.mat",
                "totalsample_990_61.mat",
                wind,mu,lam,gam_w,gam_K);
        double t6=System.currentTimeMillis();
        System.out.println("readdata time:"+(t6-t5)*1.0/1000+"s");
        System.out.println(df.format(new Date()));


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


        double t3=System.currentTimeMillis();
        Matrix S = Tools.ADMM(Ktrain,Ktest,mu,lam);
        double t4=System.currentTimeMillis();
        System.out.println("ADMM time:"+(t4-t3)*1.0/1000+"s");
        System.out.println(df.format(new Date()));


        double t7=System.currentTimeMillis();
        int [] pred;
        pred= Tools.classker_pred(Ktrain,Ktest,S.getArrayCopy(),testlab,trainlab);
        double t8=System.currentTimeMillis();
        System.out.println("classker_pred time:"+(t8-t7)*1.0/1000+"s");
        System.out.println(df.format(new Date()));

        double OA;
        OA= Tools.classeval(pred,testlab);
        System.out.println("Overall Accuracy:"+String.format("%.2f",OA)+"%");

        System.out.println("end time:"+df.format(new Date()));

//        CsvWriter csvWriter = new CsvWriter("./out/Ktrain.csv", ',', Charset.forName("UTF-8"));
//        int imgrow=Ktrain.length;
//        int imgcol=Ktrain[0].length;
//        for(int i=0;i<imgrow;i++){
//            String[] onerow=new String[imgcol];
//            for(int j=0;j<imgcol;j++){
//                onerow[j]=String.valueOf(Ktrain[i][j]);
//            }
//            csvWriter.writeRecord(onerow);
//        }
//        csvWriter.close();
//
//        csvWriter = new CsvWriter("./out/Ktest.csv", ',', Charset.forName("UTF-8"));
//        imgrow=Ktest.length;
//        imgcol=Ktest[0].length;
//        for(int i=0;i<imgrow;i++){
//            String[] onerow=new String[imgcol];
//            for(int j=0;j<imgcol;j++){
//                onerow[j]=String.valueOf(Ktest[i][j]);
//            }
//            csvWriter.writeRecord(onerow);
//        }
//        csvWriter.close();
//
//
//        csvWriter = new CsvWriter("./out/S.csv", ',', Charset.forName("UTF-8"));
//        double[][]Sarray= S.getArrayCopy();
//        imgrow=Sarray.length;
//        imgcol=Sarray[0].length;
//        for(int i=0;i<imgrow;i++){
//            String[] onerow=new String[imgcol];
//            for(int j=0;j<imgcol;j++){
//                onerow[j]=String.valueOf(Sarray[i][j]);
//            }
//            csvWriter.writeRecord(onerow);
//        }
//        csvWriter.close();
//
//        csvWriter = new CsvWriter("./out/predtest.csv", ',', Charset.forName("UTF-8"));
//        int predlen= pred.length;
//        int testlablen=testlab.length;
//        String[] predrow=new String[predlen];
//        for(int i=0;i<predlen;i++) predrow[i]=String.valueOf(pred[i]);
//        csvWriter.writeRecord(predrow);
//        String[] testlabdrow=new String[testlablen];
//        for(int i=0;i<testlablen;i++) testlabdrow[i]=String.valueOf(testlab[i]);
//        csvWriter.writeRecord(testlabdrow);
//
//        csvWriter.close();

//        int [][]totalpos= KSRCNF.totalpos;
//        CsvWriter csvWriter = new CsvWriter("./out/totalpos.csv", ',', Charset.forName("UTF-8"));
//        int posrow=totalpos.length;
//        int poscol=totalpos[0].length;
//        for(int i=0;i<posrow;i++){
//            String[] onerow=new String[poscol];
//            for(int j=0;j<poscol;j++){
//                onerow[j]=String.valueOf(totalpos[i][j]);
//            }
//            csvWriter.writeRecord(onerow);
//        }
//        csvWriter.close();

    }
}

import Jama.Matrix;
import scala.Tuple2;

import javax.tools.Tool;
import java.io.IOException;

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
        gam_K=14.904789935208639;//sig
        gam_w=0.742065795512883;//sig0s
        KSRCNF.KSRCNFinit("Indian_pines_corrected.mat",
                "trainidxtest.mat",
                "testidxtest.mat",
                "Indian_gt.mat",
                wind,mu,lam,gam_w,gam_K);
        int []trainidx2D=KSRCNF.trainidx2D;
        int []testidx2D=KSRCNF.testidx2D;
        int []trainlab=KSRCNF.trainlab;
        int []testlab=KSRCNF.testlab;
       // for(int i=0;i<trainlab.length;i++) System.out.println(trainlab[i]);
        int rows =KSRCNF.rows;
        int cols =KSRCNF.cols;
        double[][]img2D=KSRCNF.img2D;
        Tuple2<double[][],double[][]> ATA_ATX= KSRCNF.ker_lwm();

//        double [][] Ktrain=ATA_ATX._1;
//        double [][] Ktest=ATA_ATX._2;
//
//        int[] trainidx2Da=KSRCNF.trainidx2D;
//          int[][] trainposa=KSRCNF.trainpos;
//          int[] trainijw_sizea=KSRCNF.trainijw_size;
//          double[][]trainijw2D_weighta=KSRCNF.trainijw2D_weight;
//          int[][]train_ijw2Da=KSRCNF.train_ijw2D;

        Matrix S = Tools.ADMM(ATA_ATX._1,ATA_ATX._2,mu,lam);
//        S.print(S.getRowDimension(),S.getColumnDimension());
//        double[][] Stoarray= S.getArray();
//        for(int i=0;i<S.getRowDimension();i++){
//            for(int j=0;j<S.getColumnDimension();j++){
//                System.out.print(Stoarray[i][j]+" ");
//            }
//            System.out.println();
//        }

        int [] pred;
        pred= Tools.classker_pred(ATA_ATX._1,ATA_ATX._2,S.getArrayCopy(),testlab,trainlab);

        double OA;
        OA= Tools.classeval(pred,testlab);
        System.out.println(OA);
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

//        CsvWriter csvWriter = new CsvWriter("./out/Ktest.csv", ',', Charset.forName("UTF-8"));
//        int imgrow=Ktest.length;
//        int imgcol=Ktest[0].length;
//        for(int i=0;i<imgrow;i++){
//            String[] onerow=new String[imgcol];
//            for(int j=0;j<imgcol;j++){
//                onerow[j]=String.valueOf(Ktest[i][j]);
//            }
//            csvWriter.writeRecord(onerow);
//        }
//        csvWriter.close();
////
//        CsvWriter csvWriter = new CsvWriter("./out/trainijw2D_weighta.csv", ',', Charset.forName("UTF-8"));
//        int imgrow=trainijw2D_weighta.length;
//        int imgcol=trainijw2D_weighta[0].length;
//        for(int i=0;i<imgrow;i++){
//            String[] onerow=new String[imgcol];
//            for(int j=0;j<imgcol;j++){
//                onerow[j]=String.valueOf(trainijw2D_weighta[i][j]);
//            }
//            csvWriter.writeRecord(onerow);
//        }
//        csvWriter.close();

//        CsvWriter csvWriter = new CsvWriter("./out/train_ijw2Da.csv", ',', Charset.forName("UTF-8"));
//        int imgrow=train_ijw2Da.length;
//        int imgcol=train_ijw2Da[0].length;
//        for(int i=0;i<imgrow;i++){
//            String[] onerow=new String[imgcol];
//            for(int j=0;j<imgcol;j++){
//                onerow[j]=String.valueOf(train_ijw2Da[i][j]);
//            }
//            csvWriter.writeRecord(onerow);
//        }
//        csvWriter.close();
    }
}

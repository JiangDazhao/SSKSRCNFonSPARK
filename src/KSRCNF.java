import Jama.Matrix;
import com.csvreader.CsvWriter;
import scala.Tuple2;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;


public class KSRCNF {
    static Data data;
    static int wind;
    static double mu;
    static double lam;
    static double gam_w;
    static double gam_K;
    static int rows;
    static int cols;
    static int bands;
    static double[][] img2D;
    static int[][] img_gt;
    static int []trainidx2D;
    static int []testidx2D;
    static int []trainlab;
    static int []testlab;
    static int [] totallab;

    static int [] totalidx2D;

    public static void KSRCNFinit(String sdataset, String strainidx2D, String stestidx2D, String groundtruth, String stotalidx2D,
                  int wind,double mu,double lam,
                  double gam_w,double gam_K) throws IOException {
        data=new Data(sdataset, strainidx2D, stestidx2D,groundtruth,stotalidx2D);
        KSRCNF.wind=wind;
        KSRCNF.mu=mu;
        KSRCNF.lam=lam;
        rows=data.getRows();
        cols=data.getCols();
        bands=data.getBands();
        img2D=data.getImg2D();
        img_gt=data.getImg_gt();
        trainidx2D=data.getTrainidx2D();
        testidx2D=data.getTestidx2D();
        trainlab=data.getTrainlab();
        testlab=data.getTestlab();
        totalidx2D=data.getTotalidx2D();
        totallab=data.getTotallab();
        KSRCNF.gam_w=gam_w;
        KSRCNF.gam_K=gam_K;
    }

    static double[][]Ktrain;
    static double[][]Ktest;
    static double [][]Ktotal;
    static int [][]totalpos;
    static int []totalijw_size;
    static int [][]total_ijw2D;
    static double[][]totalijw2D_weight;
    static int nwind;
    static int trainidx2D_length;
    static int testidx2D_length;
    static int totalidx2D_length;

     public static Tuple2<double[][],double[][]> ker_lwm( ) throws IOException {

         SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
         nwind= (2*wind+1)*(2*wind+1);
         trainidx2D_length=trainidx2D.length;
         testidx2D_length=testidx2D.length;
         totalidx2D_length=totalidx2D.length;

         Ktrain= new double[trainidx2D_length][trainidx2D_length];
         Ktest= new double[trainidx2D_length][testidx2D_length];
         Ktotal = new double[trainidx2D_length][totalidx2D_length];
         totalpos= new int[totalidx2D_length][2];
         total_ijw2D=new int[nwind][totalidx2D_length];
         totalijw_size= new int[totalidx2D_length];
         totalijw2D_weight=new double[nwind][totalidx2D_length];

         //totalpos
         for(int i=0;i<totalidx2D_length;i++){
             int rowth2to3=totalidx2D[i]%rows;
             int colth2to3=totalidx2D[i]/rows;
             totalpos[i][0]=rowth2to3;
             totalpos[i][1]=colth2to3;
         }
         /////////
         CsvWriter csvWriter = new CsvWriter("./out/totalpos.csv", ',', Charset.forName("UTF-8"));
         int totalposrow=totalpos.length;
         int totalposcol=totalpos[0].length;
         for(int i=0;i<totalposrow;i++){
             String[] onerow=new String[totalposcol];
             for(int j=0;j<totalposcol;j++){
                 onerow[j]=String.valueOf(totalpos[i][j]);
             }
             csvWriter.writeRecord(onerow);
         }
         csvWriter.close();
         ////////
         System.out.println("have done the totalpos.... ");
         System.out.println(df.format(new Date()));

         //totalijw2D
         for(int n=0;n<totalidx2D_length;n++){
             int i=totalpos[n][0];//match
             int j=totalpos[n][1];
             int iw_begin=Math.max(i-wind,0);
             int iw_end=Math.min(i+wind,rows-1);
             int jw_begin=Math.max(j-wind,0);
             int jw_end=Math.min(j+wind,cols-1);
             int iw_size= iw_end-iw_begin+1;
             int jw_size= jw_end-jw_begin+1;
             totalijw_size[n]=iw_size*jw_size;//match

             double[][] iwarray=new double[1][iw_size];
             for(int p=0;p<iw_size;p++){
                 iwarray[0][p]=iw_begin+p;
             }
             double[][] jwarray=new double[1][jw_size];
             for(int p=0;p<jw_size;p++){
                 jwarray[0][p]=jw_begin+p;
             }
             Matrix iwmat= new Matrix(iwarray);
             Matrix jwmat= new Matrix(jwarray);

             double[][] iwreparray= new double[iw_size][jw_size];
             double[][] jwreparray= new double[iw_size][jw_size];
             for(int p=0;p<jw_size;p++)
                 for(int q=0;q<iw_size;q++){
                     iwreparray[q][p]=iwarray[0][q];
                 }
            for(int p=0;p<iw_size;p++)
                for(int q=0;q<jw_size;q++){
                    jwreparray[p][q]=jwarray[0][q];
                }
            Matrix iwrepmat = new Matrix(iwreparray);
            Matrix jwrepmat = new Matrix(jwreparray);

            Matrix ijwmat= iwrepmat.plus(jwrepmat.times(rows));
            int index=0;
            for(int p=0;p<ijwmat.getColumnDimension();p++)
                for(int q=0;q<ijwmat.getRowDimension();q++){
                   total_ijw2D[index++][n]= (int) ijwmat.get(q,p);//match
                }
         }

//         //////////////////
//         csvWriter = new CsvWriter("./out/totalijw2D.csv", ',', Charset.forName("UTF-8"));
//         int ijw2drow=total_ijw2D.length;
//         int ijw2dcol=total_ijw2D[0].length;
//         for(int i=0;i<ijw2drow;i++){
//             String[] onerow=new String[ijw2dcol];
//             for(int j=0;j<ijw2dcol;j++){
//                 onerow[j]=String.valueOf(total_ijw2D[i][j]);
//             }
//             csvWriter.writeRecord(onerow);
//         }
//         csvWriter.close();
//
//         csvWriter = new CsvWriter("./out/totalijw2Dsize.csv", ',', Charset.forName("UTF-8"));
//         int ijw2dsizerow=totalijw_size.length;
//         String[] onerow=new String[ijw2dsizerow];
//         for(int j=0;j<ijw2dsizerow;j++){
//             onerow[j]=String.valueOf(totalijw_size[j]);
//         }
//         csvWriter.writeRecord(onerow);
//         csvWriter.close();
//         //////////////////

         System.out.println("have done the totalijw2D.... ");
         System.out.println(df.format(new Date()));

         //all the w(x1,xj) of total_size
         for(int n=0;n<totalidx2D_length;n++){
             int img2Didxheart= totalidx2D[n];
             double []imgheart= KSRCNF.img2Didx_pixel(img2Didxheart);
             for(int windidxab=0;windidxab<totalijw_size[n];windidxab++){
                 int img2Didxab= total_ijw2D[windidxab][n];
                 double []imgab= KSRCNF.img2Didx_pixel(img2Didxab);
                 totalijw2D_weight[windidxab][n]= Tools.kernelcompute(imgheart,imgab,gam_w);
                 //if(n==1) System.out.println(trainijw2D_weight[windidxab][n]);
             }
         }

//         //////////////////
//         csvWriter = new CsvWriter("./out/totalijw2D_weight.csv", ',', Charset.forName("UTF-8"));
//         int weightrow=totalijw2D_weight.length;
//         int weightcol=totalijw2D_weight[0].length;
//         for(int i=0;i<weightrow;i++){
//             String[] d=new String[weightcol];
//             for(int j=0;j<weightcol;j++){
//                 onerow[j]=String.valueOf(totalijw2D_weight[i][j]);
//             }
//             csvWriter.writeRecord(d);
//         }
//         csvWriter.close();
//         /////////////////

         System.out.println("have done the w(x1,x2)... ");
         System.out.println(df.format(new Date()));


         //Ktotal_compute
         for(int p=0;p<trainidx2D_length;p++){
             if(p%50==0){
                 System.out.println("start to do "+p+" train examples of Ktotal... ");
                 System.out.println(df.format(new Date()));
             }
             for(int q=0;q<totalidx2D_length;q++){
                 Ktotal[p][q]=Ktotalelecompute(p,q,gam_K);
             }
         }
         System.out.println("have done the Ktotal_compute... ");
         System.out.println(df.format(new Date()));

         Matrix totalmat= new Matrix(Ktotal);
         Matrix Ktrainmat= totalmat.getMatrix(0,trainidx2D_length-1,0,trainidx2D_length-1);
         Matrix Ktestmat = totalmat.getMatrix(0,trainidx2D_length-1,trainidx2D_length,totalidx2D_length-1);
         Ktrain=Ktrainmat.getArrayCopy();
         Ktest=Ktestmat.getArrayCopy();

         return new Tuple2<double[][], double[][]>(Ktrain,Ktest);
        }


        //Ktotalelement_compute
        public static double Ktotalelecompute(int krowth_index, int kcolth_index,
                                              double gam_K){
             double upresult=0;
             double div=0;
             double result=0;
             double sumwa=0;
             double sumwb=0;

             //sumwa;
            for(int i=0;i<totalijw_size[krowth_index];i++)
                sumwa=sumwa+totalijw2D_weight[i][krowth_index];

            //sumwb;
            for(int i=0;i<totalijw_size[kcolth_index];i++)
                sumwb=sumwb+totalijw2D_weight[i][kcolth_index];

             for(int a=0;a<totalijw_size[krowth_index];a++){
                 for(int b=0;b<totalijw_size[kcolth_index];b++){
                     int img2Didxa = total_ijw2D[a][krowth_index];
                     int img2Didxb = total_ijw2D[b][kcolth_index];
                     double []imga = KSRCNF.img2Didx_pixel(img2Didxa);
                     double []imgb = KSRCNF.img2Didx_pixel(img2Didxb);
                     double kernelK= Tools.kernelcompute(imga,imgb,gam_K);
                     double kernelmulwab= kernelK*totalijw2D_weight[a][krowth_index]
                                            *totalijw2D_weight[b][kcolth_index];
                     upresult+=kernelmulwab;

                 }
             }
             div= sumwa*sumwb;
             result=upresult/div;
             return result;
        }


        public static double[] img2Didx_pixel(int index){
            double[]pixel;
            pixel= new double[bands];
            for(int i=0;i<bands;i++){
                pixel[i]=img2D[i][index];
            }
            return pixel;
        }


//    public static void main(String[] args) {
//        EnumMap<KernelQP,Object> QPmap = new EnumMap<KernelQP, Object>(KernelQP.class);
//        ArrayList<Double> a= new ArrayList<Double>();
//        a.add(1.0);
//        ArrayList<Double> b= new ArrayList<Double>();
//        b.add(1.0);
//        double[][] A= new double[][]();
//        A.add(a);
//        A.add(b);
//        QPmap.put(KernelQP.ATA,A);
//        System.out.println(QPmap.get(KernelQP.ATA).toString());
//    }

//    public static void main(String[] args) {
//        double[][] iwreparray= {{1.,2.},{3.,4.},{5.,6.}};
//        double[][] jwreparray= {{1.,1.},{1.,1.},{1.,1.}};
//
//        Matrix iwrepmat = new Matrix(iwreparray);
//        Matrix jwrepmat = new Matrix(jwreparray);
//        Matrix ijwmat= iwrepmat.plus(jwrepmat.times(145));
//        System.out.println(ijwmat.getRowDimension());
//        System.out.println(ijwmat.getColumnDimension());
//        ijwmat.print(3,3);
//        System.out.println(ijwmat.getColumnDimension()*ijwmat.getRowDimension());
//        double [][]arraycopy= iwrepmat.getArrayCopy();
//        for(int i=0;i<arraycopy.length;i++)
//            for(int j=0;j<arraycopy[0].length;j++){
//                System.out.println(arraycopy[i][j]+" ");
//            }
//
//        Matrix subi= iwrepmat.getMatrix(new int[]{0, 1,2}, new int[]{0});
//            subi.print(2,2);
//    }


}


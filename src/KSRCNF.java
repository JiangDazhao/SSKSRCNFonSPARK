import Jama.Matrix;
import scala.Tuple2;

import java.io.IOException;
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

        public static void KSRCNFinit(String sdataset, String strainidx2D, String stestidx2D, String groundtruth,
                      int wind,double mu,double lam,
                      double gam_w,double gam_K) throws IOException {
            data=new Data(sdataset, strainidx2D, stestidx2D,groundtruth);
            KSRCNF.wind=wind;
            KSRCNF.mu=mu;
            KSRCNF.lam=lam;
            rows=data.rows;
            cols=data.cols;
            bands=data.bands;
            img2D=data.img2D;
            img_gt=data.img_gt;
            trainidx2D=data.trainidx2D;
            testidx2D=data.testidx2D;
            trainlab=data.trainlab;
            testlab=data.testlab;
            KSRCNF.gam_w=gam_w;
            KSRCNF.gam_K=gam_K;
        }

        static double [][] Ktrain;
        static double [][] Ktest;
        static int nwind;
        static int trainidx2D_length;
        static int testidx2D_length;
        static int [][]trainpos;
        static int [][]testpos;
        static int [][]train_ijw2D;
        static int [][]test_ijw2D;
        static int []trainijw_size;
        static int []testijw_size;
        static double[][]trainijw2D_weight;
        static double[][]testijw2D_weight;

             public static Tuple2<double[][],double[][]> ker_lwm( ){
             nwind= (2*wind+1)*(2*wind+1);
             trainidx2D_length=trainidx2D.length;
             testidx2D_length=testidx2D.length;

             Ktrain = new double[trainidx2D_length][trainidx2D_length];
             Ktest = new double[trainidx2D_length][testidx2D_length];
             trainpos=new int[trainidx2D_length][2];
             testpos= new int[testidx2D_length][2];
             train_ijw2D=new int[nwind][trainidx2D_length];
             test_ijw2D= new int[nwind][testidx2D_length];
             trainijw_size= new int [trainidx2D_length];
             testijw_size=new int [testidx2D_length];
             trainijw2D_weight=new double[nwind][trainidx2D_length];
             testijw2D_weight=new double[nwind][testidx2D_length];

             //trainpos
             for(int i=0;i<trainidx2D_length;i++){
                 int rowth2to3=trainidx2D[i]%rows;
                 int colth2to3=trainidx2D[i]/rows;
                 trainpos[i][0]=rowth2to3;
                 trainpos[i][1]=colth2to3;
             }

             //testpos
             for(int i=0;i<testidx2D_length;i++){
                 int rowth2to3=testidx2D[i]%rows;
                 int colth2to3=testidx2D[i]/rows;
                 testpos[i][0]=rowth2to3;
                 testpos[i][1]=colth2to3;
             }

             //train_ijw2D
             for(int n=0;n<trainidx2D_length;n++){
                 int i=trainpos[n][0];//match
                 int j=trainpos[n][1];
                 int iw_begin=Math.max(i-wind,0);
                 int iw_end=Math.min(i+wind,rows-1);
                 int jw_begin=Math.max(j-wind,0);
                 int jw_end=Math.min(j+wind,cols-1);
                 int iw_size= iw_end-iw_begin+1;
                 int jw_size= jw_end-jw_begin+1;
                 trainijw_size[n]=iw_size*jw_size;//match

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
                        train_ijw2D[index++][n]= (int) ijwmat.get(q,p);//match
                    }
             }

             //test_ijw2D
             for(int n=0;n<testidx2D_length;n++){
                 int i=testpos[n][0];
                 int j=testpos[n][1];
                 int iw_begin=Math.max(i-wind,0);
                 int iw_end=Math.min(i+wind,rows-1);
                 int jw_begin=Math.max(j-wind,0);
                 int jw_end=Math.min(j+wind,cols-1);
                 int iw_size= iw_end-iw_begin;
                 int jw_size= jw_end-jw_begin;
                 testijw_size[n]=iw_size*jw_size;

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
                         test_ijw2D[index++][n]= (int) ijwmat.get(q,p);
                     }
             }
                 System.out.println("have done the train_ijw2D and test_ijw2D... ");

             //all the w(x1,xj) of train_size
             for(int n=0;n<trainidx2D_length;n++){
                 int img2Didxheart= trainidx2D[n];
                 double []imgheart= KSRCNF.img2Didx_pixel(img2Didxheart);
                 for(int windidxab=0;windidxab<trainijw_size[n];windidxab++){
                     int img2Didxab= train_ijw2D[windidxab][n];
                     double []imgab= KSRCNF.img2Didx_pixel(img2Didxab);
                     trainijw2D_weight[windidxab][n]= Tools.kernelcompute(imgheart,imgab,gam_w);
                     //if(n==1) System.out.println(trainijw2D_weight[windidxab][n]);
                 }
             }

             //all the w(x1,xj) of test_size
             for(int n=0;n<testidx2D_length; n++){
                 int img2Didxheart= testidx2D[n];
                 double []imgheart= KSRCNF.img2Didx_pixel(img2Didxheart);
                 for(int windidxab=0;windidxab<testijw_size[n];windidxab++){
                     int img2Didxab= test_ijw2D[windidxab][n];
                     double []imgab= KSRCNF.img2Didx_pixel(img2Didxab);
                     testijw2D_weight[windidxab][n]= Tools.kernelcompute(imgheart,imgab,gam_w);
                 }
             }
                 System.out.println("have done the w(x1,x2)... ");
                 SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                 System.out.println(df.format(new Date()));


                 //Ktrain_compute
             for(int p=0;p<trainidx2D_length;p++){
                 if(p%50==0){
                     System.out.println("have done "+p+" train examples of Ktrain... ");
                     System.out.println(df.format(new Date()));
                 }
                 for(int q=0;q<trainidx2D_length;q++){
                     Ktrain[p][q]=Ktrainelecompute(p,q,gam_K);
                 }
             }
             System.out.println("have done the Ktrain_compute... ");

             //Ktest_compute
             for(int p=0;p<trainidx2D_length;p++){
                 if(p%50==0){
                     System.out.println("have done "+p+" train examples of Ktest...");
                     System.out.println(df.format(new Date()));
                 }
                 for(int q=0;q<testidx2D_length;q++){
                     Ktest[p][q]=Ktestelecompute(p,q,gam_K);
                 }
             }
             System.out.println("have done the Ktest_compute... ");

             return new Tuple2<>(Ktrain,Ktest);
         }


        //Ktrainelement_compute
        public static double Ktrainelecompute(int krowth_index, int kcolth_index,
                                              double gam_K){
             double upresult=0;
             double div=0;
             double result=0;
             double sumwa=0;
             double sumwb=0;

             //sumwa;
            for(int i=0;i<trainijw_size[krowth_index];i++)
                sumwa=sumwa+trainijw2D_weight[i][krowth_index];
            //System.out.println(sumwa);

            //sumwb;
            for(int i=0;i<trainijw_size[kcolth_index];i++)
                sumwb=sumwb+trainijw2D_weight[i][kcolth_index];

             for(int a=0;a<trainijw_size[krowth_index];a++){
                 for(int b=0;b<trainijw_size[kcolth_index];b++){
                     int img2Didxa = train_ijw2D[a][krowth_index];
                     int img2Didxb = train_ijw2D[b][kcolth_index];
                     double []imga = KSRCNF.img2Didx_pixel(img2Didxa);
                     double []imgb = KSRCNF.img2Didx_pixel(img2Didxb);
                     double kernelK= Tools.kernelcompute(imga,imgb,gam_K);
                     double kernelmulwab= kernelK*trainijw2D_weight[a][krowth_index]
                                            *trainijw2D_weight[b][kcolth_index];
                     upresult+=kernelmulwab;
                    //if(krowth_index==1) System.out.println(kernelK);

                 }
             }
             div= sumwa*sumwb;
             result=upresult/div;
            //System.out.println(upresult);
             return result;
        }


        //Ktestelement_compute
        public static double Ktestelecompute(int krowth_index, int kcolth_index,
                                          double gam_K){
            double upresult=0;
            double div=0;
            double result=0;
            double sumwa=0;
            double sumwb=0;

            //sumwa;
            for(int i=0;i<trainijw_size[krowth_index];i++)
                sumwa=sumwa+trainijw2D_weight[i][krowth_index];

            //sumwb;
            for(int i=0;i<testijw_size[kcolth_index];i++)
                sumwb=sumwb+testijw2D_weight[i][kcolth_index];

            for(int a=0;a<trainijw_size[krowth_index];a++){
                for(int b=0;b<testijw_size[kcolth_index];b++){
                    int img2Didxa = train_ijw2D[a][krowth_index];
                    int img2Didxb = test_ijw2D[b][kcolth_index];
                    double []imga = KSRCNF.img2Didx_pixel(img2Didxa);
                    double []imgb = KSRCNF.img2Didx_pixel(img2Didxb);
                    double kernelK= Tools.kernelcompute(imga,imgb,gam_K);
                    double kernelmulwab= kernelK*trainijw2D_weight[a][krowth_index]
                            *testijw2D_weight[b][kcolth_index];
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
//
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

//    public static void main(String[] args) {
//        double []pixel={1,2,3,4};
//        double []imgab= pixel;
//        for(int i=0;i<4;i++) System.out.println(imgab[i]);
//    }

}


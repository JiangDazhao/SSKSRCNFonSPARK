import org.ujmp.core.Matrix;
import org.ujmp.jmatio.ImportMatrixMAT;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;


public class Data {
      static short [][] rawimg2D;
      static double [][] img2D;
      static double [][][] img3D;
      static int [][] img_gt;
      static int [] trainidx2D;
      static int [] testidx2D;
      static int [] trainlab;
      static int [] testlab;


      static int rows;
      static int cols;
      static int bands;
    public Data(String sdataset, String strainidx2D, String stestidx2D, String groundtruth) throws IOException {
        //load img data
        String datapathname="./resources/"+sdataset;
        String trainidxpathname="./resources/"+strainidx2D;
        String testidxpathname="./resources/"+stestidx2D;
        ImportMatrixMAT testimg = new ImportMatrixMAT();
        File imgfile  = new File(datapathname);
        Matrix Matriximg = testimg.fromFile(imgfile);
        long[] img_dimentions=Matriximg.getSize();
        this.rows = (int)img_dimentions[0];
        this.cols = (int)img_dimentions[1];
        this.bands= (int)img_dimentions[2];

        // load img_gt
        String gtpathname="./resources/"+groundtruth;
        ImportMatrixMAT groundimg = new ImportMatrixMAT();
        File groundimgfile  = new File(datapathname);
        Matrix Matrixgroundimg = testimg.fromFile(groundimgfile);
        this.img_gt=new int[rows][cols];
        for(int i=0;i<rows;i++)
            for(int j=0;j<cols;j++){
                this.img_gt[i][j]=Matrixgroundimg.getAsInt(i,j)-1;
            }


        //trainidx2D
        ImportMatrixMAT import_train = new ImportMatrixMAT();
        File trainfile = new File(trainidxpathname);
        Matrix matrix_train = import_train.fromFile(trainfile);
        long[] train_dim = matrix_train.getSize();
        int trainlen = (int) train_dim[1];
        this.trainidx2D = new int[trainlen];
        for(int i=0;i<trainlen;i++)
            this.trainidx2D[i]= matrix_train.getAsInt(0,i)-1;

        //testidx2D
        ImportMatrixMAT import_test = new ImportMatrixMAT();
        File testfile = new File(testidxpathname);
        Matrix matrix_test = import_test.fromFile(testfile);
        long[] test_dim = matrix_test.getSize();
        int testlen = (int) test_dim[1];
        this.testidx2D = new int[testlen];
        for(int i=0;i<testlen;i++)
            this.testidx2D[i]= matrix_test.getAsInt(0,i)-1;

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

        //reshape3d_2d
        this.rawimg2D = new short[bands][rows*cols];
        for(int i=0;i<bands;i++)
            for(int j=0;j<cols;j++)
                for(int k=0;k<rows;k++)
                {
                    rawimg2D[i][j*rows+k]=Matriximg.getAsShort(k,j,i);
                }

        //line_dat
        double[] sortX = new double[rows*cols*bands];
        for(int i=0;i<rawimg2D.length;i++)
            for(int j=0;j<rawimg2D[0].length;j++)
            {
                sortX[i*rawimg2D[0].length+j]=rawimg2D[i][j];
            }
        Arrays.sort(sortX);
       // System.out.println(Arrays.toString(sortX));
        int sortXL=sortX.length;
     //   System.out.println(sortXL);
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

        this.img3D=new double[rows][cols][bands];
        for (int i=0;i<bands;i++)
            for(int j=0;j<rows*cols;j++)
            {
                int rowth= j%rows;
                int colth= j/rows;
                img3D[rowth][colth][i]=img2D[i][j];
            }
    }

//    public static void main(String[] args) throws IOException {
//        Data data= new Data("Indian_pines_corrected.mat","trainidx.mat","testidx.mat");
//        System.out.println(data.getRows());
//        System.out.println(data.getCols());
//        System.out.println(data.getBands());

//    public static void main(String[] args) throws IOException {
//        Data data= new Data("Indian_pines_corrected.mat","trainidxtest.mat","testidxtest.mat");
//
//        CsvWriter csvWriter = new CsvWriter("./out/img2D.csv", ',', Charset.forName("UTF-8"));
//        int imgrow=data.bands;
//        int imgcol=data.rows*data.cols;
//        double img2D[][] = data.img2D;
//        for(int i=0;i<imgrow;i++){
//            String[] onerow=new String[imgcol];
//            for(int j=0;j<imgcol;j++){
//                onerow[j]=String.valueOf(img2D[i][j]);
//            }
//            csvWriter.writeRecord(onerow);
//        }
//        csvWriter.close();
//    }

//        ImportMatrixMAT import_train = new ImportMatrixMAT();
//        File trainfile = new File("./resources/trainidx.mat");
//        Matrix matrix_train = import_train.fromFile(trainfile);
//        long[] train_dim = matrix_train.getSize();
//        int trainlen = (int) train_dim[1];
//        trainidx2D = new int[trainlen];
//        for(int i=0;i<trainlen;i++)
//        {
//            trainidx2D[i]= matrix_train.getAsInt(0,i);
//            System.out.println(trainidx2D[i]);
//        }


//    }
}

import Jama.Matrix;

import java.util.HashSet;
import java.util.Set;
import scala.Tuple2;

public class Tools {

    public static double  kernelcompute(double[] x1,double[] x2,double gam){
        double D=0.;
        for(int i=0;i<x1.length;i++){
            D+=(x1[i]-x2[i])*(x1[i]-x2[i]);

        }
        double e=Math.E;
        double order= -1*gam*D;
        double K= Math.pow(e,order);
        return K;
    }


    public static Matrix ADMM(double[][]ATA,double[][]ATX,
                                  double mu, double lam){
            double tol=0.001;
            int maxit=1000;
            Matrix S= new Matrix(ATX.length,ATX[0].length);
            Matrix ATAmat = new Matrix(ATA);
            Matrix ATXmat = new Matrix(ATX);

            Matrix vmat = new Matrix(ATX.length,ATX[0].length);
            Matrix dmat =vmat;
            Matrix QuI= ATAmat.plus((Matrix.identity(ATA[0].length,ATA[0].length)).times(mu));
            Matrix Finverse= QuI.inverse();
            Matrix S0 = Finverse.times(ATXmat);
            for(int iter=0;iter<maxit;iter++){
                S = Finverse.times(ATXmat.plus((vmat.plus(dmat)).times(mu)));
                vmat = new Matrix(Tools.soft(S.minus(dmat),lam/mu));
                dmat = dmat.minus(S.minus(vmat));
                //System.out.println("iter:"+iter);
                if(iter%100==0)System.out.println("have done "+iter+" iters of ADMM ");

                if(Tools.checkstop(iter,S,S0,tol)){
                    return S;
                }
                S0=S;
            }
            return S;
    }


    public static int[] classker_pred(double[][]ATA,double[][]ATX,
                                      double[][]coef,
                                      int[]test_lab, int[]dict_lab){
        int [] pred= new int[test_lab.length];
        double [][]preddisterr;
        Set<Integer>classinDict_pert;
        int nClass;
        int nTest;
        Set <Integer>clsset= new HashSet();
        for(int i=0;i<dict_lab.length;i++){
            clsset.add(dict_lab[i]);
        }
        int []clsarray =new int[clsset.size()];
        int clsarrayidx=0;
        for(int cls:clsset) clsarray[clsarrayidx++]=cls;

        nClass=clsset.size();

        Matrix coefmat= new Matrix(coef);
        Matrix ATAmat = new Matrix(ATA);
        Matrix ATXmat = new Matrix(ATX);

        nTest=test_lab.length;
        preddisterr=new double[nClass][nTest];

        for(int t=0;t<nTest;t++){ //each test
            double[] err= new double[nClass];
            int nclassindex=0;
            for(int cls:clsset){
                classinDict_pert = new HashSet<>();

                for (int j = 0; j < dict_lab.length; j++) {
                    if (dict_lab[j] == cls) classinDict_pert.add(j);
                }

                int []classinDict_pertarray= new int[classinDict_pert.size()];
                int idx=0;
                for(int clsperidx:classinDict_pert){
                    classinDict_pertarray[idx++]=clsperidx;
                }


                Matrix xt_ATA_x= (((coefmat.getMatrix(classinDict_pertarray,new int[]{t})).transpose())
                        .times(ATAmat.getMatrix(classinDict_pertarray,classinDict_pertarray)))
                        .times(coefmat.getMatrix(classinDict_pertarray,new int[]{t}));

                Matrix two_xt_ATX=((coefmat.getMatrix(classinDict_pertarray,new int[]{t}).transpose())
                        .times(2))
                        .times(ATXmat.getMatrix(classinDict_pertarray,new int[]{t}));

                Matrix resultminus= xt_ATA_x.minus(two_xt_ATX);
                err[nclassindex]=resultminus.get(0,0);

                preddisterr[nclassindex][t]=err[nclassindex];
                nclassindex++;

            }
            int clsindex=0;
            for(int i=0;i<err.length;i++){
                if(err[i]<err[clsindex]) clsindex=i;
            }
            pred[t]=clsarray[clsindex];

            if(t%50==0)System.out.println("have done "+t+" examples of classker_pred");
        }
        return pred;
    }


    public static double classeval(int[]pred, int[]testlab){
        double OA=0;
        for(int i=0;i<pred.length;i++){
               if (pred[i]==testlab[i]) OA+=1.0;
           }
        OA= OA/pred.length*100.0;
        return OA;
    }


    public static double[][] soft(Matrix u, double a){
        int rowsu=u.getRowDimension();
        int colsu=u.getColumnDimension();
        double[][]signu= new double[rowsu][colsu];
        for(int i=0;i<signu.length;i++)
            for(int j=0;j<signu[0].length;j++){
                if(u.get(i,j)>0) signu[i][j]=1;
                else if (u.get(i,j)<0)signu[i][j]=-1;
                else signu[i][j]=0;
            }
        Matrix signumat = new Matrix(signu);
        double [][]absu_a=new double[rowsu][colsu];
        for(int i=0;i<absu_a.length;i++)
            for(int j=0;j<absu_a[0].length;j++){
                absu_a[i][j]=Math.abs(u.get(i,j))-a;
            }
        double [][]maxabsu_a = new double[absu_a.length][absu_a[0].length];
        for(int i=0;i<maxabsu_a.length;i++)
            for(int j=0;j<maxabsu_a[0].length;j++){
                if(absu_a[i][j]>=0) maxabsu_a[i][j]=absu_a[i][j];
                else maxabsu_a[i][j]=0;
            }
        double[][] z = new double[rowsu][colsu];
        for(int i=0;i<rowsu;i++)
            for(int j=0;j<colsu;j++){
                z[i][j]=signu[i][j]*maxabsu_a[i][j];
            }
        return z;
    }


    public static boolean checkstop(int iter, Matrix S, Matrix S0, double tol){
        boolean stop= false;
        if (iter>0){
            double dtol= (S.minus(S0)).normF()/(S0.normF());
            if(dtol<tol) stop=true;
        }
        return stop;
    }

    public static short[] Bytetoidx(byte[]data,int datatype){
        int len=data.length;
        int idxlen=len/datatype;
        short[]result=new short[idxlen];
        int n=0;
        for (int i=0;i<idxlen;i++)
            result[i]=(short)((data[n++]&0xff) | (data[n++] <<8));
        return result;
    }

    public static int[][] blockPosCal(short[]idx,int rows){
        int [][]blockpos=new int[idx.length][2];
        for(int i=0;i<idx.length;i++){
            int rowth2to3=idx[i]%rows;
            int colth2to3=idx[i]/rows;
            blockpos[i][0]=rowth2to3;
            blockpos[i][1]=colth2to3;
        }
        return blockpos;
    }


    public static Tuple2<int[][],int[]> blockIjw2DCal(short[]idx, int[][]pos, int rows, int cols, int wind){
        int [][]blockijw=new int[(2*wind+1)*(2*wind+1)][idx.length];
        int []blockijwsize=new int[idx.length];
        for(int n=0;n<idx.length;n++){
            int i=pos[n][0];
            int j=pos[n][1];
            int iw_begin=Math.max(i-wind,0);
            int iw_end=Math.min(i+wind,rows-1);
            int jw_begin=Math.max(j-wind,0);
            int jw_end=Math.min(j+wind,cols-1);
            int iw_size= iw_end-iw_begin+1;
            int jw_size= jw_end-jw_begin+1;
            blockijwsize[n]=iw_size*jw_size;//match

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
                    blockijw[index++][n]= (int) ijwmat.get(q,p);//match
                }
        }

        return new Tuple2<>(blockijw,blockijwsize);
    }



}


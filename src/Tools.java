import Jama.Matrix;

import javax.tools.Tool;
import java.util.HashSet;
import java.util.Set;

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
//            System.out.println("dict_lab：");
//            for(int i=0;i<dict_lab.length;i++) System.out.print(dict_lab[i]+" ");
//            System.out.println();

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
//            System.out.println("clsset次序：");
//            for(int cls:clsset){
//                System.out.println(cls);
//            }
        nClass=clsset.size();
        //System.out.println(test_lab.length);
        //System.out.println(dict_lab.length);
        //System.out.println(nClass);//3

        Matrix coefmat= new Matrix(coef);
        //coefmat.print(coefmat.getRowDimension(),coefmat.getColumnDimension());
        Matrix ATAmat = new Matrix(ATA);
        //ATAmat.print(ATAmat.getRowDimension(),ATAmat.getColumnDimension());
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
                    //System.out.print(clsperidx+" ");
                }
                //System.out.println();


                Matrix xt_ATA_x= (((coefmat.getMatrix(classinDict_pertarray,new int[]{t})).transpose())
                        .times(ATAmat.getMatrix(classinDict_pertarray,classinDict_pertarray)))
                        .times(coefmat.getMatrix(classinDict_pertarray,new int[]{t}));
//                System.out.println("xt_ATA_x is: ");
//                xt_ATA_x.print(xt_ATA_x.getRowDimension(),xt_ATA_x.getColumnDimension());

                Matrix two_xt_ATX=((coefmat.getMatrix(classinDict_pertarray,new int[]{t}).transpose())
                        .times(2))
                        .times(ATXmat.getMatrix(classinDict_pertarray,new int[]{t}));

//                System.out.println("two_xt_ATX is: ");
//                xt_ATA_x.print(two_xt_ATX.getRowDimension(),two_xt_ATX.getColumnDimension());


                Matrix resultminus= xt_ATA_x.minus(two_xt_ATX);
                err[nclassindex]=resultminus.get(0,0);

                //System.out.println("resultminus is: "+resultminus.get(0,0));

                preddisterr[nclassindex][t]=err[nclassindex];
                nclassindex++;

            }
            int clsindex=0;
            for(int i=0;i<err.length;i++){
                if(err[i]<err[clsindex]) clsindex=i;
            }
            pred[t]=clsarray[clsindex];

//            System.out.println("err of "+t+" :");
//            for(int i=0;i<err.length;i++) System.out.print(err[i]+" ");
//            System.out.println();

            if(t%50==0)System.out.println("have done "+t+" examples of classker_pred");
        }

//            System.out.println("每个cls每个样本列的类索引：");
//            for(int i=0;i<kclsinDict.length;i++){
//                for(int j=0;j<kclsinDict[0].length;j++){
//                    System.out.print(kclsinDict[i][j]+" ");
//                }
//                System.out.println();
//            }

//            Matrix preddictmat= new Matrix(preddisterr);
//            System.out.println("preddisterr：");
//            preddictmat.print(preddictmat.getRowDimension(),preddictmat.getColumnDimension());

        return pred;
    }

        public static double classeval(int[]pred, int[]testlab){
            double OA=0;
            for(int i=0;i<pred.length;i++){
                   //System.out.println("pred标签"+pred[i]+" "+"testlab标签"+testlab[i]);
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



//    public static void main(String[] args) {
////        double[][] iwreparray= {{-1.,-2.},{-3.,-4.},{-5.,6.}};
////        int a=3;
////        Matrix iwrepmat = new Matrix(iwreparray);
////        double [][]result= Tools.soft(iwrepmat,a);
////        Matrix resultmat= new Matrix(result);
////        resultmat.print(resultmat.getRowDimension(),resultmat.getColumnDimension());
////        double normf = iwrepmat.normF();
////        System.out.println("normf:"+normf);
////          int t=1;
////          int []array = new int[]{t};
////        System.out.println(array[0]);
////        double[][] iwreparray= {{1.,2.},{3.,4.},{5.,6.}};
////        Matrix iwrepmat = new Matrix(iwreparray);
////        Matrix sub = iwrepmat.getMatrix(new int[]{0,1},new int[]{1}).transpose();
////        sub.print(sub.getRowDimension(),sub.getColumnDimension());
//    }
}

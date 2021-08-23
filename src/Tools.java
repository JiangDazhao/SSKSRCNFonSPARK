import Jama.Matrix;
import com.sun.org.apache.xpath.internal.functions.FuncFalse;

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

        public static int[] classker_pred(double[][]ATA,double[][]ATX,
                                             double[][]coef,
                                             int[]test_lab, int[]dict_lab){
//            System.out.println("dict_lab：");
//            for(int i=0;i<dict_lab.length;i++) System.out.print(dict_lab[i]+" ");
//            System.out.println();

            int [] pred= new int[test_lab.length];
            double [][]preddisterr;
            int [][] kclsinDict;
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

            nTest=test_lab.length;
            preddisterr=new double[nClass][nTest];
            for(int t=0;t<nTest;t++){ //each test
                double[] err= new double[nClass];
                int nclassindex=0;
                for(int cls:clsset){
                    kclsinDict=new int[dict_lab.length][nTest];
                    for (int j = 0; j < dict_lab.length; j++) {
                            if (dict_lab[j] == cls) kclsinDict[j][t] = 1;
                            else kclsinDict[j][t] = 0;
                    }

                    Matrix coefmat= new Matrix(coef);
                    //coefmat.print(coefmat.getRowDimension(),coefmat.getColumnDimension());

                    Matrix ATAmat = new Matrix(ATA);
                    Matrix ATXmat = new Matrix(ATX);

//                    Matrix selectdictmat = coefmat.getMatrix(kclassinDict,new int[]{t});
//                    selectdictmat.print(selectdictmat.getRowDimension(),selectdictmat.getColumnDimension());

                    int []classinDict_pert= new int[dict_lab.length];
                    for(int i=0;i<dict_lab.length;i++){
                        classinDict_pert[i]=kclsinDict[i][t];
                    }

                    Matrix xt_ATA_x= ((coefmat.getMatrix(classinDict_pert,new int[]{t}).transpose())
                                  .times(ATAmat.getMatrix(classinDict_pert,classinDict_pert)))
                                  .times(coefmat.getMatrix(classinDict_pert,new int[]{t}));
                    Matrix two_xt_ATX=((coefmat.getMatrix(classinDict_pert,new int[]{t}).transpose())
                                    .times(2))
                                    .times(ATXmat.getMatrix(classinDict_pert,new int[]{t}));
                    err[nclassindex]=xt_ATA_x.minus(two_xt_ATX).get(0,0);
                    preddisterr[nclassindex][t]=err[nclassindex];
                    nclassindex++;

                }
                int clsindex=0;
                for(int i=0;i<err.length;i++){
                    if(err[i]<err[clsindex]) clsindex=i;
                }
                pred[t]=clsarray[clsindex];
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


        public static Matrix ADMM(double[][]ATA,double[][]ATX,
                                      double mu, double lam){
                double tol=0.001;
                int maxit=1000;
                Matrix S= new Matrix(ATX);
                Matrix ATAmat = new Matrix(ATA);
                Matrix ATXmat = new Matrix(ATX);
                Matrix vmat = new Matrix(ATX.length,ATX[0].length);
                Matrix dmat =vmat;
                Matrix QuI= ATAmat.plus((Matrix.identity(ATA.length,ATA.length)).times(mu));
                Matrix Finverse= QuI.inverse();
                Matrix S0 = Finverse.times(ATXmat);
                for(int iter=0;iter<maxit;iter++){
                    S = Finverse.times(ATXmat.plus((vmat.plus(dmat)).times(mu)));
                    vmat = new Matrix(Tools.soft(S.minus(dmat),lam/mu));
                    dmat = dmat.minus(S.minus(vmat));

                    if(Tools.checkstop(iter,S,S0,tol)){
                        return S;
                    }
                    S0=S;
                }
                return S;
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
//        double sig=0.1;
//        double D=199.1;
//        double e=Math.E;
//        double order= -1.0/(2*sig*sig)*D;
//        double K= Math.pow(e,-900);
//        System.out.println(K);
//    }
}

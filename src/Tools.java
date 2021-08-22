import Jama.Matrix;
import com.sun.org.apache.xpath.internal.functions.FuncFalse;

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

        public static double[] classker_pred(double[][]ATA,double[][]ATX,
                                             double[][]coef,
                                             double[]test_lab, double[]dict_lab){

            return null;
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

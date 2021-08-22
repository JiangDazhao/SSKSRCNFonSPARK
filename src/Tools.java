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





//    public static void main(String[] args) {
//        double sig=0.1;
//        double D=199.1;
//        double e=Math.E;
//        double order= -1.0/(2*sig*sig)*D;
//        double K= Math.pow(e,-900);
//        System.out.println(K);
//    }
}

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

object postest {
  def main(args: Array[String]): Unit = {
    val conf= new SparkConf()
      .setAppName("JavaParallelTest")
      .setMaster("local")
      .set("spark.testing.memory", "2147480000")
    val sc=new SparkContext(conf)


    val totalblockidxRDD = sc.parallelize(
      Array((1,Array(1.toShort,2.toShort,3.toShort)),
        (2,Array(4.toShort,5.toShort,6.toShort)),
        (3,Array(7.toShort,8.toShort,9.toShort)),
        (4,Array(10.toShort,11.toShort,12.toShort))),
      4)
    val sorttotalblockidxRDD= totalblockidxRDD.sortByKey()

    val header= new HSIhdr("SSKSRCNF", "./resources/")
    val posclass=new PosCal(sorttotalblockidxRDD,header)
    posclass.process()
    val pos = posclass.getpos


    for (i<-0 to 11){
      for(j<-0 to 1){
        print(pos(i)(j)+" ")
      }
      println()
    }
  }
}

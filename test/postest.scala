import java.nio.charset.Charset

import com.csvreader.CsvWriter
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast

object postest {
  def main(args: Array[String]): Unit = {
    val conf= new SparkConf()
      .setAppName("JavaParallelTest")
      .setMaster("local[*]")
      .set("spark.testing.memory", "2147480000")
    val spark=new SparkContext(conf)



    // initialize img,img_gt,train,test,total info
    val alldata = new Data("Indian_pines_corrected.mat",
      "trainidx_540_61.mat",
      "testidx_450_61.mat",
      "Indian_gt.mat",
      "totalsample_990_61.mat")
    val broadimg2D: Broadcast[Array[Array[Double]]] =spark.broadcast((alldata.getImg2D))
    //initialize totallength
    val totallength=alldata.getTotallab.length

    //partition the totalblockbyteRDD
    val notblockdata=alldata.getTotalidx2D
    var array1= Array.ofDim[Short](248)
    for(i<-0 until(248)) array1(i)=notblockdata(i).toShort
    var array2= Array.ofDim[Short](248)
    for(i<-0 until(248)) array2(i)=notblockdata(248+i).toShort
    var array3= Array.ofDim[Short](248)
    for(i<-0 until(248)) array3(i)=notblockdata(496+i).toShort
    var array4= Array.ofDim[Short](notblockdata.length-744)
    for(i<-0 until(notblockdata.length-744)) array4(i)=notblockdata(744+i).toShort

    val notsortblockidxRDD = spark.parallelize(
      Array((0,array1),
        (248,array2),
        (496,array3),
        (744,array4)),
      4).cache()
    val totalblockidxRDD= notsortblockidxRDD.sortByKey()

    val header= new HSIhdr("SSKSRCNF", "./resources/")
    val posclass=new PosCal(totalblockidxRDD,header,totallength)
    posclass.process()
    val pos= posclass.getpos


//    var csvWriter = new CsvWriter("./out/sparkpos.csv", ',', Charset.forName("UTF-8"));
//    for(i<-0 until pos.length){
//      var onerow=new Array[String](2)
//      for(j<-0 until  2) {
//        onerow(j)=String.valueOf(pos(i)(j))
//      };
//      csvWriter.writeRecord(onerow);
//    }
//    csvWriter.close();


  }
}

import java.nio.charset.Charset

import com.csvreader.CsvWriter
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.{SparkConf, SparkContext}

object HadoopIjw2DWeightTest {
  def main(args: Array[String]): Unit = {
    val conf= new SparkConf()
      .setAppName("JavaParallelTest")
      .setMaster("local[*]")
      .set("spark.testing.memory", "2147480000")
    val spark=new SparkContext(conf)

    // initialize header info
    val header= new HSIhdr("SSKSRCNF", "./resources/")
    val bands=header.getBands
    val row=header.getRow
    val col =header.getCol
    val datatype= header.getDatatype
    val datainter=header.getInter
    val len=col*row

    val wind = 5
    val mu = 1e-3
    val lam = 1e-4
    val gam_K = 0.272990750165721 //sig
    val gam_w = 2.489353418393197e-04 //sig0s

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

    //parallel the pos calculation
    val posclass=new PosCal(totalblockidxRDD,header,totallength)
    posclass.process()
    val pos: Array[Array[Int]] = posclass.getpos

    //broadcast pos
    val broadpos: Broadcast[Array[Array[Int]]] = spark.broadcast(pos)

    //parallel the totalijw2D and totalijw_size
    val totalijw2Dclass= new Totalijw2DCal(totalblockidxRDD,broadpos,header,wind,totallength)
    totalijw2Dclass.process()
    val totalijw2D: Array[Array[Int]] =totalijw2Dclass.getTotalijw2D
    val totalijw2Dsize: Array[Int] =totalijw2Dclass.getTotalijw2DSize


    //broadcast ijw2D ijw2Dsize
    val broadijw2D: Broadcast[Array[Array[Int]]]=spark.broadcast(totalijw2D)
    val broadijw2Dsize:Broadcast[Array[Int]]=spark.broadcast(totalijw2Dsize)

    //parallel the totalijw2Dweight
    val totalijw2DweightClass= new IjwWeightCal(totalblockidxRDD,broadijw2D,broadijw2Dsize,
      broadimg2D,header,wind,gam_w,totallength)
    totalijw2DweightClass.process()
    val totalijw2Dweight: Array[Array[Double]] =totalijw2DweightClass.getIjw2dWeight

//    //putout
//    var csvWriter = new CsvWriter("./out/hadoopijw2D.csv", ',', Charset.forName("UTF-8"));
//    for(i<-0 until totalijw2D.length){
//      var onerow=new Array[String](totalijw2D(0).length)
//      for(j<-0 until onerow.length) {
//        onerow(j)=String.valueOf(totalijw2D(i)(j))
//      };
//      csvWriter.writeRecord(onerow);
//    }
//    csvWriter.close();
//
//    csvWriter = new CsvWriter("./out/hadoopijw2Dsize.csv", ',', Charset.forName("UTF-8"));
//      var onerow=new Array[String](totalijw2Dsize.length)
//      for(j<-0 until onerow.length)
//        onerow(j)=String.valueOf(totalijw2Dsize(j))
//      csvWriter.writeRecord(onerow);
//    csvWriter.close();

//    var csvWriter = new CsvWriter("./out/hadoopijwweight.csv", ',', Charset.forName("UTF-8"));
//    for(i<-0 until totalijw2Dweight.length){
//      var onerow=new Array[String](totalijw2Dweight(0).length)
//      for(j<-0 until onerow.length) {
//        onerow(j)=String.valueOf(totalijw2Dweight(i)(j))
//      };
//      csvWriter.writeRecord(onerow);
//    }
//    csvWriter.close();


  }
}

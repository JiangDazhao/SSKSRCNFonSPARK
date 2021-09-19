import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date

import com.csvreader.CsvWriter
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.{SparkConf, SparkContext}

object HadoopKtotalTest {
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
    //set data format
    val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    println("start time:" + df.format(new Date))

    // initialize img,img_gt,train,test,total info
    val alldata = new Data("Indian_pines_corrected.mat",
      "trainidx_540_61.mat",
      "testidx_450_61.mat",
      "Indian_gt.mat",
      "totalsample_990_61.mat")
    val broadimg2D: Broadcast[Array[Array[Double]]] =spark.broadcast((alldata.getImg2D))
    //initialize totallength
    val totallength=alldata.getTotallab.length

    //broadcast trainidx
    val inttrainidx2D=alldata.getTrainidx2D
    val shorttrainidx2D=Array.ofDim[Short](inttrainidx2D.length)
    for(i<-0 until(inttrainidx2D.length))
      shorttrainidx2D(i)=inttrainidx2D(i).toShort
    val broadtrainidx: Broadcast[Array[Short]] =spark.broadcast((shorttrainidx2D))

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

    val t3 = System.currentTimeMillis
    println("ker_lwm start time:"+df.format(new Date))

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

    //broadcast totalijw2Dweight
    val broadijw2Dweight: Broadcast[Array[Array[Double]]] =spark.broadcast(totalijw2Dweight)

    //parallel the Ktotal
    val KtotalClass= new KtotalCal(totalblockidxRDD,broadijw2D,broadijw2Dsize,broadimg2D,
      broadijw2Dweight,broadtrainidx,header,gam_K,totallength)
    KtotalClass.process()
    val Ktotal: Array[Array[Double]] =KtotalClass.getKtotal

    val t4 = System.currentTimeMillis
    println("ker_lwm end time:"+df.format(new Date))
    println("ker_lwm time:" + (t4 - t3) * 1.0 / 1000 + "s")


    //Ktrain and Ktest
    val Ktraincollen=alldata.getTrainidx2D.length
    val Ktestcollen=totallength-Ktraincollen
    val Ktrain= Array.ofDim[Double](Ktraincollen,Ktraincollen)
    val Ktest=Array.ofDim[Double](Ktraincollen,totallength-Ktestcollen)
    for(i<-0 until(Ktraincollen))
      for (j<-0 until(Ktraincollen))
        Ktrain(j)(i)=Ktotal(j)(i)
    for(i<-0 until(Ktestcollen))
      for(j<-0 until(Ktraincollen))
        Ktest(j)(i)=Ktotal(j)(Ktraincollen+i)

    //ADMM
    val t5 = System.currentTimeMillis
    println("ADMM start time:"+df.format(new Date))
    val S = Tools.ADMM(Ktrain, Ktest, mu, lam)
    val t6 = System.currentTimeMillis
    println("ADMM end time:"+df.format(new Date))
    println("ADMM time:" + (t6- t5) * 1.0 / 1000 + "s")

    //pred
    val testlab=alldata.getTestlab
    val trainlab=alldata.getTrainlab
    var pred = Tools.classker_pred(Ktrain, Ktest, S.getArrayCopy, testlab, trainlab)

    //OA
    val OA = Tools.classeval(pred, testlab)
    System.out.println("Overall Accuracy:%.2f%%".format(OA))
    System.out.println("End time:" + df.format(new Date))

//    //putout
//    var csvWriter = new CsvWriter("./out/hadoopktotal.csv", ',', Charset.forName("UTF-8"));
//    for(i<-0 until Ktotal.length){
//      var onerow=new Array[String](Ktotal(0).length)
//      for(j<-0 until onerow.length) {
//        onerow(j)=String.valueOf(Ktotal(i)(j))
//      }
//      csvWriter.writeRecord(onerow);
//    }
//    csvWriter.close();


  }
}

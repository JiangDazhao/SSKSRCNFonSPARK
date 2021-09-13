import java.nio.charset.Charset

import com.csvreader.CsvWriter
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.{SparkConf, SparkContext}


/*
args(0)   集群运行方式 local
args(1)   job名称
args(2)   文件存放方式，如在hdfs中，
          则写入对应hdfs的位置,如hdfs://10.10.10.47:9000
          若是本地文件，则写file://
args(3)   文件主要名称

 */

object SSKSRCNFmain {
  def main(args: Array[String]): Unit = {
    val exetype= args(0)
    val jobname=args(1)   //jobname and the filename
    val filepath=args(2)  //the hadoop directory of all the data

    val  conf= new SparkConf().setMaster(exetype).setAppName(jobname).set("spark.testing.memory", "2147480000")
    val spark= new SparkContext(conf)

    // initialize header info
    val header= new HSIhdr(jobname, filepath)
    val bands=header.getBands
    val row=header.getRow
    val col =header.getCol
    val datatype= header.getDatatype
    val datainter=header.getInter
    val len=col*row

    //static parameter
    val wind = 5
    val mu = 1e-3
    val lam = 1e-4
    val gam_K = 0.272990750165721 //sig
    val gam_w = 2.489353418393197e-04 //sig0s

    // initialize img,img_gt,train,test,total info
    val alldata = new ByteData(jobname,filepath,bands,row,col,datatype)
    //broadcast img2D
    val broadimg2D: Broadcast[Array[Array[Double]]] =spark.broadcast((alldata.getImg2D))

    //partition the totalblockbyteRDD
    val totalpath=filepath+jobname+"_total"
    val totalblockbyteRDD=spark.newAPIHadoopFile(totalpath,classOf[DataInputFormat],classOf[Integer],classOf[Array[Byte]])

    //totalblockbyteRDD to totalblockidxRDD
    val totalblockidxRDD
      =totalblockbyteRDD.map(pair=>{
        val key=pair._1/2
        val blockidx=Tools.Bytetoidx(pair._2,2)
        (key,blockidx)
    }
    ).cache()


    //parallel the pos calculation
    val posclass=new PosCal(totalblockidxRDD,header)
    posclass.process()
    val pos: Array[Array[Int]] = posclass.getpos

    //broadcast pos
    val broadpos: Broadcast[Array[Array[Int]]] = spark.broadcast(pos)

    //parallel the totalijw2D and totalijw_size
    val totalijw2Dclass= new Totalijw2DCal(totalblockidxRDD,broadpos,header,wind)
    totalijw2Dclass.process()
    val totalijw2D: Array[Array[Int]] =totalijw2Dclass.getTotalijw2D
    val totalijw2Dsize: Array[Int] =totalijw2Dclass.getTotalijw2DSize


    //broadcast ijw2D ijw2Dsize
    val broadijw2D: Broadcast[Array[Array[Int]]]=spark.broadcast(totalijw2D)
    val broadijw2Dsize:Broadcast[Array[Int]]=spark.broadcast(totalijw2Dsize)

    //parallel the totalijw2Dweight
    val totalijw2DweightClass= new IjwWeightCal(totalblockidxRDD,broadijw2D,broadijw2Dsize,
      broadimg2D,header,wind,gam_w)
    totalijw2DweightClass.process()
    val totalijw2Dweight: Array[Array[Double]] =totalijw2DweightClass.getIjw2dWeight











    //    var csvWriter = new CsvWriter("./out/sparktotalijw2Dweight.csv", ',', Charset.forName("UTF-8"));
//    for(i<-0 until totalijw2Dweight.length){
//      var onerow=new Array[String](totalijw2Dweight(0).length)
//      for(j<-0 until onerow.length) {
//        onerow(j)=String.valueOf(totalijw2Dweight(i)(j))
//      };
//      csvWriter.writeRecord(onerow);
//    }
//    csvWriter.close();


//    var csvWriter = new CsvWriter("./out/sparktotalijw2D.csv", ',', Charset.forName("UTF-8"));
//    for(i<-0 until totalijw2D.length){
//      var onerow=new Array[String](totalijw2D(0).length)
//      for(j<-0 until onerow.length) {
//        onerow(j)=String.valueOf(totalijw2D(i)(j))
//      };
//      csvWriter.writeRecord(onerow);
//    }
//    csvWriter.close();
//
//    csvWriter = new CsvWriter("./out/totalijw2Dsize.csv", ',', Charset.forName("UTF-8"));
//    var onerow=new Array[String](totalijw2Dsize.length)
//    for(i<-0 until onerow.length) onerow(i)=String.valueOf(totalijw2Dsize(i));
//    csvWriter.writeRecord(onerow);
//      csvWriter.close();

  }
}


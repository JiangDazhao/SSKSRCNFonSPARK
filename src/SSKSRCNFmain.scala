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

    // initialize img,img_gt,train,test,total info
    val alldata = new ByteData(jobname,filepath,bands,row,col,datatype)
    val bdata=spark.broadcast((alldata.getImg2D))

    //partition the totalidx
    val totalpath=filepath+jobname+"_total"
    val totalblockbyte=spark.newAPIHadoopFile(totalpath,classOf[DataInputFormat],classOf[Integer],classOf[Array[Byte]])

    val totalblockidx
      =totalblockbyte.map(pair=>{
        val key=pair._1/2
        val blockidx=Tools.Bytetoidx(pair._2,2)
        (key,blockidx)
    }
    ).cache()

    //parallel the pos calculation
    val posclass=new PosCal(totalblockidx,bdata,header)
    val pos = posclass.getpos





  }
}


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

    var pos = Array.ofDim[Int](12, 2)
    pos=totalblockidxRDD.map(pair=>{
      val blockidx=pair._2
      val blockpos = Tools.blockPosCal(blockidx,145)

      (blockpos)
    }
    ).reduce((right,left)=>{
      val problockpos1=right
      val problockpos2=left
      val problockpos1_len=problockpos1.length
      val problockpos2_len=problockpos2.length
      var prosumpos=Array.ofDim[Int](problockpos1_len+problockpos2_len,2)
      for (i<- 0 until problockpos1_len)
        prosumpos(i)= problockpos1(i)
      for(i<-0 until problockpos2_len)
        prosumpos(problockpos1_len+i)=problockpos2(i)

      (prosumpos)
    }
    )
    for (i<-0 to 11){
      for(j<-0 to 1){
        print(pos(i)(j)+" ")
      }
      println()
    }
  }
}

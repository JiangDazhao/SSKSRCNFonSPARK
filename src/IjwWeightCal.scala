import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

class IjwWeightCal(val totalblockidx:RDD[(Int,Array[Short])],
                   val broadijw2D:Broadcast[Array[Array[Int]]],val broadijw2Dsize:Broadcast[Array[Int]],
                   val broadimg2D:Broadcast[Array[Array[Double]]] ,header:HSIhdr,val wind:Int,val gam_w:Double) extends Serializable {
  private var totalijw2Dweight:Array[Array[Double]]=_
  private val ijw2D=broadijw2D.value
  private val ijw2Dsize=broadijw2Dsize.value
  private val img2D=broadimg2D.value
  private val bandnum=header.getBands
  private val nwind=(2 * wind + 1) * (2 * wind + 1)

  private [this] def getIjw2dWeightP:Unit={
    totalijw2Dweight=totalblockidx.map(pair=>{
    val offset = pair._1
    val blockidx=pair._2
    val blockijwweight=Tools.blockijw2dWeightCal(blockidx,ijw2D,ijw2Dsize,
      img2D,offset,bandnum,gam_w,wind);
    (blockijwweight)
    }
    ).reduce((left,right)=>{
      val problockijwweight1=left
      val problockijwweight2=right
      val problockijwweight1_len=problockijwweight1(0).length
      val problockijwweight2_len=problockijwweight2(0).length
      var prosumijwweight=Array.ofDim[Double](nwind, problockijwweight1_len+problockijwweight2_len)
      println("left和right长度"+problockijwweight1_len+" "+problockijwweight2_len)
      for (i<- 0 until problockijwweight1_len){
        for(j<-0 until nwind){
          prosumijwweight(j)(i)=problockijwweight1(j)(i)
        }
      }
      for (i<- 0 until problockijwweight2_len){
        for(j<-0 until nwind){
          prosumijwweight(j)(problockijwweight1_len+i)=problockijwweight2(j)(i)
        }
      }
      (prosumijwweight)
    }
    )
  }
  def process():Unit={
    getIjw2dWeightP
  }

  def getIjw2dWeight=totalijw2Dweight
}

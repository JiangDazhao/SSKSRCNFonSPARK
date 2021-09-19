import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

class IjwWeightCal(val totalblockidx:RDD[(Int,Array[Short])],
                   val broadijw2D:Broadcast[Array[Array[Int]]],val broadijw2Dsize:Broadcast[Array[Int]],
                   val broadimg2D:Broadcast[Array[Array[Double]]] ,
                   header:HSIhdr,val wind:Int,val gam_w:Double,val totallength:Int) extends Serializable {
  private var mapijw2Dweight:RDD[(Int,Array[Array[Double]])]=_
  private var sortijw2Dweight:Array[(Int,Array[Array[Double]])]=_
  private var ijw2Dweight:Array[Array[Double]]=_
  private val ijw2D=broadijw2D.value
  private val ijw2Dsize=broadijw2Dsize.value
  private val img2D=broadimg2D.value
  private val bandnum=header.getBands
  private val nwind=(2 * wind + 1) * (2 * wind + 1)

  private [this] def getIjw2dWeightP:Unit={
    //map
    mapijw2Dweight=totalblockidx.map(pair=>{
    val offset = pair._1
    val blockidx=pair._2
    val blockijwweight=Tools.blockijw2dWeightCal(blockidx,ijw2D,ijw2Dsize,
      img2D,offset,bandnum,gam_w,wind);
    (offset,blockijwweight)
    }
    )

    //aggregate
    sortijw2Dweight=mapijw2Dweight.sortByKey().collect()
    ijw2Dweight=Array.ofDim[Double](nwind,totallength)
    //ijw2dweight
    for(i<-0 until(sortijw2Dweight.length)){
      val blockoffset=sortijw2Dweight(i)._1
      val blockijw2Dweight=sortijw2Dweight(i)._2
      val blockijw2Dweight_collen=blockijw2Dweight(0).length
      //ijw2dweight
      for(m<-0 until(blockijw2Dweight_collen))
        for(n<-0 until(nwind))
          ijw2Dweight(n)(blockoffset+m)=blockijw2Dweight(n)(m)
    }

  }
  def process():Unit={
    getIjw2dWeightP
  }

  def getIjw2dWeight=ijw2Dweight
}

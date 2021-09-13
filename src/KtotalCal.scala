import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

class KtotalCal(val totalblockidx:RDD[(Int,Array[Short])],
                val broadijw2D:Broadcast[Array[Array[Int]]],
                val broadijw2Dsize:Broadcast[Array[Int]],
                val broadimg2D:Broadcast[Array[Array[Double]]] ,
                val broadijw2Dweight:Broadcast[Array[Array[Double]]],
                val broadtrainidx:Broadcast[Array[Short]],
                header:HSIhdr,val gam_K:Double) extends Serializable {
  private var Ktotal:Array[Array[Double]]=_
  private val ijw2D=broadijw2D.value
  private val ijw2Dsize=broadijw2Dsize.value
  private val img2D=broadimg2D.value
  private val ijw2Dweight=broadijw2Dweight.value
  private val Ktotalrow=broadtrainidx.value.length
  private val bandnum=header.getBands

  private [this] def getKtotalP:Unit={
    Ktotal=totalblockidx.map(pair=>{
      val offset = pair._1
      val blockidx=pair._2
      val blockKtotal=Tools.blockKtotalcal(blockidx,ijw2D,ijw2Dsize,
        img2D,ijw2Dweight,Ktotalrow,offset,bandnum,gam_K)
      println("blockKtotal "+offset+" has been done...")
      (blockKtotal)
    }
    ).reduce((left,right)=>{
      val problockKtotal1=left
      val problockKtotal2=right
      val problockKtotal1_len=problockKtotal1(0).length
      val problockKtotal2_len=problockKtotal2(0).length
      var prosumKtotal=Array.ofDim[Double](Ktotalrow, problockKtotal1_len+problockKtotal2_len)
      for (i<- 0 until problockKtotal1_len){
        for(j<-0 until Ktotalrow){
          prosumKtotal(j)(i)=problockKtotal1(j)(i)
        }
      }
      for (i<- 0 until problockKtotal2_len){
        for(j<-0 until Ktotalrow){
          prosumKtotal(j)(problockKtotal1_len+i)=problockKtotal2(j)(i)
        }
      }
      (prosumKtotal)
    }
    )
  }
  def process():Unit={
    getKtotalP
  }

  def getKtotal=Ktotal
}

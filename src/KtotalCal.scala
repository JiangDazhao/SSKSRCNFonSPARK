import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

class KtotalCal(val totalblockidx:RDD[(Int,Array[Short])],
                val broadijw2D:Broadcast[Array[Array[Int]]],
                val broadijw2Dsize:Broadcast[Array[Int]],
                val broadimg2D:Broadcast[Array[Array[Double]]] ,
                val broadijw2Dweight:Broadcast[Array[Array[Double]]],
                val broadtrainidx:Broadcast[Array[Short]],
                header:HSIhdr,val gam_K:Double,val totallength:Int) extends Serializable {
  private var mapKtotal:RDD[(Int,Array[Array[Double]])]=_
  private var sortmapKtotal:Array[(Int,Array[Array[Double]])]=_
  private var Ktotal:Array[Array[Double]]=_
  private val ijw2D=broadijw2D.value
  private val ijw2Dsize=broadijw2Dsize.value
  private val img2D=broadimg2D.value
  private val ijw2Dweight=broadijw2Dweight.value
  private val Ktotalrow=broadtrainidx.value.length
  private val bandnum=header.getBands

  private [this] def getKtotalP:Unit={
    //map
    mapKtotal=totalblockidx.map(pair=>{
      val offset = pair._1
      val blockidx=pair._2
      val blockKtotal=Tools.blockKtotalcal(blockidx,ijw2D,ijw2Dsize,
        img2D,ijw2Dweight,Ktotalrow,offset,bandnum,gam_K)
      println("blockKtotal #"+offset+" has been done...")
      (offset,blockKtotal)
    }
    ).cache()

    //aggregate
    sortmapKtotal=mapKtotal.sortByKey().collect()
    println("finished collect")
    Ktotal=Array.ofDim[Double](Ktotalrow,totallength)
    //Ktotal
    for(i<-0 until(sortmapKtotal.length)){
      val blockoffset=sortmapKtotal(i)._1
      val blockKtotal=sortmapKtotal(i)._2
      val blockKtotal_collen=blockKtotal(0).length
      for(m<-0 until(blockKtotal_collen))
        for(n<-0 until(Ktotalrow))
          Ktotal(n)(blockoffset+m)=blockKtotal(n)(m)
    }
    println("finished aggregate")
  }
  def process():Unit={
    getKtotalP
  }

  def getKtotal=Ktotal
}

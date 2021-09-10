import Jama.Matrix
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD


class Totalijw2DCal(val totalblockidx:RDD[(Int,Array[Short])],val broadpos:Broadcast[Array[Array[Int]]] ,header:HSIhdr,val wind:Int) extends Serializable {
  private var totalijw2DTuple:(Array[Array[Int]],Array[Int])=_ //(totalijw2D,totalijw2Dsize)
  private val rownum=header.getRow
  private val colnum=header.getCol
  private val pos= broadpos.value
  private val windin= wind
  private val nwind=(2 * wind + 1) * (2 * wind + 1)
  private [this] def getAboutTotalijw2D:Unit={
    totalijw2DTuple=totalblockidx.map(pair=>{
      val blockidx=pair._2
      val tuple= Tools.blockIjw2DCal(blockidx,pos,rownum,colnum,windin)
      val blockijw2D=tuple._1
      val blockijwsize=tuple._2
        (blockijw2D,blockijwsize)
    }
    ).reduce((left,right)=>{
      val problockijw2d11=left._1
      val problockijw2d12=left._2
      val problockijw2d21=right._1
      val problockijw2d22=right._2
      val problockijw2d11_len=left._1(0).length
      val problockijw2d12_len=left._2.length
      val problockijw2d21_len=right._1(0).length
      val problockijw2d22_len=right._2.length
      var prosumijw2d=Array.ofDim[Int](nwind, problockijw2d11_len+problockijw2d21_len)
      var prosumijw2dsize=Array.ofDim[Int](problockijw2d12_len+problockijw2d22_len)

      //prosumijw2d
      for (i<- 0 until problockijw2d11_len){
          for(j<-0 until nwind){
            prosumijw2d(j)(i)=problockijw2d11(j)(i)
          }
      }
      for (i<- 0 until problockijw2d21_len){
        for(j<-0 until nwind){
          prosumijw2d(j)(problockijw2d11_len+i)=problockijw2d21(j)(i)
        }
      }

      //prosumijw2dsize
      for(i<-0 until problockijw2d12_len)
        prosumijw2dsize(i)=problockijw2d12(i)
      for(i<-0 until problockijw2d22_len)
        prosumijw2dsize(problockijw2d12_len+i)=problockijw2d22(i)

      (prosumijw2d,prosumijw2dsize)
    }
    )
  }

  def process():Unit={
    getAboutTotalijw2D
  }

  def getTotalijw2D=totalijw2DTuple._1
  def getTotalijw2DSize=totalijw2DTuple._2




}

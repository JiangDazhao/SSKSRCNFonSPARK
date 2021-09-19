import Jama.Matrix
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD


class Totalijw2DCal(val totalblockidx:RDD[(Int,Array[Short])],
                    val broadpos:Broadcast[Array[Array[Int]]] ,
                    header:HSIhdr,val wind:Int, val totallength:Int) extends Serializable {

  private var mapijw2DTuple:RDD[(Int,Array[Array[Int]],Array[Int])]=_
  private var sortmapijw2DTuple: Array[(Int,Array[Array[Int]],Array[Int])] =_
  private var ijw2D:Array[Array[Int]]=_
  private var ijw2Dsize:Array[Int]=_
  private val rownum=header.getRow
  private val colnum=header.getCol
  private val pos= broadpos.value
  private val windin= wind
  private val nwind=(2 * wind + 1) * (2 * wind + 1)
  private [this] def getAboutTotalijw2D:Unit={
    //map
    mapijw2DTuple=totalblockidx.map(pair=>{
      val offset = pair._1
      val blockidx=pair._2
      val tuple= Tools.blockIjw2DCal(blockidx,pos,offset,rownum,colnum,windin)
      val blockijw2D=tuple._1
      val blockijwsize=tuple._2
        (offset,blockijw2D,blockijwsize)
    }
    )

    //aggregate
    sortmapijw2DTuple=mapijw2DTuple.sortBy(_._1).collect()
    ijw2D=Array.ofDim[Int](nwind,totallength)
    ijw2Dsize=Array.ofDim[Int](totallength)
    //ijw2d
    for(i<-0 until(sortmapijw2DTuple.length)){
      val blockoffset=sortmapijw2DTuple(i)._1
      val blockijw2D=sortmapijw2DTuple(i)._2
      val blockijwsize=sortmapijw2DTuple(i)._3
      val blockijw2D_collen=blockijw2D(0).length
      //ijw2d
      for(m<-0 until(blockijw2D_collen))
        for(n<-0 until(nwind))
          ijw2D(n)(blockoffset+m)=blockijw2D(n)(m)
      //ijw2dsize
      for(m<-0 until(blockijw2D_collen))
        ijw2Dsize(blockoffset+m)=blockijwsize(m)
    }
  }


  def process():Unit={
    getAboutTotalijw2D
  }

  def getTotalijw2D=ijw2D
  def getTotalijw2DSize=ijw2Dsize




}

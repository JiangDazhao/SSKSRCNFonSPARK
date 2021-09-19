import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

class PosCal(val totalblockidx:RDD[(Int,Array[Short])], header:HSIhdr,val totallength:Int) extends Serializable {
  private var mappos:RDD[(Int,Array[Array[Int]])]=_
  private var sortmappos:Array[(Int,Array[Array[Int]])]=_
  private var pos:Array[Array[Int]]=_
  private val rownum=header.getRow

  private[this] def getPOS:Unit={
    //map
    mappos=totalblockidx.map(pair=>{
      val offset=pair._1
      val blockidx=pair._2
      val blockpos = Tools.blockPosCal(blockidx,rownum)
      (offset,blockpos)
    }
    )

    //aggregate
    sortmappos=mappos.sortByKey().collect()
    pos=Array.ofDim[Int](totallength,2)
    for(i<-0 until(sortmappos.length)){
      val blockoffset=sortmappos(i)._1
      val blockmappos=sortmappos(i)._2
      for(m<-0 until(blockmappos.length)){
        for(n<- 0 until(2)){
          pos(blockoffset+m)(n)=blockmappos(m)(n)
        }
      }
    }
  }

  def process():Unit={
    getPOS
  }

  def getpos=pos

}

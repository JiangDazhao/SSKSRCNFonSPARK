import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

class PosCal(val totalblockidx:RDD[(Int,Array[Short])],val bdata:Broadcast[Array[Array[Double]]], header:HSIhdr) {
  private var pos:Array[Array[Int]]=_


  private[this] def getPOS:Unit={
    pos=totalblockidx.map(pair=>{
        val blockidx=pair._2
        val blockpos = Tools.blockPosCal(blockidx,header.getRow)

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
  }


  def process():Unit={
    getPOS
  }

  def getpos=pos


}

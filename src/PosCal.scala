import org.apache.spark.rdd.RDD

class PosCal(val totalblockidx:RDD[(Int,Array[Short])],header:HSIhdr) extends Serializable {
  private var pos:Array[Array[Int]]=_
  private val rownum=header.getRow

  private[this] def getPOS:Unit={
    pos=totalblockidx.map(pair=>{
        val blockidx=pair._2
        val blockpos = Tools.blockPosCal(blockidx,rownum)

      (blockpos)
    }
    ).reduce((left,right)=>{
        val problockpos1=left
        val problockpos2=right

        val problockpos1_len=problockpos1.length
        val problockpos2_len=problockpos2.length
        var prosumpos=Array.ofDim[Int](problockpos1_len+problockpos2_len,2)
        for (i<- 0 until problockpos1_len){
          prosumpos(i)(0)= problockpos1(i)(0)
          prosumpos(i)(1)= problockpos1(i)(1)
        }
        for(i<-0 until problockpos2_len){
          prosumpos(problockpos1_len+i)(0)=problockpos2(i)(0)
          prosumpos(problockpos1_len+i)(1)=problockpos2(i)(1)
        }

      (prosumpos)
    }
    )
  }


  def process():Unit={
    getPOS
  }

  def getpos=pos


}

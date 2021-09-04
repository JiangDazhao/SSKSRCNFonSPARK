import com.csvreader.CsvWriter;

import java.io.*;
import java.nio.charset.Charset;

public class ReadByte {

    public static void main(String[] args) throws IOException {
        byte[] data = null;
        File src = new File("C:\\Users\\27801\\Desktop\\校内科研\\SSKSRCNFonSPARK\\resources\\SSKSRCNF_img");
//        DataInputStream din =new DataInputStream(
//                new BufferedInputStream(
//                        new FileInputStream(src)
//                )
//        );
//        BufferedReader read = new BufferedReader(new InputStreamReader(din));

        InputStream is = new FileInputStream(src);
        int size= is.available();
        data = new byte[size];
        for(int i=0;i<size;i++){
            data[i]= (byte) is.read();
        }
        int bands=200;
        int datasize=2;
        int len;
        len=data.length;
        int pixel=len/(datasize*bands);

        short[][] sdata=new short[pixel][bands];
        int n=0;
        for(int i=0;i<pixel;i++){
            for(int j=0;j<bands;j++) {
                //两位byte转short
                sdata[i][j]=(short)((data[n++]&0xff) | (data[n++] <<8));
            }
        }

        CsvWriter csvWriter = new CsvWriter("./out/shortread.csv", ',', Charset.forName("UTF-8"));
        for(int i=0;i<pixel;i++){
            String[] onerow=new String[bands];
            for(int j=0;j<bands;j++){
                onerow[j]=String.valueOf(sdata[i][j]);
            }
            csvWriter.writeRecord(onerow);
        }
        csvWriter.close();
    }

}

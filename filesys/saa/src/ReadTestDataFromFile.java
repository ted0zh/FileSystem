import java.util.ArrayList;
public class ReadTestDataFromFile{
        public void readTestDataFromFile(){

            FileHandler fh = new FileHandler();
            ArrayList<MetaData> result = fh.getAllMetadata();
            for(MetaData md: result){
                System.out.println(Utils.metaDataToString(md));
            }

        }
}

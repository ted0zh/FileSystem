import java.io.Serializable;

public class MetaData implements Serializable  {

    public String fileName;
    public int fileId;
    public int parentId;
    public boolean isDirectory;
    public int fileStart;
    public int fileEnd;
    public MetaData(){}

}

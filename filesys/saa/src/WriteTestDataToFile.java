import java.util.ArrayList;
public class WriteTestDataToFile {

    public static final int LOGICAL_SEPARATOR = 0xff;

    public void writeTestDataToFile(){


        MetaData metaData = new MetaData();
        metaData.fileName = "root";
        metaData.fileStart = 0;
        metaData.fileEnd = 0;
        metaData.fileId = Utils.ROOT_FOLDER_ID;
        metaData.parentId = 0;
        metaData.isDirectory = true;

        MetaData metaData2 = new MetaData();
        metaData2.fileName = "FileInRootFolder.txt";
        metaData2.fileStart = 1000;
        metaData2.fileEnd = 2000;
        metaData2.fileId = 2;
        metaData2.parentId = 1;
        metaData2.isDirectory = false;

        ArrayList<MetaData> objects = new ArrayList<>();
        objects.add(metaData);
        objects.add(metaData2);

        FileHandler fileHandler = new FileHandler();
        fileHandler.writeMetaData(objects);

        System.out.println("File created successfully!");

    }
}

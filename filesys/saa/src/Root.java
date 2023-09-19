import java.util.ArrayList;

public class Root {
    public void create() {

        MetaData metaData = new MetaData();
        metaData.fileName = "root";
        metaData.fileStart = 0;
        metaData.fileEnd = 0;
        metaData.fileId = Utils.ROOT_FOLDER_ID;
        metaData.parentId = 0;
        metaData.isDirectory = true;

        ArrayList<MetaData> objects = new ArrayList<>();
        objects.add(metaData);


        FileHandler fileHandler = new FileHandler();
        fileHandler.writeMetaData(objects);

        System.out.println("File created successfully!");
    }
}

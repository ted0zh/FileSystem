public class Utils {
    public static final int LOGICAL_SEPARATOR = 0xff;
    public static final String containerName = "container";
    public static final int MAX_METADATA_SIZE = 1000000;
    public static final int CONTAINER_MAX_SIZE = 1000000000;
    public static final int ROOT_FOLDER_ID = 1;

    public static String metaDataToString(MetaData obj) {
        StringBuffer sb = new StringBuffer();
        sb.append("File name: ").append(obj.fileName).append("\n")
                .append("fileId: ").append(obj.fileId).append("\n")
                .append("parentId: ").append(obj.parentId).append("\n")
                .append("isDirectory: ").append(obj.isDirectory).append("\n")
                .append("fileStart: ").append(obj.fileStart).append("\n")
                .append("fileEnd: ").append(obj.fileEnd).append("\n");
        return sb.toString();
    }
}



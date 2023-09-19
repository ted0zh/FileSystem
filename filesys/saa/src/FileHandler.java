import java.io.*;
import java.nio.file.Path;
import java.util.Collections;
import java.util.ArrayList;

public class FileHandler  {

    public Path path;
    public File file;
    public ByteConverter<MetaData> byteConverter;
    public FileHandler(){
        this.byteConverter = new ByteConverter<MetaData>();
        try {
            String currentPath = new File(".").getCanonicalPath();
            this.path = Path.of(currentPath + "\\" + Utils.containerName);
            this.file = path.toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<MetaData> getAllMetadata(){
        ArrayList<MetaData> foundObjects = new ArrayList<>();
        try {
            ArrayList<Integer> bytesTemp = new ArrayList<>();

            FileInputStream fileInputStream = new FileInputStream(file);
            int value = 0;
            int continuousSeparators = 0;
            while (true){
                value = fileInputStream.read();
                if(value == -1){
                    break;
                }
                else if(value == WriteTestDataToFile.LOGICAL_SEPARATOR){
                    continuousSeparators++;
                    if(continuousSeparators >= 2){
                        break;
                    }
                    MetaData tempMetaData = this.byteConverter.convertFromBytes(bytesTemp);
                    foundObjects.add((tempMetaData));
                    bytesTemp = new ArrayList<>();
                }
                else {
                    bytesTemp.add(value);
                    continuousSeparators = 0;
                }
            }

            fileInputStream.close();

        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }

        return foundObjects;
    }

    public void writeMetaData(ArrayList<MetaData> objects) {
        try {
            int bytesWritten = 0;
            RandomAccessFile raf = new RandomAccessFile(this.file, "rw");
            for (MetaData object : objects) {
                byte[] byteArray = this.byteConverter.convertToBytes(object);
                bytesWritten = bytesWritten + byteArray.length;
                if(bytesWritten > Utils.MAX_METADATA_SIZE - 2){
                    throw new RuntimeException("Metadata block has exceeded it's size");
                }
                raf.write(byteArray);
                raf.write(Utils.LOGICAL_SEPARATOR);
            }
            raf.write(Utils.LOGICAL_SEPARATOR);
            raf.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<FilePosition> getTakenPositionsArray(ArrayList<MetaData> metadataArray){
        ArrayList<FilePosition> takenPositions = new ArrayList<>();
        for(MetaData md: metadataArray){
            if(!md.isDirectory){
                if(md.fileStart >= Utils.MAX_METADATA_SIZE
                    && md.fileEnd >= Utils.MAX_METADATA_SIZE
                    && md.fileEnd >= md.fileStart
                ){
                  takenPositions.add(new FilePosition(md.fileStart, md.fileEnd));
                }
            }
        }
        return takenPositions;
    }

    public FilePosition findAvailableSpace(ArrayList<FilePosition> takenPositions, int fileSize){
        this.sortFilePositions(takenPositions);

        ArrayList<FilePosition> availableSpaces = new ArrayList<>();

        if(takenPositions.isEmpty()){
            availableSpaces.add(
                    new FilePosition(Utils.MAX_METADATA_SIZE+1, Utils.CONTAINER_MAX_SIZE)
            );
        }
        else {
            if (takenPositions.get(0).start != Utils.MAX_METADATA_SIZE + 1) {
                FilePosition firstSpace = new FilePosition(
                        Utils.MAX_METADATA_SIZE + 1,
                        takenPositions.get(0).start + 1
                );
                availableSpaces.add(firstSpace);
            }

            for (int i = 0; i < takenPositions.size() - 1; i++) {
                FilePosition freeSpace = new FilePosition(
                        takenPositions.get(i).end - 1,
                        takenPositions.get(i + 1).start + 1
                );
                availableSpaces.add(freeSpace);
            }

            if (takenPositions.get(takenPositions.size() - 1).end != Utils.CONTAINER_MAX_SIZE) {
                FilePosition lastPosition = new FilePosition(
                        takenPositions.get(takenPositions.size() - 1).end + 1,
                        Utils.CONTAINER_MAX_SIZE
                );
                availableSpaces.add(lastPosition);
            }
        }
        for(FilePosition fp: availableSpaces){
            int positionMaxSize = fp.end - fp.start;
            if(positionMaxSize > fileSize){
                return new FilePosition(
                        fp.start,
                        fp.start + fileSize
                );
            }
        }

        System.out.println("Space for file not found!");

        return null;
    }

    private ArrayList<FilePosition> sortFilePositions(ArrayList<FilePosition> positions){
        for(int i=0; i<positions.size()-1; i++){
            for(int j=0; j<positions.size()-1; j++){
                if(positions.get(j).start > positions.get(j+1).start){
                    Collections.swap(positions, j, j+1);
                }
            }
        }
        return positions;
    }

    public byte[] readFileContent(MetaData file){
        if(file == null) {
            System.out.println("File is null");
            return null;
        }
        if(file.fileStart >= file.fileEnd){
            System.out.println("File start is > file end");
            return null;
        }
        if(file.fileStart <= Utils.MAX_METADATA_SIZE){
            System.out.println("File data start needs to be least MAX_METADATA_SIZE + 1");
            return null;
        }
        if(file.fileEnd > Utils.CONTAINER_MAX_SIZE){
            System.out.println("File end exceeds container max size");
            return null;
        }

        int fileLength = file.fileEnd - file.fileStart;
        byte[] result = new byte[fileLength];
        try {
            RandomAccessFile raf = new RandomAccessFile(this.file, "rw");
            raf.seek(file.fileStart);
            raf.read(result, 0, fileLength);
            raf.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public boolean writeFileContent(byte[] contentByteArray, FilePosition fp){

        if(fp.start > fp.end){
            System.out.println("File start is > file end");
            return false;
        }
        if(contentByteArray.length == 0){
            System.out.println("Nothing to store. Returning success");
            return true;
        }

        try {
            RandomAccessFile raf = new RandomAccessFile(this.file, "rw");
            raf.seek(fp.start);
            raf.write(contentByteArray);
            raf.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }


    public byte[] externalFilePathToByteArray(String filePath){
        File file = new File(filePath);
        if(!file.isFile()){
            System.out.println("External path is not a file!");
            return null;
        }

        byte[] byteArray;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            byteArray = fileInputStream.readAllBytes();
            fileInputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return byteArray;
    }

    public void writeToExternalFile(byte[] byteArray, String filePath){
        File file = new File(filePath);
        try {
            file.createNewFile();
            if(!file.canWrite()){
                System.out.println("Cannot write to file here");
                return;
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(byteArray);
            fileOutputStream.close();
            System.out.println("File exported");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

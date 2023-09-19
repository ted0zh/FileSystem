import java.util.ArrayList;
import java.util.List;

public class FileSystem {

    public int currentDirectoryId;
    public FileHandler fileHandler;
    public FileSystem(){
        this.currentDirectoryId = 1; // 1 is the ID of the root directory file
        this.fileHandler = new FileHandler();
    }

    public void createDirectory(String directoryName){
        ArrayList<MetaData> allMetadata = this.fileHandler.getAllMetadata();
        if(isNamePresentInCurrentFolder(directoryName, allMetadata, this.currentDirectoryId)){
            System.out.println("File with the same name already exists.");
            return;
        }
        MetaData newFolder = new MetaData();
        newFolder.isDirectory = true;
        newFolder.fileName = directoryName;
        newFolder.parentId = this.currentDirectoryId;
        newFolder.fileId = this.getUniqueId(allMetadata);
        newFolder.fileEnd = 0;
        newFolder.fileStart = 0;
        allMetadata.add(newFolder);
        this.fileHandler.writeMetaData(allMetadata);
        System.out.println("Folder " + directoryName + " created successfully.");
    }

    public void removeEmptyDirectory(){
        if(this.currentDirectoryId == Utils.ROOT_FOLDER_ID){
            System.out.println("Root directory cannot be removed");
            return;
        }

        ArrayList<MetaData> allMetadata = this.fileHandler.getAllMetadata();
        if(this.isCurrentDirectoryEmpty(allMetadata)){
            for(int i=0; i<allMetadata.size(); i++){
                if(allMetadata.get(i).fileId == this.currentDirectoryId){
                    this.currentDirectoryId = allMetadata.get(i).parentId;
                    allMetadata.remove(i);
                    break;
                }
            }
            this.fileHandler.writeMetaData(allMetadata);
            System.out.println("Folder has been deleted successfully. Moved to parent directory.");
        }
        else{
            System.out.println("Current directory isn't empty!");
        }
    }

    public void printCurrentDirectory(){
        ArrayList<MetaData> allMetadata = this.fileHandler.getAllMetadata();
        MetaData currentDirectory = this.getCurrentDirectoryMetaData(allMetadata);
        ArrayList<MetaData> children = this.getAllChildrenOfCurrentDirectory(allMetadata);
        System.out.println(currentDirectory.fileName);
        for(MetaData md: children){
            if(md.isDirectory){
                System.out.println("    " + md.fileName + " (folder)");
            }
            else {
                System.out.println("    " + md.fileName);
            }
        }
        if(children.isEmpty()){
            System.out.println("    folder is empty*");
        }
    }

    public void changeDirectory(String wantedFolderName){
        ArrayList<MetaData> allMetadata = this.fileHandler.getAllMetadata();
        ArrayList<MetaData> children = this.getAllChildrenOfCurrentDirectory(allMetadata);
        boolean isFound = false;
        for(MetaData md: children){
            if(md.fileName.equals(wantedFolderName)){
                if(md.isDirectory){
                    this.currentDirectoryId = md.fileId;
                    isFound = true;
                    break;
                }
                else {
                    System.out.println("Provided name belongs to a file, not a directory.");
                    return;
                }
            }
        }
        if(isFound){
            System.out.println("Directory changed successfully");
        }
        else{
            System.out.println("Directory with such name doesn't exit.");
        }
    }

    public void goToParentFolder(){
        if(this.currentDirectoryId == Utils.ROOT_FOLDER_ID){
            System.out.println("You are in the root folder");
            return;
        }
        ArrayList<MetaData> allMetadata = this.fileHandler.getAllMetadata();
        MetaData currFolder = this.getCurrentDirectoryMetaData(allMetadata);
        this.currentDirectoryId = currFolder.parentId;
        System.out.println("Directory changed successfully");
    }

    public void goToRootFolder(){
        this.currentDirectoryId = Utils.ROOT_FOLDER_ID;
        System.out.println("Directory changed successfully");
    }

    public void printFileContent(String fileName){
        ArrayList<MetaData> allMetadata = this.fileHandler.getAllMetadata();

        if(!this.isNamePresentInCurrentFolder(fileName, allMetadata, this.currentDirectoryId)){
            System.out.println("File with such name doesn't exist in that directory");
            return;
        }

        MetaData wantedFile = this.getMetadataInCurrDirectoryByFileName(fileName, allMetadata, this.currentDirectoryId);
        byte[] result = this.fileHandler.readFileContent(wantedFile);
        String resultString = new String(result);
        System.out.println(resultString);
    }

    public void removeFileByName(String fileName){
        ArrayList<MetaData> allMetadata = this.fileHandler.getAllMetadata();
        boolean isFound = this.removeFileFromCurrFolderByName(fileName, allMetadata, this.currentDirectoryId);
        if(isFound){
            this.fileHandler.writeMetaData(allMetadata);
            System.out.println("File removed successfully");
        }
        else{
            System.out.println("File with such a name doesn't exist.");
        }
    }

    public  String[] splitString(String inputString,char symbol) {
        List<String> inputArray = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < inputString.length(); i++) {
            if(inputString.charAt(i) != symbol)
                sb.append(inputString.charAt(i));
            else {
                inputArray.add(sb.toString());
                sb.setLength(0);
            }
        }
        inputArray.add(sb.toString());
        return inputArray.toArray(new String[0]);
    }
    public void writeCommand(String inputString){
        inputString = inputString.strip();
        String writeRemoved = inputString.substring(5).strip();

        String[] paramArray = splitString(writeRemoved,' ');
        String fileName = paramArray[0];
        String restOfParameters = writeRemoved.substring(fileName.length()).strip();

        boolean isAppendMode;
        String content;

        if(restOfParameters.startsWith("+append")){
            isAppendMode = true;
            content = restOfParameters.substring(7).strip();
        }
        else {
            isAppendMode = false;
            content = restOfParameters;
        }

        if(content.startsWith("\"") && content.endsWith("\"")){
            content = content.substring(1, content.length()-1);
        }
        else {
            System.out.println("Wrong format. Use write <file name> {+append} \"<content>\"");
            return;
        }

        byte[] contentByteArray = content.getBytes();
        int contentSize = contentByteArray.length;

        ArrayList<MetaData> allMetadata = this.fileHandler.getAllMetadata();

        if(this.isNamePresentInCurrentFolder(fileName, allMetadata, this.currentDirectoryId)){
            if(isAppendMode){
                MetaData file = this.getMetadataInCurrDirectoryByFileName(fileName, allMetadata, this.currentDirectoryId);
                byte[] curData = this.fileHandler.readFileContent(file);
                int newLength = curData.length + contentSize;
                byte[] newData = new byte[newLength];
                int i,j;
                for(i=0; i<curData.length; i++){
                    newData[i] = curData[i];
                }
                for(j=0, i=i; j<contentByteArray.length; j++, i++){
                    newData[i] = contentByteArray[j];
                }
                this.removeFileFromCurrFolderByName(fileName, allMetadata, this.currentDirectoryId);
                this.addNewFile(newData, fileName, this.currentDirectoryId ,allMetadata);
            }
            else{
                this.removeFileFromCurrFolderByName(fileName, allMetadata, this.currentDirectoryId);
                this.addNewFile(contentByteArray, fileName, this.currentDirectoryId, allMetadata);
            }
        }
        else {
            this.addNewFile(contentByteArray, fileName, this.currentDirectoryId, allMetadata);
        }

    }

    public void importCommand(String inputString){
        inputString = inputString.strip();
        String[] paramArray = splitString(inputString,' ');
        boolean isAppendMode = false;
        String internalFile = "";
        String externalFile = "";

        if(paramArray.length < 3 || paramArray.length > 4){
            System.out.println("Wrong format.");
            System.out.println("Please input: import {+append} \"<internal file path>\" \"<external file path>\"");
            return;
        }
        else if(paramArray.length == 3){
            isAppendMode = false;
            internalFile = paramArray[1];
            externalFile = paramArray[2];

        }
        else if(paramArray.length == 4){
            if(!paramArray[1].equals("+append")){
                System.out.println("Unrecognised command: " + paramArray[1]);
                System.out.println("Maybe you mean \"+append\"");
                return;
            }
            isAppendMode = true;
            internalFile = paramArray[2];
            externalFile = paramArray[3];
        }

        byte[] byteArray = this.fileHandler.externalFilePathToByteArray(externalFile);
        if(byteArray == null){
            System.out.println("byteArray is null");
            return;
        }

        ArrayList<MetaData> allMetadata = this.fileHandler.getAllMetadata();

        MetaData newFileData = this.pathToMetadata(internalFile, allMetadata);

        if(newFileData == null){
            System.out.println("newFileData is null");
            return;
        }

        if(this.isNamePresentInCurrentFolder(newFileData.fileName, allMetadata, newFileData.parentId)){
            if(isAppendMode){
                MetaData file = this.getMetadataInCurrDirectoryByFileName(newFileData.fileName, allMetadata, newFileData.parentId);
                byte[] curData = this.fileHandler.readFileContent(file);
                int newLength = curData.length + byteArray.length;
                byte[] newData = new byte[newLength];
                int i,j;
                for(i=0; i<curData.length; i++){
                    newData[i] = curData[i];
                }
                for(j=0, i=i; j<byteArray.length; j++, i++){
                    newData[i] = byteArray[j];
                }
                this.removeFileFromCurrFolderByName(newFileData.fileName, allMetadata, newFileData.parentId);
                this.addNewFile(newData, newFileData.fileName, newFileData.parentId, allMetadata);
            }
            else {
                this.removeFileFromCurrFolderByName(newFileData.fileName, allMetadata, newFileData.parentId);
                this.addNewFile(byteArray, newFileData.fileName, newFileData.parentId, allMetadata);
            }
        }
        else{
            this.addNewFile(byteArray, newFileData.fileName, newFileData.parentId, allMetadata);
        }

    }

    public void exportCommand(String inputString){
        inputString = inputString.strip();
        String[] paramArray =splitString(inputString,' ');
        if(paramArray.length != 3){
            System.out.println("Wrong format. Use export <internal path> <external path>");
            return;
        }
        String internalFile = paramArray[1];
        String externalFile = paramArray[2];
        ArrayList<MetaData> allMetadata = this.fileHandler.getAllMetadata();
        MetaData fileData = this.pathToMetadata(internalFile, allMetadata);
        if(fileData == null){
            System.out.println("File doesn't exist.");
            return;
        }
        MetaData actualFile = this.getMetadataByNameInDirectory(fileData.fileName, fileData.parentId, allMetadata);
        byte[] byteArray = this.fileHandler.readFileContent(actualFile);
        this.fileHandler.writeToExternalFile(byteArray, externalFile);
    }

    public void copyFile(String fileName){
        ArrayList<MetaData> allMetadata = this.fileHandler.getAllMetadata();

        if(!this.isNamePresentInCurrentFolder(fileName, allMetadata, this.currentDirectoryId)){
            System.out.println("File with such name doesn't exist in current directory");
            return;
        }

        MetaData wantedFile = this.getMetadataInCurrDirectoryByFileName(fileName, allMetadata, this.currentDirectoryId);
        byte[] byteArray = this.fileHandler.readFileContent(wantedFile);

        this.addNewFile(byteArray, wantedFile.fileName + "(copy)", this.currentDirectoryId, allMetadata);
        System.out.println("File copied successfully");
    }

    private void addNewFile(byte[] contentByteArray, String fileName, int folderID, ArrayList<MetaData> allMetadata){
        int contentSize = contentByteArray.length;
        ArrayList<FilePosition> takenPositions = this.fileHandler.getTakenPositionsArray(allMetadata);
        FilePosition freeSpace = this.fileHandler.findAvailableSpace(takenPositions, contentSize);
        boolean writeSuccess  = this.fileHandler.writeFileContent(contentByteArray, freeSpace);
        if(!writeSuccess){
            System.out.println("Couldn't write file to container.");
            return;
        }

        MetaData newFile = new MetaData();
        newFile.isDirectory = false;
        newFile.fileName = fileName;
        newFile.parentId = folderID;
        newFile.fileId = this.getUniqueId(allMetadata);
        newFile.fileStart = freeSpace.start;
        newFile.fileEnd = freeSpace.end;
        allMetadata.add(newFile);
        this.fileHandler.writeMetaData(allMetadata);
        System.out.println("File " + fileName + " added successfully");
    }

    private int getUniqueId(ArrayList<MetaData> metadataArray){
        ArrayList<Integer> takenIds = new ArrayList<>();
        for(MetaData md: metadataArray){
            takenIds.add(md.fileId);
        }
        int uniqueId = -1;
        for(int i=1; i<Utils.MAX_METADATA_SIZE; i++){
            if(!takenIds.contains(i)){
                uniqueId = i;
                break;
            }
        }
        if(uniqueId == -1){
            System.out.println("SOMETHING WENT WRONG!!!!");
        }
        return uniqueId;
    }

    private boolean isNamePresentInCurrentFolder(String name, ArrayList<MetaData> metadataArray, int dirId){
        for(MetaData md: metadataArray){
            if(md.parentId == dirId){
                if(md.fileName.equals(name)){
                    return true;
                }
            }
        }
        return false;
    }

    private ArrayList<MetaData> getAllChildrenOfCurrentDirectory(ArrayList<MetaData> metadataArray){
       ArrayList<MetaData> result = new ArrayList<>();
        for(MetaData md: metadataArray){
            if(md.parentId == this.currentDirectoryId){
                result.add(md);
            }
        }
        return result;
    }

    private boolean isCurrentDirectoryEmpty(ArrayList<MetaData> metadataArray){
        ArrayList<MetaData> childArray = this.getAllChildrenOfCurrentDirectory(metadataArray);
        return childArray.isEmpty();
    }

    private MetaData getCurrentDirectoryMetaData(ArrayList<MetaData> metadataArray){
        for(MetaData md: metadataArray){
            if(md.fileId == this.currentDirectoryId){
                return md;
            }
        }
        return null;
    }

    private MetaData getMetadataInCurrDirectoryByFileName(String name, ArrayList<MetaData> metadataArray, int dirId){
        for(MetaData md: metadataArray){
            if(md.parentId == dirId){
                if(md.fileName.equals(name)){
                    return md;
                }
            }
        }
        return null;
    }

    private boolean removeFileFromCurrFolderByName(String name, ArrayList<MetaData> metadataArray, int dirId){
        boolean isFound = false;
        for(int i=0; i<metadataArray.size(); i++){
            if(metadataArray.get(i).parentId == dirId){
                if(metadataArray.get(i).fileName.equals(name)){
                    metadataArray.remove(i);
                    isFound = true;
                    break;
                }
            }
        }
        return isFound;
    }

    private MetaData pathToMetadata(String path, ArrayList<MetaData> metadataArray){
        String[] folderChain = splitString(path,'/');

        int dirId = Utils.ROOT_FOLDER_ID;
        for(int i=0; i<folderChain.length-1; i++){
            MetaData curFolder = this.getMetadataByNameInDirectory(folderChain[i], dirId, metadataArray);
            if(curFolder == null){
                System.out.println("Folder with name " + folderChain[i] + " doesn't exist");
                return null;
            }
            dirId = curFolder.fileId;
        }

        MetaData metaData = new MetaData();
        metaData.fileName = folderChain[folderChain.length-1];
        metaData.parentId = dirId;

        return metaData;
    }

    private MetaData getMetadataByNameInDirectory(String name, int directoryId, ArrayList<MetaData> metadataArray){
        for(MetaData md: metadataArray){
            if(md.parentId == directoryId && md.fileName.equals(name)){
                return md;
            }
        }
        return null;
    }

}
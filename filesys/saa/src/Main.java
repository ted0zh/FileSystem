import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class Main {
    public static void main(String []args){
        System.out.println("File system started.");
        System.out.println("Insert command \"exit\" to exit the program.");
        Scanner scanner = new Scanner(System.in);
        FileSystem fileSystem = new FileSystem();
        while (true) {
            String inputString = scanner.nextLine();
            String[] inputArray =fileSystem.splitString(inputString,' ');
            String command = inputArray[0];

            switch (command){
                case "exit": {
                    return;
                }
                case "": {
                    break;
                }

                case "*begin" : {
                    try {
                        RandomAccessFile raf = new RandomAccessFile(fileSystem.fileHandler.file, "rw");
                        raf.seek(Utils.CONTAINER_MAX_SIZE+1);
                        raf.write(Utils.LOGICAL_SEPARATOR);
                        raf.close();
                        System.out.println("Container size set successfully");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                }

                case "root": {
                    Root root = new Root();
                    root.create();
                    break;
                }
                case "test-write-data": {
                    var twd=new WriteTestDataToFile();
                    twd.writeTestDataToFile();
                    break;
                }
                case "test-read-data": {
                    var trd=new ReadTestDataFromFile();
                    trd.readTestDataFromFile();
                    break;
                }
                case "mkdir": {
                    if(inputArray.length < 2){
                        System.out.println("Please input mkdir <folder name>");
                    }
                    else {
                        String folderName = inputArray[1];
                        fileSystem.createDirectory(folderName);
                    }
                    break;
                }
                case "rmdir": {
                    fileSystem.removeEmptyDirectory();
                    break;
                }
                case "ls": {
                    fileSystem.printCurrentDirectory();
                    break;
                }
                case "cd": {
                    if(inputArray.length < 2){
                        System.out.println("Please input cd <wanted folder name> or cd.. to go back a folder.");
                    }
                    else {
                        String wantedFolderName = inputArray[1];
                        fileSystem.changeDirectory(wantedFolderName);
                    }
                    break;
                }
                case "cd..": {
                    fileSystem.goToParentFolder();
                    break;
                }
                case "cd...": {
                    fileSystem.goToRootFolder();
                    break;
                }
                case "rm": {
                    if(inputArray.length < 2){
                        System.out.println("Please input rm <wanted file name>");
                    }
                    else {
                        String fileToRemoveName = inputArray[1];
                        fileSystem.removeFileByName(fileToRemoveName);
                    }
                    break;
                }
                case "cat": {
                    if(inputArray.length < 2){
                        System.out.println("Please input cat <wanted file name>");
                    }
                    else {
                        String fileToRead = inputArray[1];
                        fileSystem.printFileContent(fileToRead);
                    }
                    break;
                }
                case "write": {
                    fileSystem.writeCommand(inputString);
                    break;
                }
                case "import": {
                    fileSystem.importCommand(inputString);
                    break;
                }
                case "export": {
                    fileSystem.exportCommand(inputString);
                    break;
                }
                case "cp": {
                    if(inputArray.length < 2){
                        System.out.println("Please input cp <wanted file name>");
                    }
                    else {
                        String fileToCopy = inputArray[1];
                        fileSystem.copyFile(fileToCopy);
                    }
                    break;
                }
                default: {
                    System.out.println("Command not recognised!");
                    break;
                }
            }
        }

    }
}

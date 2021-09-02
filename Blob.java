package gitlet;


import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {

    String sha;
    String fileName;
    File directory;
    byte[] blobBytes;

    public Blob(String sha, String fileName, File directory) {
        this.sha = sha;
        this.fileName = fileName;
        this.directory = directory;
        fileToByte();
    }

    public void fileToByte() {
        blobBytes = Utils.readContents(this.directory);
    }

}

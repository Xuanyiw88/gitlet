package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

public class StagingArea implements Serializable {

    /** HashMap of FileNames to Blob SHAS **/
    private HashMap<String, String> stagingAdded;
    private HashMap<String, String> stagingRemoved;

    public StagingArea() {
        stagingAdded = new HashMap<>();
        stagingRemoved = new HashMap<>();
    }

    public void clear() {
        stagingAdded = new HashMap<String, String>();
        stagingRemoved = new HashMap<String, String>();
    }

    public HashMap<String, String> getStagingAdded() {
        return stagingAdded;
    }

    public HashMap<String, String> getStagingRemoved() {
        return stagingRemoved;
    }

    public void writeStageAddToFile(File stagingDirectory) {
        Utils.writeObject(stagingDirectory, stagingAdded);
    }

    public void writeStageRemoveToFile(File stagingDirectory)  {
        Utils.writeObject(stagingDirectory, stagingRemoved);
    }

    public static HashMap<String, String> readCurrAdd(File stagingAddDirectory) {
        return (HashMap<String, String>) Utils.readObject(stagingAddDirectory, HashMap.class);
    }

    public static HashMap<String, String> readCurrRemove(File stagingRemoveDirectory) {
        return (HashMap<String, String>) Utils.readObject(stagingRemoveDirectory, HashMap.class);
    }

}

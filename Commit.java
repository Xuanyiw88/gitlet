package gitlet;


import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *
 *  does at a high level.
 *
 *  @author Xuanyi Wang
 */
public class Commit implements Serializable {
    /**
     *
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private String date;
    /** File Name and Blob Object that corresponds at this commit **/
    private HashMap<String, String> currBlobs;
    /** Same thing but for parents **/
    private HashMap<String, String> parentBlobs;

    /** SHA of parent **/
    private String parent1SHA;
    private String parent2SHA;
    /** SHA of the commit **/
    private String SHA;

    public Commit(String message, String parent1, String parent2, String date,
                  HashMap<String, String> parentBlobs, HashMap<String, String> currBlobs) {
        this.message = message;
        this.parent1SHA = parent1;
        this.parent2SHA = parent2;
        this.date = date;
        this.parentBlobs = parentBlobs;
        this.currBlobs = currBlobs;
    }

    public String getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    public String getCommitSHA() {
        return SHA;
    }

    public void setSHA() {
        SHA = sha1(serialize(this));
    }

    public void writeContentsToFile(File file) {
        writeObject(file, this);
    }


    public String getParent1SHA()  {
        return parent1SHA;
    }

    public String getParent2SHA() {
        return parent2SHA;
    }

    public void changeParent1SHA(String newSHA) {
        parent1SHA = newSHA;
    }

    public void changeParent2SHA(String newSHA) {
        parent2SHA = newSHA;
    }

    public HashMap<String, String> getCurrBlobs() {
        return currBlobs;
    }

    public HashMap<String, String> getParentBlobs() {
        return parentBlobs;
    }
}


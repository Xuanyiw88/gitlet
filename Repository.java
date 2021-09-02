package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *
 *  does at a high level.
 *
 *  @author Xuanyi Wang
 */
public class Repository implements Serializable {
    /**
     *
     *
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File STAGE_DIR = join(GITLET_DIR, "staging-area");
    public static final File COMMIT_DIR = join(GITLET_DIR, "commits");
    public static final File BLOB_DIR = join(GITLET_DIR, "blobs");
    public static final File BRANCH_DIR = join(GITLET_DIR, "branches");
    public static final File STAGING_ADD = join(STAGE_DIR, "staging_add");
    public static final File STAGING_REMOVE = join(STAGE_DIR, "staging_remove");
    public static final File HEAD_COMMIT = join(GITLET_DIR, "head_commit");
    public static final File CURR_BRANCH = join(GITLET_DIR, "curr_branch");
    /** Files that are staged but not yet committed**/
    private static StagingArea stagingArea = new StagingArea();

    /*  */


    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdir();
        STAGE_DIR.mkdir();
        COMMIT_DIR.mkdir();
        BLOB_DIR.mkdir();
        BRANCH_DIR.mkdir();
        try {
            STAGING_ADD.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            STAGING_REMOVE.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            HEAD_COMMIT.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stagingArea.writeStageAddToFile(STAGING_ADD);
        stagingArea.writeStageRemoveToFile(STAGING_REMOVE);
        Commit initCommit = new Commit("initial commit", null,
                null, "Thu Jan 1 00:00:00 1970 +0000", new HashMap<>(), new HashMap<>());
        initCommit.setSHA();
        String initCommitSHA = initCommit.getCommitSHA();
        File newFile = join(COMMIT_DIR, initCommitSHA);
        try {
            newFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /** Makes first branch and adds initial commit to it **/
        File firstBranch = join(BRANCH_DIR, "master");
        try {
            firstBranch.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        writeObject(firstBranch, initCommit);

        initCommit.writeContentsToFile(newFile);
        saveHeadBranch(initCommit);
        saveCurrBranch("master");
    }

    public static void add(String fileName) {

        File newFile = join(CWD, fileName);
        if (!newFile.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        /** Makes new blob **/
        String newBlobSHA = findBlobSHA(CWD, fileName);
        byte[] newBlobContents = readContents(newFile);
        HashMap<String, String> currStagingAdd = StagingArea.readCurrAdd(STAGING_ADD);
        HashMap<String, String> currStagingRemove = StagingArea.readCurrRemove(STAGING_REMOVE);
        Commit currCommit = Utils.readObject(HEAD_COMMIT, Commit.class);
        HashMap<String, String> tempCurrBlobs = currCommit.getCurrBlobs();
        File currBlobDirectory = join(BLOB_DIR, newBlobSHA);
        /** If already in staging area, clear it **/
        if (currStagingAdd.containsKey(fileName)) {
            currStagingRemove.remove(fileName);
        }
        /** If file is the same as it currently is in commit **/
        if (tempCurrBlobs.containsKey(fileName)) {
            String currBlobSHA = tempCurrBlobs.get(fileName);
            if (currBlobSHA.equals(newBlobSHA)) {
                currStagingRemove.remove(fileName);
                writeObject(STAGING_REMOVE, currStagingRemove);
                System.exit(0);
            }
        }

        try {
            currBlobDirectory.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /** Write or Overwrite the Blob and add to Staging Area **/
        writeContents(currBlobDirectory, newBlobContents);
        currStagingAdd.put(fileName, newBlobSHA);
        writeObject(STAGING_ADD, currStagingAdd);
        writeObject(STAGING_REMOVE, currStagingRemove);
    }

    public static void commit(String msg) {
        /** Checking if there is no commit message **/
        if (msg.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        HashMap<String, String> currStagingAdd = stagingArea.readCurrAdd(STAGING_ADD);
        HashMap<String, String> currStagingRemove = stagingArea.readCurrAdd(STAGING_REMOVE);
        /** Checking if staging area is empty **/
        if (currStagingAdd.isEmpty() && currStagingRemove.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        Commit newCommit;
        Commit newParent;
        HashMap<String, String> newParentBlobs;
        Commit currCommit = Utils.readObject(HEAD_COMMIT, Commit.class);
        newParent = currCommit;
        newParentBlobs = newParent.getCurrBlobs();
        /**@source found SimpleDateFormat on google **/
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss YYYY Z");
        Date date = new Date(System.currentTimeMillis());
        String currDate = formatter.format(date);
        try {
            HashMap<String, String> tempCombined = new HashMap<String, String>();
            tempCombined.putAll(newParentBlobs);
            /** overrides anything in the old commit for the new commit **/
            tempCombined.putAll(currStagingAdd);
            /** Removes items in commit that are in staging remove **/
            tempCombined.keySet().removeAll(currStagingRemove.keySet());
            newCommit = new Commit(msg, newParent.getCommitSHA(),
                    null, currDate, newParentBlobs, tempCombined);
            newCommit.setSHA();
        } catch (NullPointerException e) {
            HashMap<String, String> temp = new HashMap<String, String>();
            temp.putAll(currStagingAdd);
            temp.keySet().removeAll(currStagingRemove.keySet());
            newCommit = new Commit(msg, newParent.getCommitSHA(),
                    null, currDate, newParentBlobs, temp);
            newCommit.setSHA();
        }
        stagingArea.clear();
        Utils.writeObject(STAGING_ADD, stagingArea.getStagingAdded());
        Utils.writeObject(STAGING_REMOVE, stagingArea.getStagingRemoved());
        String newCommitSHA = newCommit.getCommitSHA();
        File newCommitDirectory = join(COMMIT_DIR, newCommitSHA);
        try {
            newCommitDirectory.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        writeObject(newCommitDirectory, newCommit);
        /** Add  Branch Support**/
        String currBranch = readObject(CURR_BRANCH, String.class);
        saveHeadBranch(newCommit);
        File currBranchFile = join(BRANCH_DIR, currBranch);
        writeObject(currBranchFile, newCommit);
    }

    public static void log() {
        Commit recentCommit = Utils.readObject(HEAD_COMMIT, Commit.class);
        while (recentCommit.getParent1SHA() != null) {
            System.out.println("===");
            System.out.println("commit " + recentCommit.getCommitSHA());
            if (recentCommit.getParent2SHA() != null) {
                System.out.println("Merge: " + recentCommit.getParent1SHA().substring(0, 7) + " "
                        + recentCommit.getParent2SHA().substring(0, 7));
            }
            System.out.println("Date: " + recentCommit.getDate());
            System.out.println(recentCommit.getMessage());
            System.out.println();
            recentCommit = readObject(join(COMMIT_DIR, recentCommit.getParent1SHA()), Commit.class);
        }
        System.out.println("===");
        System.out.println("commit " + recentCommit.getCommitSHA());
        if (recentCommit.getParent2SHA() != null) {
            System.out.println("Merge: " + recentCommit.getParent1SHA().substring(0, 8) + " "
                    + recentCommit.getParent2SHA().substring(0, 8));
        }
        System.out.println("Date: " + recentCommit.getDate());
        System.out.println(recentCommit.getMessage());
        System.out.println();
    }

    public static void checkout(String fileName) {
        Commit currCommit = Utils.readObject(HEAD_COMMIT, Commit.class);
        File currFileInCWD = Utils.join(CWD, fileName);
        if (!currCommit.getCurrBlobs().containsKey(fileName)) {
            System.out.println("File does not exist in that commit");
            System.exit(0);
        } else {
            byte[] blobContents = readContents(join(BLOB_DIR,
                    currCommit.getCurrBlobs().get(fileName)));
            Utils.writeContents(currFileInCWD, blobContents);
        }
    }

    public static void checkout(String commitID, String fileName) {
        if (commitID.length() < 40) {
            List<String> allCommits = plainFilenamesIn(COMMIT_DIR);
            for (String fileSHA: allCommits) {
                if (fileSHA.substring(0, commitID.length()).equals(commitID)) {
                    Commit currCommit = readObject(join(COMMIT_DIR, fileSHA), Commit.class);
                    File currFile = Utils.join(CWD, fileName);
                    if (!currCommit.getCurrBlobs().containsKey(fileName)) {
                        System.out.println("File does not exist in that commit");
                        System.exit(0);
                    } else {
                        byte[] blobContents = readContents(join(BLOB_DIR,
                                currCommit.getCurrBlobs().get(fileName)));
                        Utils.writeContents(currFile, blobContents);
                    }
                }
            }
        } else {
            if (!join(COMMIT_DIR, commitID).exists()) {
                System.out.println("No commit with that ID exists.");
                System.exit(0);
            }
            Commit currCommit = readObject(join(COMMIT_DIR, commitID), Commit.class);
            File currFile = Utils.join(CWD, fileName);
            if (!currCommit.getCurrBlobs().containsKey(fileName)) {
                System.out.println("File does not exist in that commit");
                System.exit(0);
            } else {
                byte[] blobContents = readContents(join(BLOB_DIR,
                        currCommit.getCurrBlobs().get(fileName)));
                Utils.writeContents(currFile, blobContents);
            }
        }
    }

    public static void checkoutBranch(String branchName) {
        File branchFile = join(BRANCH_DIR, branchName);
        String currBranch = readObject(CURR_BRANCH, String.class);
        if (!branchFile.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (currBranch.equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        Commit newCommit = readObject(branchFile, Commit.class);
        Commit currCommit = readObject(join(BRANCH_DIR, currBranch), Commit.class);
        /** Check last error case **/
        List<String> listOfFiles = plainFilenamesIn(CWD);
        HashMap<String, String> currStagingAdd = (HashMap<String, String>)
                readObject(STAGING_ADD, HashMap.class);
        for (String fileName: listOfFiles) {
            File workingDirFile = join(CWD, fileName);
            byte[] tempBytes = readContents(workingDirFile);
            String tempSHA = sha1(tempBytes);
            if (!(newCommit.getCurrBlobs().get(fileName) == null)
                    && !currCommit.getCurrBlobs().containsKey(fileName)
                    && !tempBytes.equals(readContents(join(BLOB_DIR,
                    newCommit.getCurrBlobs().get(fileName))))
                    && !currStagingAdd.containsKey(fileName)) {
                if (!(newCommit.getCurrBlobs().get(fileName) == null
                        && currCommit.getCurrBlobs().get(fileName) == null)) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }

        for (String key: currCommit.getCurrBlobs().keySet()) {
            if (!newCommit.getCurrBlobs().containsKey(key)) {
                File fileToDelete = join(CWD, key);
                restrictedDelete(fileToDelete);
            }
        }
        for (String fileName: newCommit.getCurrBlobs().keySet()) {
            byte[] newBlobContents = readContents(join(BLOB_DIR,
                    newCommit.getCurrBlobs().get(fileName)));
            File newFile = join(CWD, fileName);
            if (!newFile.exists()) {
                try {
                    newFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            writeContents(newFile, newBlobContents);
        }
        saveCurrBranch(branchName);
        saveHeadBranch(newCommit);
        stagingArea.clear();
        stagingArea.writeStageAddToFile(STAGING_ADD);
        stagingArea.writeStageRemoveToFile(STAGING_REMOVE);
    }

    public static void rm(String fileName) {
        File newFileDirectory = join(CWD, fileName);
        HashMap<String, String> currStagingAdd = StagingArea.readCurrAdd(STAGING_ADD);
        HashMap<String, String> currStagingRemove = StagingArea.readCurrRemove(STAGING_REMOVE);
        Commit headCommit = readObject(HEAD_COMMIT, Commit.class);
        if (headCommit.getCurrBlobs().containsKey(fileName)) {
            currStagingAdd.remove(fileName);
            currStagingRemove.put(fileName, headCommit.getCurrBlobs().get(fileName));
            restrictedDelete(newFileDirectory);
            writeObject(STAGING_ADD, currStagingAdd);
            writeObject(STAGING_REMOVE, currStagingRemove);
        } else if (currStagingAdd.containsKey(fileName)) {
            currStagingAdd.remove(fileName);
            writeObject(STAGING_ADD, currStagingAdd);
        } else {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
    }

    public static void branch(String branchName) {
        File newBranch = join(BRANCH_DIR, branchName);
        Commit currHeadCommit = readObject(HEAD_COMMIT, Commit.class);
        if (newBranch.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        try {
            newBranch.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        writeObject(newBranch, currHeadCommit);
    }

    public static void rmBranch(String branchName) {
        String currBranch = readObject(CURR_BRANCH, String.class);
        File removedBranch = join(BRANCH_DIR, branchName);
        if (!removedBranch.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (branchName.equals(currBranch)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        removedBranch.delete();
    }

    public static void status() {
        System.out.println("=== Branches ===");
        List<String> branchList = plainFilenamesIn(BRANCH_DIR);
        String currBranch = readObject(CURR_BRANCH, String.class);
        for (String branch : branchList) {
            if (branch.equals(currBranch)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        HashMap<String, String> currStagingAdd = (HashMap<String, String>)
                readObject(STAGING_ADD, HashMap.class);
        List<String> tempAdd = new ArrayList<>();
        for (String key: currStagingAdd.keySet()) {
            tempAdd.add(key);
        }
        Collections.sort(tempAdd);
        for (String fileName: tempAdd) {
            System.out.println(fileName);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        HashMap<String, String> currStagingRemove = (HashMap<String, String>)
                readObject(STAGING_REMOVE, HashMap.class);
        List<String> tempRemove = new ArrayList<>();
        for (String key: currStagingRemove.keySet()) {
            tempRemove.add(key);
        }
        Collections.sort(tempRemove);
        for (String fileName: tempRemove) {
            System.out.println(fileName);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        List<String> allFilesInCWD = plainFilenamesIn(CWD);
        Commit currHeadCommit = readObject(HEAD_COMMIT, Commit.class);
        for (String fileName: allFilesInCWD){
            String fileContentsSHA = sha1(readContents(join(CWD, fileName)));
            if (currHeadCommit.getCurrBlobs().containsKey(fileName)
                    && !currHeadCommit.getCurrBlobs().get(fileName).equals(fileContentsSHA)
                    && !currStagingAdd.containsKey(fileName)
                    && !currStagingRemove.containsKey(fileName)) {
                System.out.println(fileName + " (modified)");
            } else if (currStagingAdd.containsKey(fileName)
                    && !currStagingAdd.get(fileName).equals(fileContentsSHA)) {
                System.out.println(fileName + " (modified)");
            }
        }
        for (String fileName: currStagingAdd.keySet()){
            if (!join(CWD, fileName).exists()) {
                System.out.println(fileName + " (deleted)");
            }
        }
        for (String fileName: currHeadCommit.getCurrBlobs().keySet()) {
            if (!currStagingRemove.containsKey(fileName)
                    && !join(CWD, fileName).exists()){
                System.out.println(fileName + " (deleted)");
            }
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        for (String fileName: allFilesInCWD) {
            if ((!currHeadCommit.getCurrBlobs().containsKey(fileName)
                    && !currStagingAdd.containsKey(fileName))
                    || (currHeadCommit.getCurrBlobs().containsKey(fileName)
                    && currStagingRemove.containsKey(fileName))) {
                System.out.println(fileName);
            }
        }
        System.out.println();
    }

    public static void reset(String commitID) {
        if (!join(COMMIT_DIR, commitID).exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        Commit newCommit = readObject(join(COMMIT_DIR, commitID), Commit.class);
        Commit currCommit = readObject(HEAD_COMMIT, Commit.class);

        /** Overwrite Error Case **/
        List<String> listOfFiles = plainFilenamesIn(CWD);
        HashMap<String, String> currStagingAdd = (HashMap<String, String>)
                readObject(STAGING_ADD, HashMap.class);
        for (String fileName: listOfFiles) {
            File workingDirFile = join(CWD, fileName);
            byte[] tempBytes = readContents(workingDirFile);
            String tempSHA = sha1(tempBytes);
            if (!(newCommit.getCurrBlobs().get(fileName) == null)
                    && !currCommit.getCurrBlobs().containsKey(fileName)
                    && !tempBytes.equals(readContents(join(BLOB_DIR,
                    newCommit.getCurrBlobs().get(fileName))))
                    && !currStagingAdd.containsKey(fileName)) {
                if (!(newCommit.getCurrBlobs().get(fileName) == null
                        && currCommit.getCurrBlobs().get(fileName) == null)) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }
        for (String key: currCommit.getCurrBlobs().keySet()) {
            if (!newCommit.getCurrBlobs().containsKey(key)) {
                File fileToDelete = join(CWD, key);
                restrictedDelete(fileToDelete);
            }
        }
        Set<String> fileNames = newCommit.getCurrBlobs().keySet();
        for (String fileName: fileNames) {
            checkout(commitID, fileName);
        }
        String currBranch = readObject(CURR_BRANCH, String.class);
        writeObject(join(BRANCH_DIR, currBranch), newCommit);
        saveHeadBranch(newCommit);
        stagingArea.clear();
        stagingArea.writeStageAddToFile(STAGING_ADD);
        stagingArea.writeStageRemoveToFile(STAGING_REMOVE);
    }

    public static void globalLog() {
        List<String> listOfCommits = plainFilenamesIn(COMMIT_DIR);
        for (String commitID: listOfCommits) {
            Commit currCommit = readObject(join(COMMIT_DIR, commitID), Commit.class);
            System.out.println("===");
            System.out.println("commit " + commitID);
            System.out.println("Date: " + currCommit.getDate());
            System.out.println(currCommit.getMessage());
            System.out.println();
        }
    }

    public static void find(String commitMessage) {
        List<String> listOfCommits = plainFilenamesIn(COMMIT_DIR);
        boolean found = false;
        for (String commitID: listOfCommits) {
            Commit currCommit = readObject(join(COMMIT_DIR, commitID), Commit.class);
            if (currCommit.getMessage().equals(commitMessage)) {
                System.out.println(commitID);
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }


    public static void merge(String newBranchName) {
        boolean hasConflict = false;
        HashMap<String, String> currStagingAdd = StagingArea.readCurrAdd(STAGING_ADD);
        HashMap<String, String> currStagingRemove = StagingArea.readCurrRemove(STAGING_REMOVE);
        if (!currStagingAdd.isEmpty() || !currStagingRemove.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        } else if (!join(BRANCH_DIR, newBranchName).exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        } else if (readObject(CURR_BRANCH, String.class).equals(newBranchName)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        String currBranchName = readObject(CURR_BRANCH, String.class);
        Commit currBranchHeadCommit = readObject(join(BRANCH_DIR, currBranchName), Commit.class);
        Commit newBranchHeadCommit = readObject(join(BRANCH_DIR, newBranchName), Commit.class);
        List<String> listOfFiles = plainFilenamesIn(CWD);
        for (String fileName: listOfFiles) {
            File workingDirFile = join(CWD, fileName);
            byte[] tempBytes = readContents(workingDirFile);
            String tempSHA = sha1(tempBytes);
            if (!(newBranchHeadCommit.getCurrBlobs().get(fileName) == null)
                    && !currBranchHeadCommit.getCurrBlobs().containsKey(fileName)
                    && !tempBytes.equals(readContents(join(BLOB_DIR,
                    newBranchHeadCommit.getCurrBlobs().get(fileName))))
                    && !currStagingAdd.containsKey(fileName)) {
                if (!(newBranchHeadCommit.getCurrBlobs().get(fileName) == null
                        && currBranchHeadCommit.getCurrBlobs().get(fileName) == null)) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }
        Commit splitCommit = findSplit(newBranchName, currBranchName);
        if (splitCommit.getCommitSHA().equals(newBranchHeadCommit.getCommitSHA())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        } else if (splitCommit.getCommitSHA().equals(currBranchHeadCommit.getCommitSHA())) {
            System.out.println("Current branch fast-forwarded.");
            checkoutBranch(newBranchName);
            System.exit(0);
        }
        /** creates a set of all file names **/
        HashSet<String> allFileNames = new HashSet<>();
        allFileNames.addAll(currBranchHeadCommit.getCurrBlobs().keySet());
        allFileNames.addAll(newBranchHeadCommit.getCurrBlobs().keySet());
        allFileNames.addAll(splitCommit.getCurrBlobs().keySet());
        for (String key: allFileNames) {
            String currBranchSHA = currBranchHeadCommit.getCurrBlobs().get(key);
            String newBranchSHA = newBranchHeadCommit.getCurrBlobs().get(key);
            String splitSHA = splitCommit.getCurrBlobs().get(key);
            if (checkNotNull(currBranchSHA, newBranchSHA, splitSHA)
                    && currBranchSHA.equals(splitSHA)
                    && !newBranchSHA.equals(splitSHA)) {
                checkout(newBranchHeadCommit.getCommitSHA(), key);
                add(key);
            } else if (splitSHA == null && currBranchSHA == null && newBranchSHA != null) {
                checkout(newBranchHeadCommit.getCommitSHA(), key);
                add(key);
            } else if (splitSHA != null && newBranchSHA == null && splitSHA.equals(currBranchSHA)) {
                rm(key);
            } else if (splitSHA != null
                    && !splitSHA.equals(currBranchSHA)
                    && !splitSHA.equals(newBranchSHA)
                    && currBranchSHA != null && !currBranchSHA.equals(newBranchSHA)) {
                hasConflict = true;
                mergeConflict(join(CWD, key), currBranchSHA, newBranchSHA);
            } else if ((splitSHA != null
                    && newBranchSHA == null && currBranchSHA != null
                    && !splitSHA.equals(currBranchSHA)) || (splitSHA != null
                    && currBranchSHA == null && newBranchSHA != null
                    && !splitSHA.equals(newBranchSHA))) {
                hasConflict = true;
                mergeConflict(join(CWD, key), currBranchSHA, newBranchSHA);
            }
        }
        mergeCommit(hasConflict, newBranchName, currBranchName);
    }

    private static void mergeConflict(File conflictedFile, String currBlobSHA, String newBlobSHA) {
        String head = "<<<<<<< HEAD\n";
        String divide = "=======\n";
        String bottom = ">>>>>>>\n";
        String newString = "";
        newString = newString + head;
        if (currBlobSHA != null) {
            newString = newString + readContentsAsString(join(BLOB_DIR, currBlobSHA));
        }
        newString = newString + divide;
        if (newBlobSHA != null) {
            newString = newString + readContentsAsString(join(BLOB_DIR, newBlobSHA));
        }
        newString = newString + bottom;
        Utils.writeContents(conflictedFile, newString);
    }


    private static Commit findSplit(String newBranchName, String oldBranchName) {
        Commit newHeadCommit = readObject(join(BRANCH_DIR, newBranchName), Commit.class);
        Commit oldHeadCommit = readObject(join(BRANCH_DIR, oldBranchName), Commit.class);
        HashSet<String> newBranchCommits = makeNodeSet(newHeadCommit);
        Queue<Commit> oldCommitQueue = new LinkedList<>();
        oldCommitQueue.add(oldHeadCommit);
        while (!oldCommitQueue.isEmpty()) {
            oldHeadCommit = oldCommitQueue.remove();
            if (newBranchCommits.contains(oldHeadCommit.getCommitSHA())) {
                return oldHeadCommit;
            }
            if (oldHeadCommit.getParent1SHA() != null) {
                oldCommitQueue.add(readObject(join(COMMIT_DIR,
                        oldHeadCommit.getParent1SHA()), Commit.class));
            }
            if (oldHeadCommit.getParent2SHA() != null) {
                oldCommitQueue.add(readObject(join(COMMIT_DIR,
                        oldHeadCommit.getParent2SHA()), Commit.class));
            }
        }
        return null;
    }

    private static HashSet<String> makeNodeSet(Commit currCommit) {
        HashSet<String> newSet = new HashSet<>();
        newSet.add(currCommit.getCommitSHA());
        if (currCommit.getParent1SHA() != null) {
            Commit parent1Commit = readObject(join(COMMIT_DIR,
                    currCommit.getParent1SHA()), Commit.class);
            newSet.addAll(makeNodeSet(parent1Commit));
        }
        if (currCommit.getParent2SHA() != null) {
            Commit parent2Commit = readObject(join(COMMIT_DIR,
                    currCommit.getParent2SHA()), Commit.class);
            newSet.addAll(makeNodeSet(parent2Commit));
        }
        return newSet;
    }


    public static void mergeCommit(boolean hC, String newBranchName, String oldBranchName) {
        if (hC) {
            System.out.println("Encountered a merge conflict.");
        }
        String newCommitMsg = "Merged " + newBranchName + " into " + oldBranchName + ".";
        commit(newCommitMsg);
        Commit newCommit = readObject(HEAD_COMMIT, Commit.class);
        join(COMMIT_DIR, newCommit.getCommitSHA()).delete();
        String newBranchSHA = readObject(join(BRANCH_DIR, newBranchName),
                Commit.class).getCommitSHA();
        newCommit.changeParent2SHA(newBranchSHA);
        newCommit.setSHA();
        File newFile = join(COMMIT_DIR, newCommit.getCommitSHA());
        try {
            newFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        writeObject(newFile, newCommit);
        writeObject(join(BRANCH_DIR, readObject(CURR_BRANCH, String.class)), newCommit);
        saveHeadBranch(newCommit);
    }


    /** Helper Functions **/
    private static String findBlobSHA(File blobDirectory, String blobFileName) {
        File currBlobDirectory = Utils.join(blobDirectory, blobFileName);
        byte[] byteCode = readContents(currBlobDirectory);
        return Utils.sha1(byteCode);
    }

    public static void saveHeadBranch(Commit currCommit) {
        writeObject(HEAD_COMMIT, currCommit);
    }

    public static void saveCurrBranch(String branchName) {
        writeObject(CURR_BRANCH, branchName);
    }

    public static boolean checkNotNull(String a, String b, String c) {
        return !(a == null || b == null || c == null);
    }
}

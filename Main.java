package gitlet;

import jdk.javadoc.doclet.Reporter;

import java.io.File;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Xuanyi Wang
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if(args.length == 0){
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        File gitletDirectory = Repository.GITLET_DIR;
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                if(args.length != 1){
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.init();
                break;
            case "add":
                if(args.length != 2){
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (!gitletDirectory.exists()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                Repository.add(args[1]);
                break;
            case "commit":
                if(args.length != 2){
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (!gitletDirectory.exists()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                Repository.commit(args[1]);
                break;
            case "checkout":
                if (!gitletDirectory.exists()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                if(args.length == 3){
                    if(!args[1].equals("--")){
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    Repository.checkout(args[2]);
                } else if(args.length == 4){
                    if(!args[2].equals("--")){
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    Repository.checkout(args[1], args[3]);
                } else if(args.length == 2){
                    Repository.checkoutBranch(args[1]);
                } else{
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                break;
            case "log":
                if(args.length != 1){
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (!gitletDirectory.exists()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                Repository.log();
                break;
            case "rm":
                if(args.length != 2){
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (!gitletDirectory.exists()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                Repository.rm(args[1]);
                break;
            case "branch":
                if(args.length != 2){
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (!gitletDirectory.exists()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                if(args.length != 2){
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (!gitletDirectory.exists()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                Repository.rmBranch(args[1]);
                break;
            case "status":
                if(args.length != 1){
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (!gitletDirectory.exists()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                Repository.status();
                break;
            case "global-log":
                if(args.length != 1){
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (!gitletDirectory.exists()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                Repository.globalLog();
                break;
            case "find":
                if(args.length != 2){
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (!gitletDirectory.exists()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                Repository.find(args[1]);
                break;
            case "reset":
                if(args.length != 2){
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (!gitletDirectory.exists()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                Repository.reset(args[1]);
                break;
            case "merge":
                if(args.length != 2){
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (!gitletDirectory.exists()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                Repository.merge(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }
}

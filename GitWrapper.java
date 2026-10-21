import java.io.IOException;
import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.io.FileWriter;
import java.nio.file.StandardCopyOption;

public class GitWrapper {

    /**
     * Initializes a new Git repository.
     * This method creates the necessary directory structure
     * and initial files (index, HEAD) required for a Git repository.
     */
    public void init() throws IOException {
        // to-do: implement functionality here
        Git.init();
    }

    /**
     * Stages a file for the next commit.
     * This method adds a file to the index file.
     * If the file does not exist, it throws an IOException.
     * If the file is a directory, it throws an IOException.
     * If the file is already in the index, it does nothing.
     * If the file is successfully staged, it creates a blob for the file.
     * 
     * @param filePath The path to the file to be staged.
     */
    public void add(String filePath) throws IOException {
        // to-do: implement functionality here
        if (!new File(filePath).isFile()) {
            throw new IOException();
        }
        Git.createBlob(filePath);
        Git.addToIndex(filePath);
    }

    /**
     * Creates a commit with the given author and message.
     * It should capture the current state of the repository by building trees based
     * on the index file,
     * writing the tree to the objects directory,
     * writing the commit to the objects directory,
     * updating the HEAD file,
     * and returning the commit hash.
     *
     * The commit should be formatted as follows:
     * tree: <tree_sha>
     * parent: <parent_sha>
     * author: <author>
     * date: <date>
     * message: <message>
     *
     * @param author  The name of the author making the commit.
     * @param message The commit message describing the changes.
     * @return The SHA1 hash of the new commit.
     */
    public String commit(String author, String message) throws IOException, NoSuchAlgorithmException {
        return Git.commit(author, message);
    }

    /**
     * Checks out a specific commit given its hash.
     * This method should read the HEAD file to determine the "checked out" commit.
     * Then it should update the working directory to match the
     * state of the repository at that commit by tracing through the root tree and
     * all its children.
     *
     * @param commitHash The SHA1 hash of the commit to check out.
     */
    public void checkout(String commitHash) throws IOException, NoSuchAlgorithmException {
        // to-do: implement functionality here
        List<String> oldIndex = Files.readAllLines(Path.of("git/index"));
        Git.regenerateIndex(commitHash);
        List<String> newIndex = Files.readAllLines(Path.of("git/index"));

        for (String oldLine : oldIndex) {
            String oldPath = oldLine.split(" ", 2)[1];
            boolean found = false;
            for (String newLine : newIndex) {
                String newPath = newLine.split(" ", 2)[1];
                if (oldPath.equals(newPath)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                Files.delete(Path.of(oldPath));
                File folder = new File(oldPath).getParentFile();
                while (folder != null && folder.isDirectory() && folder.delete()) {
                    folder = folder.getParentFile();
                }
            }
        }
        for (String newLine : newIndex) {
            String[] parts = newLine.split(" ", 2);
            String blobHash = parts[0];
            String filePath = parts[1];
            String contents = Files.readString(Path.of("git/objects/" + blobHash));
            Path file = Path.of(filePath);
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            FileWriter wr = new FileWriter(filePath);
            wr.write(contents);
            wr.close();
        }
        FileWriter wr = new FileWriter("git/HEAD.tmp");
        wr.write(commitHash);
        wr.close();
        Files.move(Path.of("git/HEAD.tmp"), Path.of("git/HEAD"),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING);
    }
}
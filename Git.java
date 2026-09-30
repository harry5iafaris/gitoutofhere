import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

class Git {
    public static void main(String[] args) {
        try {
            init();
            File test = new File("test.txt");
            test.createNewFile();
            System.out.println(hash(test.getPath()));
            createBlob(test.getPath());
            addToIndex(test.getPath());

            File testFolder = new File("testing/test.txt");
            testFolder.createNewFile();
            System.out.println(hash(testFolder.getPath()));
            createBlob(testFolder.getPath());
            addToIndex(testFolder.getPath());
            addToIndex(testFolder.getPath());
        } catch (IOException e) {
            return;
        }      
    }

    public static void init() throws IOException {
        File git = new File("git");
        File objects = new File("git/objects");

        boolean doesEverythingExist = true;
        
        if (git.mkdir() == true) { // I can do this since .mkdir() and .createNewFile() return booleans and are idempotent :)
            doesEverythingExist = false;
        }

        if (objects.mkdir() == true) {
            doesEverythingExist = false;
        }

        File index = new File("git/index");
        File head = new File("git/HEAD");

        if (index.createNewFile() == true) {
            doesEverythingExist = false;
        }

        if (head.createNewFile() == true) {
            doesEverythingExist = false;
        }

        if (doesEverythingExist == true) {
            System.out.println("Git Repository Already Exists");
        } else {
            System.out.println("Git Repository Created");
        }
    }

    public static String hash(String filePath) throws IOException {
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line;
            StringBuilder text = new StringBuilder();
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append("\n");
            }

            // deletes last new line
            if (text.length() > 1) {
                text.delete(text.length() - 1, text.length());
            }

            br.close();

            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(text.toString().getBytes()); 
            String hashString = HexFormat.of().formatHex(hash);

            return hashString;

        } catch (NoSuchAlgorithmException e) {
            System.out.println("No such algorithm");
            return "";
        }
    }

    public static void createBlob(String filePath) {
        try {
            String hash = hash(filePath);
            File blob = new File("git/objects/" + hash);

            BufferedReader br = new BufferedReader(new FileReader(filePath));
            int c;
            FileWriter wr = new FileWriter(blob);
            while ((c = br.read()) != -1) {
                wr.write(c);
            }

            wr.close();
            br.close();

        } catch (IOException e) {
            return;
        }
    }

    public static void addToIndex(String filePath) {
        try {
            String hash = hash(filePath);
            String entry = hash + " " + filePath;
            
            BufferedReader br = new BufferedReader(new FileReader("git/index"));
            String line;
            StringBuilder sheet = new StringBuilder();
            while ((line = br.readLine()) != null) {
                // checks if entry already exists
                if (line.equals(entry)) {
                    br.close();
                    return;
                }
                
                // checks change in file
                if (line.split(" ")[1].equals(filePath) && !line.split(" ")[0].equals(hash)) {
                    sheet.append(entry);
                    sheet.append("\n");
                } else {
                    sheet.append(line);
                    sheet.append("\n");
                }
            }

            br.close();

            // deletes last new line
            if (sheet.length() > 0) {
                sheet.deleteCharAt(sheet.lastIndexOf("\n"));
            }

            // checks if file is empty
            if (hash("git/index").equals("da39a3ee5e6b4b0d3255bfef95601890afd80709")) { // SHA-1 hash for an empty file
                FileWriter wr = new FileWriter("git/index");
                wr.write(entry);
                wr.close();
            } else {
                FileWriter wr = new FileWriter("git/index");
                wr.write(sheet.toString());
                wr.close();
            }

        } catch (IOException e) {
            return;
        }
    }

    public static void rebuildTree() throws IOException {
        List<String> lines = Files.readAllLines(Path.of("git/index"));
        String deepestName = "";
        for (int i = 0; i < lines.size(); i++) {
            String[] hashAndName = lines.get(i).split(" ");
            if (hashAndName[1].split("/").length > deepestName.split("/").length) {
                deepestName = hashAndName[1];
            }
        }
        
    }

}
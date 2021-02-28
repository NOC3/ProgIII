package Server;

import Common.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ServerModel {
    Set<String> usersMail;

    public ServerModel() {
        usersMail = buildUsersMail();
    }

    public Set<String> buildUsersMail() {

        File folder = new File(String.valueOf((getClass().getResource("/mails/ciao.json"))));
        //      File folder = new File(System.getProperty("../../resources/mails"));

        System.out.println("Cartella : " + folder.getName());
        System.out.println("Cartella : " + folder.isDirectory());
        System.out.println("Cartella : " + folder.isFile());
        System.out.println("Cartella : " + folder.getAbsolutePath());
        System.out.println("Cartella : " + folder.canRead());
        System.out.println("Cartella : " + folder.toString());
        System.out.println("Contenuto : " + Arrays.toString(folder.list()));
        System.out.println();
        try {
            System.out.println("Walk "+Files.walk(Path.of(String.valueOf((getClass().getResource("/mails/"))))));
        }catch(Exception e){
            System.out.println(e);
        }
        System.out.println(folder.exists());

        File[] listOfFiles = folder.listFiles();

        Set<String> allUsers = new HashSet<>();

        assert listOfFiles != null;

        for (File f : listOfFiles)
            allUsers.add(f.getName());

        System.out.println(allUsers);

        return allUsers;
    }

    //innestare una classe che estenda Task (Server, il pool)
    // e una che implementi i singoli Runnable (switch su Message.codice)
    class Server {
        private int c;
    }
}

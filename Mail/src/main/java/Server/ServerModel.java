package Server;
import Common.*;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class ServerModel {
    Set<String> usersMail;

    public ServerModel(){
        usersMail=buildUsersMail();
    }

    public Set<String> buildUsersMail(){
//        File folder = new File(String.valueOf((getClass().getResource("./mails/"))));
        File folder = new File(System.getProperty("../../resources/mails"));

        System.out.println("Cartella : " + folder.getName());


        File [] listOfFiles = folder.listFiles();

        Set<String> allUsers = new HashSet<>();

        assert listOfFiles != null;

        for(File f : listOfFiles)
            allUsers.add(f.getName());

        System.out.println(allUsers);

        return allUsers;
    }

    //innestare una classe che estenda Task (Server, il pool)
    // e una che implementi i singoli Runnable (switch su Message.codice)
    class Server{
        private int c;
    }
}

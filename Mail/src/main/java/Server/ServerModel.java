package Server;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class ServerModel {
    Set<String> usersMail; //contains all "registered"/valid user's mail

    public ServerModel() {
        usersMail = buildUsersMail();
    }

    public Set<String> buildUsersMail() {

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource("./mails/");
        assert url != null;
        String path = url.getPath();
        File [] listOfFiles = new File(path).listFiles();

        Set<String> allUsers = new HashSet<>();
        assert listOfFiles != null;

        for (File f : listOfFiles)
            allUsers.add(f.getName().substring(0, f.getName().length()-5)); //removing .json

        //debug
        System.out.println(allUsers);

        return allUsers;
    }

    //innestare una classe che estenda Task (Server, il pool)
    // e una che implementi i singoli Runnable (switch su Message.codice)
    class Server {
        private int c;
    }
}

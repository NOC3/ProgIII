package Server;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class ServerModel {
    Set<String> usersMail; //contains all "registered"/valid user's mail
    Server srv;

    public ServerModel() {
        usersMail = buildUsersMail();
        srv = new Server();
    }

    //forse meglio set<File> per accessi futuri, es eliminazione/nuova mail
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


    /*
    * bozza per il 01/03
    * */

    //classi interne al model di comunicazione

    //forse deve essere un thread autonomo
    class Server { //la pool
        ServerSocket serverSocket;
        private static final int THREADNUM = 10;

        public Server(){
            try {
                serverSocket = new ServerSocket();
            } catch (Exception e){
                System.out.println(e.getLocalizedMessage());
            }
        }


        class Request implements Runnable{ //le singole request
            Socket clientSocket;
            ServerModel model;

            public Request(){
                clientSocket = new Socket();
                model = ServerModel.this;
            }

            @Override
            public void run() {

            }
        }
    }
}

package Server;

import Common.Message;
import org.json.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerModel {
    Set<String> usersMail; //contains all "registered"/valid user's mail
    Server srv;

    public ServerModel() {
        usersMail = buildUsersMail();
        srv = new Server();
        srv.start();

    }

    //forse meglio set<File> per accessi futuri, es eliminazione/nuova mail
    public Set<String> buildUsersMail() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource("./mails/");
        assert url != null;
        String path = url.getPath();

        File[] listOfFiles = new File(path).listFiles();

        Set<String> allUsers = new HashSet<>();
        assert listOfFiles != null;

        for (File f : listOfFiles)
            allUsers.add(f.getName().substring(0, f.getName().length() - 5)); //removing .json

        //debug
        System.out.println(allUsers);

        return allUsers;
    }


    private boolean checkLogin(String user){
        return usersMail.contains(user);
    }

    private JSONObject getMailbox(String user){
        JSONObject obj = null;
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            URL url = loader.getResource("./mails/"+user+".json");
            assert url != null;
            String path = url.getPath();
            FileReader filereader = new FileReader(path);
            JSONTokener tokener = new JSONTokener(filereader);
            obj = new JSONObject(tokener);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // typecasting obj to JSONObject
         return obj;
    }

    /*
     * bozza per il 01/03
     * */

    //classi interne al model di comunicazione

    //forse deve essere un thread autonomo
    class Server extends Thread { //la pool
        ServerSocket serverSocket;
        private static final int THREADNUM = 10;
        private final int port = 49152;

        @Override
        public void run() {
            try {
                //new threadPool
                ExecutorService execPool = Executors.newFixedThreadPool(THREADNUM);
                serverSocket = new ServerSocket(port);
                while(true) {
                    Socket incoming = serverSocket.accept();
                    execPool.execute(new Request(incoming));


                }
            } catch (Exception e) {
                System.out.println(e.getLocalizedMessage());
            } finally {
                //serversocket.close()
            }
        }


        class Request implements Runnable { //le singole request
            Socket clientSocket;
            ServerModel model;


            public Request(Socket socket) {
                clientSocket = socket;
                model = ServerModel.this;
            }

            @Override
            public void run() {
                Message message = null;
                try {
                    ObjectInputStream inputRequest = new ObjectInputStream(clientSocket.getInputStream());
                    Object msg = inputRequest.readObject();

                    if (msg instanceof Message){
                        message=(Message)msg;
                    }else{
                        //stop esecuzione
                    }
                }catch(Exception e){

                }finally{

                }

                switch(message.getOperation()){
                    case Message.SEND_NEW_EMAIL:
                        break;
                    case Message.REMOVE_EMAIL:
                        break;
                    case Message.LOGIN:
                        if(model.checkLogin((String)message.getObj())){
                            sendResponse( Message.SUCCESS,
                                    (model.getMailbox((String)message.getObj()).toString())
                            );
                        }else{
                            sendResponse( Message.ERROR,
                                    "User not found"
                            );
                        }


                        break;

                    default:
                        break;
                }
            }

            private void sendResponse(int status, Object o){
                try {
                    Message m = new Message(status,o);
                    ObjectOutputStream outputRequest = new ObjectOutputStream(clientSocket.getOutputStream());
                    outputRequest.writeObject(m);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

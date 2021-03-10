package Server;

import Common.Email;
import Common.Message;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.json.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class ServerModel {
    private Map<String, ReentrantReadWriteLock> usersLocks; //contains all "registered"/valid user's mail
    private Map<String, ArrayList<Integer>> usersMail;
    private Server srv;
    private ObservableList<Log> logs;

    public ServerModel() {
        logs = FXCollections.observableArrayList();
        buildUsersMail();

        srv = new Server();
        srv.start();
    }

    //forse meglio set<File> per accessi futuri, es eliminazione/nuova mail
    public void buildUsersMail() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource("./mails/");
        assert url != null;
        String path = url.getPath();

        File[] listOfFiles = new File(path).listFiles();

        usersLocks = new HashMap<>();
        usersMail = new HashMap<>();

        assert listOfFiles != null;

        for (File f : listOfFiles) {
            usersLocks.put(f.getName().substring(0, f.getName().length() - 5), new ReentrantReadWriteLock()); //removing .json
            usersMail.put(f.getName().substring(0, f.getName().length() - 5), new ArrayList<>());
        }
    }

    public ObservableList<Log> getLogs() {
        return logs;
    }

    private boolean checkLogin(String user) {
        return usersMail.containsKey(user);
    }

    private JSONObject getMailbox(String user) {
        JSONObject obj = null;
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            URL url = loader.getResource("./mails/" + user + ".json");
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

    private String recipientsExist(Email e) {
        ArrayList<String> rec = e.getRecipients();
        String recNotFound = "";
        for (String r : rec) {
            if (!checkLogin(r))
                recNotFound += r + ", ";
        }
        return recNotFound.equals("") ? "" : recNotFound.substring(0, recNotFound.length() - 2);
    }

    private int sendEmail(Email e) {
        int resId = -1;

        ArrayList<String> writeOnUser = e.getRecipients();

        //write on sender
        resId = writeOnJson(e, e.getSender(), "sent");

        //write on recipients
        for (String user : writeOnUser) {
            writeOnJson(e, user, "inbox");
            //notifica l'utente
        }

        return resId;
    }

    private boolean deleteOnJson(Email e, String user, String key) {
        //scrittura
        Lock wLock = (usersLocks.get(e.getSender())).writeLock();
        wLock.lock();
        boolean found = false;

        JSONObject json = getMailbox(user);

        //json.toString().replace(e.toJSON().toString(), "");

        for (int i = 0; i < ((JSONArray) json.opt(key)).length(); i++) {
            if (((JSONObject) ((JSONArray) json.opt(key)).get(i)).getInt("id") == e.getID()) {
                ((JSONArray) json.opt(key)).remove(i);
                found = true;
                break;
            }
        }

        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            URL url = loader.getResource("./mails/" + user + ".json");
            assert url != null;
            String path = url.getPath();
            FileWriter file = new FileWriter(path);
            file.write(json.toString());
            file.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        wLock.unlock();

        return found;
    }


    private int writeOnJson(Email e, String user, String key) {
        //scrittura
        Lock wLock = (usersLocks.get(e.getSender())).writeLock();
        wLock.lock();

        JSONObject json = getMailbox(user);


        int lastId = (int) json.opt("last_" + key + "_id");
        lastId += 1;

        json.put("last_" + key + "_id", lastId);

        e.setID(lastId);
        ((JSONArray) json.opt(key)).put(e.toJSON());

        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            URL url = loader.getResource("./mails/" + user + ".json");
            assert url != null;
            String path = url.getPath();
            FileWriter file = new FileWriter(path);
            file.write(json.toString());
            file.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        wLock.unlock();

        return lastId;
    }

    //classi interne al model di comunicazione

    class Server extends Thread { //la pool
        private ServerSocket serverSocket;
        private static final int THREADNUM = 10;
        private final int port = 49152;

        @Override
        public void run() {
            try {
                //new threadPool
                ExecutorService execPool = Executors.newFixedThreadPool(THREADNUM);
                serverSocket = new ServerSocket(port);
                while (true) {
                    Socket incoming = serverSocket.accept();

                    execPool.execute(new Request(incoming));
                }
            } catch (Exception e) {
                System.out.println(e.getLocalizedMessage());
            } finally {
                // serverSocket.close();
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

                    if (msg instanceof Message) {
                        message = (Message) msg;
                    } else {
                        //stop esecuzione
                    }
                } catch (Exception e) {

                } finally {

                }

                switch (message.getOperation()) {
                    case Message.SEND_NEW_EMAIL:
                        String recipientsNotFound = model.recipientsExist((Email) message.getObj());

                        if (recipientsNotFound.equals("")) {
                            int id = model.sendEmail((Email) message.getObj());
                            if (id != -1) {
                                sendResponse(Message.SUCCESS, id);
                                model.logs.add(new Log("Email inviata correttamente"));
                            } else {
                                sendResponse(Message.ERROR, "Errore nell'invio email");
                                model.logs.add(new Log("Errore nell'invio mail - cod: " + id));
                            }
                        } else {
                            sendResponse(Message.ERROR, "Recipient not found" + recipientsNotFound);
                            model.logs.add(new Log("Recipient not found"));
                        }

                        break;

                    case Message.REMOVE_EMAIL_INBOX:
                        if (deleteOnJson((Email) message.getObj(), ((Email) message.getObj()).getSender(), "inbox")) {
                            sendResponse(Message.SUCCESS, "Mail eliminata correttamente ");
                            model.logs.add(new Log("Mail eliminata correttamente "+ (((Email) message.getObj()).getSender())));
                        } else {
                            sendResponse(Message.ERROR, "Errore eliminazione mail");
                            model.logs.add(new Log("Mail eliminata correttamente "+ (((Email) message.getObj()).getSender())));

                        }
                        break;
                    case Message.REMOVE_EMAIL_SENT:

                        if (deleteOnJson((Email) message.getObj(), ((Email) message.getObj()).getSender(), "sent")) {
                            sendResponse(Message.SUCCESS, "Mail eliminata correttamente");
                            model.logs.add(new Log("Mail eliminata correttamente "+ (((Email) message.getObj()).getSender())));

                        } else {
                            sendResponse(Message.ERROR, "Errore eliminazione mail");
                            model.logs.add(new Log("Errore eliminazione mail "+ (((Email) message.getObj()).getSender())));
                        }


                        break;
                    case Message.LOGIN:
                        if (model.checkLogin((String) message.getObj())) {
                            sendResponse(Message.SUCCESS,
                                    (model.getMailbox((String) message.getObj()).toString())
                            );
                            model.logs.add(new Log("Login success from: " + ((String) message.getObj())));
                        } else {
                            sendResponse(Message.ERROR,
                                    "User not found"
                            );
                            model.logs.add(new Log("Login fail from: " + ((String) message.getObj())));
                        }


                        break;

                    default:
                        break;
                }
            }

            private void sendResponse(short status, Object o) {
                try {
                    Message m = new Message(status, o);
                    ObjectOutputStream outputRequest = new ObjectOutputStream(clientSocket.getOutputStream());
                    outputRequest.writeObject(m);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class Log {
        private String logEvent;
        private String logTime;

        public Log(String event) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            logTime = sdf.format(cal.getTime());
            logEvent = event;
        }

        public String getLogEvent() {
            return logEvent;
        }

        public String getLogTime() {
            return logTime;
        }
    }
}
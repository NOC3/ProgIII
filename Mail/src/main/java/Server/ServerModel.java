package Server;

import Common.Email;
import Common.Message;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;
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
    private Map<String, ReentrantReadWriteLock> usersLocks;
    private Map<String, ArrayList<Integer>> usersMail;
    private Server srv;
    private ObservableList<Log> logs;

    public ServerModel() {
        logs = FXCollections.observableArrayList();
        buildUsersMail();

        srv = new Server();
        srv.setDaemon(true);
        srv.start();
    }

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
        Lock rLock = (usersLocks.get(user)).readLock();
        rLock.lock();
        JSONObject obj = null;
        FileReader filereader = null;
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            URL url = loader.getResource("./mails/" + user + ".json");
            assert url != null;
            String path = url.getPath();
            filereader = new FileReader(path);
            JSONTokener tokener = new JSONTokener(filereader);
            obj = new JSONObject(tokener);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (filereader != null) {
                try {
                    filereader.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            rLock.unlock();
        }

        return obj;
    }

    private String recipientsExist(Email e) {
        ArrayList<String> rec = e.getRecipients();
        System.out.println(rec);
        String recNotFound = "";
        for (String r : rec) {
            System.out.println(r);
            if (!checkLogin(r)) {
                System.out.println("-- Not found");
                recNotFound += r + ",";
            }
        }
        System.out.println(recNotFound);
        return recNotFound.equals("") ? "" : recNotFound.substring(0, recNotFound.length() - 1);
    }

    private int sendEmail(Email e) {
        int resId;

        ArrayList<String> writeOnUser = e.getRecipients();

        //write on sender
        resId = writeOnJson(e, e.getSender(), "sent");

        //write on recipients
        for (String user : writeOnUser) {
            Integer id = writeOnJson(e, user, "inbox");
            usersMail.get(user).add(id);
        }

        return resId;
    }

    private boolean deleteOnJson(Email e, String user, String key) {
        Lock wLock = (usersLocks.get(e.getSender())).writeLock();
        wLock.lock();
        boolean found = false;

        JSONObject json = getMailbox(user);

        System.out.println(json.opt(key) + "\n" + ((JSONArray) json.opt(key)).length());
        for (int i = 0; i < ((JSONArray) json.opt(key)).length(); i++) {
            System.out.println(((JSONObject) ((JSONArray) json.opt(key)).get(i)).getInt("id") + " | " + e.getID());

            if (((JSONObject) ((JSONArray) json.opt(key)).get(i)).getInt("id") == e.getID()) {
                ((JSONArray) json.opt(key)).remove(i);
                found = true;
                break;
            }
        }

        FileWriter file = null;
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            URL url = loader.getResource("./mails/" + user + ".json");
            assert url != null;
            String path = url.getPath();
            file = new FileWriter(path);
            file.write(json.toString());
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            wLock.unlock();
        }

        return found;
    }


    private int writeOnJson(Email e, String user, String key) {
        Lock wLock = (usersLocks.get(e.getSender())).writeLock();
        wLock.lock();

        JSONObject json = getMailbox(user);

        int lastId = (int) json.opt("last_" + key + "_id");
        lastId += 1;

        json.put("last_" + key + "_id", lastId);

        e.setID(lastId);
        ((JSONArray) json.opt(key)).put(e.toJSON());

        FileWriter file = null;
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            URL url = loader.getResource("./mails/" + user + ".json");
            assert url != null;
            String path = url.getPath();
            file = new FileWriter(path);
            file.write(json.toString());
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            wLock.unlock();
        }

        return lastId;
    }

    private boolean checkNewEmail(String user) {
        return !usersMail.get(user).isEmpty();
    }

    private JSONObject getNewEmails(String user) {
        JSONArray res = new JSONArray();
        JSONObject js = new JSONObject();

        Lock rLock = (usersLocks.get(user)).readLock();
        rLock.lock();

        JSONArray json = (JSONArray) getMailbox(user).opt("inbox");
        ArrayList<Integer> idList = usersMail.get(user);
        for (int i = json.length() - 1; !idList.isEmpty(); i--) {
            if (idList.contains((((JSONObject) json.get(i)).getInt("id")))) {
                idList.remove((Object) (((JSONObject) json.get(i)).getInt("id")));
                res.put(json.get(i));
            }
        }

        rLock.unlock();
        js.put("new", res);
        return js;
    }

    public ExecutorService getServerExecutor() {
        return this.srv.execPool;
    }

    //classi interne al model di comunicazione
    class Server extends Thread {
        private ServerSocket serverSocket;
        private static final int THREADNUM = 10;
        private final int port = 49152;
        private ExecutorService execPool;

        @Override
        public void run() {
            execPool = null;
            try {
                //new threadPool
                execPool = Executors.newFixedThreadPool(THREADNUM);

                serverSocket = new ServerSocket(port);

                while (true) {
                    Socket incoming = serverSocket.accept();
                    execPool.execute(new Request(incoming));
                }

            } catch (Exception e) {
                System.out.println(e.getLocalizedMessage());

            } finally {
                try {
                    if (serverSocket != null)
                        serverSocket.close();

                    if (!execPool.isShutdown())
                        execPool.shutdown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
                Message message;
                ObjectInputStream inputRequest = null;
                try {
                    inputRequest = new ObjectInputStream(clientSocket.getInputStream());
                    Object msg = inputRequest.readObject();

                    if (msg instanceof Message)
                        message = (Message) msg;
                    else
                        return;

                    Object o = null;
                    short status = Message.ERROR;
                    String logMsg = null;

                    switch (message.getOperation()) {
                        case Message.SEND_NEW_EMAIL:
                            String recipientsNotFound = model.recipientsExist((Email) message.getObj());
                            if (recipientsNotFound.equals("")) {
                                int id = model.sendEmail((Email) message.getObj());
                                if (id != -1) {
                                    status = Message.SUCCESS;
                                    o = id;
                                    logMsg = "Email inviata correttamente";
                                } else {
                                    o = "Errore nell'invio email";
                                    logMsg = "Errore nell'invio mail - cod: " + id;
                                }
                            } else {
                                o = "Destinatari non esistenti: " + recipientsNotFound;
                                logMsg = "Destinatari errati";
                            }
                            break;

                        case Message.REMOVE_EMAIL_INBOX:
                            if (deleteOnJson((Email) ((Pair) message.getObj()).getKey(), (String) ((Pair) message.getObj()).getValue(), "inbox")) {
                                status = Message.SUCCESS;
                                o = "Mail eliminata correttamente ";
                                logMsg = "Mail eliminata correttamente " + (((Pair) message.getObj()).getValue());
                            } else {
                                o = "Errore eliminazione mail";
                                logMsg = "Errore eliminazione mail " + (((Pair) message.getObj()).getValue());
                            }
                            break;

                        case Message.REMOVE_EMAIL_SENT:
                            if (deleteOnJson((Email) ((Pair) message.getObj()).getKey(), (String) ((Pair) message.getObj()).getValue(), "sent")) {
                                status = Message.SUCCESS;
                                o = "Mail eliminata correttamente";
                                logMsg = "Mail eliminata correttamente " + (((Pair) message.getObj()).getValue());
                            } else {
                                o = "Errore eliminazione mail";
                                logMsg = "Errore eliminazione mail " + (((Pair) message.getObj()).getValue());
                            }
                            break;

                        case Message.LOGIN:
                            if (model.checkLogin((String) message.getObj())) {
                                model.usersMail.get(message.getObj()).clear();
                                status = Message.SUCCESS;
                                o = model.getMailbox((String) message.getObj()).toString();
                                logMsg = "Login success from: " + (message.getObj());
                            } else {
                                o = "Email non trovata";
                                logMsg = "Login fallito: " + (message.getObj());
                            }
                            break;

                        case Message.CHECK_NEW:
                            if (checkNewEmail((String) message.getObj())) {
                                status = Message.SUCCESS;
                                o = getNewEmails((String) message.getObj()).toString();
                                logMsg = "Invio nuove email a " + message.getObj();
                            } else {
                                o = "Nessuna nuova email";
                                logMsg = "No nuove email per " + message.getObj();
                            }
                            break;

                        default:
                            break;
                    }

                    sendResponse(status, o);
                    model.logs.add(0, new Log(logMsg));

                } catch (Exception e) {
                    System.out.println(e.getLocalizedMessage());
                } finally {
                    try {
                        if (clientSocket != null)
                            clientSocket.close();
                        if (inputRequest != null)
                            inputRequest.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            private void sendResponse(short status, Object o) {
                ObjectOutputStream outputRequest = null;
                try {
                    Message m = new Message(status, o);
                    outputRequest = new ObjectOutputStream(clientSocket.getOutputStream());
                    outputRequest.writeObject(m);
                    outputRequest.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (outputRequest != null)
                            outputRequest.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
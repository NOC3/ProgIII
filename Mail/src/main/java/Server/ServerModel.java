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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

//Model del server
public class ServerModel {
    //         < mail ,     < lock                 , unpulled mails    >>
    private Map<String, Pair<ReentrantReadWriteLock, ArrayList<Integer>>> usersUtil;
    private final Server srv;
    private final ObservableList<Log> logs;

    public ServerModel() {
        logs = FXCollections.observableArrayList();
        buildUsersUtil();

        //inizializzazione del server (la pool di thread)
        srv = new Server();
        srv.setDaemon(true);
        srv.start();
    }

    //inizializza per ogni utente il reentrantLock e l'array delle nuove email
    public void buildUsersUtil() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource("./mails/");
        assert url != null;
        String path = url.getPath();

        File[] listOfFiles = new File(path).listFiles();

        usersUtil = new HashMap<>();

        assert listOfFiles != null;

        for (File f : listOfFiles) {
            String user = f.getName().substring(0, f.getName().length() - 5);
            usersUtil.put(user, new Pair<>(new ReentrantReadWriteLock(), new ArrayList<>()));
            checkUnpulledMail(user);
        }
    }

    //recupera gli id delle nuove email dell'utente
    private void checkUnpulledMail(String user) {
        Lock wLock = usersUtil.get(user).getKey().writeLock();
        wLock.lock();

        JSONObject json = getMailbox(user);

        JSONArray js = (JSONArray) json.opt("unpulled_id");

        if (js != null && js.length() != 0) {
            ArrayList<Integer> idList = usersUtil.get(user).getValue();

            for (int i = js.length() - 1; i >= 0; i--)
                idList.add((Integer) js.remove(i));

            updateJson(user, json);
        }

        wLock.unlock();
    }

    //scrive su file gli id delle nuove email (alla chiusura del server)
    private void writeUnpulledEmail(String user) {
        Lock wLock = usersUtil.get(user).getKey().writeLock();
        wLock.lock();
        JSONObject json = getMailbox(user);

        for (int i = usersUtil.get(user).getValue().size() - 1; i >= 0; i--)
            ((JSONArray) json.opt("unpulled_id")).put(usersUtil.get(user).getValue().remove(i));

        updateJson(user, json);
        wLock.unlock();
    }

    //scrive il json aggiornato dell'utente
    private void updateJson(String user, JSONObject json) {
        FileWriter file = null;
        Lock wLock = usersUtil.get(user).getKey().writeLock();
        wLock.lock();
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
    }

    public ObservableList<Log> getLogs() {
        return logs;
    }

    //controlla che l'email utente sia valida
    private boolean checkLogin(String user) {
        return usersUtil.containsKey(user);
    }

    //restituisce l'intero JSONObject dell'utente
    private JSONObject getMailbox(String user) {
        Lock rLock = usersUtil.get(user).getKey().readLock();
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

    //controlla l'esistenza dei destinatari
    private String recipientsExist(Email e) {
        ArrayList<String> rec = e.getRecipients();
        String recNotFound = "";
        for (String r : rec) {
            if (!checkLogin(r))
                recNotFound += r + ",";
        }
        return recNotFound.equals("") ? "" : recNotFound.substring(0, recNotFound.length() - 1);
    }

    //aggiorna i file di mittente e destinatari, aggiunge l'id della mail nell'inbox dei destinatari
    //restituisce l'id della mail del mittente
    private int sendEmail(Email e) {
        int resId;

        ArrayList<String> writeOnUser = e.getRecipients();

        //scrittura sul json del mittente
        resId = writeOnJson(e, e.getSender(), "sent");

        //scrittura sui json dei destinatari
        for (String user : writeOnUser) {
            Integer id = writeOnJson(e, user, "inbox");
            usersUtil.get(user).getValue().add(id);
        }

        return resId;
    }

    //elimina un'email da inbox o sent di un utente
    private boolean deleteOnJson(Email e, String user, String key) {
        Lock wLock = (usersUtil.get(e.getSender())).getKey().writeLock();
        wLock.lock();
        boolean found = false;

        JSONObject json = getMailbox(user);

        for (int i = 0; i < ((JSONArray) json.opt(key)).length(); i++) {
            if (((JSONObject) ((JSONArray) json.opt(key)).get(i)).getInt("id") == e.getID()) {
                ((JSONArray) json.opt(key)).remove(i);
                found = true;
                break;
            }
        }

        updateJson(user, json);
        wLock.unlock();

        return found;
    }

    //aggiunge una email a inbox o sent di un utente
    private int writeOnJson(Email e, String user, String key) {
        Lock wLock = usersUtil.get(e.getSender()).getKey().writeLock();
        wLock.lock();

        JSONObject json = getMailbox(user);

        int lastId = (int) json.opt("last_" + key + "_id");
        lastId += 1;

        json.put("last_" + key + "_id", lastId);

        e.setID(lastId);
        ((JSONArray) json.opt(key)).put(e.toJSON());

        updateJson(user, json);
        wLock.unlock();

        return lastId;
    }

    //controlla se ci sono nuove email per un utente
    private boolean checkNewEmail(String user) {
        return !usersUtil.get(user).getValue().isEmpty();
    }

    //restituisce le nuove email dell'utente
    private JSONObject getNewEmails(String user) {
        JSONArray res = new JSONArray();
        JSONObject js = new JSONObject();

        Lock rLock = usersUtil.get(user).getKey().readLock();
        rLock.lock();

        JSONArray json = (JSONArray) getMailbox(user).opt("inbox");
        ArrayList<Integer> idList = usersUtil.get(user).getValue();
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

    //chiusura del server, quindi della pool
    void shutdownPool() {
        srv.execPool.shutdown();
        try {
            if (!srv.execPool.awaitTermination(60, TimeUnit.SECONDS)) {
                srv.execPool.shutdownNow();
                if (!srv.execPool.awaitTermination(60, TimeUnit.SECONDS))
                    System.out.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            srv.execPool.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            for (String user : usersUtil.keySet()) {
                if (!usersUtil.get(user).getValue().isEmpty())
                    writeUnpulledEmail(user);
            }
        }
    }

    /*
      classi interne al model di comunicazione
    */

    //gestisce la pool di thread che accetta richieste dai client
    class Server extends Thread {
        private ServerSocket serverSocket;
        //numero di thread della pool
        private static final int THREADNUM = 10;
        //parametro configurazione delle socket
        private final int port = 49152;
        private ExecutorService execPool;

        @Override
        public void run() {
            execPool = null;
            try {
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

                    if (execPool != null && !execPool.isShutdown())
                        ServerModel.this.shutdownPool();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        //gestione singole richieste dei client
        class Request implements Runnable {
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
                                model.usersUtil.get(message.getObj()).getValue().clear();
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

            //invio risposta al client
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

    //rappresentazione dell'evento
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
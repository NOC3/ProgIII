package Client;

import Common.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.util.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;

//Model del client
public class ClientModel {
    //parametri configurazione delle socket
    private String host = "localhost";
    private final int port = 49152;

    private String email;
    private SimpleListProperty<Email> inbox;
    private SimpleListProperty<Email> sent;
    private SimpleListProperty<String> notificationsList;

    public ClientModel(String email, JSONObject mailbox) {
        this.email = email;

        inbox = new SimpleListProperty<>();
        inbox.set(FXCollections.observableArrayList(new ArrayList<>()));
        sent = new SimpleListProperty<>();
        sent.set(FXCollections.observableArrayList(new ArrayList<>()));

        notificationsList = new SimpleListProperty<>();
        notificationsList.set(FXCollections.observableArrayList(new ArrayList<>()));

        parseMailbox((JSONArray) mailbox.opt("inbox"), inbox);
        parseMailbox((JSONArray) mailbox.opt("sent"), sent);

        //daemon di richiesta periodica email
        Request dm = new Request(Message.CHECK_NEW, this.email);
        dm.start();
    }


    //aggiunge alla lista passata come parametro le email presenti nel json
    //return del numero totale di email aggiunte
    public int parseMailbox(JSONArray array, SimpleListProperty<Email> list) {
        JSONObject mail;
        int i;

        for (i = 0; (mail = (JSONObject) array.opt(i)) != null; i++)
            list.add(0, fromJsonToEmail(mail));

        return i;
    }

    //funzione di parsing da json a oggetto Email
    public Email fromJsonToEmail(JSONObject jo) {
        Email e =
                new Email(jo.opt("id"), jo.opt("sender"), jo.opt("recipients"),
                        jo.opt("subject"), jo.opt("text"), jo.opt("date"));

        return e;
    }

    //metodi di get
    public SimpleListProperty<Email> getInbox() {
        return inbox;
    }

    public SimpleListProperty<String> getNotificationsList() {
        return notificationsList;
    }

    public SimpleListProperty<Email> getSent() {
        return sent;
    }

    public String getEmail() {
        return email;
    }

    //funzioni chiamate dal controller
    public void sendNewEmail(Email e) {
        ClientModel.Request send = new ClientModel.Request(Message.SEND_NEW_EMAIL, e);
        send.start();
    }

    public void deleteEmail(Email e, short op) {
        if (e != null) {
            Pair<Email, String> p = new Pair<>(e, email);
            Request r = new Request(op, p);
            r.start();
        }
    }

    //eliminazione dell'email usata nella Request se l'operazione si conclude correttamente
    public void deleteEmailFromList(SimpleListProperty<Email> list, Email e) {
        list.remove(e);
    }

    //Oggetto Request, daemon di comunicazione con il server
    class Request extends Thread {

        private short operation;
        private Object object;

        public Request(short op, Object o) {
            operation = op;
            object = o;
            this.setDaemon(true);
        }

        @Override
        public void run() {
            Message m = new Message(operation, object);
            Message response;


            if (operation == Message.CHECK_NEW) {
                //Request unica per utente, loop per la durata della connessione
                while (true) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    response = communicateToServer(m);

                    assert response != null;
                    if (response.getOperation() == Message.SUCCESS) {
                        JSONObject js = new JSONObject((String) response.getObj());
                        int mailNum = parseMailbox((JSONArray) js.opt("new"), inbox);
                        String msg = "Hai ricevuto: " + mailNum + " nuov" + (mailNum == 1 ? "a" : "e") + " mail";
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                notificationsList.add(0, msg);
                            }
                        });
                    }
                }
            } else { //Request che non sono il daemon che controlla se ci sono nuove email
                response = communicateToServer(m);

                assert response != null;
                Message finalResponse = response;

                //switch delle risposte dal server
                if (response.getOperation() == Message.SUCCESS) {
                    switch (this.operation) {
                        case Message.REMOVE_EMAIL_INBOX:
                            deleteEmailFromList(inbox, (Email) ((Pair) object).getKey());

                            Platform.runLater(
                                    () -> notificationsList.add(0, (String) finalResponse.getObj())
                            );
                            break;

                        case Message.REMOVE_EMAIL_SENT:
                            deleteEmailFromList(sent, (Email) ((Pair) object).getKey());

                            Platform.runLater(
                                    () -> notificationsList.add(0, (String) finalResponse.getObj())
                            );
                            break;

                        case Message.SEND_NEW_EMAIL:
                            Email ne = (Email) object;
                            ne.setID((int) response.getObj());
                            sent.add(0, ne);
                            String msg = "Mail inviata correttamente a: " + ne.recipientsToString();

                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    notificationsList.add(0, msg);
                                }
                            });
                            break;

                        default:
                            break;
                    }
                } else {
                    Platform.runLater(
                            () ->notificationsList.add(0, (String) finalResponse.getObj())
                    );
                }
            }
        }

        //metodo di comunicazione verso il server
        private Message communicateToServer(Message toSend) {
            Socket socket = null;
            ObjectOutputStream out = null;
            ObjectInputStream in = null;
            while (true) {
                try {
                    socket = new Socket(host, port);

                    out = new ObjectOutputStream(socket.getOutputStream());
                    out.writeObject(toSend);

                    in = new ObjectInputStream(socket.getInputStream());
                    return (Message) in.readObject();
                } catch (ConnectException e) {
                    Platform.runLater(
                            () -> {
                                String err = "Errore: impossibile connettersi al server";
                                notificationsList.add(0, err);
                            }
                    );
                    try {
                        Thread.sleep(20000);
                    } catch (Exception ee) {
                        Platform.runLater(
                                () -> {
                                    String err = "Errore generico di comunicazione con il server";
                                    notificationsList.add(0, err);
                                }
                        );
                        System.out.println(ee);
                    }
                } catch (Exception e) {
                    Platform.runLater(
                            () -> {
                                String err = "Errore generico di comunicazione con il server";
                                notificationsList.add(0, err);
                            }
                    );
                } finally {
                    try {
                        if (out != null)
                            out.close();

                        if (in != null)
                            in.close();

                        if (socket != null)
                            socket.close();

                    } catch (Exception e) {
                        System.out.println("Errore chiusura stream o socket: \n" + e);
                    }
                }
            }
        }
    }
}

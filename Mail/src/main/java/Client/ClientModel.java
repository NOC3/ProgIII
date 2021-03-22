package Client;

import Common.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.TitledPane;
import javafx.util.Pair;
import org.json.JSONArray;
import org.json.JSONObject;


import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;

public class ClientModel {
    private String host = "localhost";
    private final int port = 49152;

    private String email;
    private SimpleListProperty<Email> inbox;
    private SimpleListProperty<Email> sent;
    private SimpleListProperty<String> notificationsList;

    public ClientModel(String email, JSONObject mailbox) {
        this.email = email;

        inbox = new SimpleListProperty<Email>();
        inbox.set(FXCollections.observableArrayList(new ArrayList<Email>()));
        sent = new SimpleListProperty<Email>();
        sent.set(FXCollections.observableArrayList(new ArrayList<Email>()));

        notificationsList = new SimpleListProperty<String>();
        notificationsList.set(FXCollections.observableArrayList(new ArrayList<String>()));

        parseMailbox((JSONArray) mailbox.opt("inbox"), inbox);
        parseMailbox((JSONArray) mailbox.opt("sent"), sent);

        Request dm = new Request(Message.CHECK_NEW, this.email);
        dm.start();
    }


    public int parseMailbox(JSONArray array, SimpleListProperty<Email> list) {
        // Iterator i = array.iterator();
        JSONObject email = null;
        int i;
        for (i = 0; (email = (JSONObject) array.opt(i)) != null; i++) {
            Email e = fromJsonToEmail(email);
            list.add(0, e);
        }
        return i;
    }


    public Email fromJsonToEmail(JSONObject jo) {

        Email e =
                new Email(jo.opt("id"), jo.opt("sender"), jo.opt("recipients"),
                        jo.opt("subject"), jo.opt("text"), jo.opt("date"));

        return e;
    }

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

    public void deleteEmailFromList(SimpleListProperty<Email> list, Email e) {
        list.remove(e);
    }

    class Request extends Thread {

        private short operation;
        private Object object;

        public Request(short op, Object o) {
            operation = op;
            object = o;
            this.setDaemon(true);
        }


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
                } finally{
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

        public void run() {
            Message m = new Message(operation, object);
            Message response = null;

            //op that gets a "special treatment"
            if (operation == Message.CHECK_NEW) {
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
            } else { //si può togliere dato il while true
                response = communicateToServer(m);

                assert response != null;
                Message finalResponse = response;

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
                        () ->
                            notificationsList.add(0, (String) finalResponse.getObj())
                    );
                }
            }
        }
    }
}

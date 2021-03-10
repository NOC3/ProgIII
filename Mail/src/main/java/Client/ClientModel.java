package Client;

import Common.*;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ClientModel {
    private String host = "localhost";
    private final int port = 49152;

    private String email;
    private SimpleListProperty<Email> inbox;
    private SimpleListProperty<Email> sent;

    public ClientModel(String email, JSONObject mailbox) {
        this.email = email;

        inbox = new SimpleListProperty<Email>();
        inbox.set(FXCollections.observableArrayList(new ArrayList<Email>()));
        sent = new SimpleListProperty<Email>();
        sent.set(FXCollections.observableArrayList(new ArrayList<Email>()));


        parseMailbox((JSONArray) mailbox.opt("inbox"), inbox);
        parseMailbox((JSONArray) mailbox.opt("sent"), sent);


    }

    /*    public void initModel(ArrayList<Email> inbox) {
            this.inbox = new SimpleListProperty<Email>();
            this.inbox.set(FXCollections.observableArrayList(inbox));
        }
    */
    public void parseMailbox(JSONArray array, SimpleListProperty<Email> list) {
        // Iterator i = array.iterator();
        JSONObject email = null;
        for (int i = 0; (email = (JSONObject) array.opt(i)) != null; i++) {
            Email e = fromJsonToEmail(email);
            list.add(e);
        }
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
        System.out.println(e);
        Request r = new Request(op, e);
        r.start();
    }

    public void deleteEmailFromList(SimpleListProperty<Email> list, Email e){
        list.remove(e);
    }



    class Request extends Thread {

        private short operation;
        private Object object;

        public Request(short op, Object o) {
            operation = op;
            object = o;
        }

        public void run() {
            Message m = new Message(operation, object);
            try {
                Socket socket = new Socket(host, port);

                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject(m);

                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                Message response = (Message) in.readObject();

                if (response.getOperation() == Message.SUCCESS) {
                    switch (this.operation) {
                        case Message.REMOVE_EMAIL_INBOX:
                            deleteEmailFromList(inbox, (Email)object);
                            break;
                        case Message.REMOVE_EMAIL_SENT:
                            deleteEmailFromList(sent, (Email)object);
                            break;
                        default:
                            break;
                    }
                } else {
                    //errore scritto nelle notifiche del client
                }
            } catch (Exception e) {
                System.out.println("Errore client " + e);

            }
        }
    }

}

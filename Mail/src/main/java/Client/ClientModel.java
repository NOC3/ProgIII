package Client;

import Common.*;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class ClientModel {

    private String email;
    private SimpleListProperty<Email> inbox;
    private SimpleListProperty<Email> sent;

    public ClientModel(JSONObject mailbox) {
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
    public void parseMailbox(JSONArray array, SimpleListProperty<Email> list){
        // Iterator i = array.iterator();
        JSONObject email = null;
        for(int i = 0; (email=(JSONObject) array.opt(i))!=null; i++ ){
            Email e = fromJsonToEmail(email);
            list.add(e);
        }
    }

    public Email fromJsonToEmail(JSONObject jo){
        Email e =
                new Email(jo.opt("id"), jo.get("sender"), jo.opt("recipients"),
                jo.opt("subject"), jo.opt("text"), jo.opt("date"));
        return e;
    }

    public SimpleListProperty<Email> getInbox() {
        return inbox;
    }

    public SimpleListProperty<Email> getSent() {
        return sent;
    }
}

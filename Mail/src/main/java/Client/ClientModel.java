package Client;

import Common.*;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;

import java.util.ArrayList;

public class ClientModel {

    private String email;
    private SimpleListProperty<Email> inbox;
    private SimpleListProperty<Email> sent;

    public ClientModel() {

    }

    public void initModel(ArrayList<Email> inbox) {
        this.inbox = new SimpleListProperty<Email>();
        this.inbox.set(FXCollections.observableArrayList(inbox));
    }

}

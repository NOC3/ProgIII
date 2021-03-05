package Client;


import Common.Email;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Date;

public class ClientController {

    ClientModel model;

    @FXML
    private Tab inbox;
    @FXML
    private Tab sent;

    //@FXML
    //private ListView<Email> inboxList;
    //@FXML
    //private ListView<Email> sentList;

    @FXML
    TableView<Email> inboxList;
    @FXML
    TableColumn<Email, String> inboxDateColumn;
    @FXML
    TableColumn<Email, String> inboxSenderColumn;
    @FXML
    TableColumn<Email, String> inboxSubjectColumn;

    @FXML
    TableView<Email> sentList;
    @FXML
    TableColumn<Email, String> sentDateColumn;
    @FXML
    TableColumn<Email, String> sentRecipientsColumn;
    @FXML
    TableColumn<Email, String> sentSubjectColumn;


    public void setModel(ClientModel m){
        model=m;
        inboxDateColumn.setCellValueFactory(email
                ->new SimpleStringProperty((email.getValue().getDate()).toString()));

        inboxSenderColumn.setCellValueFactory(email
                ->new SimpleStringProperty(email.getValue().getSender()));

        inboxSubjectColumn.setCellValueFactory(email
                ->new SimpleStringProperty(email.getValue().getSubject()));

        sentDateColumn.setCellValueFactory(email
                ->new SimpleStringProperty((email.getValue().getDate()).toString()));

        sentRecipientsColumn.setCellValueFactory(email
                ->new SimpleStringProperty((email.getValue().getRecipients()).toString()));

        sentSubjectColumn.setCellValueFactory(email
                ->new SimpleStringProperty(email.getValue().getSubject()));


        sentList.setItems(this.model.getSent());
        inboxList.setItems(this.model.getInbox());

    }



}

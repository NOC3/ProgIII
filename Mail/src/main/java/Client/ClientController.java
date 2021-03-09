package Client;


import Common.Email;
import Common.Message;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;


import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientController {

    ClientModel model;

    @FXML
    private Tab inbox;
    @FXML
    private Tab sent;

    @FXML
    private TableView<Email> inboxList;
    @FXML
    private TableColumn<Email, String> inboxDateColumn;
    @FXML
    private TableColumn<Email, String> inboxSenderColumn;
    @FXML
    private TableColumn<Email, String> inboxSubjectColumn;

    @FXML
    private TableView<Email> sentList;
    @FXML
    private TableColumn<Email, String> sentDateColumn;
    @FXML
    private TableColumn<Email, String> sentRecipientsColumn;
    @FXML
    private TableColumn<Email, String> sentSubjectColumn;

    @FXML
    private Text sentSubjectText;
    @FXML
    private Text sentTextText;
    @FXML
    private Text sentRecipientsText;
    @FXML
    private Text sentDataText;

    @FXML
    private Text inboxSubjectText;
    @FXML
    private Text inboxTextText;
    @FXML
    private Text inboxSenderText;
    @FXML
    private Text inboxDataText;

    @FXML
    private Button sendNewEmail;

    @FXML
    private TextField recipientsNewEmail;
    @FXML
    private TextField subjectNewEmail;
    @FXML
    private TextArea textNewEmail;


    public void setModel(ClientModel m) {
        this.model = m;
        inboxDateColumn.setCellValueFactory(email
                -> new SimpleStringProperty((email.getValue().getDate()).toString()));

        inboxSenderColumn.setCellValueFactory(email
                -> new SimpleStringProperty(email.getValue().getSender()));

        inboxSubjectColumn.setCellValueFactory(email
                -> new SimpleStringProperty(email.getValue().getSubject()));

        sentDateColumn.setCellValueFactory(email
                -> new SimpleStringProperty((email.getValue().getDate()).toString()));

        sentRecipientsColumn.setCellValueFactory(email
                -> new SimpleStringProperty((email.getValue().getRecipients()).toString()));

        sentSubjectColumn.setCellValueFactory(email
                -> new SimpleStringProperty(email.getValue().getSubject()));


        sentList.setItems(this.model.getSent());
        inboxList.setItems(this.model.getInbox());


        inboxList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showInboxEmailDetails(newValue));
        sentList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showSentEmailDetails(newValue));

    }


    private void showSentEmailDetails(Email e) {
        if (e != null) {
            sentDataText.setText(e.getDate().toString());
            sentSubjectText.setText(e.getSubject());
            sentRecipientsText.setText(String.valueOf(e.getRecipients()));
            sentTextText.setText(e.getText());
        } else {
            sentSubjectText.setText("");
            sentTextText.setText("");
            sentRecipientsText.setText("");
            sentDataText.setText("");
        }
    }

    private void showInboxEmailDetails(Email e) {
        if (e != null) {
            inboxSubjectText.setText(e.getSubject());
            inboxTextText.setText(e.getText());
            inboxSenderText.setText(e.getSender());
            inboxDataText.setText(e.getDate().toString());
        } else {
            inboxSubjectText.setText("");
            inboxTextText.setText("");
            inboxSenderText.setText("");
            inboxDataText.setText("");
        }
    }


    @FXML
    private void sendNewEmail() {

        String[] destinatari = (recipientsNewEmail.getText()).split(",");
        ArrayList<String> rec = new ArrayList<>();
        for (String dest : destinatari) {
            rec.add(dest);
        }


        String subject = subjectNewEmail.getText();
        String text = textNewEmail.getText();
        Date data = new Date();


        Email e = new Email(-1, this.model.getEmail(), rec, subject, text, data);
        model.sendNewEmail(e);


    }



    private boolean validateEmailAddress(String email) {
        Pattern pattern = Pattern.compile("^.+@.+\\..+$");
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

}
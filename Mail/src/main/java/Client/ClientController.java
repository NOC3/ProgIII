package Client;


import Common.Email;
import Common.Message;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientController {

    public ClientModel model;

    @FXML
    private TabPane topPane;

    @FXML
    private Tab inbox;
    @FXML
    private Tab sent;
    @FXML
    private Tab newEmail;

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

    //Label dei campi delle mail
    @FXML
    private Text sentSubjectTextLabel;
    @FXML
    private Text sentRecipientsTextLabel;
    @FXML
    private Text sentDataTextLabel;
    @FXML
    private Text inboxSubjectTextLabel;
    @FXML
    private Text inboxSenderTextLabel;
    @FXML
    private Text inboxDataTextLabel;


    @FXML
    private Button sendNewEmail;
    @FXML
    private Button deleteEmailInbox;
    @FXML
    private Button deleteEmailSent;
    @FXML
    private Button forwardSent;
    @FXML
    private Button forwardInbox;
    @FXML
    private Button replySent;
    @FXML
    private Button replyInbox;
    @FXML
    private Button replyAllSent;
    @FXML
    private Button replyAllInbox;

    @FXML
    private TextField recipientsNewEmail;
    @FXML
    private TextField subjectNewEmail;
    @FXML
    private TextArea textNewEmail;

    @FXML
    private TitledPane notifications;

    @FXML
    private ListView notificationsList;


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

        notificationsList.itemsProperty().bind(this.model.getNotificationsList());


        inboxList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showInboxEmailDetails(newValue));
        sentList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showSentEmailDetails(newValue));

        notifications.setExpanded(false);


        sentSubjectTextLabel.setVisible(false);
        sentRecipientsTextLabel.setVisible(false);
        sentDataTextLabel.setVisible(false);
        inboxSubjectTextLabel.setVisible(false);
        inboxSenderTextLabel.setVisible(false);
        inboxDataTextLabel.setVisible(false);

        forwardSent.setVisible(false);
        replyAllSent.setVisible(false);
        replySent.setVisible(false);
        deleteEmailSent.setVisible(false);
        deleteEmailInbox.setVisible(false);
        replyAllInbox.setVisible(false);
        forwardInbox.setVisible(false);
        replyInbox.setVisible(false);
    }


    private void showSentEmailDetails(Email e) {
        if (e != null) {
            sentDataText.setText(e.getDate().toString());
            sentSubjectText.setText(e.getSubject());
            sentRecipientsText.setText(e.recipientsToString());
            sentTextText.setText(e.getText());

            sentSubjectTextLabel.setVisible(true);
            sentRecipientsTextLabel.setVisible(true);
            sentDataTextLabel.setVisible(true);


            forwardSent.setVisible(true);
            replyAllSent.setVisible(true);
            replySent.setVisible(true);
            deleteEmailSent.setVisible(true);

        } else {
            sentSubjectText.setText("");
            sentTextText.setText("");
            sentRecipientsText.setText("");
            sentDataText.setText("");

            sentSubjectTextLabel.setVisible(false);
            sentRecipientsTextLabel.setVisible(false);
            sentDataTextLabel.setVisible(false);

            forwardSent.setVisible(false);
            replyAllSent.setVisible(false);
            replySent.setVisible(false);
            deleteEmailSent.setVisible(false);
        }
    }

    private void showInboxEmailDetails(Email e) {
        if (e != null) {
            inboxSubjectText.setText(e.getSubject());
            inboxTextText.setText(e.getText());
            inboxSenderText.setText(e.getSender());
            inboxDataText.setText(e.getDate().toString());

            inboxSubjectTextLabel.setVisible(true);
            inboxSenderTextLabel.setVisible(true);
            inboxDataTextLabel.setVisible(true);


            deleteEmailInbox.setVisible(true);
            replyAllInbox.setVisible(true);
            forwardInbox.setVisible(true);
            replyInbox.setVisible(true);
        } else {
            inboxSubjectText.setText("");
            inboxTextText.setText("");
            inboxSenderText.setText("");
            inboxDataText.setText("");

            inboxSubjectTextLabel.setVisible(false);
            inboxSenderTextLabel.setVisible(false);
            inboxDataTextLabel.setVisible(false);

            deleteEmailInbox.setVisible(false);
            replyAllInbox.setVisible(false);
            forwardInbox.setVisible(false);
            replyInbox.setVisible(false);
        }
    }


    @FXML
    private void sendNewEmail() {
        String[] destinatari = recipientsNewEmail.getText().split(",");

        ArrayList<String> rec = new ArrayList<>();
        for (String dest : destinatari) {
            if (!validateEmailAddress(dest)) {
                //comunicazione della situa
                String mex = "Destinatari non corretti: " + dest;
                model.getNotificationsList().add(mex);
                notifications.setExpanded(true);
                return;
            }
            rec.add(dest.trim());
        }


        String subject = subjectNewEmail.getText();
        String text = textNewEmail.getText();


        Date data = new Date();
        Email e = new Email(-1, this.model.getEmail(), rec, subject, text, data);
        model.sendNewEmail(e);
    }

    @FXML
    private void deleteEmail() {
        if (topPane.getSelectionModel().getSelectedItem().getId().equals("sent")) {
            Email e = sentList.getSelectionModel().getSelectedItem();
            model.deleteEmail(e, Message.REMOVE_EMAIL_SENT);
        } else if (topPane.getSelectionModel().getSelectedItem().getId().equals("inbox")) {
            Email e = inboxList.getSelectionModel().getSelectedItem();
            model.deleteEmail(e, Message.REMOVE_EMAIL_INBOX);
        }
    }

    @FXML
    private void forward() {
        Email e = null;
        if (topPane.getSelectionModel().getSelectedItem().getId().equals("sent")) {
            e = sentList.getSelectionModel().getSelectedItem();
        } else if (topPane.getSelectionModel().getSelectedItem().getId().equals("inbox")) {
            e = inboxList.getSelectionModel().getSelectedItem();
        }

        String testo = e.toString();
        String oggetto = "Inoltro: " + e.getSubject();

        subjectNewEmail.setText(oggetto);
        textNewEmail.setText(testo);

        topPane.getSelectionModel().select(newEmail);
    }

    @FXML
    private void reply() {
        Email e = null;
        if (topPane.getSelectionModel().getSelectedItem().getId().equals("sent")) {
            e = sentList.getSelectionModel().getSelectedItem();
        } else if (topPane.getSelectionModel().getSelectedItem().getId().equals("inbox")) {
            e = inboxList.getSelectionModel().getSelectedItem();
        }


        assert e != null;
        //String testo = e.toString();
        String oggetto = "RE: " + e.getSubject();
        String recipient = e.getSender();

        subjectNewEmail.setText(oggetto);
        recipientsNewEmail.setText(recipient);

        topPane.getSelectionModel().select(newEmail);

    }

    @FXML
    private void replyAll() {
        Email e = null;
        if (topPane.getSelectionModel().getSelectedItem().getId().equals("sent")) {
            e = sentList.getSelectionModel().getSelectedItem();
        } else if (topPane.getSelectionModel().getSelectedItem().getId().equals("inbox")) {
            e = inboxList.getSelectionModel().getSelectedItem();
        }
        assert e != null;

        String recipients = e.getSender();
        for (String st : e.getRecipients()) {
            if (!st.equals(model.getEmail())) {
                recipients += "," + st;
            }
        }

        String oggetto = "RE: " + e.getSubject();

        subjectNewEmail.setText(oggetto);
        recipientsNewEmail.setText(recipients);

        topPane.getSelectionModel().select(newEmail);

    }


    private static boolean validateEmailAddress(String email) {
        Pattern pattern = Pattern.compile("^.+@.+\\..+$");
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

}
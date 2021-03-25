package Client;

import Common.Email;
import Common.Message;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;


//Controller del login
public class Login {

    //parametri configurazione delle socket
    private String host = "localhost";
    private final int port = 49152;


    @FXML
    private Button loginSubmit;
    @FXML
    private TextField loginEmail;
    @FXML
    private Label loginErrorMsg;

    //funzioni di login legata a loginSubmit
    @FXML
    public void login() {
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        Socket socket = null;
        try {
            //pattern matching per l'email
            if (!Email.validateEmailAddress(loginEmail.getText())) {
                loginErrorMsg.setText("L'email non è valida");
                return;
            }

            Message mex = new Message(Message.LOGIN, loginEmail.getText());

            socket = new Socket(host, port);

            out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(mex);

            in = new ObjectInputStream(socket.getInputStream());
            Message response = (Message) in.readObject();

            if (response.getOperation() == Message.SUCCESS) {
                //apre la view del client
                openMainView(loginEmail.getText(), new JSONObject((String) response.getObj()));
            } else {
                //error message
                loginErrorMsg.setText("Errore: impossibile effettuare il login");
            }
        } catch (ConnectException e) {
            System.out.println("Errore client " + e);
            loginErrorMsg.setText("Errore: il server è down, riprovare.");
        } catch (Exception e) {
            System.out.println("Errore client " + e);
            loginErrorMsg.setText("Errore");
        } finally {
            try {
                if (out != null)
                    out.close();

                if (in != null)
                    in.close();

                if (socket != null)
                    socket.close();

            } catch (Exception e) {
                System.out.println("Errore chiusura stream o socket " + e);
            }
        }
    }

    //nasconde la finestra di login e apre la finestra del client dopo aver settato model e controller
    private void openMainView(String userMail, JSONObject mailbox) {
        loginSubmit.getScene().getWindow().hide();
        Stage mainViewStage = new Stage();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientApp.fxml"));
            Parent root = loader.load();
            mainViewStage.setTitle(userMail + " - Email App");

            mainViewStage.setScene(new Scene(root));
            ClientController controller = loader.getController();
            ClientModel model = new ClientModel(userMail, mailbox);
            controller.setModel(model);

            mainViewStage.show();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
package Client;

import Common.Email;
import Common.Message;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.ConnectException;
import java.net.Socket;
import java.util.concurrent.Callable;

//Controller del login
public class Login{

    private String host = "localhost";
    private final int port = 49152;

    @FXML
    private Button loginSubmit;
    @FXML
    private TextField loginEmail;
    @FXML
    private Label loginErrorMsg;

    public void login() {
        //creo connessione socket
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        Socket socket = null;
        try {
            //pattern matching per l'email
            if(Email.validateEmailAddress(loginEmail.getText())){
                loginErrorMsg.setText("L'email non è valida!");
                return;
            }

            Message mex = new Message(Message.LOGIN, loginEmail.getText());

            socket = new Socket(host, port);

            out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(mex);

            in = new ObjectInputStream(socket.getInputStream());
            Message response = (Message) in.readObject();

            if (response.getOperation() == Message.SUCCESS) {
                //apro la view main
                openMainView(loginEmail.getText(), new JSONObject((String)response.getObj()));
            } else {
                //error message
                loginErrorMsg.setText("Errore");
            }
        }catch (ConnectException e) {
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

    private void openMainView(String userMail, JSONObject mailbox){
        //nascondo la view, potrebbe essere utile recuperarla con il logout??
        loginSubmit.getScene().getWindow().hide();
        Stage mainViewStage = new Stage();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientApp.fxml"));
            Parent root = loader.load();
            mainViewStage.setTitle("Email App");

            mainViewStage.setScene(new Scene(root));
            ClientController controller = loader.getController();
            ClientModel model = new ClientModel(userMail, mailbox);
            controller.setModel(model);

            mainViewStage.show();
        }catch(Exception e){
            System.out.println(e);
        }
    }
}

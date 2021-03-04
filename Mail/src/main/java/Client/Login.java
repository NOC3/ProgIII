package Client;

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

import java.net.Socket;
import java.util.concurrent.Callable;

//Controller del login
public class Login{

    private String host = "localhost";        //fissato
    private final int port = 49152;                 //o 65535, stando a wikipedia
//    https://www.adminsub.net/tcp-udp-port-finder <--- controllo porte

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
            Message mex = new Message(Message.LOGIN, loginEmail.getText());

            socket = new Socket(host, port);

            out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(mex);

            in = new ObjectInputStream(socket.getInputStream());
            Message response = (Message) in.readObject();

            if (response.getOperation() == Message.SUCCESS) {
                //apro la view main
                openMainView(new JSONObject((String)response.getObj()));
            } else {
                //error message
                loginErrorMsg.setText("Errore");
            }
        } catch (Exception e) {
            System.out.println("Errore client " + e);
            loginErrorMsg.setText("Errore");
            loginErrorMsg.setAlignment(Pos.CENTER);
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

    private void openMainView(JSONObject mailbox){
        //nascondo la view, potrebbe essere utile recuperarla con il logout??
        loginSubmit.getScene().getWindow().hide();
        Stage mainViewStage = new Stage();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientMainView.fxml"));
            Parent root = loader.load();
            mainViewStage.setTitle("Main");
            mainViewStage.setScene(new Scene(root));
            mainViewStage.show();
        }catch(Exception e){
            System.out.println(e);
        }
    }
}

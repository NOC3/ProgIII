package Client;

import Common.Message;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import netscape.javascript.JSObject;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.Socket;

//Controller del login
public class Login {

    private String host = "localhost";    //fissato
    private int port = 49152;                 //o 65535, stando a wikipedia
//    https://www.adminsub.net/tcp-udp-port-finder <--- controllo porte

    @FXML
    private Button loginSubmit;
    @FXML
    private TextField loginEmail;

//    public Login() {
//        loginSubmit.setOnAction(
//                new EventHandler<ActionEvent>() {
//                    @Override
//                    public void handle(ActionEvent actionEvent) {
//                        login();
//                    }
//                }
//        );
//    }


    @FXML
    public void initialize() {
    }


    public void login() {
        //creo connessione socket
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        Socket socket = null;
        try {
            //pattern matching per l'email

            Message mex = new Message(Message.LOGIN, loginEmail.getText(), "Try connection");

            socket = new Socket(host, port);

            out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(mex);

            in = new ObjectInputStream(socket.getInputStream());
            Message response = (Message) in.readObject();

            if (response.getOperation() == Message.SUCCESS) {
                //chiudi login e apri casella
                System.out.println("loggato utente: "+loginEmail.getText());
                setUpMailbox((JSObject) response.getObj());
            } else {
                //error message
            }
        } catch (Exception e) {
            System.out.println("Errore client " + e);
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

    public void setUpMailbox(JSObject userMails) {
//        https://www.geeksforgeeks.org/parse-json-java/
        //chiude la finestra
        //passa al model i dati presi dalla socket
        //lancia la nuova view con il suo controller
    }
}
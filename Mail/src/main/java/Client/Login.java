package Client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import Common.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

//COntroller del login
public class Login {

    private String host="localhost";    //fissato
    private int port=0;                 //fissato

    @FXML
    private Button loginSubmit;
    @FXML
    private TextField loginEmail;

    public Login() {
        loginSubmit.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        login();
                    }
                }
        );
    }



    public boolean login() {
        //creo connessione socket
        try {
            //pattern matching per l'email

            Message mex = new Message(Message.LOGIN, loginEmail.getText(), "Try connection");

            Socket socket = new Socket(host, port);

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(mex);

            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            Message response = (Message) in.readObject();

            if (response.getOperation() == Message.SUCCESS) {
                //chiudi login e apri casella
                return true;
            } else {
                return false;
                //error message
            }
        } catch (Exception e) {
            System.out.println("Errore client " + e);
        } finally {
            return false;
        }

    }

}
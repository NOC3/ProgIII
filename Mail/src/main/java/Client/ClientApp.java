package Client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

//Main del client
public class ClientApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();

            primaryStage.setTitle("Login");
            primaryStage.setScene(new Scene(root));

            primaryStage.show();
            primaryStage.setResizable(false);

            new Login();
        } catch (Exception e){
            System.out.println(e.getLocalizedMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

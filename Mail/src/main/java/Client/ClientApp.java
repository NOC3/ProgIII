package Client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {

    @Override
    public void start(Stage primaryStage) {

        try{

            Parent loader = FXMLLoader.load(getClass().getResource("/login.fxml"));
//            Parent root = loader.load();
            primaryStage.setTitle("login");
            primaryStage.setScene(new Scene(loader, 600, 400));
            primaryStage.show();

            Login l = new Login();
        } catch (Exception e){
            System.out.println(e.getLocalizedMessage());
        }
    }


    public static void main(String[] args) {
        launch(args);
    }

}
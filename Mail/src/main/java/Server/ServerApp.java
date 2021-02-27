package Server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            ServerModel a = new ServerModel();
            Parent loader = FXMLLoader.load(getClass().getResource("/logger.fxml"));
//            Parent root = loader.load();
            primaryStage.setTitle("Server logger");
            primaryStage.setScene(new Scene(loader, 600, 400));
            primaryStage.show();

        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
    }
}
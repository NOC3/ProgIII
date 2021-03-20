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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/logger.fxml"));
            Parent root = loader.load();
            primaryStage.setTitle("Server logger");
            primaryStage.setScene(new Scene(root, 600, 400));
            primaryStage.show();

            ServerController controller = loader.getController();
            ServerModel model = new ServerModel();
            controller.setModel(model);

            primaryStage.setOnCloseRequest(
                    e -> model.getServerExecutor().shutdown()
            );

        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
    }
}
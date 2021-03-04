package Server;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ServerController {
    private ServerModel model;

    @FXML
    private TableView<ServerModel.Log> logs;
    @FXML
    private TableColumn<ServerModel.Log, String> time;
    @FXML
    private TableColumn<ServerModel.Log, String> event;

    public ServerController(){

    }

    public void setModel(ServerModel model){
        this.model = model;

        time.setCellValueFactory(log
                ->new SimpleStringProperty(log.getValue().getLogTime()));

        event.setCellValueFactory(log
                ->new SimpleStringProperty(log.getValue().getLogEvent()));

        logs.setItems(this.model.getLogs());
    }


}
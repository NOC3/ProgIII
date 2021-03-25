package Server;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

//Controller del logger
public class ServerController {
    private ServerModel model;

    @FXML
    private TableView<ServerModel.Log> logs;
    @FXML
    private TableColumn<ServerModel.Log, String> time;
    @FXML
    private TableColumn<ServerModel.Log, String> event;

    //bind tra model e view
    public void setModel(ServerModel model) {
        this.model = model;

        //factory della tabella dei log
        time.setCellValueFactory(log
                -> new SimpleStringProperty(log.getValue().getLogTime()));

        event.setCellValueFactory(log
                -> new SimpleStringProperty(log.getValue().getLogEvent()));

        //bind dei log
        logs.setItems(this.model.getLogs());
    }
}
package client;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.Setter;

public class AboutProgramController {
    @Setter
    private MessageService messageService;
    @Setter
    Stage stage;
    @FXML
    AnchorPane aboutPanel;
}

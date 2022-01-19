package client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import lombok.Setter;
import model.CreateFile;
import model.CreateFolder;
import model.RefreshClientView;

import java.io.IOException;
import java.nio.file.Files;

public class FileController {
    @Setter private Stage stage;
    @Setter private MessageService messageService;
    @FXML private RadioButton clientRadioButton;
    @FXML private RadioButton serverRadioButton;
    @FXML private TextField fileNameField;


    public void launch() {
        ToggleGroup toggleGroup = new ToggleGroup();
        clientRadioButton.setToggleGroup(toggleGroup);
        serverRadioButton.setToggleGroup(toggleGroup);
    }

    public void createFile(ActionEvent actionEvent) throws IOException {
        if(clientRadioButton.isSelected()) {
            Files.createFile(messageService.getBaseDir().resolve(fileNameField.getText()));
            messageService.sendMessage(new RefreshClientView());
            stage.close();
        } else if(serverRadioButton.isSelected()) {
            messageService.sendMessage(new CreateFile(fileNameField.getText()));
            stage.close();
        }
    }
}

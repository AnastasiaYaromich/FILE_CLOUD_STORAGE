package client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;
import model.CreateFolder;
import model.FilesList;
import model.RefreshClientView;
import java.io.IOException;
import java.nio.file.Files;

public class FolderController {
    @Setter private Stage stage;
    @Setter private MessageService messageService;
    @FXML private TextField folderNameField;
    @FXML private RadioButton clientRadioButton;
    @FXML private RadioButton serverRadioButton;


    public void launch() {
        ToggleGroup toggleGroup = new ToggleGroup();
        clientRadioButton.setToggleGroup(toggleGroup);
        serverRadioButton.setToggleGroup(toggleGroup);
    }

    public void createDirectory(ActionEvent actionEvent) throws IOException {
        if(clientRadioButton.isSelected()) {
            Files.createDirectory(messageService.getBaseDir().resolve(folderNameField.getText()));
            messageService.sendMessage(new RefreshClientView());
            stage.close();


        } else if(serverRadioButton.isSelected()) {
            messageService.sendMessage(new CreateFolder(folderNameField.getText()));
            stage.close();
        }
    }




}

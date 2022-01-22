package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import model.*;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Getter
public class AuthController implements Initializable, MessageProcessor {
    @FXML public TextField loginField;
    @FXML public PasswordField passwordField;
    @FXML public Button authButton;
    @FXML public AnchorPane authPanel;

    @Setter private MessageService messageService;
    private AuthenticationRequest authenticationRequest;
    @Setter private Stage stage;
    private String userRootPath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.messageService = new MessageService(this);
    }

    public void logIn(ActionEvent actionEvent) throws IOException {
        if(loginField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            return;
        }
        authenticationRequest = new AuthenticationRequest(loginField.getText(), passwordField.getText());
        messageService.getOs().writeObject(new AuthenticationRequest(loginField.getText(), passwordField.getText()));
    }

    public void signUp(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Reg.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.show();
        RegController regController = loader.getController();
        regController.setMessageService(messageService);
        messageService.setMessageProcessor(regController);
        regController.setStage(stage);
    }

    @Override
    public void processMessage(AbstractMessage message) {
        if(message.getMessageType().equals(MessageType.AUTHENTICATION_COMPLETE)) {
            AuthenticationComplete authenticationComplete = (AuthenticationComplete) message;
            userRootPath = authenticationComplete.getRootUserPath();
        }
        if(message.getMessageType().equals(MessageType.FILES_LIST)) {
            FilesList files = (FilesList) message;
            completeAuth(files, message);
        }
        else if(message.getMessageType().equals(MessageType.USER_INFO)) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Account not exist.");
                alert.setContentText("Account not exist. Please try to LogIn again or SignUp to continue...");
                alert.show();
            });
        }
    }

    private void completeAuth(FilesList fileList, AbstractMessage message) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("mainPanel.fxml"));
                Scene scene = new Scene(loader.load());
                stage.setScene(scene);
                stage.show();
                MainController mainController = loader.getController();
                mainController.setMessageService(messageService);
                messageService.setMessageProcessor(mainController);
                FilesList files = (FilesList) message;
                mainController.pathServerField.setText(userRootPath);
                mainController.fillServerView(files.getFiles());
                mainController.launch();
                mainController.setStage(stage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
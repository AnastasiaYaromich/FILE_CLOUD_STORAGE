package client;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Setter;
import model.*;
import java.io.IOException;

public class RegController implements MessageProcessor {
   @FXML public TextField loginField;
   @FXML public TextField passwordField;
   @FXML public Button registerButton;
   @Setter private MessageService messageService;
   @Setter Stage stage;
   private String userRootPath;


    public void signUp(ActionEvent actionEvent) throws IOException {
        String login = loginField.getText();
        String password = passwordField.getText();
        if(!login.isEmpty() && !password.isEmpty()) {
            messageService.getOs().writeObject(new AddAccount(login, password));
        } else {
            Platform.runLater(()->{
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR");
                alert.setHeaderText("Please complete the form");
                alert.showAndWait();
            });
        }
    }

    @Override
    public void processMessage(AbstractMessage message) {
        if(message.getMessageType().equals(MessageType.REGISTRATION_COMPLETE)) {
            RegistrationComplete registrationComplete = (RegistrationComplete) message;
            userRootPath = registrationComplete.getRootUserPath();
        }
        if(message.getMessageType().equals(MessageType.FILES_LIST)) {
            FilesList files = (FilesList) message;
            completeRegistration(files, message);
        }

    }

    private void completeRegistration(FilesList fileList, AbstractMessage message) {
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

    public void backAuthForm(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("auth.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.show();
        AuthController authController = loader.getController();
        authController.setMessageService(messageService);
        messageService.setMessageProcessor(authController);
        authController.setStage(stage);
    }
}
package client;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("auth.fxml"));
        Parent parent = loader.load();
        AuthController authController = loader.getController();
        authController.setStage(primaryStage);
        primaryStage.setTitle("File storage");
        primaryStage.setScene(new Scene(parent));
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("listik.png")));
        primaryStage.show();
    }
}
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
        Parent parent = FXMLLoader.load(getClass().getResource("cloud_client.fxml"));
        primaryStage.setTitle("File storage");
        primaryStage.setScene(new Scene(parent));
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("icon1.png")));
        primaryStage.show();
    }
}
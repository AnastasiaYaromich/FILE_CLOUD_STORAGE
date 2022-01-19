package client;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import model.AbstractMessage;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

@Getter  @Slf4j
public class MessageService {
    private ObjectEncoderOutputStream os;
    private ObjectDecoderInputStream is;
    @Setter private Path baseDir;
    @Setter private MessageProcessor messageProcessor;

    public MessageService(MessageProcessor messageProcessor) {
        try {
            Socket socket = new Socket("localhost", 8189);
            baseDir = Paths.get(System.getProperty("user.home"));
            this.messageProcessor = messageProcessor;
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());
            Thread thread = new Thread(this::read);
            thread.setDaemon(true);
            thread.start();
        } catch (IOException e) {
            Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Connection error");
                        alert.setContentText("Server is offline. Please try again later");
                        alert.show();
                    });
        }
    }

    private void read() {
        try {
            while (true) {
                AbstractMessage message = (AbstractMessage) is.readObject();
                messageProcessor.processMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(AbstractMessage message) throws IOException {
        os.writeObject(message);
    }

}

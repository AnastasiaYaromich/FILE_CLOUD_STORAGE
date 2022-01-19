package client;
import model.AbstractMessage;

public interface MessageProcessor {
    void processMessage(AbstractMessage message);
}

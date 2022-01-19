package model;
import lombok.Getter;

public class ChangeDirectory implements AbstractMessage {
    @Getter
    private String dirName;

    public ChangeDirectory(String dirName) {
        this.dirName = dirName;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.CHANGE_DIRECTORY;
    }
}



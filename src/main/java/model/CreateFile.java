package model;
import lombok.Getter;

public class CreateFile implements AbstractMessage {
    @Getter
    private String fileName;

    public CreateFile(String folderName){
        this.fileName = folderName;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.CREATE_FILE;
    }
}

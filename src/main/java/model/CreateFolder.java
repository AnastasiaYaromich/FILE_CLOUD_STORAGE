package model;
import lombok.Getter;

public class CreateFolder implements AbstractMessage {

    @Getter
    private String folderName;

    public CreateFolder(String folderName){
        this.folderName = folderName;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.CREATE_FOLDER;
    }
}

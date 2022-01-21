package model;

public class PasteRequest implements AbstractMessage {
    private final String fileName;
    private final String type;

    public PasteRequest(String fileName, String type) {
        this.fileName = fileName;
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public String getType() {
        return type;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.PASTE_REQUEST;
    }
}

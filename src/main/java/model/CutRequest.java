package model;

public class CutRequest implements AbstractMessage {
    private final String fileName;

    public CutRequest(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.CUT_REQUEST;
    }
}

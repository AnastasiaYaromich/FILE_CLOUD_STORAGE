package model;

public class RefreshClientView implements AbstractMessage {

    @Override
    public MessageType getMessageType() {
        return MessageType.REFRESH_CLIENT_VIEW;
    }
}

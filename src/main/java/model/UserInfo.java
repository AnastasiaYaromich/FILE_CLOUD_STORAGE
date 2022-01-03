package model;

public class UserInfo implements AbstractMessage {
    String info;

    public UserInfo(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.USER_INFO;
    }
}

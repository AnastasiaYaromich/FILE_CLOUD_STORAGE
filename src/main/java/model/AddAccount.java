package model;

public class AddAccount implements AbstractMessage {

    String login;
    String password;

    public AddAccount(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getLogin() {
        return login;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.ADD_ACCOUNT;
    }
}

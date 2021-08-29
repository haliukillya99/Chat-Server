package Connection;

import java.io.Serializable;
import java.util.Set;

public class Message implements Serializable {
    private MessageType typeMessage;
    private String textMessage;
    private Set<String> usersList;

    public Message(MessageType typeMessage, String textMessage) {
        this.textMessage = textMessage;
        this.typeMessage = typeMessage;
        this.usersList = null;
    }

    public Message(MessageType typeMessage, Set<String> listUsers) {
        this.typeMessage = typeMessage;
        this.textMessage = null;
        this.usersList = listUsers;
    }

    public Message(MessageType typeMessage) {
        this.typeMessage = typeMessage;
        this.textMessage = null;
        this.usersList = null;
    }

    public MessageType getTypeMessage() {
        return typeMessage;
    }

    public Set<String> getListUsers() {
        return usersList;
    }

    public String getTextMessage() {
        return textMessage;
    }

}

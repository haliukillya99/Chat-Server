package Client;

import Connection.*;

import java.io.IOException;
import java.net.Socket;

public class Client {
    private Connection connection;
    private static ClientModel modelObj;
    private static ClientGui guiObj;
    private volatile boolean isConnect = false;

    public static void main(String[] args) {
        Client client = new Client();
        modelObj = new ClientModel();
        guiObj = new ClientGui(client);
        guiObj.initFrameClient();
        while (true) {
            if (client.isConnect()) {
                client.userNameRegistration();
                client.receiveMessageFromServer();
                client.setConnect(false);
            }
        }
    }

    public boolean isConnect() {
        return isConnect;
    }

    public void setConnect(boolean connect) {
        isConnect = connect;
    }

    protected void connectToServer() {
        if (!isConnect) {
            while (true) {
                try {
                    String addressServer = guiObj.getServerAddressFromOptionPane();
                    int port = guiObj.getPortServerFromOptionPane();

                    Socket socket = new Socket(addressServer, port);
                    connection = new Connection(socket);
                    isConnect = true;
                    guiObj.addMessage("Сервисное сообщение: Вы подключились к серверу.\n");
                    break;
                } catch (Exception e) {
                    guiObj.errorDialogWindow("Произошла ошибка! Возможно Вы ввели не верный адрес сервера или порт. Попробуйте еще раз");
                    break;
                }
            }
        } else guiObj.errorDialogWindow("Вы уже подключены!");
    }

    protected void userNameRegistration() {
        while (true) {
            try {
                Message message = connection.receive();
                if (message.getTypeMessage() == MessageType.REQUEST_USER_NAME) {
                    String nameUser = guiObj.getNameUser();
                    connection.send(new Message(MessageType.SHOW_USER_NAME, nameUser));
                }
                if (message.getTypeMessage() == MessageType.NAME_USED) {
                    guiObj.errorDialogWindow("Данное имя уже используется, введите другое");
                    String nameUser = guiObj.getNameUser();
                    connection.send(new Message(MessageType.SHOW_USER_NAME, nameUser));
                }
                if (message.getTypeMessage() == MessageType.USER_NAME_ACCEPTED) {
                    guiObj.addMessage("Сервисное сообщение: ваше имя принято!\n");
                    modelObj.setUsers(message.getListUsers());
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                guiObj.errorDialogWindow("Произошла ошибка при регистрации имени. Попробуйте переподключиться");
                try {
                    connection.close();
                    isConnect = false;
                    break;
                } catch (IOException ex) {
                    guiObj.errorDialogWindow("Ошибка при закрытии соединения");
                }
            }

        }
    }

    protected void sendMessageOnServer(String text) {
        try {
            connection.send(new Message(MessageType.TEXT_MESSAGE, text));
        } catch (Exception e) {
            guiObj.errorDialogWindow("Ошибка при отправке сообщения");
        }
    }

    protected void receiveMessageFromServer() {
        while (isConnect) {
            try {
                Message message = connection.receive();
                if (message.getTypeMessage() == MessageType.TEXT_MESSAGE) {
                    guiObj.addMessage(message.getTextMessage());
                }
                if (message.getTypeMessage() == MessageType.USER_ADDED) {
                    modelObj.addUser(message.getTextMessage());
                    guiObj.refreshListUsers(modelObj.getUsers());
                    guiObj.addMessage(String.format("Сервисное сообщение: пользователь %s присоединился к чату.\n", message.getTextMessage()));
                }
                if (message.getTypeMessage() == MessageType.REMOVED_USER) {
                    modelObj.removeUser(message.getTextMessage());
                    guiObj.refreshListUsers(modelObj.getUsers());
                    guiObj.addMessage(String.format("Сервисное сообщение: пользователь %s покинул чат.\n", message.getTextMessage()));
                }
            } catch (Exception e) {
                guiObj.errorDialogWindow("Ошибка при приеме сообщения от сервера.");
                setConnect(false);
                guiObj.refreshListUsers(modelObj.getUsers());
                break;
            }
        }
    }

    protected void disableClient() {
        try {
            if (isConnect) {
                connection.send(new Message(MessageType.DISABLE_USER));
                modelObj.getUsers().clear();
                isConnect = false;
                guiObj.refreshListUsers(modelObj.getUsers());
            } else guiObj.errorDialogWindow("Вы уже отключены.");
        } catch (Exception e) {
            guiObj.errorDialogWindow("Сервисное сообщение: произошла ошибка при отключении.");
        }
    }
}

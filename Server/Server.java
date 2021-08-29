package Server;

import Connection.*;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class Server {

    private ServerSocket serverSocket;
    private static ServerGui guiObj;
    private static ServerModel modelObj;
    private static volatile boolean IsServerWorkingNow = false;

    public static void main(String[] args) {
        Server server = new Server();
        guiObj = new ServerGui(server);
        modelObj = new ServerModel();
        guiObj.initFrameServer();
        while (true) {
            if (IsServerWorkingNow) {
                server.acceptServer();
                IsServerWorkingNow = false;
            }
        }
    }

    protected void acceptServer() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                new ServerThread(socket).start();
            } catch (Exception exception) {
                guiObj.refreshDialogWindowServer("Связь с сервером потеряна.\n");
                break;
            }
        }
    }

    protected void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            IsServerWorkingNow = true;
            guiObj.refreshDialogWindowServer("Сервер запущен.\n");
        } catch (Exception exception) {
            guiObj.refreshDialogWindowServer("Не удалось запустить сервер.\n");
        }
    }

    protected void stopServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                for (Map.Entry<String, Connection> user : modelObj.getAllUsersMultiChat().entrySet()) {
                    user.getValue().close();
                }
                serverSocket.close();
                modelObj.getAllUsersMultiChat().clear();
                guiObj.refreshDialogWindowServer("Сервер остановлен.\n");
            } else guiObj.refreshDialogWindowServer("Сервер не запущен.\n");
        } catch (Exception exception) {
            guiObj.refreshDialogWindowServer("Остановить сервер не удалось.\n");
        }
    }

    protected void sendMessageAllUsers(Message message) {
        for (Map.Entry<String, Connection> user : modelObj.getAllUsersMultiChat().entrySet()) {
            try {
                user.getValue().send(message);
            } catch (Exception exception) {
                guiObj.refreshDialogWindowServer("Ошибка отправки сообщения пользователям.\n");
            }
        }
    }

    private class ServerThread extends Thread {
        private Socket socket;

        public ServerThread(Socket socket) {
            this.socket = socket;
        }

        private String requestAndAddingUser(Connection connection) {
            while (true) {
                try {
                    connection.send(new Message(MessageType.REQUEST_USER_NAME));
                    Message responseMessage = connection.receive();
                    String userName = responseMessage.getTextMessage();

                    if (responseMessage.getTypeMessage() == MessageType.SHOW_USER_NAME && userName != null && !userName.isEmpty() && !modelObj.getAllUsersMultiChat().containsKey(userName)) {

                        modelObj.addUser(userName, connection);
                        Set<String> listUsers = new HashSet<>();
                        for (Map.Entry<String, Connection> users : modelObj.getAllUsersMultiChat().entrySet()) {
                            listUsers.add(users.getKey());
                        }

                        connection.send(new Message(MessageType.USER_NAME_ACCEPTED, listUsers));
                        sendMessageAllUsers(new Message(MessageType.USER_ADDED, userName));
                        return userName;
                    }
                    else connection.send(new Message(MessageType.NAME_USED));
                } catch (Exception exception) {
                    guiObj.refreshDialogWindowServer("Возникла ошибка при запросе и добавлении нового пользователя\n");
                }
            }
        }

        private void messagingBetweenUsers(Connection connection, String userName) {
            while (true) {
                try {
                    Message message = connection.receive();

                    if (message.getTypeMessage() == MessageType.TEXT_MESSAGE) {
                        String textMessage = String.format("%s: %s\n", userName, message.getTextMessage());
                        sendMessageAllUsers(new Message(MessageType.TEXT_MESSAGE, textMessage));
                    }

                    if (message.getTypeMessage() == MessageType.DISABLE_USER) {
                        sendMessageAllUsers(new Message(MessageType.REMOVED_USER, userName));
                        modelObj.removeUser(userName);
                        connection.close();
                        guiObj.refreshDialogWindowServer(String.format("Пользователь %s отключился.\n", socket.getRemoteSocketAddress()));
                        break;
                    }
                } catch (Exception exception) {
                    guiObj.refreshDialogWindowServer(String.format("Произошла ошибка отправки сообщения от пользователя %s, либо отключился!\n", userName));
                    break;
                }
            }
        }

        public void run() {
            guiObj.refreshDialogWindowServer(String.format("Подключился новый пользователь %s.\n", socket.getRemoteSocketAddress()));
            try {
                Connection connection = new Connection(socket);
                String nameUser = requestAndAddingUser(connection);
                messagingBetweenUsers(connection, nameUser);
            } catch (Exception exception) {
                guiObj.refreshDialogWindowServer(String.format("Произошла ошибка при рассылке сообщения от пользователя!\n"));
            }
        }
    }
}
package Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class ServerGui {
    private JFrame frameObj = new JFrame("Командное окно для запуска сервера");
    private JTextArea dialogWindow = new JTextArea(10, 40);
    private JButton buttonForStartServer = new JButton("Запустить сервер");
    private JButton buttonForStopServer = new JButton("Остановить сервер");
    private JPanel panelForButtons = new JPanel();
    private final Server server;

    public ServerGui(Server server) {
        this.server = server;
    }

    protected void initFrameServer() {
        dialogWindow.setEditable(false);
        dialogWindow.setLineWrap(true);
        frameObj.add(new JScrollPane(dialogWindow), BorderLayout.CENTER);
        panelForButtons.add(buttonForStartServer);
        panelForButtons.add(buttonForStopServer);
        frameObj.add(panelForButtons, BorderLayout.SOUTH);
        frameObj.pack();
        frameObj.setLocationRelativeTo(null);
        frameObj.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        frameObj.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                server.stopServer();
                System.exit(0);
            }
        });
        frameObj.setVisible(true);

        buttonForStartServer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int port = getPortFromOptionPane();
                server.startServer(port);
            }
        });
        buttonForStopServer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                server.stopServer();
            }
        });
    }

    public void refreshDialogWindowServer(String serviceMessage) {
        dialogWindow.append(serviceMessage);
    }

    protected int getPortFromOptionPane() {
        while (true) {
            String port = JOptionPane.showInputDialog(
                    frameObj, "Введите порт сервера:",
                    "Ввод порта сервера",
                    JOptionPane.QUESTION_MESSAGE
            );
            try {
                return Integer.parseInt(port.trim());
            } catch (Exception exception) {
                JOptionPane.showMessageDialog(
                        frameObj, "Введен неккоректный порт сервера. Попробуйте еще раз.",
                        "Ошибка ввода порта сервера", JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}
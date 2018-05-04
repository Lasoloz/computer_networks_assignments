/*
 * Heim László, hlim1626, 522-es csoport
 */
package com.gitlab.lasoloz.irc.client.ui;

import com.gitlab.lasoloz.irc.client.Client;
import com.gitlab.lasoloz.irc.message.Message;
import com.gitlab.lasoloz.irc.message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientFrame extends JFrame implements Runnable {
    private static Logger LOGGER = LoggerFactory.getLogger(ClientFrame.class);
    public static final String APP_NAME = "IRC client";

    private Client client;
    private AtomicBoolean runs;

    private MessengerPane messengerPane;
    private UserListPane userListPane;
    private JTextField messageField;

    private Thread incomingThread;
    private Thread clientWorkerThread;

    ClientFrame(Client clientObj) {
        this.client = clientObj;

        runs = new AtomicBoolean();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Message logoutMessage = new Message(MessageType.LOGOUT);
                client.writeMessage(logoutMessage);
                runs.set(false);
                if (clientWorkerThread != null) {
                    try {
                        client.close();
                        clientWorkerThread.join();
                    } catch (InterruptedException ex) {
                        LOGGER.error("Failed to join client worker thread: " +
                                ex.getMessage());
                    }
                }
                if (incomingThread != null) {
                    try {
                        incomingThread.join();
                    } catch (InterruptedException ex) {
                        LOGGER.error("Failed to join incomingThread: " +
                                ex.getMessage());
                    }
                }
                client.close();
                super.windowClosing(e);
            }
        });


        messengerPane = new MessengerPane();
        userListPane = new UserListPane();
        messageField = new JTextField();
        JButton sendButton = new JButton("Send");


        // Add left panel:
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.add(messengerPane, BorderLayout.CENTER);

        JPanel sendPanel = new JPanel();
        sendPanel.setLayout(new BorderLayout());
        sendPanel.add(messageField, BorderLayout.CENTER);
        sendPanel.add(sendButton, BorderLayout.EAST);

        leftPanel.add(sendPanel, BorderLayout.SOUTH);

        // Add to frame:
        setLayout(new GridLayout());
        add(leftPanel);
        add(userListPane);


        // Add send events:
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // Send message on enter:
        this.getRootPane().setDefaultButton(sendButton);

        setTitle("User: " + client.getUsername() + " - " + APP_NAME);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(700, 200, 600, 600);
    }


    public void runThread() {
        if (incomingThread != null || clientWorkerThread != null) {
            throw new RuntimeException(
                    "Cannot create new thread, old one might be running!");
        }
        clientWorkerThread = new Thread(client);
        clientWorkerThread.start();
        incomingThread = new Thread(this);
        incomingThread.start();
    }


    private void sendMessage() {
        String messageStr = messageField.getText();
        if (messageStr.equals("")) {
            return;
        }
        messageField.setText("");

        String username = userListPane.getSelectedUser();

        Message message;
        if (username.equals(UserListPane.SEND_TO_EVERYONE)) {
            String[] messageArray = new String[1];
            messageArray[0] = messageStr;
            message = new Message(MessageType.BROADCAST, messageArray);
        } else {
            String[] messageArray = new String[2];
            messageArray[0] = username;
            messageArray[1] = messageStr;
            message = new Message(MessageType.WHISPER, messageArray);
            messengerPane.addMessage("Whispered to `" +
                    username + "`: " + messageStr);
        }

        client.writeMessage(message);
    }


    @Override
    public void run() {
        runs.set(true);

        while (runs.get()) {
            Message message = client.fetchNextMessage();
            while (message != null) {
                MessageType type = message.getType();
                String[] messageList = message.getMessageList();
                switch (type) {
                    case LOGIN_NOTIF:
                        userListPane.addUser(messageList[0]);
                        messengerPane.addMessage("Server: `" +
                                messageList[0] +
                                "` logged in.");
                        break;
                    case LOGOUT_NOTIF:
                        userListPane.removeUser(messageList[0]);
                        messengerPane.addMessage("Server: `" +
                                messageList[0] + "` logged out.");
                        break;
                    case BROADCAST_INCOMING:
                        messengerPane.addMessage(messageList[0] +
                                ": " +
                                messageList[1]);
                        break;
                    case WHISPER_INCOMING:
                        messengerPane.addMessage(messageList[0] +
                                " whispers: " +
                                messageList[1]);
                        break;
                    default:
                        LOGGER.error("Server sent invalid message!");
                        messengerPane.addMessage(
                                "Server sent invalid message!");
                }

                message = client.fetchNextMessage();
            }

            try {
                Thread.sleep(40);
            } catch (InterruptedException ex) {
                LOGGER.error("Failed to sleep!");
            }
        }
    }
}

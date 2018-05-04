/*
 * Heim László, hlim1626, 522-es csoport
 */
package com.gitlab.lasoloz.irc.server;

import com.gitlab.lasoloz.irc.message.Message;
import com.gitlab.lasoloz.irc.message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class Server implements Runnable {
    private static Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private final static String HOSTNAME = "127.0.0.1";

    private boolean runs;
    private LinkedBlockingQueue<MessageBundle> messageQueue;
    private HashMap<String, ClientThread> clientMap;
    private final Object workerLock;

    private ServerSocket serverSocket;


    Server() {
        messageQueue = new LinkedBlockingQueue<>();
        workerLock = new Object();
        clientMap = new HashMap<>();
        try {
            serverSocket = new ServerSocket();
        } catch (IOException ex) {
            LOGGER.error("Failed to create server socket!");
        }
    }


    public void start(int portNumber) throws RuntimeException {
        try {
            LOGGER.info("Setting up server...");
            serverSocket.bind(new InetSocketAddress(HOSTNAME, portNumber));

            LOGGER.info("Waiting for connections...");

            synchronized (workerLock) {
                runs = true;
            }

            while (runs) {
                try {
                    Socket clientSocket = serverSocket.accept();

                    ClientHandler clientHandler =
                            validateClientSocket(clientSocket);

                    synchronized (workerLock) {
                        String username = clientHandler.getUsername();
                        if (clientMap.containsKey(username)) {
                            LOGGER.info("Client rejected!");
                            clientHandler.reject();
                        } else {
                            ClientThread clientThread =
                                    new ClientThread(clientHandler);

                            LOGGER.info("Client accepted: `" + username +
                                    "`!");
                            clientHandler.accept();

                            clientMap.put(username, clientThread);
                            clientThread.thread.start();
                        }

                        messageQueue.add(new MessageBundle(
                                new Message(MessageType.LOGIN_NOTIF),
                                clientHandler
                        ));
                    }
                } catch (IOException ex) {
                    LOGGER.error("Cannot accept client (maybe stopping?)");
                } catch (RuntimeException ex) {
                    LOGGER.error("Invalid client: " + ex.getMessage());
                }
            }

            serverSocket.close();
        } catch (IOException ex) {
            LOGGER.error("Failed to set up server! Exception: " +
                    ex.getMessage());
            throw new RuntimeException("Server: Failed to `start` server!");
        }

        LOGGER.info("Server listener shutting down...");
    }


    private ClientHandler validateClientSocket(Socket clientSocket) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(
                    clientSocket.getOutputStream()
            );
            ObjectInputStream ois = new ObjectInputStream(
                    clientSocket.getInputStream()
            );

            Message loginMessage;
            int tries = 100;
            while (tries > 0) {
                try {
                    loginMessage = (Message) ois.readObject();

                    if (!loginMessage.getType().equals(MessageType.LOGIN)) {
                        throw new RuntimeException(
                                "LOGIN message is required!");
                    }

                    String[] msgs = loginMessage.getMessageList();
                    if (msgs == null) {
                        throw new RuntimeException("Invalid message list!");
                    }

                    if (msgs.length < 1) {
                        throw new RuntimeException(
                                "Invalid message list length!");
                    }

                    String username = msgs[0];

                    return new ClientHandler(clientSocket, ois, oos,
                            messageQueue, username);
                } catch (ClassNotFoundException ex) {
                    --tries;
                }
            }
        } catch (IOException ex) {
            LOGGER.info("An exception occoured while validating");
        }

        throw new RuntimeException(
                "Failed to establish connection with client!");
    }


    @Override
    public void run() {
        boolean currentRuns = true;
        synchronized (workerLock) {
            runs = true;
        }

        while (currentRuns) {
            // Check threads:
            ArrayList<String> deletables = new ArrayList<>();

            synchronized (workerLock) {
                for (Map.Entry<String, ClientThread> clientThreadEntry :
                        clientMap.entrySet()) {
                    ClientThread clientThread = clientThreadEntry.getValue();
                    if (!clientThread.thread.isAlive()) {
                        try {
                            LOGGER.info("Thread with user `" +
                                    clientThread.handler.getUsername() +
                                    "` stopped!");
                            clientThread.thread.join();
                            deletables.add(clientThreadEntry.getKey());
                        } catch (InterruptedException ex) {
                            LOGGER.error(
                                    "Interrupted exception in thread join!");
                        }
                    }
                }

                for (String key : deletables) {
                    clientMap.remove(key);
                }
            }


            // Work with message queue:
            MessageBundle front = messageQueue.poll();
            while (front != null) {
                consumeMessage(front);
                front = messageQueue.poll();
            }


            try {
//                LOGGER.info("Sleep in thread!");
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                LOGGER.error("Worker thread sleep was interrupted!");
            }

            synchronized (workerLock) {
                currentRuns = runs;
            }
        }
    }


    private void consumeMessage(MessageBundle messageBundle) {
        Message message = messageBundle.getMessage();
        MessageType type = message.getType();

        if (type.equals(MessageType.LOGOUT)) {
            messageBundle.getHandler().close();

            synchronized (workerLock) {
                String[] logoutInfo = new String[1];
                logoutInfo[0] = messageBundle.getHandler().getUsername();

                Message logoutMessage = new Message(
                        MessageType.LOGOUT_NOTIF, logoutInfo);

                for (ClientThread clientThread : clientMap.values()) {
                    if (!clientThread.handler.getUsername()
                            .equals(logoutInfo[0])) {
                        clientThread.handler.writeMessage(logoutMessage);
                    }
                }
            }
        } else if (type.equals(MessageType.BROADCAST)) {
            String[] broadcastInfo = new String[2];
            broadcastInfo[0] = messageBundle.getHandler().getUsername();
            try {
                broadcastInfo[1] = message.getMessageList()[0];
            } catch (IndexOutOfBoundsException ex) {
                LOGGER.error("Got invalid broadcast message!");
            }
            Message broadcastMessage = new Message(
                    MessageType.BROADCAST_INCOMING, broadcastInfo);
            synchronized (workerLock) {
                for (ClientThread clientThread : clientMap.values()) {
                    clientThread.handler.writeMessage(broadcastMessage);
                }
            }
        } else if (type.equals(MessageType.WHISPER)) {
            String recipient = "";
            String messageStr = "";
            try {
                recipient = message.getMessageList()[0];
                messageStr = message.getMessageList()[1];
            } catch (IndexOutOfBoundsException ex) {
                LOGGER.error("Got invalid whisper message!");
            }

            String whisperInfo[] = new String[2];
            whisperInfo[0] = messageBundle.getHandler().getUsername();
            whisperInfo[1] = messageStr;
            Message whisperMessage = new Message(
                    MessageType.WHISPER_INCOMING, whisperInfo);

            synchronized (workerLock) {
                if (!clientMap.containsKey(recipient)) {
                    LOGGER.error("Invalid recipient for whisper!");
                } else {
                    clientMap.get(recipient).handler.
                            writeMessage(whisperMessage);
                }
            }
        } else if (type.equals(MessageType.LOGIN_NOTIF)) {
            String loggedInUser = messageBundle.getHandler().getUsername();

            String[] loginInfo = new String[1];
            loginInfo[0] = loggedInUser;

            Message loginMessage = new Message(
                    MessageType.LOGIN_NOTIF,
                    loginInfo);
            synchronized (workerLock) {
                for (ClientThread clientThread : clientMap.values()) {
                    String currentClient = clientThread.handler.getUsername();
                    String[] loginMessageArray = new String[1];
                    loginMessageArray[0] = currentClient;
                    if (!currentClient.equals(loggedInUser)) {
                        clientThread.handler.writeMessage(loginMessage);
                        clientMap.get(loggedInUser).handler
                                .writeMessage(new Message(
                                        MessageType.LOGIN_NOTIF,
                                        loginMessageArray
                                ));
                    }
                }
            }
        } else {
            LOGGER.error("Invalid message type in stack!");
        }
    }


    public void close() {
        LOGGER.info("Stopping server...");
        try {
            serverSocket.close();
        } catch (IOException ex) {
            LOGGER.error("Failed to close server socket!");
        }

        synchronized (workerLock) {
            runs = false;
        }
    }
}

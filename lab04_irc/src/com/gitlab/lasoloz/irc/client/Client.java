/*
 * Heim László, hlim1626, 522-es csoport
 */
package com.gitlab.lasoloz.irc.client;

import com.gitlab.lasoloz.irc.message.Message;
import com.gitlab.lasoloz.irc.message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;

public class Client implements Runnable {
    private static Logger LOGGER = LoggerFactory.getLogger(Client.class);

    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    private boolean runs;
    private LinkedList<Message> messageQueue;
    private final Object messageQueueLock;

    private String username;


    public Client() {
        runs = false;
        messageQueue = new LinkedList<>();
        messageQueueLock = new Object();
    }


    public boolean connect(String hostname, int portno, String username) {
        try {
            LOGGER.info("Connecting to server application...");
            socket = new Socket();
            socket.connect(new InetSocketAddress(hostname, portno),
                    1000);

            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            LOGGER.info("Sending login request...");
            String[] loginMessage = new String[1];
            loginMessage[0] = username;
            oos.writeObject(new Message(MessageType.LOGIN, loginMessage));

            LOGGER.info("Attempting to read server answer...");

            Message acceptStatus = (Message) ois.readObject();
            if (acceptStatus == null) {
                LOGGER.error("Server sent null object!");
                return false;
            }

            if (!acceptStatus.getType().equals(MessageType.ACCEPT)) {
                LOGGER.error("Server didn't accept login request!");
                return false;
            }

            LOGGER.info("Connected to server...");
            this.username = username;
            return true;
        } catch (IOException ex) {
            LOGGER.error("Failed to connect to server application: " +
                    ex.getMessage());
        } catch (ClassNotFoundException ex) {
            LOGGER.error("Server sent invalid message type: " +
                    ex.getMessage());
        }
        return false;
    }

    @Override
    public void run() {
        boolean currentRuns = true;
        runs = true;

        while (currentRuns) {
            try {
                Message message = (Message) ois.readObject();

                Thread.sleep(50);

                synchronized (messageQueueLock) {
                    messageQueue.add(message);
                }
            } catch (IOException ex) {
                LOGGER.error("Failed to read message: " + ex.getMessage());
            } catch (ClassNotFoundException ex) {
                LOGGER.error("Invalid message: " + ex.getMessage());
            } catch (InterruptedException ex) {
                LOGGER.error("Worker thread failed to sleep: " +
                        ex.getMessage());
            }

            synchronized (messageQueueLock) {
                currentRuns = runs;
            }
        }
    }


    public String getUsername() {
        return username;
    }


    public Message fetchNextMessage() {
        synchronized (messageQueueLock) {
            return messageQueue.pollFirst();
        }
    }


    public void writeMessage(Message message) {
        try {
            oos.writeObject(message);
            oos.flush();
        } catch (IOException ex) {
            LOGGER.error("Failed to write message to server: " +
                    ex.getMessage());
        }
    }


    public void close() {
        try {
            if (ois != null) {
                ois.close();
            }
            if (oos != null) {
                oos.close();
            }
            if (socket != null) {
                socket.close();
                LOGGER.info("Closed connection to server...");
            }

            synchronized (messageQueueLock) {
                runs = false;
            }
        } catch (IOException ex) {
            LOGGER.error("Failed to close client streams and/or socket: " +
                    ex.getMessage());
        }
    }
}

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
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientHandler implements Runnable {
    private static Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);

    private final String username;

    private boolean runs;
    private final Object handleLock;

    private Socket clientSocket;
    private ObjectInputStream socketInput;
    private ObjectOutputStream socketOutput;

    private LinkedBlockingQueue<MessageBundle> messageQueue;

    ClientHandler(
            Socket clientSocket,
            ObjectInputStream ois,
            ObjectOutputStream oos,
            LinkedBlockingQueue<MessageBundle> messageQueue,
            String username
    ) {
        this.clientSocket = clientSocket;
        this.messageQueue = messageQueue;

        socketInput = ois;
        socketOutput = oos;

        handleLock = new Object();

        this.username = username;
    }

    @Override
    public void run() {
        try {
            boolean currentRuns;
            synchronized (handleLock) {
                runs = true;
                currentRuns = true;
            }

            while (currentRuns) {
                Message message = (Message) socketInput.readObject();
                messageQueue.add(new MessageBundle(message, this));

//                LOGGER.info("Read message: " + message);

                Thread.sleep(25);
                synchronized (handleLock) {
                    currentRuns = runs;
                }
            }
        } catch (IOException ex) {
            LOGGER.error("Failed to read or write message: " + ex);
        } catch (ClassNotFoundException ex) {
            LOGGER.error("Failed to read object (invalid class): " + ex);
        } catch (InterruptedException ex) {
            LOGGER.error("Thread could not sleep: " + ex.getMessage());
        }
    }


    public void writeMessage(Message message) {
        try {
            synchronized (handleLock) {
//                LOGGER.info("Writing message: " + message);
                socketOutput.writeObject(message);
                socketOutput.flush();
            }
        } catch (IOException ex) {
            LOGGER.error("Failed to write message for client!");
        }
    }


    public void accept() {
        writeMessage(new Message(MessageType.ACCEPT));
    }


    public void reject() {
        writeMessage(new Message(MessageType.REJECT));
    }


    public void close() {
        try {
            synchronized (handleLock) {
                runs = false;
            }
            clientSocket.close();
        } catch (IOException ex) {
            LOGGER.error("Failed to close client socket: " + ex);
        }
    }


    public String getUsername() {
        return username;
    }
}

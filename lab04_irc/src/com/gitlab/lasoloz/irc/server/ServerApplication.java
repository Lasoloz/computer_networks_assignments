/*
 * Heim László, hlim1626, 522-es csoport
 */
package com.gitlab.lasoloz.irc.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Signal;
import sun.misc.SignalHandler;

public class ServerApplication implements SignalHandler {
    private static Logger LOGGER = LoggerFactory.getLogger(
            ServerApplication.class);

    private Server server;
    private Thread serverThread;
    private final Object serverLock;


    public static void main(String[] args) {
        int portno = 9010;
        if (args.length >= 1) {
            try {
                portno = Integer.parseInt(args[0]);
                LOGGER.info("Read port number: " + portno);
            } catch (NumberFormatException ex) {
                LOGGER.error(
                        "Failed to parse port number! Using default port...");
            }
        } else {
            LOGGER.info("Using default port number 9010!");
        }

        ServerApplication serverApp = new ServerApplication();

        LOGGER.info("Setting default signal handlers...");
        Signal.handle(new Signal("INT"), serverApp);
        Signal.handle(new Signal("TERM"), serverApp);

        serverApp.start(portno);

        serverApp.join();
    }


    private ServerApplication() {
        serverLock = new Object();
        server = new Server();

        serverThread = new Thread(server);
        serverThread.start();
    }


    private void start(int portno) {
        try {
            server.start(portno);
        } catch (RuntimeException ex) {
            LOGGER.error("Server runtime exception: " + ex.getMessage());
        }
    }

    private void join() {
        try {
            serverThread.join();
        } catch (InterruptedException ex) {
            LOGGER.error("Server thread join interrupted!");
        }
    }


    @Override
    public void handle(Signal signal) {
        if (signal.getName().equals("INT") ||
                signal.getName().equals("TERM")) {
            LOGGER.info("Attempting to handle SIGINT or SIGTERM...");
            synchronized (serverLock) {
                server.close();
            }
        } else {
            LOGGER.error("Unhandled signal!");
            System.exit(-1);
        }
    }
}

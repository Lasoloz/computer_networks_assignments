/*
 * Heim László
 * hlim1626
 * 522-es csoport
 * Lab05 Mandelbrot generálás
 */
package com.github.lasoloz.mandelbrot.server;

import com.github.lasoloz.mandelbrot.message.DrawRequest;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class MandelbrotServer {
    private final static String HOSTNAME = "127.0.0.1";

    private ServerSocket serverSocket;
    private ClientHandler[] clientHandlers;
    private Thread[] clientThreads;

    private BufferedImage image;
    private int imageWidth;
    private int imageHeight;


    public MandelbrotServer() {
    }

    public BufferedImage renderWithClients(
            int clientSocketCount,
            int portNumber,
            int imageWidth,
            int imageHeight) {
        image = new BufferedImage(
                imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB
        );
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;

        try {
            clientHandlers = new ClientHandler[clientSocketCount];
            clientThreads = new Thread[clientSocketCount];
            serverSocket = new ServerSocket();

            serverSocket.bind(new InetSocketAddress(HOSTNAME, portNumber));

            lookupClients();

            waitClients();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to open server socket: " + ex);
        }

        return image;
    }


    private void lookupClients() {
        int clientSocketCount = clientHandlers.length;

        // Calculate image boundaries:
        double ratio = (double) imageWidth / (double) imageHeight;
        double top, bottom, left, right, offset;
        final int stripHeight = imageHeight / clientSocketCount;

        if (ratio > 1.) {
            top = -2.0;
            bottom = 2.0;
            left = -2.0 * ratio;
            right = -left;

            offset = 4.0 / (double) clientSocketCount;
        } else {
            left = -2.0;
            right = 2.0;
            top = -2.0 / ratio;
            bottom = -top;

            offset = (bottom - top) / (double) clientSocketCount;
        }

        // Start threads after accepting clients:
        try {
            double currentVal = top;
            int pixelCount = 0;

            for (int i = 0; i < clientSocketCount - 1; ++i) {
                Socket clientSocket = serverSocket.accept();

                DrawRequest request = new DrawRequest(
                        currentVal, currentVal + offset,
                        left, right,
                        imageWidth, stripHeight
                );

                clientHandlers[i] = new ClientHandler(clientSocket, request);
                clientThreads[i] = new Thread(clientHandlers[i]);
                clientThreads[i].start();

                currentVal += offset;
                pixelCount += stripHeight;
            }

            Socket clientSocket = serverSocket.accept();
            DrawRequest request = new DrawRequest(
                    currentVal, bottom,
                    left, right,
                    imageWidth, imageHeight - pixelCount
            );

            int last = clientSocketCount - 1;
            clientHandlers[last] = new ClientHandler(clientSocket, request);
            clientThreads[last] = new Thread(clientHandlers[last]);
            clientThreads[last].start();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to open client socket: " + ex);
        }
    }


    private void waitClients() {
        int clientSocketCount = clientHandlers.length;
        Graphics2D g = image.createGraphics();

        int pixelOffset = imageHeight / clientSocketCount;
        int currentHeight = 0;

        try {
            for (int i = 0; i < clientSocketCount; ++i) {
                clientThreads[i].join();
                BufferedImage currentImage = clientHandlers[i].getImagePart();
                if (currentImage == null) {
                    throw new RuntimeException(
                            "Client thread has no image: " +
                                    clientHandlers[i].getReason()
                    );
                }

                g.drawImage(currentImage, null, 0, currentHeight);
                currentHeight += pixelOffset;
            }
        } catch (InterruptedException ex) {
            throw new RuntimeException("Failed to join client thread: " + ex);
        }

        g.dispose();
    }
}

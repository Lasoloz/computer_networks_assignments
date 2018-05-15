/*
 * Heim László
 * hlim1626
 * 522-es csoport
 * Lab05 Mandelbrot generálás
 */
package com.github.lasoloz.mandelbrot.server;

import com.github.lasoloz.mandelbrot.message.DrawRequest;
import com.github.lasoloz.mandelbrot.message.TransmittedBufferedImage;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedImage imagePart;
    private String reason;


    public ClientHandler(Socket clientSocket, DrawRequest request) {
        try {
            this.clientSocket = clientSocket;

            ObjectOutputStream oos =
                    new ObjectOutputStream(clientSocket.getOutputStream());

            oos.writeObject(request);

            reason = "";
        } catch (IOException ex) {
            throw new RuntimeException(
                    "Failed to open output stream for client handler or to" +
                            "write message: " + ex
            );
        }
    }

    @Override
    public void run() {
        try {
            ObjectInputStream ois =
                    new ObjectInputStream(clientSocket.getInputStream());

            TransmittedBufferedImage tImage = (TransmittedBufferedImage)
                    ois.readObject();
            imagePart = tImage.getImage();
        } catch (IOException ex) {
            reason = "Failed to open or read from object input stream: " + ex;
        } catch (ClassNotFoundException ex) {
            reason = "Class not found: " + ex;
        }
    }

    public BufferedImage getImagePart() {
        return imagePart;
    }

    public String getReason() {
        return reason;
    }
}

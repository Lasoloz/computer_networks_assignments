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
import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.Random;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedImage imagePart;
    private String reason;

    // Vizsga:
    Random random = new Random(new Date().getTime());

    ObjectOutputStream oos;
    ObjectInputStream ois;


    public ClientHandler(Socket clientSocket, DrawRequest request) {
        try {
            this.clientSocket = clientSocket;

            oos = new ObjectOutputStream(clientSocket.getOutputStream());
            ois = new ObjectInputStream(clientSocket.getInputStream());

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
            TransmittedBufferedImage tImage = (TransmittedBufferedImage)
                    ois.readObject();
            imagePart = tImage.getImage();

            // Vizsga:
            // Generate number:
            System.out.println("Bejött!");
            int num = random.nextInt(10000 - 1000) + 1000;
            oos.flush();
            oos.writeObject(new DrawRequest(0, 0, 0, 0, num, 0));
            oos.writeInt(num);
            System.out.println("Küldve!" + num);
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

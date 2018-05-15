/*
 * Heim László
 * hlim1626
 * 522-es csoport
 * Lab05 Mandelbrot generálás
 */
package com.github.lasoloz.mandelbrot.client;

import com.github.lasoloz.mandelbrot.message.DrawRequest;
import com.github.lasoloz.mandelbrot.message.TransmittedBufferedImage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientApplication {
    public static void main(String[] args) {
        try {
            String host = args[0];
            int port = Integer.parseInt(args[1]);

            renderMandelbrot(host, port);
        } catch (NumberFormatException ex) {
            System.err.println("Port must be a number!");
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.err.println("You must specify both host and port!");
        }
    }

    private static void renderMandelbrot(String hostname, int portno) {
        try {
            Socket serverConnection = new Socket();
            serverConnection.connect(new InetSocketAddress(hostname, portno));

            ObjectOutputStream oos =
                    new ObjectOutputStream(serverConnection.getOutputStream());
            ObjectInputStream ois =
                    new ObjectInputStream(serverConnection.getInputStream());

            DrawRequest request = (DrawRequest) ois.readObject();

            MandelbrotRenderer renderer = new MandelbrotRenderer(request);
            TransmittedBufferedImage image =
                    new TransmittedBufferedImage(renderer.render());

            oos.writeObject(image);

            oos.close();
            ois.close();
        } catch (IOException ex) {
            System.err.println("Failed to connect to server!");
        } catch (ClassNotFoundException ex) {
            System.err.println("Failed to read draw request!");
        }
    }
}

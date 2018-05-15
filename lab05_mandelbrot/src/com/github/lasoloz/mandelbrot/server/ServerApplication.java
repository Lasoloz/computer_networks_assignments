/*
 * Heim László
 * hlim1626
 * 522-es csoport
 * Lab05 Mandelbrot generálás
 */
package com.github.lasoloz.mandelbrot.server;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ServerApplication {
    public static void main(String[] args) {
        System.out.println(
                "(Usage note: ServerApplication <clientCount> <portno> <width> " +
                        "<height>)"
        );

        int clientCount = 4;
        int portNumber = 8100;
        int width = 800;
        int height = 600;
        try {
            clientCount = Integer.parseInt(args[0]);
            portNumber = Integer.parseInt(args[1]);
            width = Integer.parseInt(args[2]);
            height = Integer.parseInt(args[3]);
        } catch (NumberFormatException ex) {
            System.out.println(
                    "One of the parameters is invalid! Continuing..."
            );
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println(
                    "Not enough parameters, using default parameters for " +
                            "remaining..."
            );
        }

        if (clientCount < 1 || portNumber < 1 || width < 1 || height < 1) {
            System.err.println(
                    "No parameter can be less than 1! Setting back to original..."
            );
            clientCount = 4;
            portNumber = 8100;
            width = 800;
            height = 600;
        }

        MandelbrotServer server = new MandelbrotServer();
        try {
            BufferedImage mandelImage = server.renderWithClients(
                    clientCount, portNumber, width, height
            );

            File imageFile = new File("mandel.png");
            ImageIO.write(mandelImage, "png", imageFile);
        } catch (IOException ex) {
            System.err.println("Failed to write image!");
        } catch (RuntimeException ex) {
            System.err.println("Failed to render mandelbrot set: " + ex);
        }
    }
}

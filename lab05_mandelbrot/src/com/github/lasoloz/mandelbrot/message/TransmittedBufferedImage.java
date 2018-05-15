/*
 * Heim László
 * hlim1626
 * 522-es csoport
 * Lab05 Mandelbrot generálás
 */
package com.github.lasoloz.mandelbrot.message;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class TransmittedBufferedImage implements Serializable {
    private transient BufferedImage image;

    public TransmittedBufferedImage(BufferedImage image) {
        this.image = image;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        ImageIO.write(image, "png", out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        image = ImageIO.read(in);
    }


    public BufferedImage getImage() {
        return image;
    }
}

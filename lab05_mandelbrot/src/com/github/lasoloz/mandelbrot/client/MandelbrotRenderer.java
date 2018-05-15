/*
 * Heim László
 * hlim1626
 * 522-es csoport
 * Lab05 Mandelbrot generálás
 */
package com.github.lasoloz.mandelbrot.client;

import com.github.lasoloz.mandelbrot.message.DrawRequest;

import java.awt.*;
import java.awt.image.BufferedImage;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class MandelbrotRenderer {
    private static int depth = 500;

    private DrawRequest request;

    public MandelbrotRenderer(DrawRequest request) {
        this.request = request;
    }


    private ComplexD iterate(ComplexD zOld, ComplexD c) {
        return zOld.multiply(zOld).addToThis(c);
    }

    public BufferedImage render() {
        double top = request.getTop();
        double bottom = request.getBottom();
        double left = request.getLeft();
        double right = request.getRight();
        int width = request.getWidth();
        int height = request.getHeight();

        BufferedImage image = new BufferedImage(width, height, TYPE_INT_RGB);

        double y = top;
        double vStep = (bottom - top) / (double) height;
        double hStep = (right - left) / (double) width;
        for (int vertIdx = 0; vertIdx < height; ++vertIdx, y += vStep) {
            double x = left;
            for (int horIdx = 0; horIdx < width; ++horIdx, x += hStep) {
                ComplexD c = new ComplexD(x, y);
                ComplexD z = new ComplexD();

                int count = -1;

                float brightness = .8f;

                do {
                    z = iterate(z, c);
                    ++count;
                } while (count < depth && z.absSq() < 4.0);

                if (count == depth) {
                    brightness = 0f;
                }

                int color = Color.HSBtoRGB(
                        10f * (float) count / (float) depth,
                        .6f,
                        brightness
                );

                image.setRGB(horIdx, vertIdx, color);
            }
        }

        return image;
    }
}

/*
 * Heim László
 * hlim1626
 * 522-es csoport
 * Lab05 Mandelbrot generálás
 */
package com.github.lasoloz.mandelbrot.message;

import java.io.Serializable;

public class DrawRequest implements Serializable {
    private double top;
    private double bottom;
    private double left;
    private double right;
    private int width;
    private int height;


    public DrawRequest(
            double top, double bottom,
            double left, double right,
            int width, int height
    ) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
        this.width = width;
        this.height = height;
    }

    public double getTop() {
        return top;
    }

    public double getBottom() {
        return bottom;
    }

    public double getLeft() {
        return left;
    }

    public double getRight() {
        return right;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return "DrawRequest{" +
                "top=" + top +
                ", bottom=" + bottom +
                ", left=" + left +
                ", right=" + right +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}

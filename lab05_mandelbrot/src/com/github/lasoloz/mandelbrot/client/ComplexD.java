/*
 * Heim László
 * hlim1626
 * 522-es csoport
 * Lab05 Mandelbrot generálás
 */
package com.github.lasoloz.mandelbrot.client;

public class ComplexD {
    public double a, b;

    public ComplexD() {
        a = 0.0;
        b = 0.0;
    }

    public ComplexD(double a, double b) {
        this.a = a;
        this.b = b;
    }

    public ComplexD multiply(ComplexD other) {
        return new ComplexD(
                a * other.a - b * other.b,
                a * other.b + b * other.a
        );
    }

    public ComplexD addToThis(ComplexD other) {
        a += other.a;
        b += other.b;
        return this;
    }

    public double absSq() {
        return a * a + b * b;
    }
}

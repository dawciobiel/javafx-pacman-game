package com.dawciobiel.pacman.utils;

/**
 * Represents a point in 2D space with integer coordinates
 */
public record Point(int x, int y) {

    @Override
    public String toString() {
        return "Point{" + "x=" + x + ", y=" + y + '}';
    }
}
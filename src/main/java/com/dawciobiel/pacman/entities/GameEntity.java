package com.dawciobiel.pacman.entities;

import com.dawciobiel.pacman.utils.Direction;

public abstract class GameEntity {

    protected double x;
    protected double y;
    protected int tileX;
    protected int tileY;

    protected Direction direction;
    protected boolean isMoving = false;

    public double getDrawX() {
        return x;
    }

    public double getDrawY() {
        return y;
    }

    public int getTileX() {
        return tileX;
    }

    public int getTileY() {
        return tileY;
    }

    public Direction getDirection() {
        return direction;
    }
}
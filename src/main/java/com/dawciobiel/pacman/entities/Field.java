package com.dawciobiel.pacman.entities;

import com.dawciobiel.pacman.utils.Direction;

public class Field {

    public FieldType type;
    public Direction[] connectsWith;

    public Field() {
        type = FieldType.WALL;
    }

    public Field(FieldType type) {
        this.type = type;
    }

    public Field(FieldType type, Direction[] connectsWith) {
        this.type = type;
        this.connectsWith = connectsWith;
    }

    public FieldType getType() {
        return type;
    }

    public void setType(FieldType type) {
        this.type = type;
    }

    public Direction[] getConnectsWith() {
        return connectsWith;
    }

    public void setConnectsWith(Direction[] connectsWith) {
        this.connectsWith = connectsWith;
    }
}

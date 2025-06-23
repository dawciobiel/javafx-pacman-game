package com.dawciobiel.pacman.entities;

import com.dawciobiel.pacman.utils.Direction;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

import static com.dawciobiel.pacman.utils.GameConstants.BOARD_COLS;
import static com.dawciobiel.pacman.utils.GameConstants.BOARD_ROWS;
import static com.dawciobiel.pacman.utils.GameConstants.MOVEMENT_SPEED;
import static com.dawciobiel.pacman.utils.GameConstants.TILE_SIZE;

public class Player extends GameEntity {
    private static final double ANIMATION_SPEED = 0.15;

    private Direction queuedDirection = null;
    private double moveProgress = 0.0;
    private double animationTime = 0.0;

    public Player(int startX, int startY) {
        this.tileX = startX;
        this.tileY = startY;
        this.x = startX * TILE_SIZE + TILE_SIZE * 0.5;
        this.y = startY * TILE_SIZE + TILE_SIZE * 0.5;

        direction = Direction.RIGHT;
    }

    public void reset(final int newX, final int newY) {
        this.tileX = newX;
        this.tileY = newY;
        this.x = newX * TILE_SIZE + TILE_SIZE * 0.5;
        this.y = newY * TILE_SIZE + TILE_SIZE * 0.5;
        this.direction = Direction.RIGHT;
        this.queuedDirection = null;
        this.isMoving = false;
        this.moveProgress = 0.0;
        this.animationTime = 0.0;
    }

    public void draw(GraphicsContext gc) {
        double startAngle = switch (direction) {
            case RIGHT -> 0;
            case DOWN -> 270;
            case LEFT -> 180;
            case UP -> 90;
        };

        double mouthAngle = 45 * Math.sin(animationTime * Math.PI * 2);
        if (mouthAngle < 0) mouthAngle = 0;

        double drawX = getDrawX() - TILE_SIZE * 0.5 + 2;
        double drawY = getDrawY() - TILE_SIZE * 0.5 + 2;
        double size = TILE_SIZE - 4;

        gc.setFill(Color.YELLOW);

        if (mouthAngle > 0) {
            gc.fillArc(drawX, drawY, size, size, startAngle + mouthAngle * 0.5, 360 - mouthAngle, ArcType.ROUND);
        } else {
            gc.fillOval(drawX, drawY, size, size);
        }
    }

    public void update(Field[][] fields) {
        if (isMoving) {
            animationTime += ANIMATION_SPEED;
            if (animationTime >= 1.0) {
                animationTime = 0.0;
            }
        }

        if (queuedDirection != null && !isMoving) {
            if (canMove(queuedDirection, fields)) {
                direction = queuedDirection;
                queuedDirection = null;
                startMoving();
            }
        }

        if (!isMoving && canMove(direction, fields)) {
            startMoving();
        }

        if (isMoving) {
            moveProgress += MOVEMENT_SPEED;

            if (moveProgress >= 1.0) {
                completeMove();
                moveProgress = 0.0;
                isMoving = false;
                handleTunnels();
            } else {
                updateInterpolatedPosition();
            }
        }
    }

    private void startMoving() {
        isMoving = true;
        moveProgress = 0.0;
    }

    private void completeMove() {
        tileX += direction.dx;
        tileY += direction.dy;

        x = tileX * TILE_SIZE + TILE_SIZE * 0.5;
        y = tileY * TILE_SIZE + TILE_SIZE * 0.5;
    }

    private void updateInterpolatedPosition() {
        double startX = tileX * TILE_SIZE + TILE_SIZE * 0.5;
        double startY = tileY * TILE_SIZE + TILE_SIZE * 0.5;

        double targetX = startX + direction.dx * TILE_SIZE;
        double targetY = startY + direction.dy * TILE_SIZE;

        x = startX + (targetX - startX) * moveProgress;
        y = startY + (targetY - startY) * moveProgress;
    }

    private boolean canMove(Direction dir, Field[][] fields) {
        int nextX = tileX + dir.dx;
        int nextY = tileY + dir.dy;

        if (nextX < 0 || nextX >= fields.length || nextY < 0 || nextY >= fields[0].length) {
            return (nextX < 0 || nextX >= fields.length) && nextY == BOARD_ROWS / 2;
        }

        Field field = fields[nextX][nextY];
        boolean fieldType = field.getType() == FieldType.WALL;

        return !fieldType;
    }

    private void handleTunnels() {
        if (tileX < 0) {
            tileX = BOARD_COLS - 1;
            x = tileX * TILE_SIZE + TILE_SIZE * 0.5;
        } else if (tileX >= BOARD_COLS) {
            tileX = 0;
            x = tileX * TILE_SIZE + TILE_SIZE * 0.5;
        }
    }

    public void queueDirection(Direction newDirection) {
        this.queuedDirection = newDirection;
    }

    public boolean isAtTileCenter() {
        return !isMoving;
    }


}
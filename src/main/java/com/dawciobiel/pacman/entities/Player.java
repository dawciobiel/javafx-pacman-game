package com.dawciobiel.pacman.entities;

import com.dawciobiel.pacman.PacmanGame;
import com.dawciobiel.pacman.utils.Direction;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

import static com.dawciobiel.pacman.PacmanGame.BOARD_COLS;
import static com.dawciobiel.pacman.PacmanGame.BOARD_ROWS;

public class Player {
    private static final double ANIMATION_SPEED = 0.15;
    private double x;
    private double y;
    private int tileX;
    private int tileY;
    private Direction direction = Direction.RIGHT;
    private Direction queuedDirection = null;
    private boolean isMoving = false;
    private double moveProgress = 0.0;
    private double animationTime = 0.0;

    public Player(int startX, int startY) {
        this.tileX = startX;
        this.tileY = startY;
        this.x = startX * PacmanGame.TILE_SIZE + PacmanGame.TILE_SIZE * 0.5;
        this.y = startY * PacmanGame.TILE_SIZE + PacmanGame.TILE_SIZE * 0.5;
    }

    public void reset(final int newX, final int newY) {
        this.tileX = newX;
        this.tileY = newY;
        this.x = newX * PacmanGame.TILE_SIZE + PacmanGame.TILE_SIZE * 0.5;
        this.y = newY * PacmanGame.TILE_SIZE + PacmanGame.TILE_SIZE * 0.5;
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

        double drawX = getDrawX() - PacmanGame.TILE_SIZE * 0.5 + 2;
        double drawY = getDrawY() - PacmanGame.TILE_SIZE * 0.5 + 2;
        double size = PacmanGame.TILE_SIZE - 4;

        gc.setFill(Color.YELLOW);

        if (mouthAngle > 0) {
            gc.fillArc(drawX, drawY, size, size, startAngle + mouthAngle * 0.5, 360 - mouthAngle, ArcType.ROUND);
        } else {
            gc.fillOval(drawX, drawY, size, size);
        }
    }

    public void update(boolean[][] walls) {
        if (isMoving) {
            animationTime += ANIMATION_SPEED;
            if (animationTime >= 1.0) {
                animationTime = 0.0;
            }
        }

        if (queuedDirection != null && !isMoving) {
            if (canMove(queuedDirection, walls)) {
                direction = queuedDirection;
                queuedDirection = null;
                startMoving();
            }
        }

        if (!isMoving && canMove(direction, walls)) {
            startMoving();
        }

        if (isMoving) {
            moveProgress += PacmanGame.MOVEMENT_SPEED;

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

        x = tileX * PacmanGame.TILE_SIZE + PacmanGame.TILE_SIZE * 0.5;
        y = tileY * PacmanGame.TILE_SIZE + PacmanGame.TILE_SIZE * 0.5;
    }

    private void updateInterpolatedPosition() {
        double startX = tileX * PacmanGame.TILE_SIZE + PacmanGame.TILE_SIZE * 0.5;
        double startY = tileY * PacmanGame.TILE_SIZE + PacmanGame.TILE_SIZE * 0.5;

        double targetX = startX + direction.dx * PacmanGame.TILE_SIZE;
        double targetY = startY + direction.dy * PacmanGame.TILE_SIZE;

        x = startX + (targetX - startX) * moveProgress;
        y = startY + (targetY - startY) * moveProgress;
    }

    private boolean canMove(Direction dir, boolean[][] walls) {
        int nextX = tileX + dir.dx;
        int nextY = tileY + dir.dy;

        if (nextX < 0 || nextX >= walls.length || nextY < 0 || nextY >= walls[0].length) {
            return (nextX < 0 || nextX >= walls.length) && nextY == BOARD_ROWS / 2;
        }

        return !walls[nextX][nextY];
    }

    private void handleTunnels() {
        if (tileX < 0) {
            tileX = BOARD_COLS - 1;
            x = tileX * PacmanGame.TILE_SIZE + PacmanGame.TILE_SIZE * 0.5;
        } else if (tileX >= BOARD_COLS) {
            tileX = 0;
            x = tileX * PacmanGame.TILE_SIZE + PacmanGame.TILE_SIZE * 0.5;
        }
    }

    public void queueDirection(Direction newDirection) {
        this.queuedDirection = newDirection;
    }

    public boolean isAtTileCenter() {
        return !isMoving;
    }

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
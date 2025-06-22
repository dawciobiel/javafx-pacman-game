package com.dawciobiel.pacman.game;

import com.dawciobiel.pacman.animation.AnimationManager;
import com.dawciobiel.pacman.animation.AnimationFactory;
import com.dawciobiel.pacman.rendering.Renderer;
import javafx.scene.image.Image;

public class Pacman {
    private double x;
    private double y;
    private Direction currentDirection;
    private Direction nextDirection;
    private AnimationManager animationManager;
    private double speed;
    private boolean isDead;
    private int lives;
    private int score;

    public Pacman(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.currentDirection = Direction.RIGHT;
        this.nextDirection = Direction.RIGHT;
        this.speed = 100.0; // piksele na sekundę
        this.isDead = false;
        this.lives = 3;
        this.score = 0;

        initializeAnimations();
    }

    private void initializeAnimations() {
        animationManager = new AnimationManager();
        animationManager.addAnimation("walk_right", AnimationFactory.createPacmanWalkingRight());
        animationManager.addAnimation("walk_left", AnimationFactory.createPacmanWalkingLeft());
        animationManager.addAnimation("walk_up", AnimationFactory.createPacmanWalkingUp());
        animationManager.addAnimation("walk_down", AnimationFactory.createPacmanWalkingDown());
        animationManager.addAnimation("death", AnimationFactory.createPacmanDying());

        // Rozpocznij od animacji chodzenia w prawo
        animationManager.playAnimation("walk_right");
    }

    public void update(double deltaTime) {
        if (!isDead) {
            updatePosition(deltaTime);
            animationManager.update(deltaTime);
        } else if (animationManager.isCurrentAnimationFinished()) {
            respawn();
        }
    }

    private void updatePosition(double deltaTime) {
        // Sprawdź możliwość zmiany kierunku na następny żądany
        if (canChangeDirection(nextDirection)) {
            currentDirection = nextDirection;
            updateAnimation();
        }

        // Aktualizuj pozycję zgodnie z aktualnym kierunkiem
        double moveAmount = speed * deltaTime;

        switch (currentDirection) {
            case RIGHT -> x += moveAmount;
            case LEFT -> x -= moveAmount;
            case UP -> y -= moveAmount;
            case DOWN -> y += moveAmount;
        }
    }

    private boolean canChangeDirection(Direction newDirection) {
        // TODO: Zaimplementuj sprawdzanie kolizji ze ścianami
        // Tymczasowo zawsze zwraca true
        return true;
    }

    private void updateAnimation() {
        String animationName = switch (currentDirection) {
            case RIGHT -> "walk_right";
            case LEFT -> "walk_left";
            case UP -> "walk_up";
            case DOWN -> "walk_down";
        };
        animationManager.playAnimation(animationName);
    }

    public void render(Renderer renderer) {
        Image currentFrame = animationManager.getCurrentFrame();
        if (currentFrame != null) {
            renderer.drawSprite(currentFrame, x, y);
        }
    }

    public void setNextDirection(Direction direction) {
        this.nextDirection = direction;
    }

    public void die() {
        if (!isDead) {
            isDead = true;
            lives--;
            animationManager.playAnimation("death");
        }
    }

    private void respawn() {
        if (lives > 0) {
            isDead = false;
            // Reset pozycji do początkowej
            x = 100; // TODO: Ustaw właściwą pozycję startową
            y = 100; // TODO: Ustaw właściwą pozycję startową
            currentDirection = Direction.RIGHT;
            nextDirection = Direction.RIGHT;
            animationManager.playAnimation("walk_right");
        }
    }

    public void addScore(int points) {
        score += points;
    }

    // Gettery i settery
    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public Direction getDirection() { return currentDirection; }
    public int getLives() { return lives; }
    public int getScore() { return score; }
    public boolean isDead() { return isDead; }
    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }
}

enum Direction {
    LEFT, RIGHT, UP, DOWN
}
package com.dawciobiel.pacman.game;

import com.dawciobiel.pacman.GameEngine;
import com.dawciobiel.pacman.entities.Food;
import com.dawciobiel.pacman.entities.Ghost;
import com.dawciobiel.pacman.entities.Player;
import com.dawciobiel.pacman.entities.PowerPellet;
import com.dawciobiel.pacman.utils.GameConstants;
import com.dawciobiel.pacman.utils.GhostMode;

import java.util.Iterator;
import java.util.List;

import static com.dawciobiel.pacman.utils.GameConstants.TILE_SIZE;

public class CollisionDetector {


    public static boolean checkCollisionsWithGhosts(GameEngine gameEngine, Player pacman, List<Ghost> ghosts) {
        for (Ghost ghost : ghosts) {
            double distanceSquared = calculateDistanceSquared(pacman, ghost);
            if (CollisionDetector.checkCollisionWithOneGhost(gameEngine, distanceSquared, ghost)) {

                return true;
            }
        }

        return false;
    }

    public static boolean checkCollisionWithOneGhost(GameEngine gameEngine, double distanceSquared, Ghost ghost) {
        if (distanceSquared < (TILE_SIZE * 0.8) * (TILE_SIZE * 0.8)) {
            if (GameEngine.isPowerPelletActive && ghost.getMode() == GhostMode.FRIGHTENED) {
                GameEngine.eatGhost(ghost);
            } else if (ghost.getMode() != GhostMode.DEAD) { // Nie zabijaj, jeśli duch jest martwy
                gameEngine.executePacmanIsDead();
                if (GameState.lives <= 0) {
                    GameState.gameOver = true;
                    boolean shouldContinueTheGame = false;
                    gameEngine.executePacmanIsDeadAndNoMoreLives(shouldContinueTheGame, "game_over");

                    return true;
                } else {
                    // Restart pozycji
                    gameEngine.resetPacmanPosition();

                    // Przywróć atakujący tryb duchów
                    gameEngine.changeGhostsMode(GhostMode.SCATTER);
                }
            }
        }
        return false;
    }

    private static double calculateDistanceSquared(Player pacman, Ghost ghost) {
        double dx = ghost.getDrawX() - pacman.getDrawX();
        double dy = ghost.getDrawY() - pacman.getDrawY();

        return dx * dx + dy * dy;
    }

    public static void checkCollisionWithFood(GameEngine gameEngine, long currentTime) {
        if (GameState.pacman.isAtTileCenter()) {
            // Check collisions with food
            Iterator<Food> foodIterator = GameState.foods.iterator();
            while (foodIterator.hasNext()) {
                Food food = foodIterator.next();
                gameEngine.eatFood(food, foodIterator);
            }

            // Check collisions with power pellets
            Iterator<PowerPellet> powerPelletIterator = GameState.powerPellets.iterator();
            while (powerPelletIterator.hasNext()) {
                PowerPellet powerPellet = powerPelletIterator.next();
                if (powerPellet.x() == GameState.pacman.getTileX() && powerPellet.y() == GameState.pacman.getTileY()) {
                    GameState.addScore(GameConstants.POWER_PELLET_POINTS);
                    powerPelletIterator.remove();
                    GameEngine.soundManager.playSound("power_pellet");

                    // Aktywuj power pellet
                    GameEngine.isPowerPelletActive = true;
                    GameEngine.powerPelletStartTime = currentTime;
                    GameState.ghostMultiplier = 1;

                    // Zmień tryb duchów na frightened
                    gameEngine.changeGhostsMode(GhostMode.FRIGHTENED);
                }
            }
        }
    }
}